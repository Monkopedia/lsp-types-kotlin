# lsp-types-kotlin

KMP LSP 3.17 types and transport library. Two modules:
- `:lsp` — LSP 3.17 types + ksrpc `@KsService` interfaces (standalone, publishable)
- `:lsp-ksrpc` — ksrpc wiring layer (connection helpers, progress, init/shutdown state machine)

Design doc: `../LSP_DESIGN.md`

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
- Targets: JVM, macOS (arm64/x64), Linux x64, iOS (arm64/x64/simArm64)
