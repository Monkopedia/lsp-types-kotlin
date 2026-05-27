"""Tiny self-contained fixture for pyright client-role integration tests."""


def add(a: int, b: int) -> int:
    return a + b


def main() -> int:
    total = add(2, 3)
    return total
