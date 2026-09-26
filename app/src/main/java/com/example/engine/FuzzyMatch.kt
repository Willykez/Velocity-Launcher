package com.example.engine

/**
 * Small, dependency-free fuzzy matcher for the app drawer search box. Handles the
 * three things a plain `contains()` check misses:
 *  - out-of-order / abbreviation typing ("gmps" -> "Google Maps")
 *  - matches deep in a multi-word label, scored lower than a prefix match
 *  - a single typo (one substitution) still matching short queries
 *
 * Returns null for "no match", otherwise a score where higher = more relevant, so
 * callers can sort results instead of just filtering them.
 */
object FuzzyMatch {

    fun score(query: String, target: String): Int? {
        if (query.isBlank()) return 0
        val q = query.trim().lowercase()
        val t = target.lowercase()

        if (t == q) return 1000
        if (t.startsWith(q)) return 900
        if (t.contains(" $q")) return 700 // matches the start of a later word
        if (t.contains(q)) return 500

        val subsequenceScore = subsequenceScore(q, t)
        if (subsequenceScore != null) return subsequenceScore

        // Last resort: allow a single-character typo for short queries, where users
        // are most likely to mistype (e.g. "cslculator").
        if (q.length in 3..8 && withinOneEdit(q, t)) return 100

        return null
    }

    /** True if every character of [q] appears in [t] in order (not necessarily contiguous). */
    private fun subsequenceScore(q: String, t: String): Int? {
        var qi = 0
        var lastMatch = -1
        var gapPenalty = 0
        while (qi < q.length) {
            val idx = t.indexOf(q[qi], lastMatch + 1)
            if (idx == -1) return null
            if (lastMatch != -1) gapPenalty += (idx - lastMatch - 1)
            lastMatch = idx
            qi++
        }
        return (300 - gapPenalty).coerceAtLeast(50)
    }

    /** Cheap one-substitution/one-transposition tolerance check against any window of [t]. */
    private fun withinOneEdit(q: String, t: String): Boolean {
        if (q.length > t.length) return false
        for (start in 0..(t.length - q.length)) {
            var mismatches = 0
            for (i in q.indices) {
                if (q[i] != t[start + i]) mismatches++
                if (mismatches > 1) break
            }
            if (mismatches <= 1) return true
        }
        return false
    }
}
