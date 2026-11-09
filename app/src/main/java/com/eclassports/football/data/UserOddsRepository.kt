package com.eclassports.football.data

import com.eclassports.football.model.UserTip
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserOddsRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    /**
     * Real-time stream of all odds from Firestore collection "tips"
     */
    fun getOddsStream(category: String? = null): Flow<List<UserTip>> = callbackFlow {
        val query = if (category.isNullOrBlank()) {
            firestore.collection("tips")
        } else {
            firestore.collection("tips").whereEqualTo("category", category)
        }

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val tips = snapshot.documents.mapNotNull { doc ->
                    try {
                        var home = doc.getString("homeTeam") ?: ""
                        var away = doc.getString("awayTeam") ?: ""
                        val match = doc.getString("match") ?: ""
                        if (home.isEmpty() && away.isEmpty() && match.isNotEmpty()) {
                            val parts = match.split(" vs ", " - ", " v ", " VS ")
                            if (parts.size >= 2) {
                                home = parts[0].trim()
                                away = parts[1].trim()
                            } else {
                                home = match.trim()
                            }
                        }

                        val rawOdd = doc.get("odd") ?: doc.get("odds") ?: doc.get("oddValue") ?: doc.get("oddStr")
                        val odd = when (rawOdd) {
                            is Number -> rawOdd.toDouble()
                            is String -> rawOdd.toDoubleOrNull() ?: 1.0
                            else -> 1.0
                        }

                        UserTip(
                            id = doc.id,
                            homeTeam = home,
                            awayTeam = away,
                            league = doc.getString("league") ?: "",
                            tipPick = doc.getString("tipPick") ?: doc.getString("pick") ?: doc.getString("prediction") ?: "",
                            odd = odd,
                            status = (doc.getString("status") ?: "PENDING").trim().uppercase(),
                            category = doc.getString("category") ?: "FREE_TIPS",
                            categoryName = doc.getString("categoryName") ?: "Free Tips",
                            ticketDate = doc.getString("ticketDate") ?: doc.getString("date") ?: "",
                            kickoffTime = doc.getString("kickoffTime") ?: doc.getString("time") ?: "",
                            createdAt = (doc.get("createdAt") as? Number)?.toLong()
                                ?: (doc.get("timestamp") as? Number)?.toLong()
                                ?: (doc.get("updatedAt") as? Number)?.toLong()
                                ?: 0L
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                trySend(tips)
            }
        }

        awaitClose { registration.remove() }
    }

    suspend fun fetchAllOddsOnce(): List<UserTip> {
        val snapshot = firestore.collection("tips").get().await()
        return snapshot.documents.mapNotNull { doc ->
            try {
                UserTip(
                    id = doc.id,
                    homeTeam = doc.getString("homeTeam") ?: "",
                    awayTeam = doc.getString("awayTeam") ?: "",
                    league = doc.getString("league") ?: "",
                    tipPick = doc.getString("tipPick") ?: doc.getString("prediction") ?: "",
                    odd = doc.getDouble("odd") ?: doc.getDouble("odds") ?: 1.0,
                    status = (doc.getString("status") ?: "PENDING").trim().uppercase(),
                    category = doc.getString("category") ?: "FREE_TIPS",
                    categoryName = doc.getString("categoryName") ?: "Free Tips",
                    ticketDate = doc.getString("ticketDate") ?: doc.getString("date") ?: "",
                    kickoffTime = doc.getString("kickoffTime") ?: doc.getString("time") ?: "",
                    createdAt = doc.getLong("createdAt") ?: 0L
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
