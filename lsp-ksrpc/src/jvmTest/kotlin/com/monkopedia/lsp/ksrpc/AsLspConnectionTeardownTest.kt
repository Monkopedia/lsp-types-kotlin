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

import com.monkopedia.lsp.DefaultLanguageClient
import com.monkopedia.lsp.LogMessageParams
import com.monkopedia.lsp.PublishDiagnosticsParams
import com.monkopedia.lsp.ShowMessageParams
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlinx.coroutines.runBlocking

/**
 * Regression for issue #87: the lower-level [Pair.asLspConnection] (the
 * `InputStream`/`OutputStream` overload) must NOT wire its JSON-RPC read/write pumps
 * onto the caller's coroutine context. If it does, a consumer that drives the whole
 * thing from a `runBlocking { ... }` and tears down by force-killing the child process
 * (rather than a clean LSP `shutdown`/`exit`) can have the read pump park on a
 * non-interruptible blocking read of the dead stdout pipe that never reaches EOF — and
 * structured concurrency then refuses to let `runBlocking` return until that
 * never-returning child completes, wedging the caller forever.
 *
 * After the fix the pumps live in a connection-owned `Dispatchers.IO` + `SupervisorJob`
 * scope (daemon threads, detached from the caller's job), so killing the process and
 * dropping the connection lets `runBlocking` return promptly — the pump can no longer
 * hold it open.
 *
 * ## Boundedness (must never wedge CI)
 *
 * The scenario under test is *exactly* "a hang"; if the fix regresses, the body would
 * block forever. So the whole `runBlocking` is fenced behind a thread-based wall-clock
 * watchdog: the work runs on a daemon worker thread, and the test thread `join`s it for
 * a few seconds. A wedge therefore fails FAST and RED (a bounded assertion failure)
 * rather than stalling until the 4-minute [JvmTestWatchdog] halts the worker. Either
 * way it can never hang the build.
 *
 * Uses `cat` (always present on the CI/dev Linux + macOS hosts) as a throwaway child:
 * with no args it copies stdin to stdout, so it keeps its stdout pipe open and never
 * emits an LSP frame nor reaches EOF while alive — the precise condition that parks the
 * read pump. Gated via [requireOrSkip] so a host genuinely missing `cat` skips locally
 * but hard-fails under `-Plsp.requireIntegrationTests=true`.
 */
class AsLspConnectionTeardownTest : JvmIntegrationTestBase() {

    @Test
    fun `process-kill teardown does not wedge the caller runBlocking`() {
        requireOrSkip("cat not on PATH", isOnPath("cat"))

        runBounded(BUDGET_MS) {
            val process = ProcessBuilder("cat")
                .redirectInput(ProcessBuilder.Redirect.PIPE)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start()

            // This is the consumer pattern from the issue: build + drive entirely inside
            // a single runBlocking, then tear down by killing the process — NO clean LSP
            // shutdown, NO detached drive scope. Pre-fix, the read pump (parented to this
            // runBlocking) parks on cat's never-EOF stdout and runBlocking never returns.
            runBlocking {
                val client = object : DefaultLanguageClient() {
                    override suspend fun windowLogMessage(params: LogMessageParams) = Unit
                    override suspend fun windowShowMessage(params: ShowMessageParams) = Unit
                    override suspend fun textDocumentPublishDiagnostics(
                        params: PublishDiagnosticsParams
                    ) = Unit
                }

                val connection =
                    (process.inputStream to process.outputStream).asLspConnection()
                // Wiring the client only registers the channels (does not await a server
                // response), so this returns promptly even though `cat` speaks no LSP.
                connection.connectAsLspClient(client)

                // Tear down the "server" the hostile way: force-kill, no shutdown/exit.
                process.destroyForcibly()
                assertTrue(
                    process.waitFor(GRACE_MS, TimeUnit.MILLISECONDS),
                    "cat did not die after destroyForcibly()"
                )
                // Leaving this runBlocking is the assertion: pre-fix, the pump parked on
                // the (now dead, but possibly not EOF'd) stdout pipe keeps the block from
                // ever completing. Post-fix the pump is on a detached daemon scope, so we
                // fall through and return.
            }
        }
    }

    private companion object {
        // Generous vs. the expected sub-second teardown, but far under the 4-minute
        // JvmTestWatchdog and the 5-minute Gradle task timeout, so a wedge is caught
        // fast.
        const val BUDGET_MS = 15_000L
        const val GRACE_MS = 5_000L
    }
}

/**
 * Run [block] on a daemon worker thread and wait at most [budgetMs] for it. A wedge in
 * [block] fails the test with a bounded assertion failure rather than hanging the build;
 * the worker is a daemon so it can never keep the JVM alive even if it never returns.
 */
private fun runBounded(budgetMs: Long, block: () -> Unit) {
    var failure: Throwable? = null
    val worker = Thread({
        try {
            block()
        } catch (t: Throwable) {
            failure = t
        }
    }, "asLspConnection-teardown-worker")
    worker.isDaemon = true
    worker.start()
    worker.join(budgetMs)
    if (worker.isAlive) {
        fail(
            "asLspConnection teardown wedged: runBlocking did not return within " +
                "${budgetMs}ms after force-killing the child (issue #87)"
        )
    }
    failure?.let { throw it }
}

private fun isOnPath(binary: String): Boolean {
    val path = System.getenv("PATH") ?: return false
    return path.split(java.io.File.pathSeparator).asSequence()
        .map { java.io.File(it, binary) }
        .any { it.canExecute() }
}
