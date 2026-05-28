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

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FilterInputStream
import java.io.FilterOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Tee'd wire-frame recorder for [RealServerClientRoleTest]. Wraps a pair of
 * `(InputStream, OutputStream)` representing a spawned LSP server's stdio,
 * parses `Content-Length:`-framed JSON-RPC envelopes flowing in each direction,
 * and writes the unwrapped `params` / `result` payloads to disk so the portable
 * [CapturedCorpusReplayTest] can validate round-trip stability against REAL
 * third-party payloads — never agent-authored JSON re-fed into our own types.
 *
 * Frames are classified into the directories:
 *  - `request`            — server-bound JSON-RPC request (params written)
 *  - `notification`       — server-bound JSON-RPC notification (params written)
 *  - `response`           — client-bound JSON-RPC response (result written;
 *                           method is recovered from the correlated request id)
 *  - `clientRequest`      — client-bound JSON-RPC request from the server
 *                           (server-initiated, e.g. workspace/configuration)
 *  - `clientNotification` — client-bound notification (e.g. window/logMessage,
 *                           textDocument/publishDiagnostics)
 *
 * Output path: `<corpusRoot>/<server>/<direction>/<method>__<seq>.json`. `<seq>`
 * is monotonic per (server,direction,method) and indexed across the whole
 * recorder lifetime so multiple test runs don't overwrite each other.
 *
 * Defensive by design — recorder failure must NEVER break the underlying test.
 * Every disk write and JSON parse is wrapped in `runCatching`. A malformed
 * frame is silently skipped; the streams keep flowing the original bytes through.
 */
internal class CorpusRecorder(private val server: String, private val corpusRoot: File) {
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    /**
     * id → method correlation. Populated when we see an outbound (client → server)
     * request with a numeric/string id and a `method`. Consumed when we see the
     * corresponding inbound response — the response envelope carries the id but
     * not the method, so we need this table to know which serializer to label
     * the captured result with.
     */
    private val outboundRequestMethods = ConcurrentHashMap<String, String>()

    /**
     * id → method correlation for server-initiated requests. Populated on the
     * inbound side and consumed when the client's response goes out.
     */
    private val inboundRequestMethods = ConcurrentHashMap<String, String>()

    /**
     * Monotonic sequence per (direction,method) so multiple captures within a
     * single drive don't collide on disk.
     */
    private val seqs = ConcurrentHashMap<String, Int>()

    /**
     * Wrap the server's stdout (server → client) so we can tee inbound frames.
     */
    fun wrapInbound(stream: InputStream): InputStream =
        TeeInputStream(stream) { onInboundFrame(it) }

    /**
     * Wrap the server's stdin (client → server) so we can tee outbound frames.
     */
    fun wrapOutbound(stream: OutputStream): OutputStream =
        TeeOutputStream(stream) { onOutboundFrame(it) }

    // ---- frame handlers ------------------------------------------------------

    private fun onOutboundFrame(bytes: ByteArray) {
        val envelope = parseEnvelope(bytes) ?: return
        val method = envelope.method()
        val id = envelope.id()
        when {
            method != null && id != null -> {
                // client → server REQUEST
                outboundRequestMethods[id] = method
                writeFrame(Direction.REQUEST, method, envelope.paramsBytes())
            }

            method != null && id == null -> {
                // client → server NOTIFICATION
                writeFrame(Direction.NOTIFICATION, method, envelope.paramsBytes())
            }

            method == null && id != null -> {
                // client → server RESPONSE to a server-initiated request.
                val correlated = inboundRequestMethods.remove(id) ?: return
                writeFrame(Direction.CLIENT_RESPONSE, correlated, envelope.resultBytes())
            }
        }
    }

    private fun onInboundFrame(bytes: ByteArray) {
        val envelope = parseEnvelope(bytes) ?: return
        val method = envelope.method()
        val id = envelope.id()
        when {
            method != null && id != null -> {
                // server → client REQUEST (server-initiated)
                inboundRequestMethods[id] = method
                writeFrame(Direction.CLIENT_REQUEST, method, envelope.paramsBytes())
            }

            method != null && id == null -> {
                // server → client NOTIFICATION
                writeFrame(Direction.CLIENT_NOTIFICATION, method, envelope.paramsBytes())
            }

            method == null && id != null -> {
                // server → client RESPONSE to a client request
                val correlated = outboundRequestMethods.remove(id) ?: return
                writeFrame(Direction.RESPONSE, correlated, envelope.resultBytes())
            }
        }
    }

    private fun writeFrame(direction: Direction, method: String, payload: ByteArray?) {
        if (payload == null || payload.isEmpty()) return
        // null `params`/`result` carry no shape — skip them (we'd be persisting `null`).
        val text = runCatching { payload.toString(StandardCharsets.UTF_8) }.getOrNull() ?: return
        if (text == "null" || text.isBlank()) return
        val sanitizedMethod = method.replace('/', '_').replace("$", "dollar")
        val key = "${direction.dirName}|$sanitizedMethod"
        val seq = seqs.compute(key) { _, v -> (v ?: 0) + 1 } ?: 1
        runCatching {
            val dir = File(corpusRoot, "$server/${direction.dirName}")
            dir.mkdirs()
            val file = File(dir, "${sanitizedMethod}__$seq.json")
            file.writeText(text, StandardCharsets.UTF_8)
        }
    }

    // ---- envelope parsing ----------------------------------------------------

    /**
     * Best-effort JSON-RPC envelope view over the raw frame bytes. Returns
     * null if the bytes aren't a JSON object (defensive — never crash the test).
     */
    private fun parseEnvelope(bytes: ByteArray): Envelope? {
        val text = runCatching { bytes.toString(StandardCharsets.UTF_8) }.getOrNull() ?: return null
        val element = runCatching { json.parseToJsonElement(text) }.getOrNull() ?: return null
        if (element !is JsonObject) return null
        return Envelope(element)
    }

    private class Envelope(private val obj: JsonObject) {
        fun method(): String? = (obj["method"] as? JsonPrimitive)?.let {
            if (it is JsonNull) null else it.content
        }

        fun id(): String? {
            val elem = obj["id"] ?: return null
            if (elem is JsonNull) return null
            val prim = elem as? JsonPrimitive ?: return null
            return prim.content
        }

        fun paramsBytes(): ByteArray? = obj["params"]
            ?.takeIf { it !is JsonNull }
            ?.let {
                Json.encodeToString(JsonElement.serializer(), it)
                    .toByteArray(StandardCharsets.UTF_8)
            }

        fun resultBytes(): ByteArray? = obj["result"]
            ?.takeIf { it !is JsonNull }
            ?.let {
                Json.encodeToString(JsonElement.serializer(), it)
                    .toByteArray(StandardCharsets.UTF_8)
            }
    }

    internal enum class Direction(val dirName: String) {
        REQUEST("request"),
        RESPONSE("response"),
        NOTIFICATION("notification"),
        CLIENT_REQUEST("clientRequest"),
        CLIENT_NOTIFICATION("clientNotification"),
        CLIENT_RESPONSE("clientResponse")
    }
}

/**
 * Resolve the corpus root for a recorder. Honours `lsp.corpusRoot` for tests, falls
 * back to the canonical jvmTest resources directory so freshly captured fixtures
 * land on disk where the replay test (and a subsequent commit) expects them.
 */
internal fun captureCorpusRoot(): File {
    val override = System.getProperty("lsp.corpusRoot")
    if (!override.isNullOrBlank()) return File(override)
    val candidates = listOf(
        "lsp-ksrpc/src/jvmTest/resources/captured",
        "src/jvmTest/resources/captured"
    )
    for (p in candidates) {
        val f = File(p)
        if (f.exists() || File(p).parentFile?.exists() == true) return f
    }
    return File("src/jvmTest/resources/captured")
}

internal fun isCaptureEnabled(): Boolean = System.getProperty("lsp.captureCorpus") == "true"

// ---- tee streams (Content-Length frame extraction) ---------------------------

/**
 * Splits an LSP-framed stream and emits each frame's body bytes to [sink] AFTER
 * forwarding them through to the real consumer. The recorder NEVER buffers
 * unbounded data: it accumulates only the in-progress frame, releases it on the
 * delimiter, then resets. EOF/IOExceptions are surfaced unchanged.
 */
private class TeeInputStream(delegate: InputStream, private val sink: (ByteArray) -> Unit) :
    FilterInputStream(delegate) {
    private val parser = LspFrameParser(sink)

    override fun read(): Int {
        val b = `in`.read()
        if (b >= 0) parser.feed(b.toByte())
        return b
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val n = `in`.read(b, off, len)
        if (n > 0) parser.feed(b, off, n)
        return n
    }
}

private class TeeOutputStream(delegate: OutputStream, private val sink: (ByteArray) -> Unit) :
    FilterOutputStream(delegate) {
    private val parser = LspFrameParser(sink)

    override fun write(b: Int) {
        `out`.write(b)
        parser.feed(b.toByte())
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        `out`.write(b, off, len)
        parser.feed(b, off, len)
    }
}

/**
 * Stateful LSP wire-frame parser. The wire format is:
 *
 *   Content-Length: <N>\r\n
 *   [other headers ...]\r\n
 *   \r\n
 *   <N bytes of body>
 *
 * The parser tracks two states (HEADERS, BODY), accumulates the body up to N
 * bytes, and emits it to [sink] when complete. Anything we don't understand
 * (bad header, malformed length) is silently abandoned and the parser resets.
 * Resetting on the FIRST `\r\n\r\n` boundary in HEADERS is safe because every
 * conforming LSP impl emits `Content-Length` once per frame.
 */
private class LspFrameParser(private val sink: (ByteArray) -> Unit) {
    private enum class State { HEADERS, BODY }

    private var state: State = State.HEADERS
    private val headerBuf = ByteArrayOutputStream()
    private val bodyBuf = ByteArrayOutputStream()
    private var bodyRemaining = 0

    fun feed(b: Byte) {
        when (state) {
            State.HEADERS -> {
                headerBuf.write(b.toInt() and 0xff)
                val arr = headerBuf.toByteArray()
                // Detect end of headers: \r\n\r\n at the tail.
                if (arr.size >= 4 &&
                    arr[arr.size - 4] == 0x0D.toByte() &&
                    arr[arr.size - 3] == 0x0A.toByte() &&
                    arr[arr.size - 2] == 0x0D.toByte() &&
                    arr[arr.size - 1] == 0x0A.toByte()
                ) {
                    bodyRemaining = parseContentLength(arr)
                    headerBuf.reset()
                    if (bodyRemaining <= 0) {
                        // Malformed or zero-length — discard and stay in HEADERS.
                        return
                    }
                    state = State.BODY
                }
            }

            State.BODY -> {
                bodyBuf.write(b.toInt() and 0xff)
                bodyRemaining -= 1
                if (bodyRemaining == 0) {
                    val frame = bodyBuf.toByteArray()
                    bodyBuf.reset()
                    state = State.HEADERS
                    runCatching { sink(frame) }
                }
            }
        }
    }

    fun feed(buf: ByteArray, off: Int, len: Int) {
        for (i in 0 until len) feed(buf[off + i])
    }

    private fun parseContentLength(headerBytes: ByteArray): Int {
        val text = String(headerBytes, StandardCharsets.US_ASCII)
        // Lines are CRLF-separated; case-insensitive header name per LSP.
        for (line in text.split("\r\n")) {
            val idx = line.indexOf(':')
            if (idx <= 0) continue
            val name = line.substring(0, idx).trim()
            if (!name.equals("Content-Length", ignoreCase = true)) continue
            val value = line.substring(idx + 1).trim()
            return value.toIntOrNull() ?: return 0
        }
        return 0
    }
}
