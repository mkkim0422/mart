package com.rldjrgo.grocerynote.notify

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.rldjrgo.grocerynote.MainActivity
import com.rldjrgo.grocerynote.R
import com.rldjrgo.grocerynote.di.NudgeEntryPoint
import com.rldjrgo.grocerynote.ui.navigation.Routes
import com.rldjrgo.grocerynote.util.Analytics
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first

/**
 * 한 슬롯 실행: 설정·DB 스냅샷 → [NudgeEngine.decide] → 알림 1개 표시 → 이력 기록 → 다음 날 재예약.
 * [NudgeReceiver](알람)와 디버그 빌드의 수동 실행(MainActivity `debug_nudge_slot` extra)이 같이 쓴다 —
 * Android 14+는 adb에서 비공개 리시버로 브로드캐스트를 못 보내서 수동 시험 경로가 따로 필요했다(2026-11-22).
 */
object NudgeRunner {
    private const val TAG = "Nudge"
    /** MainActivity 인텐트 extra — 열림 집계용(Analytics nudge_opened). */
    const val EXTRA_NUDGE_TYPE = "nudge_type"
    /** MainActivity 인텐트 extra — 요리별 재료 탭에서 이 요리를 연다. */
    const val EXTRA_RECIPE_NAME = "nudge_recipe_name"
    private const val NOTIFICATION_BASE = 7000

    /** @return 보낸 알림(없으면 null). */
    suspend fun run(context: Context, slot: NudgeSlot, rearm: Boolean = true): Nudge? {
        val app = context.applicationContext
        val entry = EntryPointAccessors.fromApplication(app, NudgeEntryPoint::class.java)
        val settings = entry.settingsDataStore()
        val prefs = settings.nudgePrefs.first()
        // 먼저 내일 것을 다시 건다 — 아래서 무엇이 실패해도 알람 사슬이 끊기지 않게.
        if (rearm && prefs.enabled) {
            entry.nudgeScheduler().arm(slot, if (slot == NudgeSlot.MORNING) prefs.morningHour else prefs.eveningHour)
        }
        if (!prefs.enabled || !canNotify(app)) {
            Log.i(TAG, "skip: enabled=${prefs.enabled} canNotify=${canNotify(app)}")
            return null
        }

        val now = System.currentTimeMillis()
        val snapshot = NudgeSnapshot(
            stores = entry.storeRepository().observeActiveStores().first(),
            items = entry.itemRepository().observeAllItems().first(),
            nowMillis = now,
            firstInstallMillis = runCatching {
                app.packageManager.getPackageInfo(app.packageName, 0).firstInstallTime
            }.getOrDefault(now),
            prefs = prefs,
            recipeMainCounts = runCatching { entry.recipeBook().load().associate { it.name to it.main.size } }
                .getOrDefault(emptyMap()),
        )
        val nudge = NudgeEngine.decide(slot, snapshot)
        Log.i(
            TAG,
            "slot=$slot stores=${snapshot.stores.size} items=${snapshot.items.size} " +
                "installAgeH=${(now - snapshot.firstInstallMillis) / 3_600_000} → ${nudge?.type} " +
                "(lastSentDay=${prefs.lastSentDay} season=${prefs.seasonSent})",
        )
        if (nudge == null) return null

        show(app, nudge)
        settings.recordNudgeSent(nudge.type, NudgeEngine.epochDay(now), nudge.storeId, nudge.seasonKey)
        Analytics.nudgeSent(nudge.type.key)
        return nudge
    }

    fun canNotify(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return false
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    private fun show(context: Context, nudge: Nudge) {
        NudgeScheduler.ensureChannels(context)

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NUDGE_TYPE, nudge.type.key)
            when {
                nudge.recipeName != null -> putExtra(EXTRA_RECIPE_NAME, nudge.recipeName)
                nudge.openAdd -> putExtra("action", "ADD_ITEM")
                nudge.storeId != null -> putExtra(Routes.HOME_DEEPLINK_STORE_ARG, nudge.storeId)
                else -> putExtra("action", "OPEN_HOME")
            }
        }
        val id = NOTIFICATION_BASE + nudge.type.ordinal
        val contentPI = PendingIntent.getActivity(
            context, id, contentIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, nudge.type.channelId)
            .setSmallIcon(R.drawable.ic_stat_reminder)
            .setContentTitle(nudge.title)
            .setContentText(nudge.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(nudge.text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setAutoCancel(true)
            .setContentIntent(contentPI)
            .build()
        // 같은 종류는 같은 id → 이전 알림을 덮어써서 쌓이지 않는다.
        NotificationManagerCompat.from(context).notify(id, notification)
    }
}
