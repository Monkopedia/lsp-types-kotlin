# lsp-types-kotlin

[![CI](https://github.com/Monkopedia/lsp-types-kotlin/actions/workflows/ci.yml/badge.svg)](https://github.com/Monkopedia/lsp-types-kotlin/actions/workflows/ci.yml)

Kotlin Multiplatform LSP 3.17 types and transport library.

Two artifacts, deliberately split:

- **`com.monkopedia.lsp:lsp`** — LSP 3.17 types (structures, enums, type aliases) generated from Microsoft's [`metaModel.json`](https://microsoft.github.io/language-server-protocol/specifications/lsp/3.17/metaModel/metaModel.json). Pure `@Serializable` data classes — no transport, no service interfaces. Targets every KMP platform that `kotlinx-serialization` runs on.
- **`com.monkopedia.lsp:lsp-ksrpc`** — `LanguageServer` and `LanguageClient` `@KsService` interfaces plus connection helpers for [ksrpc](https://github.com/Monkopedia/ksrpc). Constrained to `ksrpc-jsonrpc`'s target set: JVM, JS, wasmJs, macOS, iOS, Linux, mingwX64.

## Why?

`lsp4j` is JVM-only. `xqt-kotlinx-lsp` is stuck on LSP 3.0 and dormant. There was no clean, current, KMP-native LSP types library — so this is one.

## Status

Pre-1.0. The types and codegen are complete and tested against real-world JSON samples. The transport layer (`:lsp-ksrpc`) tracks ksrpc's 1.0 release.

## Modules

```
lsp-types-kotlin/
├── lsp/                 # @Serializable types — published as :lsp
├── lsp-ksrpc/           # @KsService interfaces + connection helpers — published as :lsp-ksrpc
└── lsp-codegen/         # JVM codegen tool — not published
```

## Using `:lsp`

```kotlin
implementation("com.monkopedia.lsp:lsp:0.1.0-SNAPSHOT")
```

Pure types and serialization. No transport. Use this if you have your own JSON-RPC plumbing and just need typed LSP messages.

```kotlin
import com.monkopedia.lsp.*
import kotlinx.serialization.json.Json

val json = Json { ignoreUnknownKeys = true }
val params = json.decodeFromString<DidOpenTextDocumentParams>(payload)
println(params.textDocument.uri)
```

### Union types

LSP makes heavy use of TypeScript union types. The codegen maps each pattern to a typed Kotlin shape:

| Spec pattern | Kotlin |
| --- | --- |
| `T \| null` | `T?` |
| `boolean \| Options` | `BooleanOr<Options>` |
| `T \| T[]` | `SingleOrArray<T>` |
| `string \| StructuredType` | `StringOr<T>` |
| `integer \| string` | `IntOrString` |
| `Ref \| Ref \| ...` (named refs) | sealed interface, branches implement it |
| `Literal \| Literal \| ...` (anon objects) | sealed interface + generated branch classes |
| Mixed primitives / `LSPAny` | `JsonElement` |

So `ServerCapabilities.hoverProvider` is `BooleanOr<HoverOptions>?`; you pattern-match:

```kotlin
when (val provider = capabilities.hoverProvider) {
    is BooleanOr.BooleanValue -> if (provider.value) supportHover()
    is BooleanOr.Value -> supportHoverWith(provider.value)
    null -> { /* not supported */ }
}
```

## Using `:lsp-ksrpc`

```kotlin
implementation("com.monkopedia.lsp:lsp-ksrpc:0.1.0-SNAPSHOT")
```

### As a client (talking to `ruff server` or similar)

```kotlin
import com.monkopedia.lsp.ksrpc.*

suspend fun main() {
    val connection = ProcessBuilder("ruff", "server").asLspConnection()
    val server = connection.connectAsLspClient(MyClientImpl)

    val initResult = server.initialize(InitializeParams(...))
    server.initialized(InitializedParams())
    // ... use server.textDocumentHover(...), etc.
}
```

### As a server

```kotlin
suspend fun main() {
    val connection = stdInLspConnection()
    connection.connectAsLspServer(MyServerImpl)
}
```

### Lifecycle and progress

- `LifecycleState` tracks the LSP phase machine (Initializing → Initialized → ShuttingDown → Exited) and gates dispatch.
- `ProgressTokenRegistry` allocates `$/progress` tokens and routes incoming progress notifications to observers via `Flow`.

## Sample

`samples/echo-server` is a runnable LSP server that responds to `textDocument/hover`
with the URI and cursor position. Build a distribution and point an LSP-capable
editor at it:

```bash
./gradlew :samples:echo-server:installDist
samples/echo-server/build/install/echo-server/bin/echo-server
```

The source is small enough to read end-to-end — see
[`EchoServer.kt`](samples/echo-server/src/main/kotlin/com/monkopedia/lsp/sample/EchoServer.kt).

## Building

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk
./gradlew build           # compile + lint + test all modules
./gradlew :lsp:jvmTest    # just the type/serialization tests
./gradlew ktlintFormat    # auto-format
```

## Regenerating types

The generated source is committed. Re-run when the spec moves:

```bash
# Pull a fresh metaModel.json for a given LSP version
./gradlew :lsp-codegen:downloadMetaModel -PlspVersion=3.17

# Generate Kotlin source into :lsp and :lsp-ksrpc
./gradlew :lsp-codegen:generate
./gradlew :lsp:apiDump :lsp-ksrpc:apiDump  # if the public API changed
./gradlew ktlintFormat                      # tidy generated code
```

## License

Apache 2.0 — see the file headers.
