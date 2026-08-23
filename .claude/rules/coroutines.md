---
description: Structured concurrency rules for coroutine usage
globs: "**/*.kt"
---

# Coroutine Rules

## Detaching work from the caller

Default: launch into a scope you were **given**. Constructing your own scope detaches
the work from the caller's structured-concurrency tree — sometimes necessary, always a
decision.

**Why this exception exists:** parenting to the caller once let non-cancellable work
block its teardown — a read parked on a dead fd was a child of the caller's job, and
structured concurrency refused to let `runBlocking` return until it finished (#87).
That is context, not a third test. The two questions below are the whole test.

**When you construct a `CoroutineScope(...)`, you must be able to state both:**

1. **What ends its work** — EOF, stream close, an explicit `cancel()`. A loop whose exit
   depends on a blocking read returning counts only if closing the stream definitely
   unblocks that read **on every target we build for. If you are unsure, it does not
   count — use an explicit `cancel()` instead.**
2. **Why it is not a child of the caller's job** — which is the reason you are
   constructing one at all. `CoroutineScope(SupervisorJob() + ...)` gives it its own
   job, so the caller's teardown never waits on it.

**Both must be answerable. A violation is a constructed scope that fails either
question.** If the honest answer to *"what stops this?"* is *"nothing"* or *"it gets
garbage-collected"*, that is the violation.

**Write both answers in a comment at the construction site.** An unwritten answer is a
violation: a reviewer cannot check your reasoning, only your text. Both existing
detached scopes in this repo already do this — follow them.

**Dispatcher choice is a separate obligation.** `Dispatchers.IO` and `Dispatchers.Default`
run on daemon threads, so work left parked on them can never hold the process open. A
dispatcher with non-daemon threads — `newSingleThreadContext`, a custom executor — **can**,
so a scope using one **must** be explicitly cancelled. Inheriting the caller's dispatcher
is fine: it cannot outlive resources the caller already owns.

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
