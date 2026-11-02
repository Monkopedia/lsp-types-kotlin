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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import platform.posix.F_GETFD
import platform.posix.close
import platform.posix.fcntl
import platform.posix.pipe
import platform.posix.write

/**
 * A second [LspServerProcess.close] must not touch an fd it no longer owns (#181).
 *
 * The kernel hands out the lowest free descriptor, so once the process's pipe fds are
 * released, fresh pipes receive those very numbers. If a second `close()` calls
 * `close(2)` on the stored numbers again, it destroys the unrelated fresh pipes.
 *
 * The reuse is asserted, not assumed: without it the test could pass without ever
 * exercising the hazard.
 */
class LspServerProcessCloseNativeTest {

    // runBlocking (not runTest): spawns a real child and needs real wall-clock time for
    // the bounded kill/reap in close() and for the channel threads to release their fds.
    @Test
    fun `a second close does not close an unrelated fd that reused the number`() =
        runBlocking(Dispatchers.Default) {
            val before = openFds()
            val proc = spawnLspServer(listOf("cat"))
            val held = openFds() - before
            assertTrue(held.isNotEmpty(), "spawnLspServer held no new fds")

            proc.close()
            // Whoever owns them (the first close() or the ksrpc channel threads), every
            // fd the process held must be released, or a fresh pipe cannot reuse it.
            val released = withTimeoutOrNull(RELEASE_TIMEOUT_MS) {
                while (held.any(::isOpen)) delay(10)
            }
            assertNotNull(released, "fds never released: ${held.filter(::isOpen)} of $held")

            val fresh = mutableListOf<Pair<Int, Int>>()
            try {
                while (!fresh.flatMap { it.toList() }.containsAll(held) &&
                    fresh.size < MAX_PIPES
                ) {
                    fresh += freshPipe()
                }
                assertTrue(
                    fresh.flatMap { it.toList() }.containsAll(held),
                    "precondition: fresh pipes $fresh did not reuse every held fd $held"
                )

                proc.close()

                val byte = byteArrayOf(1)
                for ((readFd, writeFd) in fresh) {
                    assertTrue(isOpen(readFd), "second close() closed unrelated fd $readFd")
                    assertTrue(isOpen(writeFd), "second close() closed unrelated fd $writeFd")
                    assertEquals(1L, write(writeFd, byte.refTo(0), 1u), "fd $writeFd unwritable")
                }
            } finally {
                for ((readFd, writeFd) in fresh) {
                    close(readFd)
                    close(writeFd)
                }
            }
        }

    private fun isOpen(fd: Int): Boolean = fcntl(fd, F_GETFD) != -1

    private fun openFds(): Set<Int> = (0 until FD_SCAN_LIMIT).filter(::isOpen).toSet()

    private fun freshPipe(): Pair<Int, Int> = memScoped {
        val fds = allocArray<IntVar>(2)
        check(pipe(fds) == 0) { "pipe() failed" }
        fds[0] to fds[1]
    }

    private companion object {
        const val FD_SCAN_LIMIT = 1024
        const val MAX_PIPES = 8
        const val RELEASE_TIMEOUT_MS = 10_000L
    }
}
