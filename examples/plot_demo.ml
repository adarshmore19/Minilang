// ============================================================
// MiniLang 2.0 - Scientific Plotting and Visualization Demo
// Matplotlib-style charts in MiniLang!
// ============================================================

import plot

say "Generating scientific charts..."

plotClear()
plotTitle("Quarterly Performance & Growth")
plotXLabel("Quarter / Step")
plotYLabel("Value ($k)")
plotGrid(true)

// 1. Line plot: Projected Growth
steps = [1, 2, 3, 4, 5, 6, 7, 8]
growth = [10, 15, 25, 40, 60, 90, 130, 180]
plotLine(steps, growth, "Projected Revenue")

// 2. Scatter plot: Actual Sales
sales = [12, 14, 28, 38, 65, 88, 125, 185]
plotScatter(steps, sales, "Actual Revenue")

// Save to disk
saved = plotSave("output/quarterly_growth.png", 800, 500)
if saved {
    say "Successfully saved chart to 'output/quarterly_growth.png'! 📊"
}

// 3. Bar Chart: Department Performance
plotClear()
plotTitle("Department Efficiency Ratings")
plotXLabel("Departments")
plotYLabel("Score (out of 100)")

departments = ["Engineering", "Design", "Marketing", "Research", "Support"]
scores = [94, 88, 79, 96, 85]
plotBar(departments, scores, "Q3 Ratings")

savedBar = plotSave("output/department_ratings.png", 800, 500)
if savedBar {
    say "Successfully saved bar chart to 'output/department_ratings.png'! 📈"
}

say "Plotting complete! You can open the generated images in the output/ folder."
