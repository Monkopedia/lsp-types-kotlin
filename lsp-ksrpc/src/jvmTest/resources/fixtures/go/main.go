// Tiny self-contained fixture for gopls client-role integration tests.
package main

func add(a int, b int) int {
	return a + b
}

func main() {
	total := add(2, 3)
	_ = total
}
