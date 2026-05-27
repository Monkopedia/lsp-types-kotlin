// Tiny self-contained fixture for typescript-language-server integration tests.

function add(a: number, b: number): number {
    return a + b;
}

function main(): number {
    const total = add(2, 3);
    return total;
}

main();
