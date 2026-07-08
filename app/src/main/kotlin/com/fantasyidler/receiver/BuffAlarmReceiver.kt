package com.fantasyidler.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fantasyidler.notification.SessionNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BuffAlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var notificationManager: SessionNotificationManager

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.getStringExtra(EXTRA_BUFF_TYPE)) {
            BUFF_XP_BOOST -> notificationManager.showXpBoostExpired()
            BUFF_BLESSING -> notificationManager.showBlessingExpired()
        }
    }

    companion object {
        const val EXTRA_BUFF_TYPE       = "buff_type"
        const val BUFF_XP_BOOST         = "xp_boost"
        const val BUFF_BLESSING         = "blessing"
        const val REQUEST_CODE_XP_BOOST = 3001
        const val REQUEST_CODE_BLESSING = 3002
    }
}
