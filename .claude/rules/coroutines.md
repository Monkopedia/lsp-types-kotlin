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

1. **What ends its work — or why never ending is harmless.** One of:
   - a terminator that definitely fires: EOF or a stream close that genuinely unblocks
     the read on every target we build for; or an explicit `cancel()` — but note
     **`cancel()` does not interrupt a blocking syscall.** A coroutine parked in
     `read(2)` stays parked through cancellation, so "we cancel it" is not an answer
     for a blocking read; or
   - **the work may BLOCK forever awaiting input that never arrives**, and that is
     acceptable because nothing awaits it and it cannot hold the process open — a daemon
     dispatcher on JVM, process exit elsewhere. Say so explicitly. This is the honest
     answer for a pump parked on a dead fd, and pretending such a read has a terminator
     would be worse.
     **This branch is for work that is BLOCKED, not work that is BUSY.** A loop that
     keeps doing something — `while (true) { delay(1.seconds); flush() }` — is not
     parked; it runs forever, and "nothing awaits it" does not excuse it. That needs a
     real terminator from the first branch.
2. **Why it is not a child of the caller's job** — which is the reason you are
   constructing one at all. `CoroutineScope(SupervisorJob() + ...)` gives it its own
   job, so the caller's teardown never waits on it.

**Both must be answerable. A violation is a constructed scope that fails either
question.** If the honest answer to *"what stops this?"* is *"nothing"* or *"it gets
garbage-collected"*, that is the violation.

**Write both answers in a comment at the construction site.** An unwritten answer is a
violation: a reviewer cannot check your reasoning, only your text.

`LspProcessConnection.kt` is the exemplar — it names the terminator (winds down on clean
EOF), the reason never-ending is harmless (daemon `Dispatchers.IO`, so a pump on a dead
fd cannot hold the JVM), and why it is not a child of the caller. **`LspConnection.kt`
is not** — it documents Q2 at length and never answers Q1, which is a gap in that
comment rather than in the code.

**Dispatcher choice is a separate obligation.** `Dispatchers.IO` and `Dispatchers.Default`
run on daemon threads, so work left parked on them can never hold the process open. A
dispatcher with non-daemon threads — `newSingleThreadContext`, a custom executor — **can**,
so a scope that **chooses** one **must** be explicitly cancelled. **And if its work
blocks in a syscall, cancellation will not free the thread** — so choosing a non-daemon
dispatcher for a blocking read is a process hang whether or not you cancel it. Use a
daemon dispatcher.

**Inheriting the caller's dispatcher is different from choosing one, and the difference
is the whole point.** When you inherit, your answer is *"no worse than the caller"* — the
scope runs wherever the caller already runs, so it cannot outlive resources the caller
had anyway. That is legitimate, and checkable at the site: you can see that you
inherited. **It is not a claim that the dispatcher is daemon, and must not be written as
one.**

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
