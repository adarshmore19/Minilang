fn factorial(n) {
    if (n <= 1) {
        return 1;
    }
    return n * factorial(n - 1);
}

let result = factorial(5);

if (result == 120) {
    print("MiniLang works!");
} else {
    print("Something is wrong.");
}
