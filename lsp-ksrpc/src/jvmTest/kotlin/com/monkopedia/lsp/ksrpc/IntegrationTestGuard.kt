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
