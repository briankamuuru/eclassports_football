package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class TipCategory(val id: String, val defaultTitle: String) {
    FREE("free", "Free Tips"),
    VIP_2_PLUS("vip_2_plus", "2+ Tips VIP"),
    VIP_3_PLUS("vip_3_plus", "3+ Tips VIP"),
    VIP_5_PLUS("vip_5_plus", "5+ Tips VIP"),
    VIP_SPECIAL("vip_special", "Special VIP");

    companion object {
        fun fromId(id: String): TipCategory {
            return entries.firstOrNull { it.id == id } ?: FREE
        }
    }
}

enum class TipStatus {
    PENDING,
    WON,
    LOST
}

@Entity(tableName = "tips")
data class TipEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // from TipCategory.id
    val matchDate: String, // ISO "yyyy-MM-dd", e.g. "2026-10-12"
    val kickoffTime: String, // "HH:mm", e.g. "16:30"
    val league: String, // e.g. "Premier League"
    val homeTeam: String, // e.g. "Arsenal"
    val awayTeam: String, // e.g. "Chelsea"
    val prediction: String, // e.g. "Over 2.5 Goals"
    val odds: Double, // e.g. 1.85
    val status: String = TipStatus.PENDING.name, // PENDING, WON, LOST
    val createdAt: Long = System.currentTimeMillis()
)

data class DateGroupedTips(
    val dateIso: String,
    val formattedDate: String,
    val tips: List<TipEntity>,
    val numberOfOdds: Int,
    val accumulatedOdds: Double
)

object DateGroupingHelper {
    private val displayFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.US)

    fun formatDate(isoDate: String): String {
        return try {
            val parsed = LocalDate.parse(isoDate)
            parsed.format(displayFormatter)
        } catch (_: Exception) {
            isoDate
        }
    }

    /**
     * Groups predictions by their actual match date,
     * sorts dates chronologically (earliest first),
     * sorts predictions within each date by kickoff time (earliest first)
     * with consistent secondary ordering,
     * and calculates the number of odds and accumulated odds product.
     */
    fun groupAndSortPredictions(tips: List<TipEntity>): List<DateGroupedTips> {
        if (tips.isEmpty()) return emptyList()

        // 1. Group by matchDate (actual match date, not entry date)
        val groupedByDate = tips.groupBy { it.matchDate }

        // 2. Sort dates in chronological order (earliest displayed first)
        val sortedDates = groupedByDate.keys.sortedWith { d1, d2 ->
            val p1 = runCatching { LocalDate.parse(d1) }.getOrNull()
            val p2 = runCatching { LocalDate.parse(d2) }.getOrNull()
            when {
                p1 != null && p2 != null -> p1.compareTo(p2)
                p1 != null -> -1
                p2 != null -> 1
                else -> d1.compareTo(d2)
            }
        }

        // 3. For each date group, sort predictions by kickoff time and calculate summary
        return sortedDates.mapNotNull { dateKey ->
            val groupTips = groupedByDate[dateKey] ?: return@mapNotNull null
            if (groupTips.isEmpty()) return@mapNotNull null

            // Sort within date group by kickoff time earliest to latest, then secondary ordering
            val sortedGroupTips = groupTips.sortedWith(
                compareBy<TipEntity> { it.kickoffTime }
                    .thenBy { it.id }
                    .thenBy { it.homeTeam }
            )

            // Calculate accumulated odds as the numeric product of all individual odds
            // If one prediction, accumulated odds equals that prediction's odds
            val count = sortedGroupTips.size
            val accumulatedOdds = sortedGroupTips.fold(1.0) { acc, tip -> acc * tip.odds }

            DateGroupedTips(
                dateIso = dateKey,
                formattedDate = formatDate(dateKey),
                tips = sortedGroupTips,
                numberOfOdds = count,
                accumulatedOdds = accumulatedOdds
            )
        }
    }
}
