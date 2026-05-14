package com.rldjrgo.grocerynote.di

import android.content.Context
import com.rldjrgo.grocerynote.data.local.AppDatabase
import com.rldjrgo.grocerynote.data.local.ItemDao
import com.rldjrgo.grocerynote.data.local.StoreDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.build(context)

    @Provides
    fun provideStoreDao(db: AppDatabase): StoreDao = db.storeDao()

    @Provides
    fun provideItemDao(db: AppDatabase): ItemDao = db.itemDao()
}
