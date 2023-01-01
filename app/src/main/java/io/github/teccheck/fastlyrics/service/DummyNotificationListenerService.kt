package io.github.teccheck.fastlyrics.service

import android.content.Context
import android.service.notification.NotificationListenerService
import androidx.core.app.NotificationManagerCompat

/* Dummy class needed for access to media information */
class DummyNotificationListenerService : NotificationListenerService() {

    companion object {
        fun canAccessNotifications(context: Context): Boolean {
            return NotificationManagerCompat.getEnabledListenerPackages(context)
                .contains(context.packageName)
        }
    }
}