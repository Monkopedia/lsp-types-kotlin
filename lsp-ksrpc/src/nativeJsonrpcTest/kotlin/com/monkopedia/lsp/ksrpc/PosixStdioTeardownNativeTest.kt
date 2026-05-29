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
@file:OptIn(ExperimentalForeignApi::class)

package com.monkopedia.lsp.ksrpc

import com.monkopedia.ksrpc.sockets.posixFileReadChannel
import com.monkopedia.ksrpc.sockets.posixFileWriteChannel
import io.ktor.utils.io.cancel
import io.ktor.utils.io.close
import io.ktor.utils.io.readAvailable
import io.ktor.utils.io.writeFully
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import platform.posix.pipe

/**
 * Native (Kotlin/Native: linuxX64 + macOS) teardown guarantee for the posix-fd
 * byte-channel primitive that backs the native stdio LSP transport
 * (ksrpc-sockets `posixFileReadChannel` / `posixFileWriteChannel`).
 *
 * ## Why this test exists (#53 / the deferred native leg of #47)
 *
 * The native external-process posix-stdio integration was deferred because the
 * only native posix-fd read primitive busy-looped forever on EOF: its dedicated
 * reader thread treated `read(2) == 0` (peer closed the write end) as
 * `continue`, spinning instead of terminating, which wedged process exit and
 * could hang the build on teardown. ksrpc 1.1.0 ships the #201 fix: EOF now
 * `break`s, so the reader thread terminates and the read channel reaches
 * end-of-stream once the write end is closed.
 *
 * This test proves that on a real posix pipe, end to end:
 *  1. a byte round-trip works over real fds (write → flush → read back), then
 *  2. closing the write channel (a real EOF on the read fd) makes the reader
 *     terminate — the read channel reports end-of-stream — within a bounded
 *     [withTimeout] watchdog.
 *
 * If the EOF busy-loop regressed, step 2's reader would never reach
 * end-of-stream and the [withTimeout] fails the test (bounded), rather than
 * wedging `allTests`. The whole point of the fix is that teardown TERMINATES, so
 * this test must never be able to hang.
 */
class PosixStdioTeardownNativeTest {

    @Test
    fun `posix fd channel round-trips and the reader terminates on EOF`() = runTest {
        withContext(Dispatchers.Default) {
            val fds = memScoped {
                val pair = allocArray<IntVar>(2)
                check(pipe(pair) == 0) { "Failed to create POSIX pipe" }
                pair[0] to pair[1]
            }
            val readChannel = posixFileReadChannel(fds.first)
            val writeChannel = posixFileWriteChannel(fds.second)

            val terminatedCleanly = runCatching {
                withTimeout(TEARDOWN_TIMEOUT_MS) {
                    // 1. Round-trip a payload over the real posix fds.
                    val payload = "shutdown-roundtrip".encodeToByteArray()
                    writeChannel.writeFully(payload, 0, payload.size)
                    writeChannel.flush()

                    val drain = ByteArray(payload.size)
                    var read = 0
                    while (read < payload.size) {
                        val n = readChannel.readAvailable(drain, read, payload.size - read)
                        check(n > 0) { "Unexpected end of stream while draining the round-trip" }
                        read += n
                    }
                    assertEquals(
                        payload.decodeToString(),
                        drain.decodeToString(),
                        "payload should round-trip byte-for-byte over the posix pipe"
                    )

                    // 2. Close the write end -> real EOF on the read fd. With the #201 fix the
                    //    reader thread breaks on read()==0 and the channel reaches end-of-stream;
                    //    pre-fix it busy-looped here forever and this withTimeout would fire.
                    //    (Already flushed above; the async close() is fine and the only public
                    //    close on this ktor version.)
                    @Suppress("DEPRECATION")
                    writeChannel.close()

                    val tail = ByteArray(16)
                    while (true) {
                        val n = readChannel.readAvailable(tail, 0, tail.size)
                        if (n == -1) break // end-of-stream: the reader terminated on EOF
                    }
                }
                true
            }.getOrElse { false }

            // Best-effort: release the read side regardless of outcome.
            readChannel.cancel()

            assertTrue(
                terminatedCleanly,
                "posix read channel must reach end-of-stream after the write end closes " +
                    "(ksrpc #201 EOF-terminates fix); it did not within ${TEARDOWN_TIMEOUT_MS}ms"
            )
        }
    }

    private companion object {
        const val TEARDOWN_TIMEOUT_MS = 10_000L
    }
}
