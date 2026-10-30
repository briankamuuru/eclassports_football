package com.example

import com.example.data.DateGroupingHelper
import com.example.data.TipCategory
import com.example.data.TipEntity
import com.example.data.TipStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class ExampleUnitTest {

    @Test
    fun testAccumulatedOddsAndChronologicalDateSorting() {
        // Example from prompt:
        // 12 October 2026: Team A vs Team B (1.50), Team C vs Team D (1.80), Team E vs Team F (2.00)
        // Accumulated Odds = 1.50 * 1.80 * 2.00 = 5.40
        val tip1 = TipEntity(
            id = 1,
            category = TipCategory.FREE.id,
            matchDate = "2026-10-12",
            kickoffTime = "15:00",
            league = "Premier League",
            homeTeam = "Team A",
            awayTeam = "Team B",
            prediction = "Home win",
            odds = 1.50,
            status = TipStatus.PENDING.name
        )
        val tip2 = TipEntity(
            id = 2,
            category = TipCategory.FREE.id,
            matchDate = "2026-10-12",
            kickoffTime = "18:00",
            league = "La Liga",
            homeTeam = "Team C",
            awayTeam = "Team D",
            prediction = "Over 1.5",
            odds = 1.80,
            status = TipStatus.PENDING.name
        )
        val tip3 = TipEntity(
            id = 3,
            category = TipCategory.FREE.id,
            matchDate = "2026-10-12",
            kickoffTime = "20:00",
            league = "Serie A",
            homeTeam = "Team E",
            awayTeam = "Team F",
            prediction = "Away win",
            odds = 2.00,
            status = TipStatus.PENDING.name
        )

        val groups = DateGroupingHelper.groupAndSortPredictions(listOf(tip3, tip1, tip2))

        assertEquals(1, groups.size)
        val group = groups[0]
        assertEquals("2026-10-12", group.dateIso)
        assertEquals(3, group.numberOfOdds)
        assertEquals(5.40, group.accumulatedOdds, 0.001)

        val formattedAcc = String.format(Locale.US, "%.2f", group.accumulatedOdds)
        assertEquals("5.40", formattedAcc)

        // Verify kickoff time ordering (15:00, then 18:00, then 20:00)
        assertEquals("15:00", group.tips[0].kickoffTime)
        assertEquals("18:00", group.tips[1].kickoffTime)
        assertEquals("20:00", group.tips[2].kickoffTime)
    }

    @Test
    fun testSinglePredictionDateGroup() {
        val singleTip = TipEntity(
            id = 10,
            category = TipCategory.VIP_2_PLUS.id,
            matchDate = "2026-10-14",
            kickoffTime = "19:00",
            league = "Champions League",
            homeTeam = "Team X",
            awayTeam = "Team Y",
            prediction = "Home win",
            odds = 2.25,
            status = TipStatus.PENDING.name
        )

        val groups = DateGroupingHelper.groupAndSortPredictions(listOf(singleTip))
        assertEquals(1, groups.size)
        val group = groups[0]
        assertEquals(1, group.numberOfOdds)
        assertEquals(2.25, group.accumulatedOdds, 0.001)
    }

    @Test
    fun testChronologicalDatesEarliestFirst() {
        val tipOct14 = TipEntity(
            id = 1,
            category = TipCategory.FREE.id,
            matchDate = "2026-10-14",
            kickoffTime = "12:00",
            league = "L1",
            homeTeam = "A",
            awayTeam = "B",
            prediction = "1",
            odds = 1.5,
            status = TipStatus.PENDING.name
        )
        val tipOct11 = TipEntity(
            id = 2,
            category = TipCategory.FREE.id,
            matchDate = "2026-10-11",
            kickoffTime = "14:00",
            league = "L2",
            homeTeam = "C",
            awayTeam = "D",
            prediction = "2",
            odds = 1.7,
            status = TipStatus.PENDING.name
        )
        val tipOct13 = TipEntity(
            id = 3,
            category = TipCategory.FREE.id,
            matchDate = "2026-10-13",
            kickoffTime = "16:00",
            league = "L3",
            homeTeam = "E",
            awayTeam = "F",
            prediction = "X",
            odds = 1.9,
            status = TipStatus.PENDING.name
        )

        val groups = DateGroupingHelper.groupAndSortPredictions(listOf(tipOct14, tipOct11, tipOct13))
        assertEquals(3, groups.size)
        // Earliest first
        assertEquals("2026-10-11", groups[0].dateIso)
        assertEquals("2026-10-13", groups[1].dateIso)
        assertEquals("2026-10-14", groups[2].dateIso)
    }
}
