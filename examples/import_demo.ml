// ============================================================
// MiniLang 2.0 - Module & Package Import System Demo
// ============================================================

import "examples/math_helper.ml"
import algo

say "======================================"
say "   MiniLang Module & Import Demo      "
say "======================================"

// 1. Using functions from imported file
say "5 squared = " + str(square(5))
say "3 cubed = " + str(cube(3))
say "6 factorial = " + str(factorial(6))

// 2. Using classes from imported file
v = Vector2D(3, 4)
say "Created vector: " + v.describe()
say "Magnitude: " + str(v.magnitude())

// 3. Combining with algorithm package
data = [square(2), square(3), square(4), square(5)]
say "Squares dataset: " + str(data)
say "Sum of squares: " + str(sum(data))
say "Average of squares: " + str(mean(data))

say "\nModule import completed successfully! 🎉"
