// ============================================
// MiniLang 2.0 Feature Showcase
// ============================================

print("--- 1. Floating-point Math & Math Builtins ---");
let radius = 5.5;
let area = 3.14159 * radius * radius;
print("Circle radius: " + str(radius));
print("Circle area: " + str(area));
print("Square root of 256: " + str(sqrt(256)));
print("2 to the power of 10: " + str(pow(2, 10)));
print("Min(15, 42): " + str(min(15, 42)));
print("Max(15, 42): " + str(max(15, 42)));

print("");
print("--- 2. Dynamic Arrays & Manipulation ---");
let numbers = [10, 20, 30];
print("Initial array: " + str(numbers));
print("Length: " + str(len(numbers)));

push(numbers, 40);
push(numbers, 50);
print("After push(40, 50): " + str(numbers));

let last = pop(numbers);
print("Popped item: " + str(last));
print("After pop: " + str(numbers));

numbers[1] = 999;
print("Modified index 1: " + str(numbers));

print("");
print("--- 3. For Loops, Break, and Continue ---");
print("Even numbers from 1 to 10:");
for (let i = 1; i <= 10; i = i + 1) {
    if (i % 2 != 0) {
        continue;
    }
    print("Even: " + str(i));
}

print("");
print("--- 4. Types & Conversions ---");
print("Type of 42: " + type(42));
print("Type of 3.14: " + type(3.14));
print("Type of 'hello': " + type("hello"));
print("Type of [1, 2]: " + type([1, 2]));

print("");
print("All MiniLang 2.0 features demonstrated successfully!");
