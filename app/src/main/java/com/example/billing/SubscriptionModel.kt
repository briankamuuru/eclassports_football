package com.example.billing

import com.example.data.TipCategory
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PackageType

/**
 * The five independent VIP sections in eClassports Football.
 * Each section has its own independent subscription and entitlement.
 */
enum class VipSection(
    val id: String,
    val cardTitle: String,
    val entitlementId: String,
    val weeklyProductId: String,
    val monthlyProductId: String,
    val weeklyTitle: String,
    val monthlyTitle: String,
    val tipCategory: TipCategory
) {
    ALL_VIPS(
        id = "all_vips",
        cardTitle = "ALL VIPS",
        entitlementId = "all_vips",
        weeklyProductId = "all_vips_weekly",
        monthlyProductId = "all_vips_monthly",
        weeklyTitle = "All VIPs Weekly",
        monthlyTitle = "All VIPs Monthly",
        tipCategory = TipCategory.ALL_VIP
    ),
    VIP_2_PLUS(
        id = "2plus_vip",
        cardTitle = "2+ VIP",
        entitlementId = "2plus_vip",
        weeklyProductId = "2plus_vip_weekly",
        monthlyProductId = "2plus_vip_monthly",
        weeklyTitle = "2+ Weekly",
        monthlyTitle = "2+ Monthly",
        tipCategory = TipCategory.VIP_2_PLUS
    ),
    VIP_3_PLUS(
        id = "3plus_vip",
        cardTitle = "3+ VIP",
        entitlementId = "3plus_vip",
        weeklyProductId = "3plus_vip_weekly",
        monthlyProductId = "3plus_vip_monthly",
        weeklyTitle = "3+ Weekly",
        monthlyTitle = "3+ Monthly",
        tipCategory = TipCategory.VIP_3_PLUS
    ),
    VIP_5_PLUS(
        id = "5plus_vip",
        cardTitle = "5+ VIP",
        entitlementId = "5plus_vip",
        weeklyProductId = "5plus_vip_weekly",
        monthlyProductId = "5plus_vip_monthly",
        weeklyTitle = "5+ Weekly",
        monthlyTitle = "5+ Monthly",
        tipCategory = TipCategory.VIP_5_PLUS
    ),
    SPECIAL_VIP(
        id = "special_vip",
        cardTitle = "SPECIAL VIP",
        entitlementId = "special_vip",
        weeklyProductId = "special_vip_weekly",
        monthlyProductId = "special_vip_monthly",
        weeklyTitle = "Special Weekly",
        monthlyTitle = "Special Monthly",
        tipCategory = TipCategory.VIP_SPECIAL
    );

    companion object {
        fun fromTipCategory(category: TipCategory): VipSection? = when (category) {
            TipCategory.ALL_VIP -> ALL_VIPS
            TipCategory.VIP_2_PLUS -> VIP_2_PLUS
            TipCategory.VIP_3_PLUS -> VIP_3_PLUS
            TipCategory.VIP_5_PLUS -> VIP_5_PLUS
            TipCategory.VIP_SPECIAL -> SPECIAL_VIP
            TipCategory.FREE -> null
        }

        fun fromId(id: String): VipSection? = entries.firstOrNull {
            it.id.equals(id, ignoreCase = true) ||
                    it.entitlementId.equals(id, ignoreCase = true) ||
                    it.name.equals(id, ignoreCase = true)
        }
    }
}

enum class PlanDuration {
    WEEKLY,
    MONTHLY
}

/**
 * Represents a weekly or monthly subscription plan for a specific VIP section.
 */
data class SectionPlan(
    val section: VipSection,
    val duration: PlanDuration,
    val productId: String,
    val planTitle: String,
    val durationLabel: String,
    val accessDescription: String,
    val priceFormatted: String,
    val periodFormatted: String,
    val rcPackage: Package? = null
)

/**
 * Global subscription state tracking independent access for each VIP section.
 */
data class SubscriptionState(
    val unlockedSections: Set<VipSection> = emptySet(),
    val sectionExpirations: Map<VipSection, String> = emptyMap(),
    val sectionPlans: Map<VipSection, List<SectionPlan>> = emptyMap(),
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val isConfigured: Boolean = false,
    val isSandboxOrDemo: Boolean = false
) {
    val isVip: Boolean get() = unlockedSections.isNotEmpty()

    fun isSectionUnlocked(section: VipSection): Boolean = unlockedSections.contains(section)
}

object DefaultSectionPlans {
    /**
     * Standard default plans for each section when RevenueCat offerings are not yet published.
     */
    fun createDefaultPlansForSection(section: VipSection): List<SectionPlan> {
        val (weeklyPrice, monthlyPrice) = when (section) {
            VipSection.ALL_VIPS -> "$9.99" to "$29.99"
            VipSection.VIP_2_PLUS -> "$4.99" to "$14.99"
            VipSection.VIP_3_PLUS -> "$5.99" to "$16.99"
            VipSection.VIP_5_PLUS -> "$6.99" to "$19.99"
            VipSection.SPECIAL_VIP -> "$7.99" to "$24.99"
        }

        return listOf(
            SectionPlan(
                section = section,
                duration = PlanDuration.WEEKLY,
                productId = section.weeklyProductId,
                planTitle = section.weeklyTitle,
                durationLabel = "7 Days Access",
                accessDescription = "Access to ${section.cardTitle} for 7 days. Expires automatically unless renewed.",
                priceFormatted = weeklyPrice,
                periodFormatted = "/week"
            ),
            SectionPlan(
                section = section,
                duration = PlanDuration.MONTHLY,
                productId = section.monthlyProductId,
                planTitle = section.monthlyTitle,
                durationLabel = "1 Month Access",
                accessDescription = "Access to ${section.cardTitle} for 1 month. Expires automatically unless renewed.",
                priceFormatted = monthlyPrice,
                periodFormatted = "/month"
            )
        )
    }

    /**
     * Maps RevenueCat packages to SectionPlans for each of the five VIP sections.
     */
    fun mapOfferingsToSectionPlans(packages: List<Package>): Map<VipSection, List<SectionPlan>> {
        val result = mutableMapOf<VipSection, MutableList<SectionPlan>>()
        for (section in VipSection.entries) {
            result[section] = createDefaultPlansForSection(section).toMutableList()
        }

        for (pkg in packages) {
            val prodId = pkg.product.id.lowercase()
            for (section in VipSection.entries) {
                val matchesWeekly = prodId == section.weeklyProductId.lowercase() ||
                        (prodId.contains(section.id.lowercase()) && pkg.packageType == PackageType.WEEKLY)
                val matchesMonthly = prodId == section.monthlyProductId.lowercase() ||
                        (prodId.contains(section.id.lowercase()) && pkg.packageType == PackageType.MONTHLY)

                if (matchesWeekly) {
                    val list = result[section] ?: mutableListOf()
                    val idx = list.indexOfFirst { it.duration == PlanDuration.WEEKLY }
                    val updated = SectionPlan(
                        section = section,
                        duration = PlanDuration.WEEKLY,
                        productId = pkg.product.id,
                        planTitle = section.weeklyTitle,
                        durationLabel = "7 Days Access",
                        accessDescription = "Access to ${section.cardTitle} for 7 days. Expires automatically unless renewed.",
                        priceFormatted = pkg.product.price.formatted,
                        periodFormatted = "/week",
                        rcPackage = pkg
                    )
                    if (idx >= 0) list[idx] = updated else list.add(updated)
                } else if (matchesMonthly) {
                    val list = result[section] ?: mutableListOf()
                    val idx = list.indexOfFirst { it.duration == PlanDuration.MONTHLY }
                    val updated = SectionPlan(
                        section = section,
                        duration = PlanDuration.MONTHLY,
                        productId = pkg.product.id,
                        planTitle = section.monthlyTitle,
                        durationLabel = "1 Month Access",
                        accessDescription = "Access to ${section.cardTitle} for 1 month. Expires automatically unless renewed.",
                        priceFormatted = pkg.product.price.formatted,
                        periodFormatted = "/month",
                        rcPackage = pkg
                    )
                    if (idx >= 0) list[idx] = updated else list.add(updated)
                }
            }
        }

        return result
    }
}
