# lsp-types-kotlin

KMP LSP 3.17 types and transport library. Four Gradle projects:
- `:lsp` — LSP 3.17 types + ksrpc `@KsService` interfaces (standalone, publishable). **Almost entirely generated.**
- `:lsp-ksrpc` — ksrpc wiring layer (connection helpers, progress, init/shutdown state machine). **Partly generated — see below.**
- `:lsp-codegen` — the generator. Not published. Has no test source set (see #157).
- `:samples:echo-server` — a runnable sample server.

Design doc: `../LSP_DESIGN.md` (outside this repo).

## ⛔ Generated code — read this before editing anything

**49 files, ~12,763 lines, are generated from `lsp-codegen/src/main/resources/metaModel.json`.**
Every one carries this header:

```kotlin
// Auto-generated from LSP metaModel.json
```

**Editing a generated file silently loses your work.** The next `:lsp-codegen:generate` overwrites it — no error, no merge conflict, nothing to notice in a diff. Fix the *generator*, never the output.

**⚠️ It is NOT "`:lsp` is generated, `:lsp-ksrpc` is hand-written." That shorthand is false in the direction that loses work.** The generator writes into **both** modules (`lsp-codegen/build.gradle.kts` passes two output roots):

```
lsp/src/commonMain/kotlin/...                        44 generated files
lsp-ksrpc/src/commonMain/kotlin/com/monkopedia/lsp/    5 generated files:
    DefaultLanguageClient.kt   DefaultLanguageServer.kt
    KsrpcLanguageClient.kt     KsrpcLanguageServer.kt
    LifecycleTrackingLanguageServer.kt
```

Those five sit next to hand-written wiring code and read like ordinary wiring. **Check for the header before editing any file under `:lsp-ksrpc`'s `commonMain`.**

To regenerate:

```bash
./gradlew :lsp-codegen:generate
./gradlew :lsp:ktlintFormat :lsp-ksrpc:ktlintFormat   # both, not just :lsp
```

CI enforces that committed output matches the generator (the "Codegen reproducibility" step).

## Build & Test

`JAVA_HOME=/usr/lib/jvm/java-21-openjdk` for every Gradle command.

```bash
# Build
./gradlew build

# Tests
./gradlew allTests

# Lint
./gradlew ktlintCheck

# Auto-fix formatting
./gradlew ktlintFormat
```

## Workflow

Non-trivial change: design → approve → test → implement → verify. See `.claude/rules/workflow.md`.

When launching a sub-agent, point it at `.claude/agent-preamble.md` for the standard rules.

## Code Style

- ktlint with `android_studio` style (see `.editorconfig`)
- No wildcard imports
- Structured concurrency — see `.claude/rules/coroutines.md`

## Dependencies

- **ksrpc** (`../ksrpc`) — JSON-RPC transport with LSP conventions
- **kotlinx-serialization** — JSON serialization for LSP types
- **24 declared targets** — do not assume a short list. Measured across all `*.gradle.kts`:
  `jvm js wasmJs wasmWasi linuxX64 linuxArm64 mingwX64 macosX64 macosArm64 iosX64 iosArm64
   iosSimulatorArm64 tvosX64 tvosArm64 tvosSimulatorArm64 watchosX64 watchosArm32 watchosArm64
   watchosDeviceArm64 watchosSimulatorArm64 androidNativeArm32 androidNativeArm64
   androidNativeX86 androidNativeX64`
- Published: 35 platform coordinates + 2 root modules = 37 Central artifacts, group `com.monkopedia.lsp`.
  Note **not every target executes a test** — see #144 for the x64 Apple coordinates.
