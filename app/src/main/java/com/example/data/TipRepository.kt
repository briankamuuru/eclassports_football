package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

class TipRepository(context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val tipDao = database.tipDao()
    private val listeners = mutableListOf<ListenerRegistration>()
    private val cachedDocsByCollection = mutableMapOf<String, List<TipEntity>>()

    init {
        // Subscribe to Firebase Cloud Messaging topics for match & VIP alerts
        try {
            val fcm = FirebaseMessaging.getInstance()
            val topics = listOf(
                "all_users",
                "tips_alerts",
                "free_tips",
                "vip_tips",
                "tips",
                "odds",
                "general",
                "notifications"
            )
            for (topic in topics) {
                fcm.subscribeToTopic(topic)
            }

            fcm.token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.i("TipRepository", "FCM Device Token registered: $token")
                    try {
                        val db = FirebaseFirestore.getInstance()
                        val tokenData = mapOf(
                            "token" to token,
                            "platform" to "android",
                            "updatedAt" to System.currentTimeMillis(),
                            "device" to "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
                        )
                        db.collection("fcm_tokens").document(token).set(tokenData)
                    } catch (e: Exception) {
                        Log.w("TipRepository", "Could not sync FCM token to Firestore: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("TipRepository", "FCM initialization: ${e.message}")
        }

        // Connect exclusively to Cloud Firestore project eclassports
        startFirestoreSync()
    }

    /**
     * Connects to Cloud Firestore 'tips' and 'odds' collections using real-time addSnapshotListener.
     * All displayed odds and tips update automatically whenever the admin publishes or updates a match.
     */
    fun startFirestoreSync() {
        try {
            listeners.forEach { it.remove() }
            listeners.clear()
            cachedDocsByCollection.clear()

            val firestore = FirebaseFirestore.getInstance()
            val targetCollections = listOf("tips", "odds")

            for (collName in targetCollections) {
                val listener = firestore.collection(collName)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.w("TipRepository", "Firestore collection '$collName' error: ${error.message}")
                            return@addSnapshotListener
                        }

                        val parsedList = snapshot?.documents?.mapNotNull { parseTipDocument(it) } ?: emptyList()
                        cachedDocsByCollection[collName] = parsedList
                        updateMergedTips()
                    }
                listeners.add(listener)
            }
        } catch (e: Exception) {
            Log.e("TipRepository", "Failed to initialize Firestore listener: ${e.message}")
        }
    }

    private fun updateMergedTips() {
        CoroutineScope(Dispatchers.IO).launch {
            val allMerged = cachedDocsByCollection.values.flatten().distinctBy { it.id }
            tipDao.replaceWithFirestoreTips(allMerged)
        }
    }

    /**
     * Manually triggers a fresh fetch from Firestore 'tips' and 'odds' collections.
     */
    suspend fun refreshFromFirestore() {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val tipsSnapshot = runCatching { firestore.collection("tips").get().await() }.getOrNull()
            val oddsSnapshot = runCatching { firestore.collection("odds").get().await() }.getOrNull()

            val tipsList = tipsSnapshot?.documents?.mapNotNull { parseTipDocument(it) } ?: emptyList()
            val oddsList = oddsSnapshot?.documents?.mapNotNull { parseTipDocument(it) } ?: emptyList()

            val combined = (tipsList + oddsList).distinctBy { it.id }
            tipDao.replaceWithFirestoreTips(combined)
        } catch (e: Exception) {
            Log.e("TipRepository", "Manual Firestore refresh failed: ${e.message}")
        }
    }

    /**
     * Parses a Firestore DocumentSnapshot supporting the exact TipModel specification:
     * - id: String
     * - category: String ('FREE_TIPS', 'VIP_2_PLUS', 'VIP_3_PLUS', 'VIP_5_PLUS', 'SPECIAL_VIP')
     * - categoryName: String (e.g. 'FREE TIPS', '2+ TIPS VIP')
     * - homeTeam: String
     * - awayTeam: String
     * - ticketDate: String (format yyyy-MM-dd)
     * - kickoffTime: String (format HH:mm)
     * - league: String
     * - tipPick: String (e.g. 'Home Win', 'Over 2.5 Goals', 'GG')
     * - odd: Double (decimal odds value)
     * - status: String ('PENDING', 'WON', 'LOST')
     */
    private fun parseTipDocument(doc: DocumentSnapshot): TipEntity? {
        return try {
            val rawId = doc.get("id")
            val id = when (rawId) {
                is String -> rawId.ifEmpty { doc.id }
                is Number -> rawId.toString()
                else -> doc.id
            }

            // Category parsing supporting 'FREE_TIPS', 'VIP_2_PLUS', 'VIP_3_PLUS', 'VIP_5_PLUS', 'SPECIAL_VIP'
            val rawCategory = doc.getString("category") ?: "FREE_TIPS"
            val categoryEnum = TipCategory.fromId(rawCategory)
            val categoryName = doc.getString("categoryName") ?: categoryEnum.defaultTitle

            // Date parsing supporting ticketDate (format yyyy-MM-dd), date, matchDate, or timestamp
            val rawDate = doc.get("ticketDate") ?: doc.get("date") ?: doc.get("matchDate") ?: doc.get("timestamp")
            var ticketDate = when (rawDate) {
                is String -> rawDate.trim()
                is Timestamp -> SimpleDateFormat("yyyy-MM-dd", Locale.US).format(rawDate.toDate())
                else -> ""
            }
            if (ticketDate.isEmpty()) {
                val rawTime = doc.get("createdAt") ?: doc.get("updatedAt") ?: doc.get("timestamp")
                ticketDate = when (rawTime) {
                    is Timestamp -> SimpleDateFormat("yyyy-MM-dd", Locale.US).format(rawTime.toDate())
                    is Number -> SimpleDateFormat("yyyy-MM-dd", Locale.US).format(java.util.Date(rawTime.toLong()))
                    else -> SimpleDateFormat("yyyy-MM-dd", Locale.US).format(java.util.Date())
                }
            }

            // Teams parsing supporting homeTeam/awayTeam or match ("Home vs Away")
            var homeTeam = doc.getString("homeTeam")?.trim() ?: ""
            var awayTeam = doc.getString("awayTeam")?.trim() ?: ""
            val match = doc.getString("match")?.trim() ?: ""
            if (homeTeam.isEmpty() && awayTeam.isEmpty() && match.isNotEmpty()) {
                val split = match.split(" vs ", " - ", " v ", " VS ")
                if (split.size >= 2) {
                    homeTeam = split[0].trim()
                    awayTeam = split[1].trim()
                } else {
                    homeTeam = match
                }
            }

            val kickoffTime = doc.getString("kickoffTime")?.trim()
                ?: doc.getString("time")?.trim()
                ?: ""
            val league = doc.getString("league")?.trim() ?: ""

            // Market pick supporting tipPick, pick, or prediction
            val tipPick = doc.getString("tipPick")?.trim()
                ?: doc.getString("pick")?.trim()
                ?: doc.getString("prediction")?.trim()
                ?: ""

            // Odds supporting odd, odds, oddValue, or oddStr
            val rawOdd = doc.get("odd") ?: doc.get("odds") ?: doc.get("oddValue") ?: doc.get("oddStr")
            val odd = when (rawOdd) {
                is Number -> rawOdd.toDouble()
                is String -> rawOdd.toDoubleOrNull() ?: 1.0
                else -> 1.0
            }

            // Status: 'PENDING', 'WON', 'LOST'
            val rawStatus = (doc.getString("status") ?: TipStatus.PENDING.name).trim().uppercase()
            val status = when (rawStatus) {
                "WON" -> TipStatus.WON.name
                "LOST" -> TipStatus.LOST.name
                else -> TipStatus.PENDING.name
            }

            val rawCreatedAt = doc.get("createdAt")
            val createdAt = when (rawCreatedAt) {
                is Number -> rawCreatedAt.toLong()
                is Timestamp -> rawCreatedAt.toDate().time
                else -> System.currentTimeMillis()
            }

            val rawUpdatedAt = doc.get("updatedAt")
            val updatedAt = when (rawUpdatedAt) {
                is Number -> rawUpdatedAt.toLong()
                is Timestamp -> rawUpdatedAt.toDate().time
                else -> System.currentTimeMillis()
            }

            TipEntity(
                id = id,
                category = categoryEnum.id,
                categoryName = categoryName,
                matchDate = ticketDate,
                kickoffTime = kickoffTime,
                league = league,
                homeTeam = homeTeam,
                awayTeam = awayTeam,
                prediction = tipPick,
                odds = if (odd > 0.0) odd else 1.0,
                status = status,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        } catch (e: Exception) {
            Log.w("TipRepository", "Error parsing Firestore document ${doc.id}: ${e.message}")
            null
        }
    }

    /**
     * Active tips for today / active matches where status == 'PENDING'
     */
    fun getActiveTips(category: TipCategory): Flow<List<TipEntity>> {
        return if (category == TipCategory.ALL_VIP) {
            tipDao.getAllActiveVipTips()
        } else {
            tipDao.getActiveTips(category.id)
        }
    }

    /**
     * Historical results where status != 'PENDING' (WON or LOST)
     */
    fun getSettledTips(category: TipCategory): Flow<List<TipEntity>> {
        return if (category == TipCategory.ALL_VIP) {
            tipDao.getAllSettledVipTips()
        } else {
            tipDao.getSettledTips(category.id)
        }
    }

    fun getAllTipsByCategory(category: TipCategory): Flow<List<TipEntity>> {
        return if (category == TipCategory.ALL_VIP) {
            tipDao.getAllVipTips()
        } else {
            tipDao.getAllTipsByCategory(category.id)
        }
    }
}
