package com.example

import android.app.Application
import com.example.billing.SubscriptionManager
import com.example.util.NotificationHelper

class EClassportsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize RevenueCat and Google Billing
        SubscriptionManager.init(this)
        // Initialize Notification Channels
        NotificationHelper.createNotificationChannel(this)
    }
}
