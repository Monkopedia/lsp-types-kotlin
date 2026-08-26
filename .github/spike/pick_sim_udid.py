#!/usr/bin/env python3
"""Spike #144 — print the UDID of an available simulator for one OS family.

KGP supplies a `device` convention only to a simulator test task it already
considers ENABLED, so once the host-arch check is overridden the device has to
come from outside. A UDID rather than a device NAME on purpose: names drift
with the runner image ("Apple TV 4K (3rd generation)"), and a guessed name
fails in exactly the way a simulator that cannot run x86_64 fails — which is
the thing being measured and must not be confused with a typo.

Usage:  pick_sim_udid.py iOS|tvOS|watchOS      (prints nothing if none exist)
"""
import json
import sys


def main() -> int:
    if len(sys.argv) != 2:
        print("usage: pick_sim_udid.py <iOS|tvOS|watchOS>", file=sys.stderr)
        return 2
    family = sys.argv[1]
    try:
        devices = json.load(sys.stdin)["devices"]
    except (json.JSONDecodeError, KeyError) as e:
        print(f"pick_sim_udid.py: unusable simctl JSON: {e}", file=sys.stderr)
        return 1
    # Sorted for determinism: an unsorted dict would pick a different runtime
    # between runs and make a failure look intermittent.
    for runtime, devs in sorted(devices.items()):
        if f"SimRuntime.{family}-" in runtime and devs:
            print(devs[0]["udid"])
            return 0
    print(f"pick_sim_udid.py: no available {family} simulator", file=sys.stderr)
    return 0


if __name__ == "__main__":
    sys.exit(main())
