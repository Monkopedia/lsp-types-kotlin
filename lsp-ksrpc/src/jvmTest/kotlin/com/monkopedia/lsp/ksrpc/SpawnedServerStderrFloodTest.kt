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

import com.monkopedia.ksrpc.channels.SingleChannelConnection
import com.monkopedia.lsp.ClientCapabilities
import com.monkopedia.lsp.DefaultLanguageClient
import com.monkopedia.lsp.InitializeParams
import com.monkopedia.lsp.LogMessageParams
import com.monkopedia.lsp.PublishDiagnosticsParams
import com.monkopedia.lsp.ShowMessageParams
import java.io.File
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

/**
 * Regression test for issue #165: a spawned server whose stderr is an **undrained pipe**
 * blocks once the OS pipe buffer fills (64 KiB on Linux) and silently stops servicing LSP
 * requests.
 *
 * The child here is the real `samples/echo-server`, fronted by a `/bin/sh` wrapper that
 * writes [FLOOD_BYTES] to stderr **before** `exec`ing the server. If stderr is a pipe that
 * nobody reads, the wrapper wedges mid-flood, the `exec` never happens, and `initialize`
 * never gets an answer — the test fails on its `withTimeout` budget rather than hanging.
 *
 * Note the deliberate absence of `redirectError(DISCARD)`. **Every other JVM test in this
 * repo sets it**, which is precisely why CI could not see #165: those tests configure away
 * the production condition before the test starts. A test that discards stderr proves
 * nothing here.
 *
 * The flood is 'x' characters, and under the fix ([spawnLspServer] inherits stderr) they
 * land on the test worker's own stderr — ~[FLOOD_BYTES] of noise in the test report is the
 * visible cost of stderr actually reaching the host, and is the point rather than a defect.
 */
class SpawnedServerStderrFloodTest : JvmIntegrationTestBase() {

    /**
     * `spawnLspServer` builds its `ProcessBuilder` internally, so **no caller-side
     * workaround exists** — nothing in the signature reaches `redirectError`. It must
     * therefore leave stderr drained by itself, matching what the posix `actual` already
     * does (its forked child never `dup2`s `STDERR_FILENO`, so fd 2 is inherited).
     */
    @Test
    fun `spawnLspServer survives a child that floods stderr past the pipe buffer`() {
        val script = echoServerScriptOrSkip()
        runBlocking(Dispatchers.IO) {
            driveBounded {
                val proc = spawnLspServer(floodThenExec(script))
                try {
                    assertInitializes(proc.connection)
                } finally {
                    proc.close()
                }
            }
        }
    }

    /**
     * The counterpart guarantee for the caller-owned entry point: `asLspConnection` is
     * called on a `ProcessBuilder` the caller configured, so the library must **not**
     * override the caller's stderr choice. Here the caller redirects stderr to a file; the
     * connection must work AND the flood must end up in that file — which it cannot if the
     * library silently forced `INHERIT` (or `PIPE`, or `DISCARD`) over the caller's choice.
     */
    @Test
    fun `asLspConnection honours the caller's own stderr redirect`() {
        val script = echoServerScriptOrSkip()
        val stderrLog = File.createTempFile("lsp-stderr-flood", ".log")
        stderrLog.deleteOnExit()
        runBlocking(Dispatchers.IO) {
            driveBounded {
                val builder = ProcessBuilder(floodThenExec(script))
                    .redirectError(ProcessBuilder.Redirect.to(stderrLog))
                assertInitializes(builder.asLspConnection())
            }
        }
        assertTrue(
            stderrLog.length() >= FLOOD_BYTES,
            "caller's stderr redirect was overridden: ${stderrLog.length()} bytes captured, " +
                "expected at least $FLOOD_BYTES"
        )
    }

    private suspend fun assertInitializes(connection: SingleChannelConnection<String>) {
        val client = object : DefaultLanguageClient() {
            override suspend fun windowLogMessage(params: LogMessageParams) = Unit
            override suspend fun windowShowMessage(params: ShowMessageParams) = Unit
            override suspend fun textDocumentPublishDiagnostics(params: PublishDiagnosticsParams) =
                Unit
        }
        val server = connection.connectAsLspClient(client)
        val init = withTimeout(BUDGET_MS) {
            server.initialize(
                InitializeParams(
                    capabilities = ClientCapabilities(),
                    processId = ProcessHandle.current().pid().toInt(),
                    rootUri = null
                )
            )
        }
        assertNotNull(
            init.capabilities,
            "server never answered initialize — it is wedged on an undrained stderr pipe"
        )
    }

    /**
     * Drive in a scope detached from the enclosing `runBlocking`, awaiting only a
     * [CompletableDeferred] — a wedged ksrpc pump must not keep the worker JVM alive
     * (issue #79), which matters here because the RED case of this test is a wedge.
     */
    private suspend fun driveBounded(block: suspend () -> Unit) {
        val driveScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val done = CompletableDeferred<Unit>()
        driveScope.launch {
            try {
                block()
                done.complete(Unit)
            } catch (t: Throwable) {
                done.completeExceptionally(t)
            }
        }
        try {
            done.await()
        } finally {
            driveScope.cancel()
        }
    }

    private fun echoServerScriptOrSkip(): File {
        // Gradle runs JVM tests from the module dir (lsp-ksrpc/); the install lives at the
        // repo root. Search both, as RawClientServerTest does.
        val script = listOf(
            File("samples/echo-server/build/install/echo-server/bin/echo-server"),
            File("../samples/echo-server/build/install/echo-server/bin/echo-server")
        ).firstOrNull { it.exists() }
        requireOrSkip(
            "echo-server not built; run :samples:echo-server:installDist",
            script != null
        )
        requireOrSkip("POSIX /bin/sh required for the stderr-flood wrapper", SH.canExecute())
        return script!!
    }

    /**
     * A `/bin/sh` wrapper that writes [FLOOD_BYTES] to stderr and only then `exec`s the
     * echo-server, so the flood is unavoidably in front of every LSP response. `exec`
     * hands the wrapper's stdin/stdout (and stderr) straight to the server, so the
     * connection under test is the server's own stdio.
     */
    private fun floodThenExec(script: File): List<String> = listOf(
        SH.path,
        "-c",
        """
        i=0
        while [ "${'$'}i" -lt $FLOOD_LINES ]; do
            printf '%s\n' "${"x".repeat(LINE_LENGTH - 1)}" >&2
            i=${'$'}((i + 1))
        done
        JAVA_HOME="${'$'}1" exec "${'$'}0"
        """.trimIndent(),
        script.absolutePath,
        // The distZip launcher needs a JDK; don't rely on the worker's inherited env.
        System.getProperty("java.home")
    )

    private companion object {
        val SH = File("/bin/sh")
        const val LINE_LENGTH = 256
        const val FLOOD_LINES = 640

        /**
         * 160 KiB — comfortably past the 64 KiB Linux pipe buffer (and macOS's 16–64 KiB),
         * with margin for a kernel configured larger.
         */
        const val FLOOD_BYTES = LINE_LENGTH * FLOOD_LINES

        const val BUDGET_MS = 45_000L
    }
}
