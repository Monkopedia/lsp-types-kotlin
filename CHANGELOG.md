# Changelog

All notable changes to lsp-types-kotlin are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/). The public API is
stable under semantic versioning as of 1.0.0; the `1.0.0-RC*` entries below
trace the path to it.

## [1.0.0] - 2026-05-30

First stable release — the public API is now under semantic versioning.
Identical in content to `1.0.0-RC5`; see that entry (and RC1–RC4) for the full
set of changes. Built on ksrpc 1.1.0.

## [1.0.0-RC5] - 2026-05-29

Validation, robustness, and tooling pass: comprehensive real-LSP integration
coverage, a unified cross-platform process-spawn API, teardown-hardening, and
full ABI tracking — toward a stable 1.0.

### ⚠️ Behavior change (pre-1.0, migration note)
- **Wiring a server no longer implicitly keeps the caller alive.** `asLspConnection`'s
  JSON-RPC read pump now runs in a **connection-owned** coroutine scope instead of as a
  child of the caller's `runBlocking` (#87). This fixes a class of teardown wedges (see
  Fixed), but it means a server `main` written as
  `runBlocking { stdInLspConnection().connectAsLspServer(impl) }` will now **return
  immediately** instead of blocking. Servers must explicitly stay alive while serving —
  e.g. `awaitCancellation()` (or await your `LifecycleState` reaching `EXITED`). The
  `samples/echo-server` shows the pattern.

### Added
- **Native external-server process spawn.** `spawnLspServer(command, env): LspServerProcess`
  (`pid` / `connection` / bounded `kill()` / `close()`) — a single cross-platform API via
  `expect`/`actual`, with `actual`s on JVM (`ProcessBuilder`) and Kotlin/Native posix
  (linux + apple; `fork`/`exec`). JVM gains a real process handle it previously lacked (#47).
- **Comprehensive real-LSP integration suite** (#41): real Eclipse lsp4j interop covering
  every method in both directions; a real-server client-role matrix (clangd / pyright /
  typescript-language-server / gopls / rust-analyzer, run nightly); a transport matrix
  (in-memory / TCP / stdio / ksrpc-relay); and native + wasmJs round-trips.
- **Coverage gates with fail-on-regression baselines** (#66/#73/#74): 100% LSP-method
  wire coverage (server 73/73, client 20/20), 100% union-branch coverage, plus Kover
  serializer line/branch coverage — all guarded so coverage can't silently regress.
- **klib (native/JS/wasm) ABI validation** in BCV (#89), so the multiplatform public
  surface (including `spawnLspServer` and the wasmJs API) is tracked alongside the JVM `.api`.

### Changed
- **ksrpc 1.0.0 → 1.1.0** (#53). Brings the #201 fix that makes the Kotlin/Native posix
  reader terminate cleanly on EOF.
- Payload-corpus tests are now sourced exclusively from upstream (LSP spec /
  vscode-languageserver-node / lsprotocol) and captured real-server traffic; the prior
  synthetic, self-referential corpus was removed (#61/#62/#63).

### Fixed
- **Native posix-stdio teardown no longer hangs** (#53, via ksrpc #201): the reader broke
  on `read()==0` (EOF) instead of busy-looping, so process/connection teardown terminates.
- **`:lsp-ksrpc` JVM integration tests no longer wedge on teardown** (#79): real-server
  pumps driven under `runBlocking` could park on a force-killed process's dead stream;
  pumps now run detached on daemon scopes.
- **`asLspConnection` can no longer wedge a caller's `runBlocking`** on process-kill
  teardown (#87) — see the behavior change above.

## [1.0.0-RC4] - 2026-05-25

Hardening + ergonomics pass from an independent API review.

### Fixed
- **Unreachable union branches** from colliding serializer discriminators:
  `WorkspaceEditDocumentChanges.DeleteFile` and
  `ServerCapabilitiesNotebookDocumentSync`'s registration branch never decoded.
  The codegen now resolves discriminator collisions (kind-value / subtype field)
  and asserts branch discriminators are distinct (#26).
- **`DefaultLanguageServer`/`DefaultLanguageClient` notifications** were generated
  to throw `NotImplementedError`; an un-overridden notification would crash the
  receive loop. They now default to a no-op (requests still throw) (#27).
- **`GlobalScope` in `LspProcessConnection`** — the stdout pump is now scoped to
  the caller's job, fixing a leak and the structured-concurrency violation (#28).

### Added
- Companion factories on the union wrappers: `BooleanOr(true)`,
  `SingleOrArray.single(x)`/`multiple(xs)`, `StringOr("s")`, `IntOrString(1)` (#30).
- `LifecycleState` is coroutine-aware: `phases: StateFlow<Phase>`, `awaitInitialized()`,
  and a non-throwing `advanceTo`; plus a `connectAsLspServer(server, lifecycle)`
  overload that advances the lifecycle automatically (#32).

### Changed
- **Required-but-nullable** generated fields (e.g. `InitializeParams.processId`,
  `rootUri`) now default to `null`, so callers can omit them (#31).
- **Method-result unions are now strict types** (were `JsonElement`):
  `textDocument/definition`·`/declaration`·`/typeDefinition`·`/implementation`
  (`Definition | DefinitionLink[]`), `/completion`
  (`CompletionItem[] | CompletionList`), `/documentSymbol`
  (`SymbolInformation[] | DocumentSymbol[]`). `workspace/symbol` stays `JsonElement`
  (its two array branches are structurally indistinguishable on the wire) (#29).

### ⚠️ Breaking (pre-1.0)
- `Hover.contents` (RC3) and the method-result return types above change from
  `JsonElement` to strict sealed types. Source-breaking for consumers that read
  those as `JsonElement`; the wire format is unchanged.

## [1.0.0-RC3] - 2026-05-25

### Changed
- **Every feasible union field is now a strict Kotlin type.** The codegen previously
  fell back to `JsonElement` for several union shapes; those are now generated as
  sealed interfaces with typed branches and content-discriminating serializers:
  - mixed struct-ref + inline-literal unions (#16, #17),
  - `StructRef | EnumRef` and the remaining classifier gaps (#19, #20),
  - `A | X | X[]` unions such as `Hover.contents`
    (`MarkupContent | MarkedString | MarkedString[]`) (#22, #24).
  `Hover.contents` is now `HoverContents` (was `JsonElement`), with ergonomic
  `HoverContents.markdown(...)` / `.plaintext(...)` / `.markup(...)` / `.string(...)`
  builders. The only fields that remain `JsonElement` are the genuinely opaque
  `LSPAny` slots the protocol defines as free-form (`data`, `initializationOptions`,
  `experimental`, command `arguments`, telemetry/settings payloads).
- **CI:** the macOS leg is scoped to Apple targets only, dropping a redundant full
  build now covered by the Linux leg (#15, #21).
- **Build:** `:lsp-codegen` is excluded from binary-compatibility-validation — it is
  an internal generator, not a published artifact (#18, #23).

## [1.0.0-RC2] - 2026-05-21

### Added
- `:lsp-ksrpc` now covers the full ksrpc-core target set (#11, #13): adds js,
  wasmJs, macosX64, linuxArm64, mingwX64. The `@KsService` interfaces are available
  on all of these; the JSON-RPC connection helpers cover the same set except
  mingwX64 (which ksrpc-jsonrpc doesn't build). Web (wasmJs) consumers connect by
  relaying the `LanguageServer` over a ksrpc channel — no LSP JSON-RPC needed
  client-side.

### Fixed
- Publish pipeline: vanniktech maven-publish 0.36.0 + a raised Central deployment
  timeout.

## [1.0.0-RC1] - 2026-05-21

### Added
- First release candidate. KMP LSP 3.17 types + transport library:
  - `com.monkopedia.lsp:lsp` — LSP 3.17 types (structures, enums, type aliases)
    generated from Microsoft's `metaModel.json`, plus transport-agnostic
    `LanguageServer` / `LanguageClient` interfaces. Targets every
    kotlinx-serialization-supported platform.
  - `com.monkopedia.lsp:lsp-ksrpc` — ksrpc wiring (`@KsService` interfaces,
    connection helpers, lifecycle state machine, progress) built on ksrpc 1.0.0.
- Wire compatibility verified against clangd, Eclipse lsp4j, and raw JSON-RPC bytes.

[1.0.0]: https://github.com/Monkopedia/lsp-types-kotlin/releases/tag/v1.0.0
[1.0.0-RC5]: https://github.com/Monkopedia/lsp-types-kotlin/releases/tag/v1.0.0-RC5
[1.0.0-RC4]: https://github.com/Monkopedia/lsp-types-kotlin/releases/tag/v1.0.0-RC4
[1.0.0-RC3]: https://github.com/Monkopedia/lsp-types-kotlin/releases/tag/v1.0.0-RC3
[1.0.0-RC2]: https://github.com/Monkopedia/lsp-types-kotlin/releases/tag/v1.0.0-RC2
[1.0.0-RC1]: https://github.com/Monkopedia/lsp-types-kotlin/releases/tag/v1.0.0-RC1
