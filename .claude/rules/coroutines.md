---
description: Structured concurrency rules for coroutine usage
globs: "**/*.kt"
---

# Coroutine Rules

## Detaching work from the caller

Default: launch into a scope you were **given**. Constructing your own scope detaches
the work from the caller's structured-concurrency tree — sometimes necessary, always a
decision.

**Detaching is justified only when parenting to the caller would let non-cancellable
work block the caller's teardown.** That is a real failure mode here, not a hypothetical:
a read parked on a dead fd was once a child of the caller's job, and structured
concurrency refused to let `runBlocking` return until it finished (#87).

**When you construct a `CoroutineScope(...)`, you must be able to state both:**

1. **What ends its work** — EOF, stream close, an explicit `cancel()`.
2. **Why it cannot keep the caller or the process alive** — a daemon dispatcher
   (`Dispatchers.IO`/`Default` on JVM), or something that definitely cancels it.

**A violation is a constructed scope where you cannot name (1) or (2).** If the honest
answer to *"what stops this?"* is *"nothing"* or *"it gets garbage-collected"*, that is
the violation. Both conditions are answerable by reading the construction site; if
answering needs repo-wide context, treat that as a smell in the code, not in the rule.

**Never `GlobalScope`.** It gives you detachment with no owner and **no way to cancel
if you later need one** — a constructed scope gives the same detachment and strictly
more control, so `GlobalScope` is never the better tool. This applies to tests.

## Other

- No `runBlocking` in production code. Tests may use it.
- Use `withContext(Dispatchers.IO)` for blocking I/O.

## Note on scope

The two rules above differ deliberately in whether tests are carved out. `runBlocking`
in a test is harmless — the test *is* the top of the tree. A leaked or uncancellable
scope in a test is not harmless: it outlives the test that made it and lands on whichever
test runs next.
