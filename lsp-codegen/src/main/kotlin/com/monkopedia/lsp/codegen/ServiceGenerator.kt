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
package com.monkopedia.lsp.codegen

/**
 * Generates LSP service interfaces.
 *
 * Two flavors per direction:
 *
 * - **Clean interfaces** (`LanguageServer`, `LanguageClient`) live in `:lsp`. No
 *   transport-specific annotations — just `suspend fun` signatures. The companion
 *   object exposes wire-method names as `const val` constants so other transport
 *   adaptors can reference them.
 *
 * - **Ksrpc subinterfaces** (`KsrpcLanguageServer`, `KsrpcLanguageClient`) live in
 *   `:lsp-ksrpc`. They extend the clean interface and `RpcService`, adding
 *   `@KsService` / `@KsMethod(LanguageServer.METHOD_NAME)` / `@KsNotification` on
 *   `override` methods. This is what users implement when using ksrpc as the
 *   transport.
 *
 * Requests/notifications are split by `messageDirection`:
 * - `clientToServer` → server interface
 * - `serverToClient` → client interface
 * - `both` → both interfaces
 */
class ServiceGenerator(private val resolver: TypeResolver, private val model: MetaModel) {

    /**
     * Methods handled by the transport layer (ksrpc), not exposed on service interfaces.
     * `$/cancelRequest` is owned by `JsonRpcCancellationConvention.Lsp`.
     */
    private val transportHandled = setOf("$/cancelRequest")

    private val serverRequests = model.requests.filter {
        it.method !in transportHandled &&
            it.messageDirection in setOf(MessageDirection.CLIENT_TO_SERVER, MessageDirection.BOTH)
    }
    private val serverNotifications = model.notifications.filter {
        it.method !in transportHandled &&
            it.messageDirection in setOf(MessageDirection.CLIENT_TO_SERVER, MessageDirection.BOTH)
    }
    private val clientRequests = model.requests.filter {
        it.method !in transportHandled &&
            it.messageDirection in setOf(MessageDirection.SERVER_TO_CLIENT, MessageDirection.BOTH)
    }
    private val clientNotifications = model.notifications.filter {
        it.method !in transportHandled &&
            it.messageDirection in setOf(MessageDirection.SERVER_TO_CLIENT, MessageDirection.BOTH)
    }

    // ---- Clean interfaces (in :lsp) ----

    fun generateCleanServer(): String = generateCleanInterface(
        name = "LanguageServer",
        doc = "LSP Language Server interface — methods the client calls on the server.",
        requests = serverRequests,
        notifications = serverNotifications
    )

    fun generateCleanClient(): String = generateCleanInterface(
        name = "LanguageClient",
        doc = "LSP Language Client interface — methods the server calls on the client.",
        requests = clientRequests,
        notifications = clientNotifications
    )

    // ---- Ksrpc subinterfaces (in :lsp-ksrpc) ----

    fun generateKsrpcServer(): String = generateKsrpcInterface(
        name = "KsrpcLanguageServer",
        baseName = "LanguageServer",
        doc = "ksrpc-annotated [LanguageServer] for use with the JSON-RPC transport.\n" +
            "Implement this (or extend [DefaultLanguageServer]) to host an LSP server\n" +
            "via [com.monkopedia.lsp.ksrpc.connectAsLspServer].",
        requests = serverRequests,
        notifications = serverNotifications
    )

    fun generateKsrpcClient(): String = generateKsrpcInterface(
        name = "KsrpcLanguageClient",
        baseName = "LanguageClient",
        doc = "ksrpc-annotated [LanguageClient] for use with the JSON-RPC transport.\n" +
            "Implement this (or extend [DefaultLanguageClient]) to host an LSP client\n" +
            "via [com.monkopedia.lsp.ksrpc.connectAsLspClient].",
        requests = clientRequests,
        notifications = clientNotifications
    )

    // ---- Default base classes (in :lsp-ksrpc, extend the ksrpc subinterface) ----

    fun generateDefaultServer(): String = generateDefaultClass(
        name = "DefaultLanguageServer",
        interfaceName = "KsrpcLanguageServer",
        doc = "Default [KsrpcLanguageServer]: unimplemented requests throw " +
            "NotImplementedError; notifications are no-ops.\n" +
            "Subclass and override only what you need.",
        requests = serverRequests,
        notifications = serverNotifications
    )

    fun generateDefaultClient(): String = generateDefaultClass(
        name = "DefaultLanguageClient",
        interfaceName = "KsrpcLanguageClient",
        doc = "Default [KsrpcLanguageClient]: unimplemented requests throw " +
            "NotImplementedError; notifications are no-ops.\n" +
            "Subclass and override only what you need.",
        requests = clientRequests,
        notifications = clientNotifications
    )

    /**
     * A `KsrpcLanguageServer` decorator that delegates every call to a wrapped
     * server while advancing a [LifecycleState] on the lifecycle methods. Plain
     * (un-annotated) overrides — like `DefaultLanguageServer` — so it satisfies the
     * `@KsService` interface without re-declaring `@KsMethod` (which the ksrpc FIR
     * plugin only permits on the interface itself).
     */
    fun generateLifecycleTrackingServer(): String {
        val w = CodeWriter()
        w.kdoc(
            "Wraps a [KsrpcLanguageServer], delegating every method while advancing\n" +
                "[com.monkopedia.lsp.ksrpc.LifecycleState] on `initialized` / `shutdown` /\n" +
                "`exit`. Used by `connectAsLspServer(server, lifecycle)`."
        )
        val header = "internal class LifecycleTrackingLanguageServer(" +
            "private val delegate: KsrpcLanguageServer, " +
            "private val lifecycle: LifecycleState) : KsrpcLanguageServer"
        w.block(header) {
            for (req in serverRequests) {
                line()
                generateTrackingImpl(this, req.method, req.result, req.params)
            }
            for (notif in serverNotifications) {
                line()
                generateTrackingImpl(this, notif.method, null, notif.params)
            }
        }
        return w.toString()
    }

    private fun generateTrackingImpl(
        w: CodeWriter,
        method: String,
        result: LspType?,
        params: LspType?
    ) {
        val arg = if (params != null) "params" else ""
        emitMethodSignature(w, method, result, params, prefix = "override suspend fun ") {
                _,
                name
            ->
            when (name) {
                "initialized" -> {
                    w.line("    delegate.initialized($arg)")
                    w.line("    lifecycle.advanceTo(LifecycleState.Phase.INITIALIZED)")
                }

                "shutdown" -> {
                    w.line("    val result = delegate.shutdown()")
                    w.line("    lifecycle.advanceTo(LifecycleState.Phase.SHUTTING_DOWN)")
                    w.line("    return result")
                }

                "exit" -> {
                    w.line("    delegate.exit()")
                    w.line("    lifecycle.advanceTo(LifecycleState.Phase.EXITED)")
                }

                else -> {
                    val call = "delegate.$name($arg)"
                    if (result != null) w.line("    return $call") else w.line("    $call")
                }
            }
        }
    }

    // ---- Implementation ----

    private fun generateCleanInterface(
        name: String,
        doc: String,
        requests: List<Request>,
        notifications: List<Notification>
    ): String {
        val w = CodeWriter()
        w.kdoc(doc)
        w.block("interface $name") {
            for (req in requests) {
                line()
                generateCleanRequest(this, req)
            }
            for (notif in notifications) {
                line()
                generateCleanNotification(this, notif)
            }

            // Companion object with method-name constants.
            line()
            block("companion object") {
                for (req in requests) {
                    line(
                        "const val ${req.method.toMethodConstName()}: String = " +
                            "\"${req.method}\""
                    )
                }
                for (notif in notifications) {
                    line(
                        "const val ${notif.method.toMethodConstName()}: String = " +
                            "\"${notif.method}\""
                    )
                }
            }
        }
        return w.toString()
    }

    private fun generateCleanRequest(w: CodeWriter, req: Request) {
        w.kdoc(req.documentation, req.since)
        if (req.errorData != null) {
            val errorType = resolver.resolve(req.errorData)
            w.line("// errorData: $errorType")
        }
        emitMethodSignature(w, req.method, req.result, req.params, prefix = "suspend fun ")
    }

    private fun generateCleanNotification(w: CodeWriter, notif: Notification) {
        w.kdoc(notif.documentation, notif.since)
        emitMethodSignature(w, notif.method, null, notif.params, prefix = "suspend fun ")
    }

    private fun generateKsrpcInterface(
        name: String,
        baseName: String,
        doc: String,
        requests: List<Request>,
        notifications: List<Notification>
    ): String {
        val w = CodeWriter()
        w.kdoc(doc)
        w.line("@KsService")
        w.block("interface $name : $baseName, RpcService") {
            for (req in requests) {
                line()
                generateKsrpcRequestOverride(this, req, baseName)
            }
            for (notif in notifications) {
                line()
                generateKsrpcNotificationOverride(this, notif, baseName)
            }
        }
        return w.toString()
    }

    private fun generateKsrpcRequestOverride(w: CodeWriter, req: Request, baseName: String) {
        // ksrpc compiler plugin doesn't resolve const val references — use string
        // literal here. The matching const val on the [baseName] companion still
        // documents the wire name for users who want to reference it.
        w.line("@KsMethod(\"${req.method}\")")
        emitMethodSignature(w, req.method, req.result, req.params, prefix = "override suspend fun ")
    }

    private fun generateKsrpcNotificationOverride(
        w: CodeWriter,
        notif: Notification,
        baseName: String
    ) {
        w.line("@KsMethod(\"${notif.method}\")")
        w.line("@KsNotification")
        emitMethodSignature(w, notif.method, null, notif.params, prefix = "override suspend fun ")
    }

    private fun generateDefaultClass(
        name: String,
        interfaceName: String,
        doc: String,
        requests: List<Request>,
        notifications: List<Notification>
    ): String {
        val w = CodeWriter()
        w.kdoc(doc)
        w.block("open class $name : $interfaceName") {
            for (req in requests) {
                line()
                generateDefaultRequestImpl(this, req)
            }
            for (notif in notifications) {
                line()
                generateDefaultNotificationImpl(this, notif)
            }
        }
        return w.toString()
    }

    private fun generateDefaultRequestImpl(w: CodeWriter, req: Request) {
        emitMethodSignature(
            w,
            req.method,
            req.result,
            req.params,
            prefix = "override suspend fun "
        ) { _, methodName ->
            // Body: throw NotImplementedError. Use explicit block so the inferred
            // return type is the declared one, not Nothing.
            w.line("    throw NotImplementedError(\"$methodName not implemented\")")
        }
    }

    private fun generateDefaultNotificationImpl(w: CodeWriter, notif: Notification) {
        // Notifications are fire-and-forget with no response channel — a default
        // that threw would crash the receive loop on any notification the subclass
        // didn't override. Default to a no-op; override to handle.
        emitMethodSignature(
            w,
            notif.method,
            null,
            notif.params,
            prefix = "override suspend fun "
        ) { _, _ ->
            w.line("    // No-op by default; override to handle this notification.")
        }
    }

    /**
     * Emit a method signature shared between clean / ksrpc-override / default-impl
     * generators. If [body] is non-null, emits `signature {` + body + `}`. Otherwise
     * emits a bare abstract `signature`.
     */
    private fun emitMethodSignature(
        w: CodeWriter,
        wireMethod: String,
        result: LspType?,
        params: LspType?,
        prefix: String,
        body: ((CodeWriter, String) -> Unit)? = null
    ) {
        val methodName = wireMethod.toMethodName()
        val capitalized = methodName.replaceFirstChar { it.uppercase() }
        val returnType = if (result != null) {
            ": " + resolver.resolve(result, "${capitalized}Result")
        } else {
            ""
        }
        val paramSignature = if (params != null) {
            "params: " + resolver.resolve(params, "${capitalized}Params")
        } else {
            ""
        }
        val signature = "$prefix$methodName($paramSignature)$returnType"
        if (body != null) {
            w.line("$signature {")
            body(w, methodName)
            w.line("}")
        } else {
            w.line(signature)
        }
    }
}

/**
 * Convert an LSP method name like "textDocument/hover" to a Kotlin method name like "textDocumentHover".
 */
private fun String.toMethodName(): String {
    // Handle $/ prefix (e.g., "$/cancelRequest" → "cancelRequest")
    val cleaned = removePrefix("$/")
    return cleaned.split("/").mapIndexed { i, part ->
        if (i == 0) part else part.replaceFirstChar { it.uppercase() }
    }.joinToString("")
}

/**
 * Convert an LSP method name like "textDocument/hover" or "$/progress" to an
 * UPPER_SNAKE_CASE constant name like `TEXT_DOCUMENT_HOVER` or `PROGRESS`.
 */
private fun String.toMethodConstName(): String {
    val cleaned = removePrefix("$/")
    return cleaned.split("/").joinToString("_") { part ->
        // camelCase → CAMEL_CASE
        part.replace(Regex("([a-z])([A-Z])"), "$1_$2").uppercase()
    }
}
