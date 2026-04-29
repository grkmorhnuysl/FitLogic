package com.fitlogic.ai.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FitLogicFirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var notificationManager: FitLogicNotificationManager

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: message.data["title"] ?: "FitLogic"
        val body = message.notification?.body ?: message.data["body"] ?: "Yeni bir bildirimin var."
        val channel = message.data["channel"] ?: FitLogicNotificationManager.CHANNEL_WORKOUT
        notificationManager.showSimpleNotification(channel, title, body)
    }
}
