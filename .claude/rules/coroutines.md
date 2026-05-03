---
description: Structured concurrency rules for coroutine usage
globs: "**/*.kt"
---

# Coroutine Rules

- No `GlobalScope` — always use structured concurrency
- No unscoped `CoroutineScope()` construction
- No `runBlocking` in production code (tests are fine)
- Use `withContext(Dispatchers.IO)` for blocking I/O
