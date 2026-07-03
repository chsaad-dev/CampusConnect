package com.campusconnect.core.utils

object SmartAlgorithms {

    /**
     * Calculates Jaccard Similarity between two text strings using word tokens.
     * Returns a score between 0.0 (completely different) and 1.0 (identical).
     */
    fun calculateTextSimilarity(s1: String, s2: String): Double {
        val stopWords = setOf("the", "a", "an", "is", "are", "was", "were", "and", "or", "but", "in", "on", "at", "to", "for", "with", "of")
        val words1 = s1.lowercase()
            .split(Regex("\\W+"))
            .filter { it.isNotEmpty() && !stopWords.contains(it) }
            .toSet()

        val words2 = s2.lowercase()
            .split(Regex("\\W+"))
            .filter { it.isNotEmpty() && !stopWords.contains(it) }
            .toSet()

        if (words1.isEmpty() && words2.isEmpty()) return 1.0
        val intersection = words1.intersect(words2).size
        val union = words1.union(words2).size
        return intersection.toDouble() / union.toDouble()
    }

    /**
     * Calculates the matching score of a user's skills against a job's required skills.
     * Returns match percentage (e.g. 66.66).
     */
    fun calculateSkillMatch(userSkills: List<String>, jobSkills: List<String>): Double {
        if (jobSkills.isEmpty()) return 100.0
        val cleanUserSkills = userSkills.map { it.trim().lowercase() }.toSet()
        val cleanJobSkills = jobSkills.map { it.trim().lowercase() }.toSet()

        val matched = cleanJobSkills.intersect(cleanUserSkills).size
        return (matched.toDouble() / cleanJobSkills.size.toDouble()) * 100.0
    }

    /**
     * Calculates popularity score for trending ranking.
     */
    fun calculatePopularityScore(likes: Int, downloads: Int, comments: Int): Int {
        return (likes * 2) + (downloads * 3) + (comments * 2)
    }
}
