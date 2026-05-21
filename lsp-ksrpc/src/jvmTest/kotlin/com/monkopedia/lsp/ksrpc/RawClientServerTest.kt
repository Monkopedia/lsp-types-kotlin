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
package com.monkopedia.lsp.ksrpc

import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Tests that drive **our generated server** via the actual LSP wire protocol,
 * without using any of our own client code. The "client" here is plain JVM
 * code writing raw `Content-Length:`-framed JSON to stdin and reading bytes
 * back from stdout.
 *
 * This is the inverse of [RealServerIntegrationTest]: there, our client talks
 * to a real LSP server (clangd). Here, a real LSP client (just raw bytes —
 * what an editor like vscode actually sends) talks to our server.
 *
 * The server under test is the `samples/echo-server` distribution. The test
 * is gated on the install having been built, so running just this test in
 * isolation requires `./gradlew :samples:echo-server:installDist` first.
 */
class RawClientServerTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `echo-server handles initialize, hover, shutdown, exit`() {
        // Gradle runs JVM tests from the module dir (lsp-ksrpc/) — the install
        // lives at the project root level. Search both for robustness.
        val script = listOf(
            File("samples/echo-server/build/install/echo-server/bin/echo-server"),
            File("../samples/echo-server/build/install/echo-server/bin/echo-server")
        ).firstOrNull { it.exists() }
        requireOrSkip(
            "echo-server not built; run :samples:echo-server:installDist",
            script != null
        )
        script!!

        val process = ProcessBuilder(script.absolutePath)
            .redirectInput(ProcessBuilder.Redirect.PIPE)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            // The wrapper script needs JAVA_HOME unless `java` is on PATH, and the
            // distZip script doesn't pin a Java version. Inherit from the test JVM.
            .also { it.environment()["JAVA_HOME"] = System.getProperty("java.home") }
            .start()

        try {
            val out = process.outputStream
            val input = process.inputStream

            // ---- initialize ----
            val initReq = """{
                "jsonrpc":"2.0",
                "id":1,
                "method":"initialize",
                "params":{
                    "capabilities":{},
                    "processId":null,
                    "rootUri":null
                }
            }""".compact()
            sendMessage(out, initReq)
            val initJson = readUntilId(input, expectedId = 1, timeoutMs = 15_000)
            assertEquals("2.0", initJson["jsonrpc"]?.jsonPrimitive?.contentOrNull)
            assertEquals(1, initJson["id"]?.jsonPrimitive?.contentOrNull?.toIntOrNull())
            val result = initJson["result"]?.jsonObject
            assertNotNull(result, "initialize must return a result, got: $initJson")
            // Our echo-server advertises hoverProvider via BooleanOr.BooleanValue(true)
            // — wire shape is just `true`.
            assertEquals(
                true,
                result.jsonObject["capabilities"]?.jsonObject
                    ?.get("hoverProvider")?.jsonPrimitive?.contentOrNull?.toBoolean()
            )

            // ---- initialized notification (no response) ----
            sendMessage(out, """{"jsonrpc":"2.0","method":"initialized","params":{}}""")

            // ---- textDocument/didOpen notification ----
            sendMessage(
                out,
                """{
                    "jsonrpc":"2.0",
                    "method":"textDocument/didOpen",
                    "params":{
                        "textDocument":{
                            "uri":"file:///foo.kt",
                            "languageId":"kotlin",
                            "version":1,
                            "text":"fun main() {}"
                        }
                    }
                }""".compact()
            )

            // ---- textDocument/hover ----
            sendMessage(
                out,
                """{
                    "jsonrpc":"2.0",
                    "id":2,
                    "method":"textDocument/hover",
                    "params":{
                        "textDocument":{"uri":"file:///foo.kt"},
                        "position":{"line":3,"character":5}
                    }
                }""".compact()
            )
            // Server may send notifications (publishDiagnostics, $/progress, etc.)
            // interleaved with the response. Read until we get a message whose id
            // matches the request.
            val hoverJson = readUntilId(input, expectedId = 2, timeoutMs = 5_000)
            assertEquals(2, hoverJson["id"]?.jsonPrimitive?.contentOrNull?.toIntOrNull())
            val hoverResult = hoverJson["result"]?.jsonObject
            assertNotNull(hoverResult)
            // echo-server returns Hover with markdown contents shaped as MarkupContent.
            val contents = hoverResult["contents"]?.jsonObject
            assertNotNull(contents)
            assertEquals("markdown", contents["kind"]?.jsonPrimitive?.contentOrNull)
            val text = contents["value"]?.jsonPrimitive?.contentOrNull
            assertNotNull(text)
            // Should mention the URI and the position.
            assert(text.contains("file:///foo.kt")) { "Expected URI in hover text: $text" }
            assert(text.contains("3:5")) { "Expected position in hover text: $text" }

            // ---- shutdown ----
            // Send `params: {}` rather than omitting params — ksrpc 1.0.0-RC2's
            // 0-arg method dispatch chokes on missing params. (Real LSP clients
            // also vary here; we accept the more permissive shape.)
            sendMessage(out, """{"jsonrpc":"2.0","id":3,"method":"shutdown","params":{}}""")
            val shutdownJson = readUntilId(input, expectedId = 3, timeoutMs = 5_000)
            assertEquals(3, shutdownJson["id"]?.jsonPrimitive?.contentOrNull?.toIntOrNull())
            // shutdown's result is null per LSP spec.

            // ---- exit (notification) ----
            sendMessage(out, """{"jsonrpc":"2.0","method":"exit","params":{}}""")

            // Server should terminate within a couple of seconds.
            assert(process.waitFor(5, TimeUnit.SECONDS)) {
                "echo-server didn't exit cleanly after exit notification"
            }
            assertEquals(0, process.exitValue())
        } finally {
            if (process.isAlive) {
                process.destroyForcibly()
                process.waitFor(2, TimeUnit.SECONDS)
            }
        }
    }
}

private val readJson = Json { ignoreUnknownKeys = true }

/**
 * Read framed messages from [input] until one with `id == expectedId` arrives.
 * Notifications (no `id` field) are silently dropped — they're side-channel
 * traffic like `$/progress` or `window/logMessage`.
 */
private fun readUntilId(input: InputStream, expectedId: Int, timeoutMs: Long): JsonObject {
    val deadline = System.currentTimeMillis() + timeoutMs
    while (true) {
        val remaining = deadline - System.currentTimeMillis()
        if (remaining <= 0) error("Timeout waiting for response with id=$expectedId")
        val msg = readMessage(input, timeoutMs = remaining)
        val obj = readJson.parseToJsonElement(msg).jsonObject
        val id = obj["id"]?.jsonPrimitive?.contentOrNull?.toIntOrNull()
        if (id == expectedId) return obj
        // else: a notification or unrelated response, keep reading.
    }
}

private fun sendMessage(out: OutputStream, jsonBody: String) {
    val bytes = jsonBody.toByteArray(Charsets.UTF_8)
    val header = "Content-Length: ${bytes.size}\r\n\r\n".toByteArray(Charsets.US_ASCII)
    out.write(header)
    out.write(bytes)
    out.flush()
}

/**
 * Read one Content-Length-framed message from [input]. Blocks until a complete
 * message is available; throws after [timeoutMs] if input stalls.
 */
private fun readMessage(input: InputStream, timeoutMs: Long): String {
    val deadline = System.currentTimeMillis() + timeoutMs

    // Read header bytes one at a time looking for \r\n\r\n.
    val headerBuf = StringBuilder()
    var contentLength = -1
    while (true) {
        if (System.currentTimeMillis() > deadline) {
            error("Timeout reading header. Got so far: $headerBuf")
        }
        if (input.available() == 0) {
            Thread.sleep(10)
            continue
        }
        val b = input.read()
        if (b == -1) error("EOF reading header. Got: $headerBuf")
        headerBuf.append(b.toChar())
        if (headerBuf.endsWith("\r\n\r\n")) {
            val match = Regex("Content-Length: (\\d+)").find(headerBuf)
                ?: error("No Content-Length in header: $headerBuf")
            contentLength = match.groupValues[1].toInt()
            break
        }
    }

    val body = ByteArray(contentLength)
    var offset = 0
    while (offset < contentLength) {
        if (System.currentTimeMillis() > deadline) {
            error("Timeout reading body. Got $offset/$contentLength bytes")
        }
        val n = input.read(body, offset, contentLength - offset)
        if (n == -1) error("EOF reading body. Got $offset/$contentLength bytes")
        offset += n
    }
    return String(body, Charsets.UTF_8)
}

/** Strip whitespace from a JSON string for inclusion in test fixtures. */
private fun String.compact(): String = Json.parseToJsonElement(this).toString()
