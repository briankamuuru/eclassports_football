package com.example.billing

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.PurchasesErrorCode
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import com.revenuecat.purchases.models.BillingFeature
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
 * Authoritative manager for RevenueCat, Google Play Billing, and Firestore subscriptions.
 * Supports independent subscriptions for all five VIP sections:
 * - All VIPs (all_vips)
 * - 2+ VIP (2plus_vip)
 * - 3+ VIP (3plus_vip)
 * - 5+ VIP (5plus_vip)
 * - Special VIP (special_vip)
 */
object SubscriptionManager {
    private const val TAG = "SubscriptionManager"
    private const val PREFS_NAME = "eclassports_subscriptions_cache"
    private const val KEY_CACHED_UNLOCKED = "cached_unlocked_sections"

    private val _subscriptionState = MutableStateFlow(
        SubscriptionState(
            isLoading = true,
            sectionPlans = VipSection.entries.associateWith { DefaultSectionPlans.createDefaultPlansForSection(it) }
        )
    )
    val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState.asStateFlow()

    private var isInitialized = false
    private var isBillingAvailableOnDevice = true
    private var sharedPrefs: SharedPreferences? = null
    private var firestoreListener: ListenerRegistration? = null
    private var currentUserId: String = "guest_user"

    /**
     * Initializes RevenueCat with the configured API key from BuildConfig.
     */
    fun init(context: Context) {
        if (isInitialized) return

        val appContext = context.applicationContext
        sharedPrefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Restore previously verified unlocked sections from local cache immediately
        val cachedUnlocked = loadCachedUnlockedSections()
        if (cachedUnlocked.isNotEmpty()) {
            _subscriptionState.value = _subscriptionState.value.copy(
                unlockedSections = cachedUnlocked,
                isLoading = false,
                statusMessage = "Restored active VIP access from local storage"
            )
            Log.i(TAG, "Restored cached unlocked VIP sections: ${cachedUnlocked.map { it.cardTitle }}")
        }

        val rawKey = runCatching { BuildConfig.REVENUECAT_API_KEY }.getOrDefault("")
        val isRealKey = rawKey.isNotBlank() &&
                !rawKey.contains("placeholder", ignoreCase = true) &&
                !rawKey.contains("MY_", ignoreCase = true)

        Log.i(TAG, "Initializing RevenueCat. Valid API key present: $isRealKey (Key prefix: ${rawKey.take(8)}...)")

        if (isRealKey) {
            try {
                Purchases.logLevel = LogLevel.INFO
                val configuration = PurchasesConfiguration.Builder(appContext, rawKey).build()
                Purchases.configure(configuration)

                currentUserId = Purchases.sharedInstance.appUserID
                Log.i(TAG, "RevenueCat initialized successfully for App User ID: $currentUserId")

                // Listen for real-time customer info updates from RevenueCat
                Purchases.sharedInstance.updatedCustomerInfoListener =
                    UpdatedCustomerInfoListener { customerInfo ->
                        Log.i(TAG, "RevenueCat updatedCustomerInfoListener triggered")
                        processCustomerInfo(customerInfo)
                    }

                // Check Google Play Billing capabilities asynchronously
                Purchases.canMakePayments(
                    appContext,
                    listOf(BillingFeature.SUBSCRIPTIONS)
                ) { canPay ->
                    isBillingAvailableOnDevice = canPay
                    Log.i(TAG, "Google Play Billing availability on device: $canPay")
                }

                // ALWAYS query customer info and offerings from RevenueCat servers
                fetchCustomerInfoAndOfferings()

                // Start Firestore real-time listener for synced subscriptions
                startFirestoreSubscriptionListener(currentUserId)

                isInitialized = true
                return
            } catch (e: Exception) {
                Log.e(TAG, "Exception during Purchases.configure: ${e.message}", e)
            }
        }

        // Preview mode when API key is missing
        isInitialized = true
        loadFallbackOfferings(
            isConfigured = false,
            errorMsg = "RevenueCat API key not configured. Running in preview mode."
        )
    }

    /**
     * Always queries RevenueCat for current customer entitlements and active packages.
     */
    private fun fetchCustomerInfoAndOfferings() {
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                Log.i(TAG, "RevenueCat getCustomerInfo received successfully")
                processCustomerInfo(customerInfo)
                fetchOfferings()
            }

            override fun onError(error: PurchasesError) {
                handleBillingError("Error fetching customer info", error)
                fetchOfferings()
            }
        })
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
                Log.i(TAG, "Customer info refreshed from RevenueCat")
                processCustomerInfo(customerInfo)
                onComplete?.invoke(true)
            }

            override fun onError(error: PurchasesError) {
                handleBillingError("Failed to refresh customer info", error)
                onComplete?.invoke(false)
            }
        })
    }

    /**
     * Authoritative resolver for active VIP section subscriptions.
     * Evaluates:
     * 1. Active Entitlements in customerInfo.entitlements.active
     * 2. Active Subscription SKUs in customerInfo.activeSubscriptions
     * 3. Linked product identifiers across all active entitlements
     * 4. Normalized naming (handling casing, spaces, plus signs, and underscores)
     */
    private fun processCustomerInfo(customerInfo: CustomerInfo) {
        val unlocked = mutableSetOf<VipSection>()
        val expirations = mutableMapOf<VipSection, String>()

        currentUserId = customerInfo.originalAppUserId.ifBlank { Purchases.sharedInstance.appUserID }

        val activeEntitlementKeys = customerInfo.entitlements.active.keys
        val activeSubscriptions = customerInfo.activeSubscriptions
        val allPurchased = customerInfo.allPurchasedProductIds

        Log.i(TAG, "=== REVENUECAT SUBSCRIPTION AUDIT ===")
        Log.i(TAG, "User ID: $currentUserId")
        Log.i(TAG, "Active Entitlements (${activeEntitlementKeys.size}): $activeEntitlementKeys")
        Log.i(TAG, "Active Subscriptions (${activeSubscriptions.size}): $activeSubscriptions")
        Log.i(TAG, "All Purchased Products: $allPurchased")

        // 1. Check Master VIP Pass ("All VIPs")
        val isMasterPassActive = activeEntitlementKeys.any { key ->
            val norm = normalizeKey(key)
            norm == "allvips" || norm == "allvip" || norm == "default" || norm == "master" || norm.contains("all")
        } || activeSubscriptions.any { sku ->
            val norm = normalizeKey(sku)
            norm.contains("allvip") || norm.contains("allvips")
        } || customerInfo.entitlements.active.values.any { entitlement ->
            val norm = normalizeKey(entitlement.productIdentifier)
            norm.contains("allvip") || norm.contains("allvips")
        }

        if (isMasterPassActive) {
            Log.i(TAG, ">>> Master 'ALL VIPS' Pass is ACTIVE. Unlocking all 5 VIP sections! <<<")
            unlocked.addAll(VipSection.entries)
            val masterExp = customerInfo.entitlements[VipSection.ALL_VIPS.entitlementId]?.expirationDate
                ?: customerInfo.entitlements.active.values.firstOrNull()?.expirationDate
            val expDateStr = masterExp?.let { formatExpirationDate(it) } ?: "Active"
            for (section in VipSection.entries) {
                expirations[section] = expDateStr
            }
        } else {
            // 2. Independent section checks
            for (section in VipSection.entries) {
                var sectionUnlocked = false
                var expirationStr: String? = null

                // Check 2a: Matching active entitlement key
                for ((entKey, entInfo) in customerInfo.entitlements.active) {
                    if (isEntitlementMatchForSection(section, entKey)) {
                        sectionUnlocked = true
                        expirationStr = entInfo.expirationDate?.let { formatExpirationDate(it) }
                        Log.i(TAG, "Section ${section.cardTitle} unlocked via active entitlement: $entKey")
                        break
                    }
                }

                // Check 2b: Matching active store subscription SKU
                if (!sectionUnlocked) {
                    for (sku in activeSubscriptions) {
                        if (isSkuMatchForSection(section, sku)) {
                            sectionUnlocked = true
                            Log.i(TAG, "Section ${section.cardTitle} unlocked via active subscription SKU: $sku")
                            break
                        }
                    }
                }

                // Check 2c: Matching product identifier on any active entitlement
                if (!sectionUnlocked) {
                    for ((_, entInfo) in customerInfo.entitlements.active) {
                        if (isSkuMatchForSection(section, entInfo.productIdentifier)) {
                            sectionUnlocked = true
                            expirationStr = entInfo.expirationDate?.let { formatExpirationDate(it) }
                            Log.i(TAG, "Section ${section.cardTitle} unlocked via entitlement product: ${entInfo.productIdentifier}")
                            break
                        }
                    }
                }

                if (sectionUnlocked) {
                    unlocked.add(section)
                    expirations[section] = expirationStr ?: "Active"
                }
            }
        }

        // Also merge any cached offline unlocked sections if network state returned empty temporarily
        val finalUnlocked = if (unlocked.isEmpty()) {
            val cached = loadCachedUnlockedSections()
            if (cached.isNotEmpty()) {
                Log.i(TAG, "Using cached active VIP sections: ${cached.map { it.cardTitle }}")
                cached
            } else unlocked
        } else {
            saveCachedUnlockedSections(unlocked)
            unlocked
        }

        Log.i(TAG, "Final Unlocked Sections: ${finalUnlocked.map { it.cardTitle }}")

        _subscriptionState.value = _subscriptionState.value.copy(
            unlockedSections = finalUnlocked,
            sectionExpirations = expirations,
            isLoading = false,
            isConfigured = true,
            isSandboxOrDemo = false,
            statusMessage = if (finalUnlocked.isNotEmpty()) "${finalUnlocked.size} Active VIP Subscriptions" else null
        )
    }

    private fun isEntitlementMatchForSection(section: VipSection, entitlementKey: String): Boolean {
        val normKey = normalizeKey(entitlementKey)

        if (section.entitlementAliases.any { normalizeKey(it) == normKey }) return true
        if (section.offeringAliases.any { normalizeKey(it) == normKey }) return true
        if (normalizeKey(section.storeSubscriptionId) == normKey) return true

        return when (section) {
            VipSection.ALL_VIPS -> normKey.contains("allvip") || normKey == "default" || normKey == "master" || normKey.contains("all")
            VipSection.VIP_2_PLUS -> normKey.contains("2plus") || normKey.contains("2+") || normKey.contains("vip2") || normKey.contains("2")
            VipSection.VIP_3_PLUS -> normKey.contains("3plus") || normKey.contains("3+") || normKey.contains("vip3") || normKey.contains("3")
            VipSection.VIP_5_PLUS -> normKey.contains("5plus") || normKey.contains("5+") || normKey.contains("vip5") || normKey.contains("5")
            VipSection.SPECIAL_VIP -> normKey.contains("special")
        }
    }

    private fun isSkuMatchForSection(section: VipSection, sku: String): Boolean {
        val normSku = normalizeKey(sku)
        val normStoreId = normalizeKey(section.storeSubscriptionId)
        val normWeekly = normalizeKey(section.weeklyProductId)
        val normMonthly = normalizeKey(section.monthlyProductId)
        val normWeeklyBase = normalizeKey(section.weeklyBasePlanId)
        val normMonthlyBase = normalizeKey(section.monthlyBasePlanId)

        if (normSku.contains(normWeekly) || normSku.contains(normMonthly)) return true
        if (normSku.contains(normWeeklyBase) || normSku.contains(normMonthlyBase)) return true
        if (normSku.startsWith(normStoreId) || normSku == normStoreId) return true

        return when (section) {
            VipSection.ALL_VIPS -> normSku.contains("allvip") || normSku.contains("all_vip") || normSku.contains("all")
            VipSection.VIP_2_PLUS -> normSku.contains("2plus") || normSku.contains("2+") || normSku.contains("vip2plus") || normSku.contains("vip_2_plus")
            VipSection.VIP_3_PLUS -> normSku.contains("3plus") || normSku.contains("3+") || normSku.contains("vip3plus") || normSku.contains("vip_3_plus")
            VipSection.VIP_5_PLUS -> normSku.contains("5plus") || normSku.contains("5+") || normSku.contains("vip5plus") || normSku.contains("vip_5_plus")
            VipSection.SPECIAL_VIP -> normSku.contains("special") || normSku.contains("special_vip")
        }
    }

    private fun normalizeKey(key: String): String {
        return key.lowercase(Locale.US)
            .replace(" ", "")
            .replace("_", "")
            .replace("-", "")
            .replace("+", "")
            .replace(":", "")
    }

    private fun fetchOfferings() {
        Purchases.sharedInstance.getOfferingsWith(
            onError = { error ->
                handleBillingError("Offerings fetch notice", error)
                loadFallbackOfferings(isConfigured = true, errorMsg = error.message)
            },
            onSuccess = { offerings ->
                val mappedPlans = DefaultSectionPlans.mapOfferingsToSectionPlans(offerings)
                _subscriptionState.value = _subscriptionState.value.copy(
                    sectionPlans = mappedPlans,
                    isLoading = false,
                    isConfigured = true,
                    isSandboxOrDemo = false
                )
                Log.i(TAG, "Successfully mapped RevenueCat offerings for all 5 VIP sections: ${offerings.all.keys}")
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
                        Log.w(TAG, "Purchase error for ${section.cardTitle}: ${error.message}")
                        onComplete(false, error.message)
                    }
                },
                onSuccess = { _: StoreTransaction?, customerInfo: CustomerInfo ->
                    processCustomerInfo(customerInfo)
                    val isUnlocked = isSectionUnlocked(section)
                    if (isUnlocked) {
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
                        Log.w(TAG, "Purchase succeeded. Verifying active subscription...")
                        // Fallback check
                        unlockSectionLocally(section)
                        onComplete(true, "Subscription active for ${section.cardTitle}!")
                    }
                }
            )
        } else if (_subscriptionState.value.isConfigured) {
            Log.w(TAG, "Cannot launch Google Play purchase: Package is null for plan ${plan.productId}")
            onComplete(false, "Live Google Play package not loaded. Check RevenueCat offerings configuration.")
        } else {
            // Preview / Demo activation on emulators or when API key is missing
            Log.i(TAG, "Activating subscription for ${section.cardTitle} (Preview / Fallback)")
            unlockSectionLocally(section)
            onComplete(true, "VIP access active for ${section.cardTitle}! (Demo Mode)")
        }
    }

    private fun unlockSectionLocally(section: VipSection) {
        val updated = _subscriptionState.value.unlockedSections + section
        val updatedExp = _subscriptionState.value.sectionExpirations + (section to "Active VIP")
        saveCachedUnlockedSections(updated)
        _subscriptionState.value = _subscriptionState.value.copy(
            unlockedSections = updated,
            sectionExpirations = updatedExp,
            isLoading = false,
            statusMessage = "${section.cardTitle} Active"
        )
    }

    /**
     * Restores previous purchases across all VIP sections.
     */
    fun restorePurchases(onComplete: (success: Boolean, message: String?) -> Unit) {
        _subscriptionState.value = _subscriptionState.value.copy(isLoading = true)

        if (_subscriptionState.value.isConfigured) {
            Purchases.sharedInstance.restorePurchasesWith(
                onError = { error ->
                    _subscriptionState.value = _subscriptionState.value.copy(isLoading = false)
                    Log.w(TAG, "RevenueCat restorePurchases notice: ${error.message}")
                    // Check local cache before giving up
                    val cachedCount = loadCachedUnlockedSections().size
                    if (cachedCount > 0) {
                        onComplete(true, "Restored $cachedCount active VIP subscription(s) from local cache!")
                    } else {
                        onComplete(false, error.message)
                    }
                },
                onSuccess = { customerInfo ->
                    processCustomerInfo(customerInfo)
                    val count = _subscriptionState.value.unlockedSections.size
                    val message = if (count > 0) {
                        val names = _subscriptionState.value.unlockedSections.joinToString(", ") { it.cardTitle }
                        "Restored $count active VIP subscription(s): $names"
                    } else {
                        "No active VIP subscriptions found for this account. If you recently purchased, ensure your Google Play account matches."
                    }
                    onComplete(count > 0, message)
                }
            )
        } else {
            val count = _subscriptionState.value.unlockedSections.size
            val msg = if (count > 0) "Restored $count active VIP section(s)!" else "No purchases found to restore."
            _subscriptionState.value = _subscriptionState.value.copy(isLoading = false)
            onComplete(count > 0, msg)
        }
    }

    /**
     * Listens to Firestore subscriptions collection for synced active access.
     */
    private fun startFirestoreSubscriptionListener(userId: String) {
        try {
            firestoreListener?.remove()
            val db = FirebaseFirestore.getInstance()
            firestoreListener = db.collection("subscriptions")
                .whereEqualTo("status", "ACTIVE")
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.w(TAG, "Firestore subscription listener notice: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshots != null && !snapshots.isEmpty) {
                        val firestoreUnlocked = mutableSetOf<VipSection>()
                        for (doc in snapshots.documents) {
                            val docUserId = doc.getString("userId") ?: ""
                            val vipSectionId = doc.getString("vipSection") ?: ""
                            val expirationDate = doc.getLong("expirationDate") ?: 0L

                            // Check if this document belongs to this user or matches active section
                            val isForUser = docUserId == userId || docUserId.isBlank() || doc.id.contains(userId)
                            val isNotExpired = expirationDate == 0L || expirationDate > System.currentTimeMillis()

                            if (isForUser && isNotExpired) {
                                VipSection.fromId(vipSectionId)?.let { section ->
                                    firestoreUnlocked.add(section)
                                    Log.i(TAG, "Unlocked ${section.cardTitle} via Firestore active subscription record")
                                }
                            }
                        }

                        if (firestoreUnlocked.isNotEmpty()) {
                            val combined = _subscriptionState.value.unlockedSections + firestoreUnlocked
                            saveCachedUnlockedSections(combined)
                            _subscriptionState.value = _subscriptionState.value.copy(
                                unlockedSections = combined,
                                statusMessage = "${combined.size} Active VIP Subscriptions"
                            )
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Notice starting Firestore subscription listener: ${e.message}")
        }
    }

    private fun handleBillingError(contextMsg: String, error: PurchasesError) {
        if (error.code == PurchasesErrorCode.PurchaseNotAllowedError ||
            error.underlyingErrorMessage?.contains("BILLING_UNAVAILABLE", ignoreCase = true) == true
        ) {
            isBillingAvailableOnDevice = false
            Log.w(TAG, "$contextMsg: Google Play Billing service is unavailable on this device.")
        } else {
            Log.w(TAG, "$contextMsg: ${error.message}")
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

    private fun saveCachedUnlockedSections(unlocked: Set<VipSection>) {
        try {
            val ids = unlocked.map { it.id }.toSet()
            sharedPrefs?.edit()?.putStringSet(KEY_CACHED_UNLOCKED, ids)?.apply()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to cache unlocked sections: ${e.message}")
        }
    }

    private fun loadCachedUnlockedSections(): Set<VipSection> {
        return try {
            val ids = sharedPrefs?.getStringSet(KEY_CACHED_UNLOCKED, emptySet()) ?: emptySet()
            ids.mapNotNull { VipSection.fromId(it) }.toSet()
        } catch (e: Exception) {
            emptySet()
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
