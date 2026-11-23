package com.rldjrgo.grocerynote.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** [NudgeScheduler]의 아침/저녁 알람이 깨운다 → [NudgeRunner.run]. */
class NudgeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != NudgeScheduler.ACTION_FIRE) return
        val slot = runCatching { NudgeSlot.valueOf(intent.getStringExtra(NudgeScheduler.EXTRA_SLOT).orEmpty()) }
            .getOrNull() ?: return

        val pending = goAsync()
        val app = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                runCatching { NudgeRunner.run(app, slot) }
                    .onFailure { android.util.Log.w("Nudge", "run failed", it) }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val EXTRA_NUDGE_TYPE = NudgeRunner.EXTRA_NUDGE_TYPE
        const val EXTRA_RECIPE_NAME = NudgeRunner.EXTRA_RECIPE_NAME
    }
}
