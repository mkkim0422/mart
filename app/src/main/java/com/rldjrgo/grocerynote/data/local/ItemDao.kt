package com.rldjrgo.grocerynote.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** Aggregate row for per-mart active item counts (tab badges). */
data class StoreActiveCount(val storeId: Long, val count: Int)

@Dao
interface ItemDao {

    /** Any-change signal for the widget auto-refresher (reacts to add/rename/move/complete/delete). */
    @Query("SELECT * FROM items")
    fun observeAll(): Flow<List<ItemEntity>>

    @Query(
        """
        SELECT * FROM items
        WHERE store_id = :storeId AND is_completed = 0
        ORDER BY display_order ASC, id ASC
        """
    )
    fun observeActiveItemsByStore(storeId: Long): Flow<List<ItemEntity>>

    @Query(
        """
        SELECT * FROM items
        WHERE is_completed = 1
        ORDER BY completed_at DESC
        """
    )
    fun observeCompletedItems(): Flow<List<ItemEntity>>

    @Query(
        """
        SELECT * FROM items
        WHERE store_id = :storeId AND is_completed = 1
        ORDER BY completed_at DESC
        """
    )
    fun observeCompletedItemsByStore(storeId: Long): Flow<List<ItemEntity>>

    @Query(
        """
        SELECT store_id AS storeId, COUNT(*) AS count FROM items
        WHERE is_completed = 0
        GROUP BY store_id
        """
    )
    fun observeActiveCounts(): Flow<List<StoreActiveCount>>

    @Query(
        """
        SELECT store_id AS storeId, COUNT(*) AS count FROM items
        WHERE is_completed = 1
        GROUP BY store_id
        """
    )
    fun observeCompletedCounts(): Flow<List<StoreActiveCount>>

    @Query("SELECT * FROM items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Long): ItemEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertItem(item: ItemEntity): Long

    @Update
    suspend fun updateItem(item: ItemEntity)

    @Delete
    suspend fun deleteItem(item: ItemEntity)

    @Query("UPDATE items SET is_completed = 1, completed_at = :completedAt WHERE id = :id")
    suspend fun markCompleted(id: Long, completedAt: Long = System.currentTimeMillis())

    @Query("UPDATE items SET is_completed = 0, completed_at = NULL WHERE id = :id")
    suspend fun markActive(id: Long)

    /** Set (or clear, when [at] is null) the one-shot reminder time of an item. */
    @Query("UPDATE items SET reminder_at = :at WHERE id = :id")
    suspend fun setReminder(id: Long, at: Long?)

    /** All items that still have a reminder pending — used to re-arm alarms after reboot. */
    @Query("SELECT * FROM items WHERE reminder_at IS NOT NULL")
    suspend fun getItemsWithReminder(): List<ItemEntity>

    @Query(
        """
        SELECT COALESCE(MAX(display_order), -1) FROM items
        WHERE store_id = :storeId AND is_completed = 0
        """
    )
    suspend fun getMaxOrderInStore(storeId: Long): Int

    /**
     * DISTINCT recent item names across all marts — feeds autocomplete chips and
     * "frequently bought" suggestions in the add-item sheet.
     */
    @Query(
        """
        SELECT name FROM items
        GROUP BY name
        ORDER BY MAX(created_at) DESC
        LIMIT :limit
        """
    )
    suspend fun getRecentItemNames(limit: Int = 50): List<String>

    /** Per-mart "frequently bought" — only names ever registered in THIS store. */
    @Query(
        """
        SELECT name FROM items
        WHERE store_id = :storeId
        GROUP BY name
        ORDER BY MAX(created_at) DESC
        LIMIT :limit
        """
    )
    suspend fun getRecentItemNamesByStore(storeId: Long, limit: Int = 20): List<String>

    /** Remove a name from a store's "frequently bought" list (deletes its rows in that store). */
    @Query("DELETE FROM items WHERE store_id = :storeId AND name = :name")
    suspend fun deleteItemsByStoreAndName(storeId: Long, name: String)
}
