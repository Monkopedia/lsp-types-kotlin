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
package com.monkopedia.lsp

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * SPIKE ONLY (#144) — do not merge.
 *
 * The :lsp twin of this probe. It has to exist separately in this module: the
 * assertion is per test-results directory, and :lsp-ksrpc's macosX64Test /
 * iosX64Test write their own. Without a marker of their own, those two targets
 * could only be asserted on a count — which cannot tell an x64 execution from
 * an arm64 one, the exact ambiguity this spike exists to remove.
 */
class SpikeArchProbeKsrpcTest {
    @OptIn(ExperimentalNativeApi::class)
    @Test
    fun reportsCpuArchitecture() {
        println("SPIKE_ARCH cpu=${Platform.cpuArchitecture} os=${Platform.osFamily}")
        assertTrue(Platform.cpuArchitecture.name.isNotEmpty())
    }
}
