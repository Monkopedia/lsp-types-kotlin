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
import kotlin.test.assertTrue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import platform.posix.ECHILD
import platform.posix.WNOHANG
import platform.posix.errno
import platform.posix.waitpid

/**
 * Once [LspServerProcess.kill] has reaped the child, the pid is free for the kernel to
 * hand to an unrelated process, so no later `kill()` or `close()` may signal it (#193).
 *
 * Counts the `kill(2)` calls through [posixSignal] rather than staging a real pid
 * reuse, so the test is deterministic and runs on every posix target with no skip path.
 */
class LspServerProcessKillNativeTest {

    // runBlocking (not runTest): spawns a real child and needs real wall-clock time for
    // the bounded kill/reap.
    @Test
    fun `kill signals and reaps a live child then never signals its pid again`() =
        runBlocking(Dispatchers.Default) {
            val signals = mutableListOf<Pair<Int, Int>>()
            val original = posixSignal
            posixSignal = { pid, sig ->
                signals += pid to sig
                original(pid, sig)
            }
            try {
                val proc = spawnLspServer(listOf("cat"))
                val pid = proc.pid.toInt()

                // (a) A live child is signalled and reaped by the first kill().
                proc.kill()
                assertTrue(signals.isNotEmpty(), "first kill() sent no signal to live child")
                assertTrue(signals.all { it.first == pid }, "signalled a foreign pid: $signals")
                val (result, err) = memScoped {
                    val status = allocArray<IntVar>(1)
                    waitpid(pid, status, WNOHANG) to errno
                }
                assertEquals(-1, result, "child $pid was not reaped by kill()")
                assertEquals(ECHILD, err, "child $pid was not reaped by kill()")

                // (b) The pid is now free for reuse: nothing may signal it again.
                val afterReap = signals.toList()
                proc.kill()
                proc.close()
                assertEquals(
                    afterReap,
                    signals,
                    "kill()/close() signalled reaped pid $pid again"
                )
            } finally {
                posixSignal = original
            }
        }
}
