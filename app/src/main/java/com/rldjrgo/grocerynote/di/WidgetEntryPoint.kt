package com.rldjrgo.grocerynote.di

import com.rldjrgo.grocerynote.data.repository.ItemRepository
import com.rldjrgo.grocerynote.data.repository.StoreRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt EntryPoint for Glance — Glance widgets are not @AndroidEntryPoint so we have to
 * pull dependencies via EntryPointAccessors.fromApplication(...) from inside
 * GroceryWidget.provideGlance and from ActionCallback.onAction.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun storeRepository(): StoreRepository
    fun itemRepository(): ItemRepository
}
