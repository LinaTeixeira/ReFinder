package pt.ua.icm.refinder.data.model

import kotlin.math.*

fun findPossibleMatches(
    currentItem: LostItem,
    allItems: List<LostItem>
): List<ItemMatch> {
    val oppositeType = if (currentItem.type == "lost") "found" else "lost"

    return allItems
        .filter { it.id != currentItem.id && it.type == oppositeType && it.status != "claimed" }
        .mapNotNull { candidate ->
            val score = calculateMatchScore(currentItem, candidate)

            if (score >= 70) {
                ItemMatch(
                    item = candidate,
                    score = score,
                    reason = buildMatchReason(currentItem, candidate, score)
                )
            } else null
        }
        .sortedByDescending { it.score }
        .take(5)
}

private fun calculateMatchScore(a: LostItem, b: LostItem): Int {
    var score = 0

    if (a.category.equals(b.category, ignoreCase = true)) {
        score += 40
    }

    val commonWordsCount = commonWords(a.title + " " + a.description, b.title + " " + b.description)
    score += minOf(commonWordsCount * 10, 30)

    val distance = distanceKm(a.latitude, a.longitude, b.latitude, b.longitude)
    if (distance != null) {
        score += when {
            distance <= 0.5 -> 20
            distance <= 2.0 -> 12
            distance <= 5.0 -> 6
            else -> 0
        }
    }

    if (a.date == b.date && a.date.isNotBlank()) {
        score += 10
    }

    return score.coerceAtMost(100)
}

private fun commonWords(textA: String, textB: String): Int {
    val ignored = setOf(
        "de", "da", "do", "das", "dos", "um", "uma", "o", "a", "e",
        "com", "sem", "no", "na", "em", "para", "item", "objeto"
    )

    val wordsA = textA.lowercase()
        .split(" ", ",", ".", "-", "_", "/", "\n")
        .map { it.trim() }
        .filter { it.length >= 3 && it !in ignored }
        .toSet()

    val wordsB = textB.lowercase()
        .split(" ", ",", ".", "-", "_", "/", "\n")
        .map { it.trim() }
        .filter { it.length >= 3 && it !in ignored }
        .toSet()

    return wordsA.intersect(wordsB).size
}

private fun buildMatchReason(a: LostItem, b: LostItem, score: Int): String {
    val reasons = mutableListOf<String>()

    if (a.category.equals(b.category, ignoreCase = true)) {
        reasons.add("mesma categoria")
    }

    if (commonWords(a.title + " " + a.description, b.title + " " + b.description) > 0) {
        reasons.add("descrição parecida")
    }

    val distance = distanceKm(a.latitude, a.longitude, b.latitude, b.longitude)
    if (distance != null && distance <= 2.0) {
        reasons.add("localização próxima")
    }

    if (a.date == b.date && a.date.isNotBlank()) {
        reasons.add("mesma data")
    }

    return if (reasons.isEmpty()) {
        "$score% de compatibilidade"
    } else {
        "$score% de compatibilidade · ${reasons.joinToString(", ")}"
    }
}

private fun distanceKm(
    lat1: Double?,
    lon1: Double?,
    lat2: Double?,
    lon2: Double?
): Double? {
    if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) return null

    val earthRadiusKm = 6371.0

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(lat1)) *
            cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2.0)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earthRadiusKm * c
}
