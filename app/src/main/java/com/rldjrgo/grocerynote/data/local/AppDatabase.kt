package com.rldjrgo.grocerynote.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(
    entities = [StoreEntity::class, ItemEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun storeDao(): StoreDao
    abstract fun itemDao(): ItemDao

    companion object {
        const val DB_NAME = "grocery_note.db"

        /** Build with the seed callback wired in. */
        fun build(context: Context): AppDatabase {
            // Lazy holder to break the chicken-and-egg between Room.build and the callback
            // needing the built instance to seed.
            val instanceHolder = arrayOfNulls<AppDatabase>(1)
            val seedScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            val callback = object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    val instance = instanceHolder[0] ?: return
                    seedScope.launch {
                        seedDefaultStores(instance.storeDao())
                    }
                }
            }
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DB_NAME,
            )
                .addCallback(callback)
                .build()
            instanceHolder[0] = instance
            return instance
        }

        private suspend fun seedDefaultStores(dao: StoreDao) {
            val now = System.currentTimeMillis()
            val seeds = listOf(
                StoreEntity(
                    name = "이마트",
                    colorHex = "#FFB800",
                    iconKey = "cart",
                    displayOrder = 0,
                    createdAt = now,
                ),
                StoreEntity(
                    name = "다이소",
                    colorHex = "#F04452",
                    iconKey = "store",
                    displayOrder = 1,
                    createdAt = now + 1,
                ),
                StoreEntity(
                    name = "쿠팡",
                    colorHex = "#3182F6",
                    iconKey = "box",
                    displayOrder = 2,
                    createdAt = now + 2,
                ),
            )
            seeds.forEach { dao.insertStore(it) }
        }
    }
}
