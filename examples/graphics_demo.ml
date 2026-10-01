// =========================================================
// MiniLang 2.0: Hardware-Accelerated Graphics & OpenGL Demo
// Combines Python-like OOP with immediate-mode 2D/GL API
// =========================================================

print("Starting MiniLang 2.0 Graphics Window...");

let winWidth = 800;
let winHeight = 600;
glInitWindow(winWidth, winHeight, "MiniLang 2.0 - Hardware Accelerated OpenGL Engine");

// Ball class using OOP concepts
class Ball {
    init(x, y, vx, vy, radius, r, g, b) {
        self.x = x;
        self.y = y;
        self.vx = vx;
        self.vy = vy;
        self.radius = radius;
        self.r = r;
        self.g = g;
        self.b = b;
    }

    update(maxWidth, maxHeight) {
        self.x = self.x + self.vx;
        self.y = self.y + self.vy;

        // Bounce horizontally
        if (self.x - self.radius < 0.0) {
            self.x = self.radius;
            self.vx = 0.0 - self.vx;
        }
        if (self.x + self.radius > maxWidth) {
            self.x = maxWidth - self.radius;
            self.vx = 0.0 - self.vx;
        }

        // Bounce vertically
        if (self.y - self.radius < 0.0) {
            self.y = self.radius;
            self.vy = 0.0 - self.vy;
        }
        if (self.y + self.radius > maxHeight) {
            self.y = maxHeight - self.radius;
            self.vy = 0.0 - self.vy;
        }
    }

    render() {
        glColor(self.r, self.g, self.b);
        glFillCircle(self.x, self.y, self.radius);
    }
}

// Create a cluster of colorful bouncing balls
let balls = [];
push(balls, Ball(100.0, 150.0, 3.5, 2.8, 22.0, 255.0, 80.0, 80.0));   // Coral Red
push(balls, Ball(300.0, 200.0, -2.8, 3.2, 18.0, 80.0, 220.0, 120.0)); // Emerald
push(balls, Ball(500.0, 350.0, 4.0, -2.5, 28.0, 70.0, 160.0, 255.0)); // Sky Blue
push(balls, Ball(200.0, 400.0, -3.2, -3.0, 16.0, 240.0, 200.0, 60.0)); // Amber
push(balls, Ball(650.0, 250.0, 2.5, 4.0, 24.0, 210.0, 100.0, 255.0)); // Purple

let frameCount = 0;
let maxFrames = 120; // Run 120 smooth frames for automated testing, or until closed

while (glIsOpen() && frameCount < maxFrames) {
    // 1. Clear with sleek dark blue background (OpenGL glClear)
    glClear(20.0, 24.0, 35.0);

    // 2. Draw HUD and Title Banner
    glColor(40.0, 48.0, 68.0);
    glFillRect(10.0, 10.0, winWidth - 20.0, 50.0);

    glColor(255.0, 255.0, 255.0);
    glText("MiniLang 2.0: Object-Oriented Physics & Graphics Demo", 25.0, 42.0, 18.0);

    // 3. Update and render all OOP balls
    for (let i = 0; i < len(balls); i = i + 1) {
        let b = balls[i];
        b.update(winWidth, winHeight);
        b.render();
    }

    // 4. OpenGL-style Immediate Mode Triangle in center
    glColor(255.0, 180.0, 50.0);
    glBegin("TRIANGLES");
    glVertex(400.0, 240.0);
    glVertex(360.0, 310.0);
    glVertex(440.0, 310.0);
    glEnd();

    // 5. Render mouse cursor follower if inside window
    let mx = glMouseX();
    let my = glMouseY();
    if (mx > 0 && my > 0) {
        glColor(255.0, 255.0, 255.0);
        glCircle(mx, my, 12.0);
    }

    // 6. Present frame buffer (~60 FPS)
    glUpdate();
    frameCount = frameCount + 1;
}

print("Rendered " + str(frameCount) + " frames successfully!");
glQuit();
print("Graphics window closed cleanly.");
