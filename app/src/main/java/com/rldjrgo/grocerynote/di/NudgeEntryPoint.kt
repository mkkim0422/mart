package com.rldjrgo.grocerynote.di

import com.rldjrgo.grocerynote.data.local.SettingsDataStore
import com.rldjrgo.grocerynote.data.recipes.RecipeBook
import com.rldjrgo.grocerynote.data.repository.ItemRepository
import com.rldjrgo.grocerynote.data.repository.StoreRepository
import com.rldjrgo.grocerynote.notify.NudgeScheduler
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Hilt EntryPoint for the nudge BroadcastReceiver / BootReceiver / App.onCreate (not @AndroidEntryPoint). */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface NudgeEntryPoint {
    fun itemRepository(): ItemRepository
    fun storeRepository(): StoreRepository
    fun settingsDataStore(): SettingsDataStore
    fun nudgeScheduler(): NudgeScheduler
    fun recipeBook(): RecipeBook
}
