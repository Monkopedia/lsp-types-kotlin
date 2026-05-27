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

import kotlin.test.fail
import org.junit.Assume.assumeTrue

/**
 * Guards an integration-test precondition (a real server on PATH, the
 * echo-server install being built, ...).
 *
 * By default a missing precondition skips the test via [assumeTrue], so a
 * local `./gradlew` run without `clangd` installed (or without the echo-server
 * built) stays green.
 *
 * When `-Plsp.requireIntegrationTests=true` is passed — which CI does — a
 * missing precondition is a hard failure instead. A skipped integration test
 * must not be allowed to pass as green in CI, where the inputs are guaranteed
 * to be present; a skip there means the harness is broken.
 */
fun requireOrSkip(message: String, condition: Boolean) {
    if (System.getProperty("lsp.requireIntegrationTests") == "true") {
        if (!condition) {
            fail("Integration test precondition not met (-Plsp.requireIntegrationTests): $message")
        }
    } else {
        assumeTrue(message, condition)
    }
}

/**
 * Guards a real-server integration-test precondition (an external language
 * server such as `clangd`, `pyright`, `gopls`, ... being on PATH).
 *
 * This is a **separate** gate from [requireOrSkip] on purpose. Real external
 * servers aren't guaranteed to be installed everywhere (their CI install is
 * tracked separately), so a missing server must NOT trip the
 * `-Plsp.requireIntegrationTests` gate — otherwise per-PR CI, which only
 * guarantees the in-repo inputs (echo-server build, clangd), would fail on the
 * other servers. By default a missing server skips the test via [assumeTrue].
 *
 * When `-Plsp.requireRealServers=true` is passed — the dedicated real-server
 * job does this — a missing server is a hard failure instead, so a skip can't
 * silently pass as green where the server is guaranteed present.
 */
fun requireRealServerOrSkip(message: String, condition: Boolean) {
    if (System.getProperty("lsp.requireRealServers") == "true") {
        if (!condition) {
            fail("Real-server precondition not met (-Plsp.requireRealServers): $message")
        }
    } else {
        assumeTrue(message, condition)
    }
}
