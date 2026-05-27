/* Tiny self-contained fixture for clangd client-role integration tests.
 * No external includes so clangd can parse it without a compile_commands.json. */

int add(int a, int b) {
    return a + b;
}

int main(void) {
    int total = add(2, 3);
    return total;
}
