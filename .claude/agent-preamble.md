# Agent preamble

Read this once when launched. Apply to every agent task in this repo.

## Tooling

- Use `Read`, `Grep`, `Glob`, `Edit`, `Write` over their `bash` equivalents (`cat`, `grep`, `find`, `sed`).
- Spawn sub-agents with model `opus` for any code work.
- Run agents in the background unless the user asks otherwise.

## What to NOT touch

- **⛔ GENERATED SOURCE — 49 files carrying `// Auto-generated from LSP metaModel.json`.** Editing one **silently loses your work**: the next `:lsp-codegen:generate` overwrites it with no error, no conflict, and nothing to notice in a diff. Fix `:lsp-codegen`, never the output.
  **It is NOT "`:lsp` is generated, `:lsp-ksrpc` is hand-written."** The generator writes into **both**. In `:lsp-ksrpc` the split is a **package boundary**: generated output lives only in package `com.monkopedia.lsp` (`lsp-ksrpc/src/commonMain/kotlin/com/monkopedia/lsp/`, 5 files as a cross-check); hand-written wiring lives elsewhere (`com.monkopedia.lsp.ksrpc`). It holds because the generator hard-codes that services dir (`lsp-codegen/.../codegen/Main.kt:195`), so don't add hand-written files to that package.
  The generated files read exactly like wiring, so **the header is the authority: grep for it before editing any file under `:lsp-ksrpc`'s `commonMain`.**
- Anything under `.claude/` (rules, agent-preamble, project memory): triggers approval prompts and blocks autonomous work. If you genuinely need a rule change, surface it to the user first.
- Build outputs (`build/`, `.gradle/`, etc).

## Workflow

For any non-trivial change: design → approve → test → implement → verify. See `.claude/rules/workflow.md`.

## Build & verify

`JAVA_HOME=/usr/lib/jvm/java-21-openjdk` for every Gradle command.

Always before committing:
- `./gradlew ktlintFormat` (formats Kotlin)
- `./gradlew allTests ktlintCheck` for the modules you touched

Match commit-message style — `git log --oneline -10` for recent examples.
