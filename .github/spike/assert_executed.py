#!/usr/bin/env python3
"""Spike #144 — assert each named native test task EXECUTED, and on which arch.

Structure follows hauler's `.github/assert_tests_executed.py`, which exists
because a 179-line bash version shipped seven defects, four fail-open. The traps
it names are avoided here the same way: a real XML parser (not a greedy BRE that
takes the last match, and not one that can match a <testsuite> line quoted
inside CDATA), int() rather than a glob class, Python ints rather than shell
arithmetic that is octal-fatal on "08" and wraps at 64 bits, and no globs
evaluated under `set -u`.

What this adds on top, and why: a non-zero <testsuite tests="N"> proves a test
EXECUTED, never that it ASSERTED anything, and Kotlin/Native has no `Assume` —
a self-skipping native test returns early and emits no <skipped/>, so the XML is
numerically identical to a real pass (#151). For an x64 task there is a second,
worse ambiguity: a count could in principle come from an arm64 binary. So each
*X64Test directory must ALSO carry the SpikeArchProbeTest marker reporting
cpu=X64 from inside the binary that ran. A marker saying ARM64 is a hard failure,
not a pass — that is precisely the silent fallback this spike exists to rule out.

Usage:  assert_executed.py <results-dir> [<results-dir> ...]
"""
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

MARKER = re.compile(r"SPIKE_ARCH cpu=(\w+) os=(\w+)")


def err(msg: str) -> None:
    print(f"::error::{msg}")


def check(path: str) -> bool:
    d = Path(path)
    task = d.name
    expect_x64 = task.endswith("X64Test")

    if not d.is_dir():
        err(f"{path}: no results directory — cannot determine whether it ran.")
        err("This is NOT a pass. A suite whose count cannot be read did not report.")
        return False

    files = sorted(p for p in d.glob("*.xml") if p.is_file())
    if not files:
        err(f"{path}: directory exists but contains NO XML — the task produced no report.")
        return False

    # `total` counts EXECUTED tests = tests - skipped (as ci.yml does since #175):
    # a file whose every test is skipped executed nothing. `failed` collects any
    # file reporting failures or errors — a run containing a failure is not a
    # pass, however many tests it executed (#177).
    total, parsed, bad, archs, failed = 0, 0, [], set(), []
    for f in files:
        try:
            root = ET.parse(f).getroot()
        except (ET.ParseError, OSError) as e:
            bad.append(f"{f.name}: {e}")
            continue
        suites = [root] if root.tag == "testsuite" else list(root.iter("testsuite"))
        n = None
        for s in suites:
            if s.get("tests") is not None:
                counts = {}
                for attr in ("tests", "skipped", "failures", "errors"):
                    raw = s.get(attr, "0" if attr != "tests" else None)
                    try:
                        counts[attr] = int(raw, 10)
                    except ValueError:
                        bad.append(f"{f.name}: {attr}={raw!r} is not an integer")
                        counts = None
                        break
                if counts is not None:
                    n = counts["tests"] - counts["skipped"]
                    if counts["failures"] or counts["errors"]:
                        failed.append(
                            f"{f.name}: failures={counts['failures']} errors={counts['errors']}"
                        )
                break
        if n is None:
            bad.append(f"{f.name}: no <testsuite tests=...> attribute")
            continue
        parsed += 1
        total += n
        try:
            for m in MARKER.finditer(f.read_text(errors="replace")):
                archs.add(m.group(1))
        except OSError as e:
            bad.append(f"{f.name}: unreadable for arch marker: {e}")

    # A SUBTOTAL IS NOT A TOTAL.
    if bad:
        if parsed == 0:
            err(f"{task}: {len(files)} XML file(s), NONE parseable. Parser failure, not a zero run.")
        else:
            err(f"{task}: only {parsed} of {len(files)} file(s) yielded a count — PARTIAL PARSE.")
            err(f"{total} would be a subtotal, not a total; it is not reported as one.")
        for b in bad[:5]:
            err(f"  {b}")
        return False

    if failed:
        err(f"{task}: {len(failed)} file(s) report failing or erroring tests — NOT a pass.")
        for b in failed[:5]:
            err(f"  {b}")
        return False

    if total <= 0:
        err(f"{task}: {len(files)} file(s) parsed cleanly and report ZERO tests executed (tests − skipped).")
        return False

    arch_str = ",".join(sorted(archs)) if archs else "<no SPIKE_ARCH marker>"
    print(f"{task}: EXECUTED {total} tests from {len(files)} file(s); arch marker: {arch_str}")

    if expect_x64:
        if not archs:
            err(f"{task}: {total} tests counted but NO SPIKE_ARCH marker in any XML.")
            err("The count alone cannot distinguish an x64 run from an arm64 one. Not a pass.")
            return False
        if archs != {"X64"}:
            err(f"{task}: SPIKE_ARCH reported {arch_str} — this is NOT an x64 execution.")
            return False
    return True


def main(argv: list) -> int:
    if not argv:
        err("assert_executed.py called with no paths — an assertion over an empty set is not a pass.")
        return 1
    ok, examined = True, 0
    for p in argv:
        ok &= check(p)
        examined += 1
    if examined != len(argv):
        err(f"examined only {examined} of {len(argv)}. Unexamined is not passed.")
        return 1
    return 0 if ok else 1


if __name__ == "__main__":
    try:
        sys.exit(main(sys.argv[1:]))
    except Exception as e:
        err(f"assert_executed.py crashed: {type(e).__name__}: {e}")
        err("A crash is not a pass.")
        sys.exit(1)
