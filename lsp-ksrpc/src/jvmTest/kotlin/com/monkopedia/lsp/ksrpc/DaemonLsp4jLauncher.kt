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

import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger
import org.eclipse.lsp4j.jsonrpc.Launcher
import org.eclipse.lsp4j.launch.LSPLauncher
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.LanguageServer

/**
 * Hang-proof lsp4j launcher wrapper for the JVM integration tests (issue #79).
 *
 * ## Why this exists
 *
 * `LSPLauncher.createClientLauncher(client, in, out)` (and the server variant)
 * build the launcher through `Launcher.Builder.create()`, which — when no
 * executor is supplied — defaults to `Executors.newCachedThreadPool()`. That
 * pool's threads are **non-daemon**. `startListening()` submits the reader loop
 * (`ConcurrentMessageProcessor`, which blocks in `StreamMessageProducer.listen()`
 * on `InputStream.read()`) onto that pool.
 *
 * On teardown the tests call `listening.cancel(true)`, but a blocking read on a
 * `PipedInputStream` or a subprocess stream is **not interruptible** — the read
 * does not unwind, so the non-daemon pool thread survives. A single live
 * non-daemon thread keeps the Gradle test-worker JVM from exiting after the suite
 * passes, which is exactly the intermittent `:lsp-ksrpc:jvmTest` wedge tracked by
 * issue #79.
 *
 * ## The fix
 *
 * Supply an explicit [Executors.newCachedThreadPool] backed by a **daemon**
 * [ThreadFactory] via the 5-arg `LSPLauncher` overload. Daemon threads never
 * block JVM shutdown, so even if a reader thread is wedged on a non-interruptible
 * read the worker can still exit cleanly. [shutdown] then makes a best-effort
 * `shutdownNow()` so the threads are torn down promptly in the common
 * (non-wedged) case rather than lingering until process exit.
 */
class DaemonLsp4jLauncher<T> private constructor(
    private val launcher: Launcher<T>,
    private val executor: ExecutorService
) {
    /** The remote proxy stub for the peer (server or client). */
    val remoteProxy: T get() = launcher.remoteProxy

    /** Begin the reader loop on the daemon executor. Returns the listening future. */
    fun startListening(): Future<Void> = launcher.startListening()

    /**
     * Best-effort teardown: stop the daemon executor. Safe to call from a
     * `finally`; never throws. Because the executor's threads are daemons, a
     * reader thread still wedged on a non-interruptible read after this call can
     * no longer keep the worker JVM alive.
     */
    fun shutdown() {
        runCatching { executor.shutdownNow() }
    }

    companion object {
        private val THREAD_INDEX = AtomicInteger(0)

        private fun daemonExecutor(): ExecutorService = Executors.newCachedThreadPool(
            ThreadFactory { runnable ->
                Thread(runnable, "lsp4j-test-reader-${THREAD_INDEX.getAndIncrement()}")
                    .apply { isDaemon = true }
            }
        )

        /** Daemon-backed equivalent of [LSPLauncher.createClientLauncher]. */
        fun createClientLauncher(
            client: LanguageClient,
            input: InputStream,
            output: OutputStream
        ): DaemonLsp4jLauncher<LanguageServer> {
            val executor = daemonExecutor()
            val launcher = LSPLauncher.createClientLauncher(
                client,
                input,
                output,
                executor
            ) { it }
            return DaemonLsp4jLauncher(launcher, executor)
        }

        /** Daemon-backed equivalent of [LSPLauncher.createServerLauncher]. */
        fun createServerLauncher(
            server: LanguageServer,
            input: InputStream,
            output: OutputStream
        ): DaemonLsp4jLauncher<LanguageClient> {
            val executor = daemonExecutor()
            val launcher = LSPLauncher.createServerLauncher(
                server,
                input,
                output,
                executor
            ) { it }
            return DaemonLsp4jLauncher(launcher, executor)
        }
    }
}
