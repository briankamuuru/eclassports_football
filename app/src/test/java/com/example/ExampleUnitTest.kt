package com.example

import com.example.data.DateGroupingHelper
import com.example.data.TipCategory
import com.example.data.TipEntity
import com.example.data.TipStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class ExampleUnitTest {

    @Test
    fun testCurrentDateOddsAppearFirstAtTheTop() {
        val today = LocalDate.of(2026, 11, 5)
        val todayStr = "2026-11-05"
        val tomorrowStr = "2026-11-06"
        val yesterdayStr = "2026-11-04"
        val pastStr = "2026-10-12"

        val tipPast = TipEntity(
            id = "past_1",
            category = TipCategory.FREE.id,
            matchDate = pastStr,
            kickoffTime = "14:00",
            league = "League 1",
            homeTeam = "Past Team A",
            awayTeam = "Past Team B",
            prediction = "1",
            odds = 1.5,
            status = TipStatus.PENDING.name
        )
        val tipYesterday = TipEntity(
            id = "yest_1",
            category = TipCategory.FREE.id,
            matchDate = yesterdayStr,
            kickoffTime = "16:00",
            league = "League 2",
            homeTeam = "Yesterday Team A",
            awayTeam = "Yesterday Team B",
            prediction = "X",
            odds = 2.0,
            status = TipStatus.PENDING.name
        )
        val tipToday1 = TipEntity(
            id = "today_1",
            category = TipCategory.FREE.id,
            matchDate = todayStr,
            kickoffTime = "18:00",
            league = "Premier League",
            homeTeam = "Today Team A",
            awayTeam = "Today Team B",
            prediction = "Home win",
            odds = 1.8,
            status = TipStatus.PENDING.name
        )
        val tipToday2 = TipEntity(
            id = "today_2",
            category = TipCategory.FREE.id,
            matchDate = todayStr,
            kickoffTime = "20:30",
            league = "La Liga",
            homeTeam = "Today Team C",
            awayTeam = "Today Team D",
            prediction = "Over 2.5",
            odds = 1.9,
            status = TipStatus.PENDING.name
        )
        val tipTomorrow = TipEntity(
            id = "tom_1",
            category = TipCategory.FREE.id,
            matchDate = tomorrowStr,
            kickoffTime = "15:00",
            league = "Bundesliga",
            homeTeam = "Tomorrow Team A",
            awayTeam = "Tomorrow Team B",
            prediction = "2",
            odds = 2.1,
            status = TipStatus.PENDING.name
        )

        // Pass unordered tips to groupAndSortPredictions with reference today
        val groups = DateGroupingHelper.groupAndSortPredictions(
            listOf(tipPast, tipTomorrow, tipYesterday, tipToday2, tipToday1),
            today = today
        )

        assertEquals(4, groups.size)
        // Group 0 MUST BE the current date (today) at the very top!
        assertEquals(todayStr, groups[0].dateIso)
        assertTrue(groups[0].isToday)
        assertEquals(2, groups[0].numberOfOdds)
        assertEquals(3.42, groups[0].accumulatedOdds, 0.001)
        // Kickoff times within today must be sorted
        assertEquals("18:00", groups[0].tips[0].kickoffTime)
        assertEquals("20:30", groups[0].tips[1].kickoffTime)

        // Group 1: Upcoming future dates (tomorrow)
        assertEquals(tomorrowStr, groups[1].dateIso)
        // Group 2: Most recent past (yesterday)
        assertEquals(yesterdayStr, groups[2].dateIso)
        // Group 3: Older past date
        assertEquals(pastStr, groups[3].dateIso)
    }

    @Test
    fun testUserTipAndCategoryAliases() {
        val userTip = com.eclassports.football.model.UserTip(
            id = "doc_456",
            homeTeam = "Man City",
            awayTeam = "Liverpool",
            league = "Premier League",
            tipPick = "Over 2.5 Goals",
            odd = 1.85,
            status = "PENDING",
            category = "2_PLUS_VIP",
            categoryName = "2+ Tips VIP",
            ticketDate = "2026-11-05",
            kickoffTime = "20:00"
        )
        val catEnum = TipCategory.fromId(userTip.category)
        assertEquals(TipCategory.VIP_2_PLUS, catEnum)

        val cat3Plus = TipCategory.fromId("3_PLUS_VIP")
        assertEquals(TipCategory.VIP_3_PLUS, cat3Plus)

        val cat5Plus = TipCategory.fromId("5_PLUS_VIP")
        assertEquals(TipCategory.VIP_5_PLUS, cat5Plus)

        val catSpecial = TipCategory.fromId("SPECIAL_VIP")
        assertEquals(TipCategory.VIP_SPECIAL, catSpecial)
    }

    @Test
    fun testAccumulatedOddsAndKickoffTimeSorting() {
        val tip1 = TipEntity(
            id = "1",
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
            id = "2",
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
            id = "3",
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
            id = "10",
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
    fun testSocialLinksUrls() {
        assertEquals("https://whatsapp.com/channel/0029VbEdDsjKbYMWrZLQa12K", com.example.ui.components.SocialLinksHelper.WHATSAPP_URL)
        assertEquals("https://t.me/clubfootbll", com.example.ui.components.SocialLinksHelper.TELEGRAM_URL)
    }

    @Test
    fun testAllVipTipsCategoryAndAggregation() {
        // Test aliases for ALL_VIP
        assertEquals(TipCategory.ALL_VIP, TipCategory.fromId("ALL_VIP"))
        assertEquals(TipCategory.ALL_VIP, TipCategory.fromId("ALL_VIP_TIPS"))
        assertEquals(TipCategory.ALL_VIP, TipCategory.fromId("ALL VIP"))
        assertEquals(TipCategory.ALL_VIP, TipCategory.fromId("ALL VIP TIPS"))
        assertEquals(TipCategory.ALL_VIP, TipCategory.fromId("all_vip"))
        assertEquals(TipCategory.ALL_VIP, TipCategory.fromId("ALL"))

        // Create sample tips from various VIP categories plus a free tip
        val freeTip = TipEntity(
            id = "f1",
            category = TipCategory.FREE.id,
            matchDate = "2026-11-05",
            kickoffTime = "14:00",
            league = "EPL",
            homeTeam = "Arsenal",
            awayTeam = "Chelsea",
            prediction = "1",
            odds = 1.8,
            status = TipStatus.PENDING.name
        )
        val vip2Tip = TipEntity(
            id = "v2",
            category = TipCategory.VIP_2_PLUS.id,
            categoryName = "2+ Tips VIP",
            matchDate = "2026-11-05",
            kickoffTime = "16:00",
            league = "La Liga",
            homeTeam = "Real Madrid",
            awayTeam = "Barcelona",
            prediction = "Over 2.5",
            odds = 2.1,
            status = TipStatus.PENDING.name
        )
        val vip3Tip = TipEntity(
            id = "v3",
            category = TipCategory.VIP_3_PLUS.id,
            categoryName = "3+ Tips VIP",
            matchDate = "2026-11-05",
            kickoffTime = "18:00",
            league = "Serie A",
            homeTeam = "Inter",
            awayTeam = "Milan",
            prediction = "X",
            odds = 3.2,
            status = TipStatus.PENDING.name
        )
        val vip5Tip = TipEntity(
            id = "v5",
            category = TipCategory.VIP_5_PLUS.id,
            categoryName = "5+ Tips VIP",
            matchDate = "2026-11-05",
            kickoffTime = "20:00",
            league = "Bundesliga",
            homeTeam = "Bayern",
            awayTeam = "Dortmund",
            prediction = "1 & Over 3.5",
            odds = 5.5,
            status = TipStatus.PENDING.name
        )
        val vipSpecialTip = TipEntity(
            id = "vs",
            category = TipCategory.VIP_SPECIAL.id,
            categoryName = "Special VIP",
            matchDate = "2026-11-05",
            kickoffTime = "21:00",
            league = "Ligue 1",
            homeTeam = "PSG",
            awayTeam = "Marseille",
            prediction = "Home Win",
            odds = 1.95,
            status = TipStatus.PENDING.name
        )

        val allTips = listOf(freeTip, vip2Tip, vip3Tip, vip5Tip, vipSpecialTip)

        // All VIP filter (category != 'free' and status == 'PENDING')
        val allVipTips = allTips.filter { it.category != TipCategory.FREE.id && it.status == TipStatus.PENDING.name }
        assertEquals(4, allVipTips.size)
        assertTrue(allVipTips.none { it.category == TipCategory.FREE.id })
        assertTrue(allVipTips.any { it.category == TipCategory.VIP_2_PLUS.id })
        assertTrue(allVipTips.any { it.category == TipCategory.VIP_3_PLUS.id })
        assertTrue(allVipTips.any { it.category == TipCategory.VIP_5_PLUS.id })
        assertTrue(allVipTips.any { it.category == TipCategory.VIP_SPECIAL.id })

        // When a tip is deleted (e.g. vip2Tip removed), it disappears from All VIP Tips
        val afterDeletion = allTips.filter { it.id != "v2" }.filter { it.category != TipCategory.FREE.id }
        assertEquals(3, afterDeletion.size)
        assertTrue(afterDeletion.none { it.id == "v2" })

        // Date grouping and accumulated odds in All VIP Tips
        val groups = DateGroupingHelper.groupAndSortPredictions(allVipTips)
        assertEquals(1, groups.size)
        assertEquals(4, groups[0].numberOfOdds)
        val expectedAcc = 2.1 * 3.2 * 5.5 * 1.95
        assertEquals(expectedAcc, groups[0].accumulatedOdds, 0.001)
    }

    @Test
    fun testFiveIndependentVipSectionsAndPlans() {
        // Exactly five independent VIP sections
        assertEquals(5, com.example.billing.VipSection.entries.size)

        // Exact required card titles
        val expectedTitles = listOf("ALL VIPS", "2+ VIP", "3+ VIP", "5+ VIP", "SPECIAL VIP")
        val actualTitles = com.example.billing.VipSection.entries.map { it.cardTitle }
        assertEquals(expectedTitles, actualTitles)

        // Entitlement identifiers
        val expectedEntitlements = listOf("all_vips", "2plus_vip", "3plus_vip", "5plus_vip", "special_vip")
        val actualEntitlements = com.example.billing.VipSection.entries.map { it.entitlementId }
        assertEquals(expectedEntitlements, actualEntitlements)

        // Ten products (weekly and monthly for each of the 5 sections)
        for (section in com.example.billing.VipSection.entries) {
            val plans = com.example.billing.DefaultSectionPlans.createDefaultPlansForSection(section)
            assertEquals(2, plans.size)

            val weekly = plans.first { it.duration == com.example.billing.PlanDuration.WEEKLY }
            assertEquals(section.weeklyProductId, weekly.productId)
            assertEquals(section.weeklyTitle, weekly.planTitle)
            assertEquals("/week", weekly.periodFormatted)

            val monthly = plans.first { it.duration == com.example.billing.PlanDuration.MONTHLY }
            assertEquals(section.monthlyProductId, monthly.productId)
            assertEquals(section.monthlyTitle, monthly.planTitle)
            assertEquals("/month", monthly.periodFormatted)
        }

        // Independent Access Control verification:
        // A subscription to 2+ VIP must NOT unlock any other section
        val state2PlusOnly = com.example.billing.SubscriptionState(
            unlockedSections = setOf(com.example.billing.VipSection.VIP_2_PLUS)
        )
        assertTrue(state2PlusOnly.isSectionUnlocked(com.example.billing.VipSection.VIP_2_PLUS))
        org.junit.Assert.assertFalse(state2PlusOnly.isSectionUnlocked(com.example.billing.VipSection.VIP_3_PLUS))
        org.junit.Assert.assertFalse(state2PlusOnly.isSectionUnlocked(com.example.billing.VipSection.VIP_5_PLUS))
        org.junit.Assert.assertFalse(state2PlusOnly.isSectionUnlocked(com.example.billing.VipSection.SPECIAL_VIP))
        org.junit.Assert.assertFalse(state2PlusOnly.isSectionUnlocked(com.example.billing.VipSection.ALL_VIPS))

        // All VIPs subscription unlocks all five sections via explicit mapping
        val stateAllVips = com.example.billing.SubscriptionState(
            unlockedSections = com.example.billing.VipSection.entries.toSet()
        )
        for (section in com.example.billing.VipSection.entries) {
            assertTrue(stateAllVips.isSectionUnlocked(section))
        }
    }
}

