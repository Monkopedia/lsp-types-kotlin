# Changelog

All notable changes to lsp-types-kotlin are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/); versions track the
`1.0.0-RC*` release-candidate train toward a stable `1.0.0`.

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

[1.0.0-RC3]: https://github.com/Monkopedia/lsp-types-kotlin/releases/tag/v1.0.0-RC3
[1.0.0-RC2]: https://github.com/Monkopedia/lsp-types-kotlin/releases/tag/v1.0.0-RC2
[1.0.0-RC1]: https://github.com/Monkopedia/lsp-types-kotlin/releases/tag/v1.0.0-RC1
