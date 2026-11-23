package com.example.billing

import com.example.data.TipCategory
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PackageType

/**
 * The five independent VIP sections in eClassports Football.
 * Matches exact Google Play Subscription products and RevenueCat Offerings:
 *
 * 1. ALL VIPS:
 *    - Google Play ID: all_vip
 *    - Base Plans: all_vip:allvip-weekly, all_vip:allvip-monthly
 *    - RevenueCat Offering: default (display: "all vips", 2 packages)
 *
 * 2. 2+ VIP:
 *    - Google Play ID: vip_2_plus
 *    - Base Plans: vip_2_plus:2plus-weekly, vip_2_plus:2plus-monthly
 *    - RevenueCat Offering: 2+ vip (2 packages)
 *
 * 3. 3+ VIP:
 *    - Google Play ID: vip_3_plus
 *    - Base Plans: vip_3_plus:3plus-weekly, vip_3_plus:3plus-monthly
 *    - RevenueCat Offering: 3+ vip (2 packages)
 *
 * 4. 5+ VIP:
 *    - Google Play ID: vip_5_plus
 *    - Base Plans: vip_5_plus:5plus-weekly, vip_5_plus:5plus-monthly
 *    - RevenueCat Offering: 5 + vip (2 packages)
 *
 * 5. SPECIAL VIP:
 *    - Google Play ID: special_vip
 *    - Base Plans: special_vip:specialvip-weekly, special_vip:specialvip-monthly
 *    - RevenueCat Offering: special vip (2 packages)
 */
enum class VipSection(
    val id: String,
    val cardTitle: String,
    val offeringId: String,
    val offeringAliases: List<String>,
    val entitlementId: String,
    val entitlementAliases: List<String>,
    val storeSubscriptionId: String,
    val weeklyProductId: String,
    val monthlyProductId: String,
    val weeklyBasePlanId: String,
    val monthlyBasePlanId: String,
    val weeklyTitle: String,
    val monthlyTitle: String,
    val tipCategory: TipCategory
) {
    ALL_VIPS(
        id = "all_vips",
        cardTitle = "ALL VIPS",
        offeringId = "default",
        offeringAliases = listOf("default", "all vips", "all_vips", "all vip", "all_vip"),
        entitlementId = "all_vips",
        entitlementAliases = listOf("all_vips", "all_vip", "all vips", "default", "master", "allvip"),
        storeSubscriptionId = "all_vip",
        weeklyProductId = "all_vip:allvip-weekly",
        monthlyProductId = "all_vip:allvip-monthly",
        weeklyBasePlanId = "allvip-weekly",
        monthlyBasePlanId = "allvip-monthly",
        weeklyTitle = "All VIPs Weekly",
        monthlyTitle = "All VIPs Monthly",
        tipCategory = TipCategory.ALL_VIP
    ),
    VIP_2_PLUS(
        id = "2plus_vip",
        cardTitle = "2+ VIP",
        offeringId = "2+ vip",
        offeringAliases = listOf("2+ vip", "2+vip", "2plus_vip", "2plus vip", "vip_2_plus"),
        entitlementId = "2plus_vip",
        entitlementAliases = listOf("2plus_vip", "vip_2_plus", "2+ vip", "2+vip", "2plus", "vip_2plus"),
        storeSubscriptionId = "vip_2_plus",
        weeklyProductId = "vip_2_plus:2plus-weekly",
        monthlyProductId = "vip_2_plus:2plus-monthly",
        weeklyBasePlanId = "2plus-weekly",
        monthlyBasePlanId = "2plus-monthly",
        weeklyTitle = "2+ Weekly",
        monthlyTitle = "2+ Monthly",
        tipCategory = TipCategory.VIP_2_PLUS
    ),
    VIP_3_PLUS(
        id = "3plus_vip",
        cardTitle = "3+ VIP",
        offeringId = "3+ vip",
        offeringAliases = listOf("3+ vip", "3+vip", "3plus_vip", "3plus vip", "vip_3_plus"),
        entitlementId = "3plus_vip",
        entitlementAliases = listOf("3plus_vip", "vip_3_plus", "3+ vip", "3+vip", "3plus", "vip_3plus"),
        storeSubscriptionId = "vip_3_plus",
        weeklyProductId = "vip_3_plus:3plus-weekly",
        monthlyProductId = "vip_3_plus:3plus-monthly",
        weeklyBasePlanId = "3plus-weekly",
        monthlyBasePlanId = "3plus-monthly",
        weeklyTitle = "3+ Weekly",
        monthlyTitle = "3+ Monthly",
        tipCategory = TipCategory.VIP_3_PLUS
    ),
    VIP_5_PLUS(
        id = "5plus_vip",
        cardTitle = "5+ VIP",
        offeringId = "5 + vip",
        offeringAliases = listOf("5 + vip", "5+ vip", "5+vip", "5plus_vip", "5plus vip", "vip_5_plus"),
        entitlementId = "5plus_vip",
        entitlementAliases = listOf("5plus_vip", "vip_5_plus", "5 + vip", "5+ vip", "5+vip", "5plus", "vip_5plus"),
        storeSubscriptionId = "vip_5_plus",
        weeklyProductId = "vip_5_plus:5plus-weekly",
        monthlyProductId = "vip_5_plus:5plus-monthly",
        weeklyBasePlanId = "5plus-weekly",
        monthlyBasePlanId = "5plus-monthly",
        weeklyTitle = "5+ Weekly",
        monthlyTitle = "5+ Monthly",
        tipCategory = TipCategory.VIP_5_PLUS
    ),
    SPECIAL_VIP(
        id = "special_vip",
        cardTitle = "SPECIAL VIP",
        offeringId = "special vip",
        offeringAliases = listOf("special vip", "special_vip", "specialvip"),
        entitlementId = "special_vip",
        entitlementAliases = listOf("special_vip", "special vip", "specialvip", "special"),
        storeSubscriptionId = "special_vip",
        weeklyProductId = "special_vip:specialvip-weekly",
        monthlyProductId = "special_vip:specialvip-monthly",
        weeklyBasePlanId = "specialvip-weekly",
        monthlyBasePlanId = "specialvip-monthly",
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
                    it.offeringId.equals(id, ignoreCase = true) ||
                    it.storeSubscriptionId.equals(id, ignoreCase = true) ||
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
     * Standard fallback plans for each section when offline or before offerings load.
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
                accessDescription = "Access to ${section.cardTitle} for 7 days. Renews automatically unless cancelled.",
                priceFormatted = weeklyPrice,
                periodFormatted = "/week"
            ),
            SectionPlan(
                section = section,
                duration = PlanDuration.MONTHLY,
                productId = section.monthlyProductId,
                planTitle = section.monthlyTitle,
                durationLabel = "1 Month Access",
                accessDescription = "Access to ${section.cardTitle} for 1 month. Renews automatically unless cancelled.",
                priceFormatted = monthlyPrice,
                periodFormatted = "/month"
            )
        )
    }

    /**
     * Maps RevenueCat Offerings to SectionPlans for each of the five VIP sections.
     */
    fun mapOfferingsToSectionPlans(offerings: Offerings): Map<VipSection, List<SectionPlan>> {
        val result = mutableMapOf<VipSection, MutableList<SectionPlan>>()
        for (section in VipSection.entries) {
            result[section] = createDefaultPlansForSection(section).toMutableList()
        }

        val allPackagesAcrossOfferings = offerings.all.values.flatMap { it.availablePackages }

        for (section in VipSection.entries) {
            // Find offering using exact offering ID, aliases, or display name (serverDescription)
            var offering = offerings.getOffering(section.offeringId)
            if (offering == null) {
                for (alias in section.offeringAliases) {
                    val normAlias = normalize(alias)
                    offering = offerings.getOffering(alias) ?: offerings.all.entries.firstOrNull { (key, off) ->
                        key.equals(alias, ignoreCase = true) ||
                                normalize(key) == normAlias ||
                                normalize(off.serverDescription) == normAlias ||
                                off.serverDescription.contains(alias, ignoreCase = true)
                    }?.value
                    if (offering != null) break
                }
            }

            // For ALL_VIPS, fallback to default/current offering
            if (offering == null && section == VipSection.ALL_VIPS) {
                offering = offerings.current ?: offerings.all.values.firstOrNull()
            }

            val packages = offering?.availablePackages?.takeIf { it.isNotEmpty() }
                ?: findPackagesForSection(section, allPackagesAcrossOfferings)

            var weeklyPkg: Package? = null
            var monthlyPkg: Package? = null

            for (pkg in packages) {
                val id = pkg.identifier.lowercase()
                val prodId = pkg.product.id.lowercase()
                val normId = normalize(id)
                val normProdId = normalize(prodId)
                val normWeeklyBase = normalize(section.weeklyBasePlanId)
                val normMonthlyBase = normalize(section.monthlyBasePlanId)

                val isWeekly = pkg.packageType == PackageType.WEEKLY ||
                        id == "\$rc_weekly" ||
                        id == "weekly" ||
                        id.contains("weekly") ||
                        (id.contains("week") && !id.contains("month")) ||
                        normProdId.contains(normWeeklyBase) ||
                        normId.contains(normWeeklyBase)

                val isMonthly = pkg.packageType == PackageType.MONTHLY ||
                        id == "\$rc_monthly" ||
                        id == "monthly" ||
                        id.contains("monthly") ||
                        (id.contains("month") && !id.contains("week")) ||
                        normProdId.contains(normMonthlyBase) ||
                        normId.contains(normMonthlyBase)

                if (isWeekly && weeklyPkg == null) {
                    weeklyPkg = pkg
                }
                if (isMonthly && monthlyPkg == null) {
                    monthlyPkg = pkg
                }
            }

            // Fallback order assignment if package type wasn't explicitly tagged
            if (weeklyPkg == null && packages.isNotEmpty()) {
                weeklyPkg = packages.getOrNull(0)
            }
            if (monthlyPkg == null && packages.size > 1) {
                monthlyPkg = packages.getOrNull(1)
            }

            val sectionList = mutableListOf<SectionPlan>()

            if (weeklyPkg != null) {
                sectionList.add(
                    SectionPlan(
                        section = section,
                        duration = PlanDuration.WEEKLY,
                        productId = weeklyPkg.product.id,
                        planTitle = section.weeklyTitle,
                        durationLabel = "7 Days Access",
                        accessDescription = "Access to ${section.cardTitle} for 7 days. Renews automatically unless cancelled.",
                        priceFormatted = weeklyPkg.product.price.formatted,
                        periodFormatted = "/week",
                        rcPackage = weeklyPkg
                    )
                )
            } else {
                sectionList.add(createDefaultPlansForSection(section).first { it.duration == PlanDuration.WEEKLY })
            }

            if (monthlyPkg != null) {
                sectionList.add(
                    SectionPlan(
                        section = section,
                        duration = PlanDuration.MONTHLY,
                        productId = monthlyPkg.product.id,
                        planTitle = section.monthlyTitle,
                        durationLabel = "1 Month Access",
                        accessDescription = "Access to ${section.cardTitle} for 1 month. Renews automatically unless cancelled.",
                        priceFormatted = monthlyPkg.product.price.formatted,
                        periodFormatted = "/month",
                        rcPackage = monthlyPkg
                    )
                )
            } else {
                sectionList.add(createDefaultPlansForSection(section).first { it.duration == PlanDuration.MONTHLY })
            }

            result[section] = sectionList
        }

        return result
    }

    private fun findPackagesForSection(section: VipSection, packages: List<Package>): List<Package> {
        val keywords = listOf(
            section.storeSubscriptionId,
            section.weeklyBasePlanId,
            section.monthlyBasePlanId,
            section.id
        ).map { normalize(it) }

        return packages.filter { pkg ->
            val normId = normalize(pkg.identifier)
            val normProdId = normalize(pkg.product.id)
            keywords.any { kw -> normId.contains(kw) || normProdId.contains(kw) } ||
                    (section == VipSection.ALL_VIPS && (normId.contains("all") || normProdId.contains("all") || normId.contains("default")))
        }
    }

    private fun normalize(str: String): String {
        return str.lowercase()
            .replace(" ", "")
            .replace("_", "")
            .replace("-", "")
            .replace("+", "")
            .replace(":", "")
    }
}
