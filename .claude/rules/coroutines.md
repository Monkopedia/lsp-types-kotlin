---
description: Structured concurrency rules for coroutine usage
globs: "**/*.kt"
---

# Coroutine Rules

## Detaching work from the caller

Default: launch into a scope you were **given**.

**When you construct your own `CoroutineScope(...)`: give it its own `Job`, and be able to
say its work either**

- **ends on a signal that definitely fires** — EOF, a stream close that genuinely unblocks
  the read, an explicit `cancel()` that reaches a suspension point; **or**
- **stays parked forever, occupying no non-daemon thread and awaited by nothing.**

**A violation is a constructed scope where neither is true.** If the honest answer to
*"what stops this?"* is *"nothing"* or *"it gets garbage-collected"*, that is the violation.

**The fact that makes this decidable: a parked coroutine can only hold the process open by
occupying a non-daemon thread.** Suspension occupies nothing. A blocking syscall occupies a
thread — and **`cancel()` does not interrupt one**, so *"we cancel it"* is not an answer for
a blocking read. `Dispatchers.IO` and `Dispatchers.Default` are daemon-backed and do not
count; `newSingleThreadContext` and custom executors do, whoever chose them.

A loop that keeps *doing* something — `while (true) { delay(1.seconds); flush() }` — is
neither: it does not end, and it is not parked. It needs a terminator.

**Why the exception exists at all:** parenting to the caller once let non-cancellable work
block its teardown — a read parked on a dead fd was a child of the caller's job, and
structured concurrency refused to let `runBlocking` return until it finished (#87). Its own
`Job` is what prevents that.

**Write the answer in a comment at the construction site.** An unwritten answer is a
violation: a reviewer cannot check your reasoning, only your text.
`LspProcessConnection.kt` is the exemplar — it names the terminator, and why a pump parked
on a dead fd is harmless. **`LspConnection.kt` is not**: it says such a pump cannot hold the
*caller's* `runBlocking` open, where what matters is the *process* (see #171).

**Never `GlobalScope`.** It detaches with no owner and **can never be cancelled if you later
need to** — a constructed scope gives the same detachment and strictly more control. This
applies to tests.

## Other

- No `runBlocking` in production code. Tests may use it.
- Use `withContext(Dispatchers.IO)` for blocking I/O.

## Note on scope

The two rules above differ deliberately in whether tests are carved out. `runBlocking` in a
test is harmless — the test *is* the top of the tree. A leaked or uncancellable scope in a
test is not: it outlives the test that made it and lands on whichever test runs next.
