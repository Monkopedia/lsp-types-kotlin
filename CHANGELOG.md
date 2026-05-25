# Changelog

All notable changes to lsp-types-kotlin are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/); versions track the
`1.0.0-RC*` release-candidate train toward a stable `1.0.0`.

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
