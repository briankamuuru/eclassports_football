package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

enum class TipCategory(val id: String, val defaultTitle: String, val firestoreKey: String) {
    FREE("free", "Free Tips", "FREE_TIPS"),
    ALL_VIP("all_vip", "All VIP Tips", "ALL_VIP"),
    VIP_2_PLUS("vip_2_plus", "2+ Tips VIP", "VIP_2_PLUS"),
    VIP_3_PLUS("vip_3_plus", "3+ Tips VIP", "VIP_3_PLUS"),
    VIP_5_PLUS("vip_5_plus", "5+ Tips VIP", "VIP_5_PLUS"),
    VIP_SPECIAL("vip_special", "Special VIP", "SPECIAL_VIP");

    companion object {
        fun fromId(id: String): TipCategory {
            val normalized = id.trim().uppercase()
            return when {
                normalized == "FREE_TIPS" || normalized == "FREE" || normalized == "FREE TIPS" -> FREE
                normalized == "ALL_VIP" || normalized == "ALL_VIP_TIPS" || normalized == "ALL VIP" || normalized == "ALL VIP TIPS" || normalized == "ALL" -> ALL_VIP
                normalized == "2_PLUS_VIP" || normalized == "VIP_2_PLUS" || normalized.contains("2+") || normalized.contains("2_PLUS") -> VIP_2_PLUS
                normalized == "3_PLUS_VIP" || normalized == "VIP_3_PLUS" || normalized.contains("3+") || normalized.contains("3_PLUS") -> VIP_3_PLUS
                normalized == "5_PLUS_VIP" || normalized == "VIP_5_PLUS" || normalized.contains("5+") || normalized.contains("5_PLUS") -> VIP_5_PLUS
                normalized == "SPECIAL_VIP" || normalized == "VIP_SPECIAL" || normalized.contains("SPECIAL") -> VIP_SPECIAL
                else -> entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: FREE
            }
        }
    }
}

enum class TipStatus {
    PENDING,
    WON,
    LOST
}

@IgnoreExtraProperties
data class TipModel(
    val id: String = "",
    val category: String = "FREE_TIPS", // FREE_TIPS, VIP_2_PLUS, VIP_3_PLUS, VIP_5_PLUS, SPECIAL_VIP
    val categoryName: String = "",
    val ticketDate: String = "",
    val homeTeam: String = "",
    val awayTeam: String = "",
    val match: String = "",
    val kickoffTime: String = "",
    val league: String = "",
    val tipPick: String = "",
    val odd: Double = 1.0,
    val status: String = "PENDING", // PENDING, WON, LOST
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    fun toEntity(fallbackId: String = ""): TipEntity {
        val resolvedId = id.ifEmpty { fallbackId.ifEmpty { UUID.randomUUID().toString() } }
        val categoryEnum = TipCategory.fromId(category)

        var home = homeTeam.trim()
        var away = awayTeam.trim()
        if (home.isEmpty() && away.isEmpty() && match.isNotEmpty()) {
            val parts = match.split(" vs ", " - ", " v ")
            if (parts.size >= 2) {
                home = parts[0].trim()
                away = parts[1].trim()
            } else {
                home = match.trim()
            }
        }

        return TipEntity(
            id = resolvedId,
            category = categoryEnum.id,
            categoryName = if (categoryName.isNotEmpty()) categoryName else categoryEnum.defaultTitle,
            matchDate = ticketDate.trim(),
            kickoffTime = kickoffTime.trim(),
            league = league.trim(),
            homeTeam = home,
            awayTeam = away,
            prediction = tipPick.trim(),
            odds = if (odd > 0.0) odd else 1.0,
            status = when (status.trim().uppercase()) {
                "WON" -> TipStatus.WON.name
                "LOST" -> TipStatus.LOST.name
                else -> TipStatus.PENDING.name
            },
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

@Entity(tableName = "tips")
data class TipEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val category: String, // from TipCategory.id (free, vip_2_plus, vip_3_plus, vip_5_plus, vip_special)
    val categoryName: String = "",
    val matchDate: String, // ISO "yyyy-MM-dd"
    val kickoffTime: String, // "HH:mm"
    val league: String,
    val homeTeam: String,
    val awayTeam: String,
    val prediction: String, // tipPick
    val odds: Double, // odd
    val status: String = TipStatus.PENDING.name, // PENDING, WON, LOST
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class DateGroupedTips(
    val dateIso: String,
    val formattedDate: String,
    val tips: List<TipEntity>,
    val numberOfOdds: Int,
    val accumulatedOdds: Double,
    val isToday: Boolean = false
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
     * Groups predictions by date and sorts date groups so that the CURRENT DATE (today)
     * appears FIRST / at the top.
     * Subsequent future dates appear next (closest upcoming first),
     * followed by historical/past dates (most recent past first).
     */
    fun groupAndSortPredictions(
        tips: List<TipEntity>,
        today: LocalDate = LocalDate.now()
    ): List<DateGroupedTips> {
        if (tips.isEmpty()) return emptyList()

        val groupedByDate = tips.groupBy { it.matchDate }

        val sortedDates = groupedByDate.keys.sortedWith { d1, d2 ->
            if (d1 == d2) return@sortedWith 0
            val p1 = runCatching { LocalDate.parse(d1) }.getOrNull()
            val p2 = runCatching { LocalDate.parse(d2) }.getOrNull()

            val isD1Today = (p1 != null && p1.isEqual(today)) || (p1 == null && d1 == today.toString())
            val isD2Today = (p2 != null && p2.isEqual(today)) || (p2 == null && d2 == today.toString())

            when {
                // Current date odds appear first / at the top
                isD1Today && !isD2Today -> -1
                isD2Today && !isD1Today -> 1
                isD1Today && isD2Today -> 0

                p1 != null && p2 != null -> {
                    val p1Future = p1.isAfter(today)
                    val p2Future = p2.isAfter(today)
                    when {
                        // Future/upcoming dates: closest to today first
                        p1Future && p2Future -> p1.compareTo(p2)
                        // Future dates come before past dates
                        p1Future && !p2Future -> -1
                        !p1Future && p2Future -> 1
                        // Past dates: most recent past first
                        else -> p2.compareTo(p1)
                    }
                }
                p1 != null -> -1
                p2 != null -> 1
                else -> d1.compareTo(d2)
            }
        }

        return sortedDates.mapNotNull { dateKey ->
            val groupTips = groupedByDate[dateKey] ?: return@mapNotNull null
            if (groupTips.isEmpty()) return@mapNotNull null

            val sortedGroupTips = groupTips.sortedWith(
                compareBy<TipEntity> { it.kickoffTime }
                    .thenBy { it.id }
                    .thenBy { it.homeTeam }
            )

            val count = sortedGroupTips.size
            val accumulatedOdds = sortedGroupTips.fold(1.0) { acc, tip -> acc * tip.odds }
            val parsedDate = runCatching { LocalDate.parse(dateKey) }.getOrNull()
            val isToday = (parsedDate != null && parsedDate.isEqual(today)) || dateKey == today.toString()

            DateGroupedTips(
                dateIso = dateKey,
                formattedDate = formatDate(dateKey),
                tips = sortedGroupTips,
                numberOfOdds = count,
                accumulatedOdds = accumulatedOdds,
                isToday = isToday
            )
        }
    }
}
