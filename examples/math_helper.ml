// MiniLang 2.0 - Reusable Math Helper Module

def square(x) {
    return x * x
}

def cube(x) {
    return x * x * x
}

def factorial(n) {
    if n <= 1 {
        return 1
    }
    return n * factorial(n - 1)
}

class Vector2D {
    fn init(x, y) {
        this.x = x
        this.y = y
    }

    fn magnitude() {
        return sqrt(this.x * this.x + this.y * this.y)
    }

    fn describe() {
        return "Vector(" + str(this.x) + ", " + str(this.y) + ")"
    }
}
