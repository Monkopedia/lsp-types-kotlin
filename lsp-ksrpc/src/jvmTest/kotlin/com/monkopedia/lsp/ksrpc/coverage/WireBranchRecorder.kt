/*
 * Copyright 2025 Jason Monk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.monkopedia.lsp.ksrpc.coverage

import com.monkopedia.lsp.HoverContents
import com.monkopedia.lsp.ksrpc.fixtures.ConformanceWireRecorder
import java.io.File
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.net.JarURLConnection
import java.util.Collections
import java.util.IdentityHashMap
import java.util.concurrent.ConcurrentHashMap
import java.util.jar.JarFile
import kotlin.reflect.KClass
import kotlinx.serialization.json.JsonElement

/**
 * Process-wide JVM recorder for LSP union-branch coverage (#74).
 *
 * The conformance fixtures (`ConformanceLanguageServer`, `ConformanceLanguageClient`)
 * push every typed param they receive and every typed result they return through
 * [ConformanceWireRecorder.observeValue]. [install] wires that hook to [recordValue],
 * which walks the value transitively and notes every sealed-interface subclass it
 * encounters in [observedBranches]. The end-of-suite [WireBranchCoverageReportTest]
 * snapshots the result, writes `wire-branch-coverage.md` and fails on regression
 * against `wire-branch-baseline.txt`.
 *
 * Observed branches are recorded as `<UnionFqn>.<SubclassSimpleName>` strings —
 * e.g. `com.monkopedia.lsp.HoverContents.MarkupContentValue` or
 * `com.monkopedia.lsp.InlineValue.InlineValueText` — so both nested and top-level
 * subclasses round-trip through baselines and reports.
 *
 * Thread-safe: the observation set is a [ConcurrentHashMap.newKeySet] and the
 * walker uses a per-call identity-tracking visited set so parallel coroutines /
 * transports can record concurrently without lock contention or false sharing.
 */
object WireBranchRecorder {

    private val _observedBranches: MutableSet<String> = ConcurrentHashMap.newKeySet()

    /** Read-only snapshot of every union-branch encountered so far. */
    val observedBranches: Set<String> get() = _observedBranches.toSet()

    /**
     * Install the JVM recorder callback into [ConformanceWireRecorder]. Idempotent;
     * the wire-branch report test installs it before driving the integration suite.
     */
    fun install() {
        ConformanceWireRecorder.valueCallback = ::recordValue
    }

    /** Clear the recorded branches. Test-only. */
    fun reset() {
        _observedBranches.clear()
    }

    /**
     * Walk [value] transitively. For every sealed-interface subclass encountered
     * (anywhere in the lsp package), add an entry of the form
     * `<sealedFqn>.<subclassSimpleName>` to [observedBranches].
     */
    fun recordValue(value: Any?) {
        if (value == null) return
        val visited = Collections.newSetFromMap(IdentityHashMap<Any, Boolean>())
        walk(value, visited)
    }

    /**
     * Reflectively enumerate every sealed interface/class in
     * `com.monkopedia.lsp.*` and the set of permitted subclasses each declares.
     * Returns a map from union FQN → set of `<UnionFqn>.<subclassSimpleName>`
     * branch identifiers.
     *
     * The enumeration walks the codebase that contains [HoverContents] —
     * a known compiled class in `com.monkopedia.lsp` — to locate the
     * directory (or jar) that holds every other class in the package, then
     * reads the branches from Kotlin's own sealed metadata ([sealedBranches]).
     */
    fun introspectUnions(): Map<String, Set<String>> {
        val result = mutableMapOf<String, MutableSet<String>>()
        for (cls in lspPackageClasses()) {
            // Only treat as a "union" if it has at least one sealed subclass
            val perms = sealedBranches(cls)
            if (perms.isEmpty()) continue
            val branches = perms.mapTo(mutableSetOf()) { sub ->
                "${cls.name}.${sub.simpleName}"
            }
            result[cls.name] = branches
        }
        return result
    }

    private val sealedBranchCache = ConcurrentHashMap<Class<*>, List<Class<*>>>()

    /**
     * The direct subclasses of [cls] if it is a Kotlin sealed class/interface, else
     * empty. Read from Kotlin metadata ([KClass.sealedSubclasses]), not
     * [Class.getPermittedSubclasses]: main is compiled to Java 11 bytecode, and
     * Kotlin only emits the JVM `PermittedSubclasses` attribute at target 17+, so
     * the JDK API would see no sealed classes at all.
     */
    private fun sealedBranches(cls: Class<*>): List<Class<*>> = sealedBranchCache.getOrPut(cls) {
        // kotlin-reflect throws on compiler-synthetic classes (file facades,
        // lambdas); none of those is a sealed union.
        runCatching {
            val k = cls.kotlin
            if (k.isSealed) k.sealedSubclasses.map { it.java } else emptyList()
        }.getOrDefault(emptyList())
    }

    private fun walk(value: Any?, visited: MutableSet<Any>) {
        if (value == null) return
        // Container traversal MUST run before the leaf check — most JDK list /
        // map / array container classes live in `java.util.*` and would
        // otherwise be skipped along with their elements.
        when (value) {
            is Collection<*> -> {
                if (!visited.add(value)) return
                value.forEach { walk(it, visited) }
                return
            }

            is Map<*, *> -> {
                if (!visited.add(value)) return
                value.keys.forEach { walk(it, visited) }
                value.values.forEach { walk(it, visited) }
                return
            }

            is Array<*> -> {
                if (!visited.add(value)) return
                value.forEach { walk(it, visited) }
                return
            }
        }
        if (isLeaf(value)) return
        if (!visited.add(value)) return

        recordSealedBranches(value.javaClass)
        walkProperties(value, visited)
    }

    /**
     * A small set of leaf types we explicitly skip. Strings, primitive boxes,
     * enums, JSON elements and anything in JDK packages (apart from containers,
     * which are handled above) carry no union branches and would otherwise pull
     * in JDK internals.
     */
    private fun isLeaf(value: Any): Boolean = when (value) {
        is String, is Number, is Boolean, is Char, is Byte, is Short -> true

        is Enum<*> -> true

        is JsonElement -> true

        else -> {
            val pkg = value.javaClass.`package`?.name
            pkg != null && (
                pkg.startsWith("java.") ||
                    pkg.startsWith("javax.") ||
                    pkg.startsWith("kotlin.") ||
                    pkg.startsWith("kotlinx.serialization.json")
                )
        }
    }

    private fun recordSealedBranches(cls: Class<*>) {
        val queue: ArrayDeque<Class<*>> = ArrayDeque()
        val seen = HashSet<Class<*>>()
        queue.add(cls)
        while (queue.isNotEmpty()) {
            val c = queue.removeFirst()
            if (!seen.add(c)) continue
            // Direct supertype iteration: interfaces + superclass.
            for (iface in c.interfaces) {
                tryRecord(iface, cls)
                queue.add(iface)
            }
            c.superclass?.let { sup ->
                tryRecord(sup, cls)
                queue.add(sup)
            }
        }
    }

    private fun tryRecord(supertype: Class<*>, runtime: Class<*>) {
        if (supertype.`package`?.name != "com.monkopedia.lsp") return
        // Find the sealed-subclass entry that [runtime] satisfies. For nested
        // branches (e.g. HoverContents$MarkupContentValue) the runtime class IS
        // the sealed entry; for top-level branches (e.g. InlineValueText vs
        // InlineValue) the runtime class IS the sealed entry as well.
        val perm = sealedBranches(supertype)
        val match = perm.firstOrNull { it == runtime || it.isAssignableFrom(runtime) }
            ?: return
        _observedBranches.add("${supertype.name}.${match.simpleName}")
    }

    /**
     * Walk every public no-arg `getXxx()` getter on [value]'s class hierarchy and
     * recurse into the returned object. Limiting to JavaBean-style getters keeps
     * Kotlin inline-class plumbing methods (`box-impl`, `unbox-impl`, `equals-impl0`,
     * etc.) and arbitrary helper functions out of the walk.
     */
    private fun walkProperties(value: Any, visited: MutableSet<Any>) {
        val cls = value.javaClass
        // Only consider declared methods up the chain; PROCEED carefully — many
        // Kotlin types inherit Object#getClass which is also a `getX` shape and
        // returns Class<*> (we skip via package check on the return).
        var current: Class<*>? = cls
        while (current != null && current != Any::class.java) {
            for (m in current.declaredMethods) {
                if (!isGetter(m)) continue
                val child = try {
                    m.isAccessible = true
                    m.invoke(value)
                } catch (_: Throwable) {
                    continue
                }
                walk(child, visited)
            }
            current = current.superclass
        }
    }

    private fun isGetter(m: Method): Boolean {
        if (m.parameterCount != 0) return false
        if (Modifier.isStatic(m.modifiers)) return false
        if (!Modifier.isPublic(m.modifiers)) return false
        if (m.returnType == Void.TYPE) return false
        val name = m.name
        if (!name.startsWith("get") || name.length <= 3) return false
        // Exclude getClass() and the Kotlin Companion shape.
        if (name == "getClass") return false
        return true
    }

    private fun lspPackageClasses(): Sequence<Class<*>> = sequence {
        val anchor = HoverContents::class.java
        val codeSource = anchor.protectionDomain?.codeSource ?: return@sequence
        val location = codeSource.location ?: return@sequence
        val packagePath = "com/monkopedia/lsp/"
        when {
            location.protocol == "file" -> {
                val root = File(location.toURI())
                if (root.isDirectory) {
                    val pkgDir = File(root, packagePath)
                    if (pkgDir.isDirectory) {
                        for (cls in classesUnderDir(pkgDir, "com.monkopedia.lsp.")) {
                            yield(cls)
                        }
                    }
                } else if (root.isFile && root.name.endsWith(".jar")) {
                    for (cls in classesUnderJar(root, packagePath)) {
                        yield(cls)
                    }
                }
            }

            location.protocol == "jar" -> {
                val conn = location.openConnection() as? JarURLConnection ?: return@sequence
                val jarFile = conn.jarFile
                for (cls in classesUnderJarFile(jarFile, packagePath)) {
                    yield(cls)
                }
            }
        }
    }

    private fun classesUnderDir(dir: File, packagePrefix: String): Sequence<Class<*>> = sequence {
        val files = dir.listFiles() ?: return@sequence
        for (file in files) {
            if (!file.isFile) continue
            if (!file.name.endsWith(".class")) continue
            // Skip inner classes (contain '$') — their parent is enumerated via
            // sealedBranches already, and we'd otherwise double-record.
            if (file.name.contains('$')) continue
            val simple = file.name.removeSuffix(".class")
            val fqn = packagePrefix + simple
            val cls =
                runCatching { Class.forName(fqn, false, HoverContents::class.java.classLoader) }
                    .getOrNull() ?: continue
            yield(cls)
        }
    }

    private fun classesUnderJar(jarFile: File, packagePath: String): Sequence<Class<*>> =
        classesUnderJarFile(JarFile(jarFile), packagePath)

    private fun classesUnderJarFile(jar: JarFile, packagePath: String): Sequence<Class<*>> =
        sequence {
            for (entry in jar.entries()) {
                val name = entry.name
                if (!name.startsWith(packagePath)) continue
                if (!name.endsWith(".class")) continue
                // Only top-level classes — skip entries with '$' after the package.
                val tail = name.removePrefix(packagePath)
                if (tail.contains('/')) continue
                if (tail.contains('$')) continue
                val fqn = name.removeSuffix(".class").replace('/', '.')
                val cls = runCatching {
                    Class.forName(fqn, false, HoverContents::class.java.classLoader)
                }.getOrNull() ?: continue
                yield(cls)
            }
        }
}
