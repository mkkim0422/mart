package com.rldjrgo.grocerynote.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(
    entities = [StoreEntity::class, ItemEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun storeDao(): StoreDao
    abstract fun itemDao(): ItemDao

    companion object {
        const val DB_NAME = "grocery_note.db"

        /** v1 → v2: add the nullable one-shot reminder column on items. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE items ADD COLUMN reminder_at INTEGER")
            }
        }

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
                .addMigrations(MIGRATION_1_2)
                .build()
            instanceHolder[0] = instance
            return instance
        }

        private suspend fun seedDefaultStores(dao: StoreDao) {
            val now = System.currentTimeMillis()
            // Default marts on first install: 쿠팡, 다이소 (이마트 제외).
            val seeds = listOf(
                StoreEntity(
                    name = "쿠팡",
                    colorHex = "#3182F6",
                    iconKey = "emoji:🚀",
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
            )
            seeds.forEach { dao.insertStore(it) }
        }
    }
}
