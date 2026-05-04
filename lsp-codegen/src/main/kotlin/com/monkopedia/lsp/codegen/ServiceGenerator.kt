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
 * Generates ksrpc @KsService interfaces for LanguageServer and LanguageClient.
 *
 * Requests/notifications are split by messageDirection:
 * - clientToServer → LanguageServer interface
 * - serverToClient → LanguageClient interface
 * - both → both interfaces
 */
class ServiceGenerator(private val resolver: TypeResolver, private val model: MetaModel) {

    /**
     * Methods handled by the transport layer (ksrpc), not exposed on service interfaces.
     * `$/cancelRequest` is owned by `JsonRpcCancellationConvention.Lsp`.
     */
    private val transportHandled = setOf("$/cancelRequest")

    /**
     * Generate an abstract `Default${name}` class with every method throwing
     * `NotImplementedError` by default. Useful for test stubs and partial
     * implementations.
     */
    fun generateDefaultServer(): String = generateDefaultClass(
        "DefaultLanguageServer",
        "LanguageServer",
        "Default LanguageServer where every method throws NotImplementedError.\n" +
            "Subclass and override only what you need.",
        requests = model.requests.filter {
            it.method !in transportHandled &&
                it.messageDirection in
                setOf(MessageDirection.CLIENT_TO_SERVER, MessageDirection.BOTH)
        },
        notifications = model.notifications.filter {
            it.method !in transportHandled &&
                it.messageDirection in
                setOf(MessageDirection.CLIENT_TO_SERVER, MessageDirection.BOTH)
        }
    )

    fun generateDefaultClient(): String = generateDefaultClass(
        "DefaultLanguageClient",
        "LanguageClient",
        "Default LanguageClient where every method throws NotImplementedError.\n" +
            "Subclass and override only what you need.",
        requests = model.requests.filter {
            it.method !in transportHandled &&
                it.messageDirection in
                setOf(MessageDirection.SERVER_TO_CLIENT, MessageDirection.BOTH)
        },
        notifications = model.notifications.filter {
            it.method !in transportHandled &&
                it.messageDirection in
                setOf(MessageDirection.SERVER_TO_CLIENT, MessageDirection.BOTH)
        }
    )

    fun generateServer(): String = generateInterface(
        name = "LanguageServer",
        doc = "LSP Language Server interface — methods the client calls on the server.",
        requests = model.requests.filter {
            it.method !in transportHandled &&
                it.messageDirection in
                setOf(MessageDirection.CLIENT_TO_SERVER, MessageDirection.BOTH)
        },
        notifications = model.notifications.filter {
            it.method !in transportHandled &&
                it.messageDirection in
                setOf(MessageDirection.CLIENT_TO_SERVER, MessageDirection.BOTH)
        }
    )

    fun generateClient(): String = generateInterface(
        name = "LanguageClient",
        doc = "LSP Language Client interface — methods the server calls on the client.",
        requests = model.requests.filter {
            it.method !in transportHandled &&
                it.messageDirection in
                setOf(MessageDirection.SERVER_TO_CLIENT, MessageDirection.BOTH)
        },
        notifications = model.notifications.filter {
            it.method !in transportHandled &&
                it.messageDirection in
                setOf(MessageDirection.SERVER_TO_CLIENT, MessageDirection.BOTH)
        }
    )

    private fun generateInterface(
        name: String,
        doc: String,
        requests: List<Request>,
        notifications: List<Notification>
    ): String {
        val w = CodeWriter()
        w.kdoc(doc)
        w.line("@com.monkopedia.ksrpc.annotation.KsService")
        w.block("interface $name : com.monkopedia.ksrpc.RpcService") {
            for (req in requests) {
                line()
                generateRequestMethod(this, req)
            }
            for (notif in notifications) {
                line()
                generateNotificationMethod(this, notif)
            }
        }
        return w.toString()
    }

    private fun generateRequestMethod(w: CodeWriter, req: Request) {
        w.kdoc(req.documentation, req.since)
        val methodName = req.method.toMethodName()
        val wireName = req.method

        w.line("@com.monkopedia.ksrpc.annotation.KsMethod(\"$wireName\")")

        // Add @KsError for methods with typed error data
        if (req.errorData != null) {
            val errorType = resolver.resolve(req.errorData)
            w.line("// errorData: $errorType")
        }

        val returnType = resolver.resolve(
            req.result,
            "${methodName.replaceFirstChar {
                it.uppercase()
            }}Result"
        )
        val params = req.params
        if (params != null) {
            val paramType = resolver.resolve(
                params,
                "${methodName.replaceFirstChar {
                    it.uppercase()
                }}Params"
            )
            w.line("suspend fun $methodName(params: $paramType): $returnType")
        } else {
            w.line("suspend fun $methodName(): $returnType")
        }
    }

    private fun generateNotificationMethod(w: CodeWriter, notif: Notification) {
        w.kdoc(notif.documentation, notif.since)
        val methodName = notif.method.toMethodName()
        val wireName = notif.method

        w.line("@com.monkopedia.ksrpc.annotation.KsMethod(\"$wireName\")")
        w.line("@com.monkopedia.ksrpc.annotation.KsNotification")

        val params = notif.params
        if (params != null) {
            val paramType = resolver.resolve(
                params,
                "${methodName.replaceFirstChar {
                    it.uppercase()
                }}Params"
            )
            w.line("suspend fun $methodName(params: $paramType)")
        } else {
            w.line("suspend fun $methodName()")
        }
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
        val methodName = req.method.toMethodName()
        val returnType = resolver.resolve(
            req.result,
            "${methodName.replaceFirstChar { it.uppercase() }}Result"
        )
        val params = req.params
        val signature = if (params != null) {
            val paramType = resolver.resolve(
                params,
                "${methodName.replaceFirstChar { it.uppercase() }}Params"
            )
            "override suspend fun $methodName(params: $paramType): $returnType"
        } else {
            "override suspend fun $methodName(): $returnType"
        }
        // Explicit block (not `=`) so the return type comes from the signature, not Nothing.
        w.line("$signature {")
        w.indent {
            line("throw NotImplementedError(\"$methodName not implemented\")")
        }
        w.line("}")
    }

    private fun generateDefaultNotificationImpl(w: CodeWriter, notif: Notification) {
        val methodName = notif.method.toMethodName()
        val params = notif.params
        val signature = if (params != null) {
            val paramType = resolver.resolve(
                params,
                "${methodName.replaceFirstChar { it.uppercase() }}Params"
            )
            "override suspend fun $methodName(params: $paramType)"
        } else {
            "override suspend fun $methodName()"
        }
        // Explicit block so return type is Unit, not Nothing.
        w.line("$signature {")
        w.indent {
            line("throw NotImplementedError(\"$methodName not implemented\")")
        }
        w.line("}")
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
