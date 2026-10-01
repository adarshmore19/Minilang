// ============================================================
// MiniLang 2.0 - Interactive Turtle Graphics Demo
// Learn visual geometry and create beautiful generative art!
// ============================================================

import turtle

say "Initializing Turtle Graphics..."

turtleInit(600, 600, "MiniLang Turtle Art")
turtleSpeed(1) // Fast drawing
penSize(2)

say "Drawing colorful geometric spiral..."

// Draw a colorful 36-segment geometric star spiral
colors = [
    [231, 76, 60],   // Red
    [243, 156, 18],  // Orange
    [241, 196, 15],  // Yellow
    [46, 204, 113],  // Green
    [52, 152, 219],  // Blue
    [155, 89, 182]   // Purple
]

dist = 5
repeat 60 {
    // Cycle through palette
    c = colors[dist % 6]
    penColor(c[0], c[1], c[2])

    forward(dist)
    turnRight(59) // 59 degrees produces a mesmerizing rotating hexagon spiral
    dist = dist + 3
}

// Draw a central circle badge
penUp()
forward(20)
penDown()
penColor(255, 215, 0) // Gold
penSize(3)
turtleCircle(25)

saved = turtleSave("output/turtle_art.png")
if saved {
    say "Turtle masterpiece saved to 'output/turtle_art.png'! 🐢🎨"
}

say "Turtle graphics demo complete!"
