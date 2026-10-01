// ============================================================
// MiniLang 2.0 - Kid-Friendly Programming Demo
// Simple, intuitive, and fun for a 10-year-old kid!
// ============================================================

say "======================================"
say "   Welcome to MiniLang for Kids! 🚀   "
say "======================================"

// 1. Easy variables without 'let' or semicolons
playerName = "SuperCoder"
level = 1
coins = 50
say "Player: " + playerName
say "Level: " + str(level)
say "Coins: " + str(coins)

// 2. Fun string multiplication
fanCheer = "Hip Hip Hurray! " * 2
say fanCheer

// 3. Repeat loops - do something multiple times easily!
say "Counting down for rocket launch..."
countdown = 5
repeat 5 times {
    say "T-minus: " + str(countdown)
    countdown = countdown - 1
}
say "Blast off! 🚀✨"

// 4. For-in loops with range
say "Doing 3 jumping jacks:"
for jack in range(1, 4) {
    say "Jumping jack #" + str(jack) + "! 🤸"
}

// 5. Friendly lists and loops
pets = ["🐶 Dog", "🐱 Cat", "🐰 Bunny", "🦜 Parrot"]
say "My favorite pets:"
for pet in pets {
    say "  * " + pet
}

// 6. Natural English conditions with 'and', 'or', 'not', 'is'
score = 95
hasGoldenKey = true

if score >= 90 and hasGoldenKey {
    say "🏆 Champion! You unlocked the secret treasure room!"
}

if level is 1 or not hasGoldenKey {
    say "Welcome to the beginner realm!"
}

say "You are ready to code anything! 🎉"
