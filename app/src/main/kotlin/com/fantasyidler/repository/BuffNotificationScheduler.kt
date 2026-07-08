package com.fantasyidler.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.fantasyidler.receiver.BuffAlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuffNotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun scheduleXpBoostExpiry(expiresAt: Long) {
        if (expiresAt <= System.currentTimeMillis()) return
        schedule(BuffAlarmReceiver.REQUEST_CODE_XP_BOOST, BuffAlarmReceiver.BUFF_XP_BOOST, expiresAt)
    }

    fun cancelXpBoostExpiry() = cancel(BuffAlarmReceiver.REQUEST_CODE_XP_BOOST)

    fun scheduleBlessingExpiry(expiresAt: Long) {
        if (expiresAt <= System.currentTimeMillis()) return
        schedule(BuffAlarmReceiver.REQUEST_CODE_BLESSING, BuffAlarmReceiver.BUFF_BLESSING, expiresAt)
    }

    fun cancelBlessingExpiry() = cancel(BuffAlarmReceiver.REQUEST_CODE_BLESSING)

    private fun schedule(requestCode: Int, buffType: String, expiresAt: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = Intent(context, BuffAlarmReceiver::class.java)
            .apply { putExtra(BuffAlarmReceiver.EXTRA_BUFF_TYPE, buffType) }
            .let { PendingIntent.getBroadcast(context, requestCode, it, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE) }
        try {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, expiresAt, pi)
        } catch (_: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, expiresAt, pi)
        }
    }

    private fun cancel(requestCode: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(
            context, requestCode,
            Intent(context, BuffAlarmReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        am.cancel(pi)
    }
}
