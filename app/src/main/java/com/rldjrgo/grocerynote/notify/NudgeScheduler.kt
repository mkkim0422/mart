package com.rldjrgo.grocerynote.notify

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import com.rldjrgo.grocerynote.data.local.SettingsDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

/**
 * 하루 2번(아침·저녁) [NudgeReceiver]를 깨우는 알람. 항목 알림([com.rldjrgo.grocerynote.reminder.ReminderScheduler])과
 * 같은 이유로 **inexact** `setAndAllowWhileIdle` — 정확 알람 권한 없음, 몇 분 늦어도 되는 알림.
 *
 * 알람은 슬롯당 1개(요청 코드 고정)라 몇 번을 다시 걸어도 겹치지 않는다. 리시버가 울린 뒤 다음 날 것을
 * 다시 건다. 프로세스 시작([com.rldjrgo.grocerynote.App])·재부팅([com.rldjrgo.grocerynote.reminder.BootReceiver])·
 * 설정 변경 때 [ensureArmed]로 맞춘다. 마스터 토글이 꺼지면 알람 자체를 지운다(하루 2번 깨우는 것도 아낌).
 */
@Singleton
class NudgeScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsDataStore,
) {
    /** 현재 설정에 맞춰 두 슬롯을 (다시) 건다. 몇 번 불러도 안전. */
    suspend fun ensureArmed() {
        val p = settings.nudgePrefs.first()
        if (!p.enabled) {
            cancelAll()
            return
        }
        arm(NudgeSlot.MORNING, p.morningHour)
        arm(NudgeSlot.EVENING, p.eveningHour)
    }

    /** [slot]을 오늘 [hour]:00 에, 이미 지났으면 내일 같은 시각에 건다. */
    fun arm(slot: NudgeSlot, hour: Int) {
        val am = context.getSystemService<AlarmManager>() ?: return
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= now + 60_000L) cal.add(Calendar.DAY_OF_YEAR, 1)
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent(slot, create = true)!!)
    }

    fun cancelAll() {
        val am = context.getSystemService<AlarmManager>() ?: return
        NudgeSlot.values().forEach { slot ->
            pendingIntent(slot, create = false)?.let { pi ->
                am.cancel(pi)
                pi.cancel()
            }
        }
    }

    private fun pendingIntent(slot: NudgeSlot, create: Boolean): PendingIntent? {
        val intent = Intent(context, NudgeReceiver::class.java).apply {
            action = ACTION_FIRE
            putExtra(EXTRA_SLOT, slot.name)
        }
        var flags = PendingIntent.FLAG_IMMUTABLE
        flags = flags or if (create) PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_NO_CREATE
        return PendingIntent.getBroadcast(context, REQUEST_BASE + slot.ordinal, intent, flags)
    }

    companion object {
        const val ACTION_FIRE = "com.rldjrgo.grocerynote.action.NUDGE_FIRE"
        const val EXTRA_SLOT = "extra_slot"
        private const val REQUEST_BASE = 9000

        /** 종류별 채널(폰 설정에서 종류별로 끌 수 있게). 중요도 DEFAULT = 소리 O, 화면 위로 튀어나오진 않음. */
        fun ensureChannels(context: Context) {
            val nm = context.getSystemService<NotificationManager>() ?: return
            NudgeType.values().forEach { t ->
                if (nm.getNotificationChannel(t.channelId) != null) return@forEach
                nm.createNotificationChannel(
                    NotificationChannel(t.channelId, t.channelName, NotificationManager.IMPORTANCE_DEFAULT).apply {
                        description = t.channelDesc
                    },
                )
            }
        }
    }
}
