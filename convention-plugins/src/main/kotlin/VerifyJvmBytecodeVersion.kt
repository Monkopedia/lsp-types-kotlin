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

import java.io.DataInputStream
import java.util.zip.ZipFile
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Fails if any `.class` entry in [jars] has a class-file major version other than
 * [expectedMajor]. Guards the published `-jvm` artifacts' bytecode level: KGP silently
 * falls back to the running JDK's target if the `jvmTarget` setting is ever lost.
 */
abstract class VerifyJvmBytecodeVersion : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val jars: ConfigurableFileCollection

    @get:Input
    abstract val expectedMajor: Property<Int>

    /** Stamp file so the task is up-to-date when the jars have not changed. */
    @get:OutputFile
    abstract val report: RegularFileProperty

    @TaskAction
    fun verify() {
        val expected = expectedMajor.get()
        val lines = mutableListOf<String>()
        val bad = mutableListOf<String>()
        for (jar in jars.files) {
            var count = 0
            ZipFile(jar).use { zip ->
                for (entry in zip.entries()) {
                    if (entry.isDirectory || !entry.name.endsWith(".class")) continue
                    val major = DataInputStream(zip.getInputStream(entry)).use { input ->
                        if (input.readInt() != CLASS_MAGIC) {
                            throw GradleException("${jar.name}!${entry.name}: not a class file")
                        }
                        input.readUnsignedShort() // minor
                        input.readUnsignedShort()
                    }
                    count++
                    if (major != expected) bad += "${jar.name}!${entry.name}: major $major"
                }
            }
            if (count == 0) throw GradleException("${jar.name}: contains no classes to verify")
            lines += "${jar.name}: $count classes checked"
        }
        if (bad.isNotEmpty()) {
            throw GradleException(
                "${bad.size} class(es) are not class-file major $expected " +
                    "(published JVM bytecode must stay Java ${expected - 44}):\n" +
                    bad.take(20).joinToString("\n") +
                    if (bad.size > 20) "\n... and ${bad.size - 20} more" else ""
            )
        }
        report.get().asFile.writeText(lines.joinToString("\n", postfix = "\n"))
        lines.forEach { logger.lifecycle(it) }
    }

    private companion object {
        const val CLASS_MAGIC = 0xCAFEBABE.toInt()
    }
}
