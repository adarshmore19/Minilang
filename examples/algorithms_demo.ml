// ============================================================
// MiniLang 2.0 - Fast Algorithms and Statistics Demo
// ============================================================

import algo

say "=========================================="
say "   MiniLang Fast Algorithms Standard Lib  "
say "=========================================="

// 1. Array Generation with Range
say "\n1. Range Generator:"
evens = range(0, 20, 2)
say "Evens from 0 to 18: " + str(evens)

countdown = range(10, 0, -1)
say "Countdown: " + str(countdown)

// 2. High-performance Statistics
say "\n2. Statistics Library:"
dataset = [12, 45, 67, 23, 89, 34, 56, 78, 90, 11]
say "Dataset: " + str(dataset)
say "Count: " + str(len(dataset))
say "Sum: " + str(sum(dataset))
say "Mean (Average): " + str(mean(dataset))
say "Median: " + str(median(dataset))

// 3. Sorting (Ascending & Descending)
say "\n3. Sorting:"
unsorted = [42, 17, 89, 5, 23, 76, 1, 99, 34]
say "Original:   " + str(unsorted)

sortedAsc = sort(unsorted)
say "Ascending:  " + str(sortedAsc)

sortedDesc = sort(unsorted, false)
say "Descending: " + str(sortedDesc)

// 4. Reverse
say "\n4. Reversals:"
say "Reversed array: " + str(reverse([1, 2, 3, 4, 5]))
say "Palindrome test: 'racecar' -> '" + reverse("racecar") + "'"
say "Word reversal:   'stressed' -> '" + reverse("stressed") + "'"

// 5. Binary Search (O(log n))
say "\n5. Binary Search:"
database = sort([105, 302, 408, 12, 89, 730, 512, 601, 230])
say "Sorted Database: " + str(database)

targets = [408, 12, 730, 999]
for t in targets {
    idx = binarySearch(database, t)
    if idx >= 0 {
        say "Found target " + str(t) + " at index " + str(idx) + "! 🎯"
    } else {
        say "Target " + str(t) + " not found in database. ❌"
    }
}

say "\nAlgorithm demo completed successfully! ✨"
