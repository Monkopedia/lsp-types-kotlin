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

import org.junit.BeforeClass

/**
 * Shared base for the JVM integration tests that spawn real OS resources
 * (subprocesses, sockets, stdio pipes, lsp4j launchers). Extending this arms the
 * [JvmTestWatchdog] hang diagnostic (issue #79) for the worker JVM via a JUnit
 * `@BeforeClass`, with no per-test boilerplate. Installation is idempotent, so it
 * is harmless that every integration class triggers it.
 *
 * JUnit runs a superclass `@BeforeClass` before each subclass's tests, so the
 * watchdog is armed as soon as the first integration class in a worker starts.
 */
abstract class JvmIntegrationTestBase {
    companion object {
        @JvmStatic
        @BeforeClass
        fun armHangWatchdog() {
            JvmTestWatchdog.install()
        }
    }
}
