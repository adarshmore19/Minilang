// =========================================================
// MiniLang 2.0: Object-Oriented Programming (OOP) Showcase
// Python-like syntax with classes, methods, 'this' & 'self'
// =========================================================

print("=== MiniLang 2.0 OOP Demonstration ===");

// 1. Vector2D Class with 'this'
class Vector2D {
    init(x, y) {
        this.x = x;
        this.y = y;
    }

    magnitude() {
        return sqrt(this.x * this.x + this.y * this.y);
    }

    add(other) {
        return Vector2D(this.x + other.x, this.y + other.y);
    }

    scale(factor) {
        this.x = this.x * factor;
        this.y = this.y * factor;
    }
}

let v1 = Vector2D(3.0, 4.0);
print("Vector v1 magnitude (expected 5.0):");
print(v1.magnitude());

let v2 = Vector2D(1.0, 2.0);
let v3 = v1.add(v2);
print("v3 = v1 + v2 coordinates:");
print(v3.x);
print(v3.y);

// 2. Particle Class with 'self' (Python style)
class Particle {
    init(id, x, y, vx, vy) {
        self.id = id;
        self.x = x;
        self.y = y;
        self.vx = vx;
        self.vy = vy;
        self.alive = true;
    }

    step() {
        self.x = self.x + self.vx;
        self.y = self.y + self.vy;
    }

    report() {
        print("Particle #" + str(self.id) + " at (" + str(round(self.x)) + ", " + str(round(self.y)) + ")");
    }
}

// 3. Entity System managing dynamic collection of instances
let particles = [];
push(particles, Particle(1, 10.0, 20.0, 1.5, 0.5));
push(particles, Particle(2, 50.0, 80.0, -2.0, 1.0));
push(particles, Particle(3, 100.0, 100.0, 0.0, -1.0));

print("\nSimulating 3 particles over 5 frames:");
for (let frame = 1; frame <= 5; frame = frame + 1) {
    print("--- Frame " + str(frame) + " ---");
    for (let i = 0; i < len(particles); i = i + 1) {
        let p = particles[i];
        p.step();
        p.report();
    }
}

print("\nType introspection:");
print("type(Vector2D) -> " + type(Vector2D));
print("type(v1)       -> " + type(v1));
print("type(v1.add)   -> " + type(v1.add));

print("\nOOP Demonstration completed successfully!");
