package com.example.util

import android.os.Build
import android.util.Log
import com.example.data.TipRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FirebaseNotificationService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New FCM registration token: $token")
        // Register token in Firestore collection 'fcm_tokens' so admin can send targeted push messages
        try {
            val db = FirebaseFirestore.getInstance()
            val tokenData = mapOf(
                "token" to token,
                "platform" to "android",
                "updatedAt" to System.currentTimeMillis(),
                "device" to "${Build.MANUFACTURER} ${Build.MODEL}"
            )
            db.collection("fcm_tokens").document(token).set(tokenData)
        } catch (e: Exception) {
            Log.w("FCM", "Failed to update FCM token in Firestore: ${e.message}")
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Message received from: ${remoteMessage.from}, data: ${remoteMessage.data}")

        // 1. Resolve notification title
        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: remoteMessage.data["subject"]
            ?: remoteMessage.data["header"]
            ?: "eClassports Football"

        // 2. Resolve notification body/message
        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: remoteMessage.data["message"]
            ?: remoteMessage.data["msg"]
            ?: remoteMessage.data["text"]
            ?: remoteMessage.data["alert"]
            ?: remoteMessage.data["content"]
            ?: run {
                val match = remoteMessage.data["match"]
                val pick = remoteMessage.data["tipPick"] ?: remoteMessage.data["prediction"] ?: remoteMessage.data["pick"]
                val odds = remoteMessage.data["odd"] ?: remoteMessage.data["odds"]
                if (!match.isNullOrBlank() && !pick.isNullOrBlank()) {
                    "$match: $pick (Odds: ${odds ?: "1.0"})"
                } else {
                    "New football predictions and VIP updates are available!"
                }
            }

        val category = remoteMessage.data["category"]

        // 3. Display the notification to the user
        NotificationHelper.showTipNotification(
            context = this,
            title = title,
            message = body,
            category = category
        )

        // 4. Trigger background refresh of latest tips from Firestore so data is fresh
        try {
            CoroutineScope(Dispatchers.IO).launch {
                TipRepository(applicationContext).refreshFromFirestore()
            }
        } catch (e: Exception) {
            Log.w("FCM", "Background sync after push message: ${e.message}")
        }
    }
}
