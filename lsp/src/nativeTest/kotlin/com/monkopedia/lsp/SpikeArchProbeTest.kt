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
 * Reports, from inside the running Kotlin/Native test binary, which CPU
 * architecture that binary was built for. This is the evidence that
 * distinguishes "the x64 test binary really executed" from "something arm64 ran
 * and the result XML looked the same".
 */
class SpikeArchProbeTest {
    @OptIn(ExperimentalNativeApi::class)
    @Test
    fun reportsCpuArchitecture() {
        println("SPIKE_ARCH cpu=${Platform.cpuArchitecture} os=${Platform.osFamily}")
        assertTrue(Platform.cpuArchitecture.name.isNotEmpty())
    }
}
