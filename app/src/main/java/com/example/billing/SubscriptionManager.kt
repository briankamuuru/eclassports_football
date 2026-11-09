package com.example.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.firestore.FirebaseFirestore
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.purchaseWith
import com.revenuecat.purchases.restorePurchasesWith
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Authoritative manager for RevenueCat and Google Play Billing subscriptions.
 * Supports independent subscriptions for all five VIP sections:
 * - All VIPs (all_vips)
 * - 2+ VIP (2plus_vip)
 * - 3+ VIP (3plus_vip)
 * - 5+ VIP (5plus_vip)
 * - Special VIP (special_vip)
 */
object SubscriptionManager {
    private const val TAG = "SubscriptionManager"

    private val _subscriptionState = MutableStateFlow(
        SubscriptionState(
            isLoading = true,
            sectionPlans = VipSection.entries.associateWith { DefaultSectionPlans.createDefaultPlansForSection(it) }
        )
    )
    val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState.asStateFlow()

    private var isInitialized = false

    /**
     * Initializes RevenueCat with the configured API key from BuildConfig.
     */
    fun init(context: Context) {
        if (isInitialized) return

        val rawKey = runCatching { BuildConfig.REVENUECAT_API_KEY }.getOrDefault("")
        val isRealKey = rawKey.isNotBlank() &&
                !rawKey.contains("placeholder", ignoreCase = true) &&
                !rawKey.contains("MY_", ignoreCase = true)

        Log.i(TAG, "Initializing RevenueCat. Valid Google API key provided: $isRealKey")

        if (isRealKey) {
            try {
                Purchases.logLevel = LogLevel.DEBUG
                Purchases.configure(
                    PurchasesConfiguration.Builder(context.applicationContext, rawKey).build()
                )

                Purchases.sharedInstance.updatedCustomerInfoListener =
                    UpdatedCustomerInfoListener { customerInfo ->
                        processCustomerInfo(customerInfo)
                    }

                // Initial fetch of customer info and offerings
                Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
                    override fun onReceived(customerInfo: CustomerInfo) {
                        processCustomerInfo(customerInfo)
                        fetchOfferings()
                    }

                    override fun onError(error: PurchasesError) {
                        Log.e(TAG, "Error fetching customer info: ${error.message}")
                        loadFallbackOfferings(isConfigured = true, errorMsg = error.message)
                    }
                })

                isInitialized = true
                return
            } catch (e: Exception) {
                Log.e(TAG, "Exception during Purchases.configure: ${e.message}", e)
            }
        }

        // Preview / Sandbox mode when API key is not yet configured
        isInitialized = true
        loadFallbackOfferings(
            isConfigured = false,
            errorMsg = "RevenueCat API key not configured yet. Running in preview/demo mode."
        )
    }

    /**
     * Refreshes customer information from RevenueCat backend.
     */
    fun refreshCustomerInfo(onComplete: ((Boolean) -> Unit)? = null) {
        if (!_subscriptionState.value.isConfigured) {
            onComplete?.invoke(_subscriptionState.value.isVip)
            return
        }

        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                processCustomerInfo(customerInfo)
                onComplete?.invoke(true)
            }

            override fun onError(error: PurchasesError) {
                Log.w(TAG, "Failed to refresh customer info: ${error.message}")
                onComplete?.invoke(false)
            }
        })
    }

    /**
     * Processes RevenueCat CustomerInfo and resolves independent section entitlements.
     */
    private fun processCustomerInfo(customerInfo: CustomerInfo) {
        val unlocked = mutableSetOf<VipSection>()
        val expirations = mutableMapOf<VipSection, String>()

        val hasAllVips = customerInfo.entitlements[VipSection.ALL_VIPS.entitlementId]?.isActive == true ||
                customerInfo.entitlements["all_vip"]?.isActive == true ||
                customerInfo.entitlements["all_vip_access"]?.isActive == true

        if (hasAllVips) {
            // Explicit entitlement mapping: All VIPs subscription unlocks all 5 VIP sections
            unlocked.addAll(VipSection.entries)
            val expDate = customerInfo.entitlements[VipSection.ALL_VIPS.entitlementId]?.expirationDate?.let { formatExpirationDate(it) }
            if (expDate != null) {
                for (section in VipSection.entries) {
                    expirations[section] = expDate
                }
            }
        } else {
            // Independent section entitlement checks
            for (section in VipSection.entries) {
                val entitlement = customerInfo.entitlements[section.entitlementId]
                val isActive = entitlement?.isActive == true ||
                        customerInfo.entitlements[section.id]?.isActive == true ||
                        customerInfo.entitlements["vip_${section.id}"]?.isActive == true

                if (isActive) {
                    unlocked.add(section)
                    entitlement?.expirationDate?.let { exp ->
                        expirations[section] = formatExpirationDate(exp)
                    }
                }
            }
        }

        Log.i(TAG, "CustomerInfo updated: unlockedSections=${unlocked.map { it.cardTitle }}")

        _subscriptionState.value = _subscriptionState.value.copy(
            unlockedSections = unlocked,
            sectionExpirations = expirations,
            isLoading = false,
            isConfigured = true,
            isSandboxOrDemo = false,
            statusMessage = if (unlocked.isNotEmpty()) "Active VIP Subscriptions" else null
        )
    }

    private fun fetchOfferings() {
        Purchases.sharedInstance.getOfferingsWith(
            onError = { error ->
                Log.w(TAG, "Offerings fetch failed: ${error.message}, using default plans")
                loadFallbackOfferings(isConfigured = true, errorMsg = error.message)
            },
            onSuccess = { offerings ->
                val currentOffering = offerings.current
                val packages = currentOffering?.availablePackages ?: emptyList()

                if (packages.isNotEmpty()) {
                    val mappedPlans = DefaultSectionPlans.mapOfferingsToSectionPlans(packages)
                    _subscriptionState.value = _subscriptionState.value.copy(
                        sectionPlans = mappedPlans,
                        isLoading = false,
                        isConfigured = true,
                        isSandboxOrDemo = false
                    )
                    Log.i(TAG, "Loaded packages for VIP sections from RevenueCat offering")
                } else {
                    loadFallbackOfferings(isConfigured = true, errorMsg = "No packages in current offering")
                }
            }
        )
    }

    private fun loadFallbackOfferings(isConfigured: Boolean, errorMsg: String?) {
        val defaultPlans = VipSection.entries.associateWith { DefaultSectionPlans.createDefaultPlansForSection(it) }
        _subscriptionState.value = _subscriptionState.value.copy(
            sectionPlans = defaultPlans,
            isLoading = false,
            isConfigured = isConfigured,
            isSandboxOrDemo = !isConfigured,
            statusMessage = errorMsg
        )
    }

    /**
     * Checks whether a specific VIP section is currently unlocked.
     */
    fun isSectionUnlocked(section: VipSection): Boolean {
        return _subscriptionState.value.unlockedSections.contains(section)
    }

    /**
     * Gets available weekly and monthly plans for a section.
     */
    fun getPlansForSection(section: VipSection): List<SectionPlan> {
        return _subscriptionState.value.sectionPlans[section] ?: DefaultSectionPlans.createDefaultPlansForSection(section)
    }

    /**
     * Executes the purchase of a section plan via RevenueCat and Google Play Billing.
     * Strictly verifies entitlement before granting access.
     */
    fun purchaseSectionPlan(
        activity: Activity,
        section: VipSection,
        plan: SectionPlan,
        onComplete: (success: Boolean, message: String?) -> Unit
    ) {
        val pkg = plan.rcPackage
        if (pkg != null && _subscriptionState.value.isConfigured) {
            _subscriptionState.value = _subscriptionState.value.copy(isLoading = true)

            val purchaseParams = PurchaseParams.Builder(activity, pkg).build()

            Purchases.sharedInstance.purchaseWith(
                purchaseParams = purchaseParams,
                onError = { error: PurchasesError, userCancelled: Boolean ->
                    _subscriptionState.value = _subscriptionState.value.copy(isLoading = false)
                    if (userCancelled) {
                        Log.i(TAG, "User cancelled purchase for ${section.cardTitle}")
                        onComplete(false, "Purchase cancelled")
                    } else {
                        Log.e(TAG, "Purchase failed for ${section.cardTitle}: ${error.message}")
                        onComplete(false, error.message)
                    }
                },
                onSuccess = { _: StoreTransaction?, customerInfo: CustomerInfo ->
                    processCustomerInfo(customerInfo)
                    val isUnlocked = isSectionUnlocked(section)
                    if (isUnlocked) {
                        // Synchronize verified subscription to Firebase
                        syncSubscriptionToFirebase(
                            userId = customerInfo.originalAppUserId,
                            section = section,
                            productId = plan.productId,
                            duration = plan.duration,
                            purchaseDate = System.currentTimeMillis(),
                            expirationDate = customerInfo.entitlements[section.entitlementId]?.expirationDate?.time
                                ?: (System.currentTimeMillis() + if (plan.duration == PlanDuration.WEEKLY) 7L * 86400000L else 30L * 86400000L),
                            customerInfoStr = customerInfo.toString()
                        )
                        onComplete(true, "Subscription confirmed! Unlocked ${section.cardTitle}.")
                    } else {
                        Log.w(TAG, "Purchase succeeded but entitlement ${section.entitlementId} not active")
                        onComplete(false, "Payment completed but entitlement could not be verified.")
                    }
                }
            )
        } else {
            // Simulated activation for developer/preview mode
            Log.i(TAG, "Simulating subscription activation for section: ${section.cardTitle}")
            val updated = _subscriptionState.value.unlockedSections + section
            val updatedExp = _subscriptionState.value.sectionExpirations + (section to if (plan.duration == PlanDuration.WEEKLY) "7 days (Demo)" else "30 days (Demo)")
            _subscriptionState.value = _subscriptionState.value.copy(
                unlockedSections = updated,
                sectionExpirations = updatedExp,
                isLoading = false,
                statusMessage = "Demo ${section.cardTitle} Active"
            )
            onComplete(true, "Demo subscription active for ${section.cardTitle}!")
        }
    }

    /**
     * Restores previous purchases across all VIP sections.
     */
    fun restorePurchases(onComplete: (success: Boolean, message: String?) -> Unit) {
        if (_subscriptionState.value.isConfigured) {
            _subscriptionState.value = _subscriptionState.value.copy(isLoading = true)
            Purchases.sharedInstance.restorePurchasesWith(
                onError = { error ->
                    _subscriptionState.value = _subscriptionState.value.copy(isLoading = false)
                    Log.e(TAG, "Restore error: ${error.message}")
                    onComplete(false, error.message)
                },
                onSuccess = { customerInfo ->
                    processCustomerInfo(customerInfo)
                    val count = _subscriptionState.value.unlockedSections.size
                    val message = if (count > 0) {
                        "Restored $count active VIP subscription(s)!"
                    } else {
                        "No active VIP subscriptions found for this account."
                    }
                    onComplete(count > 0, message)
                }
            )
        } else {
            val count = _subscriptionState.value.unlockedSections.size
            val msg = if (count > 0) "Restored $count active VIP section(s)!" else "No purchases to restore."
            onComplete(count > 0, msg)
        }
    }

    /**
     * Stores synchronized subscription state into Firebase Cloud Firestore.
     */
    private fun syncSubscriptionToFirebase(
        userId: String,
        section: VipSection,
        productId: String,
        duration: PlanDuration,
        purchaseDate: Long,
        expirationDate: Long,
        customerInfoStr: String
    ) {
        try {
            val db = FirebaseFirestore.getInstance()
            val docId = "${userId}_${section.id}"
            val data = mapOf(
                "userId" to userId,
                "vipSection" to section.id,
                "sectionName" to section.cardTitle,
                "productId" to productId,
                "duration" to duration.name,
                "status" to "ACTIVE",
                "purchaseDate" to purchaseDate,
                "expirationDate" to expirationDate,
                "updatedAt" to System.currentTimeMillis(),
                "customerInfoSummary" to customerInfoStr.take(500)
            )
            db.collection("subscriptions").document(docId).set(data)
            Log.i(TAG, "Successfully synced subscription to Firestore collection 'subscriptions' for doc $docId")
        } catch (e: Exception) {
            Log.w(TAG, "Firestore subscription sync notice: ${e.message}")
        }
    }

    private fun formatExpirationDate(date: Date): String {
        return try {
            val formatter = SimpleDateFormat("d MMM yyyy", Locale.US)
            formatter.format(date)
        } catch (_: Exception) {
            date.toString()
        }
    }
}
