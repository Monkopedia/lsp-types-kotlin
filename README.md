# lsp-types-kotlin

[![CI](https://github.com/Monkopedia/lsp-types-kotlin/actions/workflows/ci.yml/badge.svg)](https://github.com/Monkopedia/lsp-types-kotlin/actions/workflows/ci.yml)

Kotlin Multiplatform LSP 3.17 types and transport library.

Two artifacts, deliberately split:

- **`com.monkopedia.lsp:lsp`** — LSP 3.17 types (structures, enums, type aliases) generated from Microsoft's [`metaModel.json`](https://microsoft.github.io/language-server-protocol/specifications/lsp/3.17/metaModel/metaModel.json), plus transport-agnostic `LanguageServer` / `LanguageClient` interfaces (no annotations, just `suspend fun` signatures) with method-name `const val` constants on their companion objects. Targets every KMP platform that `kotlinx-serialization` runs on.
- **`com.monkopedia.lsp:lsp-ksrpc`** — `KsrpcLanguageServer` / `KsrpcLanguageClient` subinterfaces that extend the clean ones and add `@KsService` / `@KsMethod` / `@KsNotification` for use with [ksrpc](https://github.com/Monkopedia/ksrpc), plus `Default*` base classes and connection helpers. The `@KsService` **interfaces** target the full ksrpc-core set — JVM, JS, wasmJs, macOS (arm64/x64), iOS (arm64/x64/simulator), Linux (x64/arm64), mingwX64. The **JSON-RPC connection helpers** (`asLspConnection` / `connectAsLsp*`) cover the same set except mingwX64, which `ksrpc-jsonrpc` doesn't build. Targets without the helpers (and the web especially) connect by relaying the service over a ksrpc channel — see [On web](#on-web-wasmjs-relay-through-a-server).

## Why?

`lsp4j` is JVM-only. `xqt-kotlinx-lsp` is stuck on LSP 3.0 and dormant. There was no clean, current, KMP-native LSP types library — so this is one.

## Status

`1.0.1`. The types and codegen are complete and tested against real-world JSON samples, including lsp4j-driven wire-compatibility round-trips. Every union the spec models as a typeable shape — fields and method results alike — is generated as a strict Kotlin type. The slots left as `JsonElement` are the genuinely opaque `LSPAny` ones the protocol defines as free-form (`data`, `initializationOptions`, `experimental`, command `arguments`, etc.), plus the single method result (`workspace/symbol`) whose two array branches are structurally indistinguishable on the wire. The transport layer (`:lsp-ksrpc`) builds on ksrpc 1.1.1. Published to Maven Central; the public API is stable under semantic versioning as of 1.0.0.

## Modules

```
lsp-types-kotlin/
├── lsp/                 # @Serializable types — published as :lsp
├── lsp-ksrpc/           # @KsService interfaces + connection helpers — published as :lsp-ksrpc
└── lsp-codegen/         # JVM codegen tool — not published
```

## Using `:lsp`

```kotlin
implementation("com.monkopedia.lsp:lsp:1.0.1")
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
| `Ref \| Literal \| ...` (refs + anon objects) | sealed interface, mixed branches |
| `Literal \| Literal \| ...` (anon objects) | sealed interface + generated branch classes |
| `StructRef \| EnumRef` | sealed interface, both branches implement it |
| `A \| X \| X[]` (e.g. `Hover.contents`) | sealed interface with `A`, `X`, `List<X>` branches |
| Opaque `LSPAny` (`data`, `experimental`, ...) | `JsonElement` |

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
implementation("com.monkopedia.lsp:lsp-ksrpc:1.0.1")
```

The `:lsp-ksrpc` artifact provides `KsrpcLanguageServer` / `KsrpcLanguageClient`
— subinterfaces of the clean `LanguageServer` / `LanguageClient` from `:lsp` that
add `@KsService` / `@KsMethod` / `@KsNotification` for use with the JSON-RPC
transport. Implement them directly, or subclass `DefaultLanguageServer` /
`DefaultLanguageClient` and override only the methods you care about.

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

### Building on a different transport

If you don't want to use ksrpc, depend on `:lsp` only and implement the clean
`LanguageServer` / `LanguageClient` interfaces against your own transport. The
companion objects expose every method's wire path as a `const val`:

```kotlin
LanguageServer.TEXT_DOCUMENT_HOVER  // "textDocument/hover"
LanguageServer.INITIALIZE           // "initialize"
LanguageClient.WINDOW_SHOW_MESSAGE  // "window/showMessage"
```

### On web (wasmJs): relay through a server

A browser can't spawn a process or open the stdio/socket channel the JSON-RPC
helpers expect, so a wasmJs client doesn't talk JSON-RPC directly. Because
`KsrpcLanguageServer` is a `@KsService`, ksrpc can **relay** it over any ksrpc
channel — no hand-written proxy. Run the JSON-RPC edge on a server, then serve
the obtained service to the browser:

```kotlin
// Server (JVM/native): talk JSON-RPC to the real language server, then
// re-serve that same LanguageServer instance over a ksrpc channel (e.g. a
// WebSocket) to web clients. ksrpc forwards the calls.
val realServer = ProcessBuilder("ruff", "server").asLspConnection()
    .connectAsLspClient(MyClientImpl)
yourKsrpcChannel.serve(realServer)
```

```kotlin
// wasmJs client: consume the LanguageServer interface over the ksrpc channel.
// No JSON-RPC, no transport code — just the @KsService interface from :lsp-ksrpc.
val server: LanguageServer = yourKsrpcChannel.connect()
server.initialize(InitializeParams(...))
```

This is why `:lsp-ksrpc`'s interfaces target every ksrpc-core platform even
where the JSON-RPC helpers don't: the interface is all the web side needs.

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
