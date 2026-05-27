// Tiny self-contained fixture for rust-analyzer client-role integration tests.

fn add(a: i32, b: i32) -> i32 {
    a + b
}

fn main() {
    let total = add(2, 3);
    let _ = total;
}
