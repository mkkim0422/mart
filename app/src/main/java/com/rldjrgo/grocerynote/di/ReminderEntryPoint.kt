package com.rldjrgo.grocerynote.di

import com.rldjrgo.grocerynote.data.repository.ItemRepository
import com.rldjrgo.grocerynote.data.repository.StoreRepository
import com.rldjrgo.grocerynote.reminder.ReminderScheduler
import com.rldjrgo.grocerynote.util.WidgetUpdater
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt EntryPoint for the reminder BroadcastReceivers (not @AndroidEntryPoint),
 * pulled via EntryPointAccessors.fromApplication(...) inside onReceive.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface ReminderEntryPoint {
    fun itemRepository(): ItemRepository
    fun storeRepository(): StoreRepository
    fun reminderScheduler(): ReminderScheduler
    fun widgetUpdater(): WidgetUpdater
}
