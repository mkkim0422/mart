package com.rldjrgo.grocerynote.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

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
}
