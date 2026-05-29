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

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Diagnostic safety net for the intermittent `:lsp-ksrpc:jvmTest` CI wedge
 * (issue #79).
 *
 * One of the JVM integration tests occasionally leaves a non-daemon thread alive
 * (or blocked on a non-interruptible read) on teardown, so the Gradle test-worker
 * JVM never exits after the suite passes — the task then hangs for an unbounded
 * time (45–118 min observed) until manually cancelled.
 *
 * This watchdog does NOT fix the leak. It makes the next wedge LOUD and
 * DIAGNOSABLE: a single daemon thread (so it can never itself be the leak) that,
 * after [WATCHDOG_TIMEOUT_MS], dumps every live thread's name + stack to stderr
 * and then forcibly halts the JVM. That converts a silent multi-hour stall into a
 * fast RED failure with a stack dump pointing at the culprit thread.
 *
 * The dump fires at 4 minutes, comfortably under the Gradle 5-minute task
 * `timeout` configured in `lsp-ksrpc/build.gradle.kts`, so the diagnostic output
 * is produced *before* Gradle kills the worker (Gradle's own timeout fails the
 * task but does not print what hung).
 *
 * Wiring: [JvmIntegrationTestBase] installs the watchdog from a JUnit
 * `@BeforeClass`, so every integration test class that extends it arms the net
 * without per-test boilerplate. Installation is idempotent — the first test class
 * to run in a worker JVM arms it once for the lifetime of that worker.
 */
object JvmTestWatchdog {
    // 4 minutes — under the 5-minute Gradle task timeout so the dump lands first.
    private const val WATCHDOG_TIMEOUT_MS = 4L * 60L * 1000L

    private val installed = AtomicBoolean(false)

    /**
     * Arms the watchdog for the current worker JVM. Idempotent: subsequent calls
     * are no-ops, so it is safe to call from every test class's `@BeforeClass`.
     */
    fun install() {
        if (!installed.compareAndSet(false, true)) return
        val watchdog = Thread({
            try {
                Thread.sleep(WATCHDOG_TIMEOUT_MS)
            } catch (_: InterruptedException) {
                return@Thread
            }
            dumpAllThreadsAndHalt()
        }, "jvmTest-hang-watchdog")
        // MUST be a daemon: a non-daemon watchdog would itself keep the worker
        // JVM alive and become the very leak we are trying to surface.
        watchdog.isDaemon = true
        watchdog.start()
    }

    private fun dumpAllThreadsAndHalt() {
        val err = System.err
        err.println(
            "\n==== jvmTest-hang-watchdog: suite did not finish within " +
                "${WATCHDOG_TIMEOUT_MS / 1000}s — dumping all thread stacks (issue #79) ===="
        )
        val stacks = Thread.getAllStackTraces()
        err.println("Live threads: ${stacks.size}")
        for ((thread, stack) in stacks) {
            val daemon = if (thread.isDaemon) "daemon " else ""
            err.println(
                "\n\"${thread.name}\" #${thread.threadId()} " +
                    "${daemon}prio=${thread.priority} state=${thread.state}"
            )
            for (frame in stack) {
                err.println("\tat $frame")
            }
        }
        err.println("\n==== end thread dump; forcing halt(1) (issue #79) ====")
        err.flush()
        // Force the wedged worker to die now rather than waiting on Gradle's task
        // timeout, so the failure is as fast and unambiguous as possible. halt()
        // (not exit()) skips shutdown hooks — appropriate when a thread is already
        // wedged and a clean shutdown may itself block.
        Runtime.getRuntime().halt(1)
    }
}
