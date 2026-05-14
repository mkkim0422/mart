package com.rldjrgo.grocerynote.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {

    @Query(
        """
        SELECT * FROM stores
        WHERE is_archived = 0
        ORDER BY display_order ASC, id ASC
        """
    )
    fun observeActiveStores(): Flow<List<StoreEntity>>

    @Query("SELECT * FROM stores WHERE id = :id LIMIT 1")
    suspend fun getStoreById(id: Long): StoreEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertStore(store: StoreEntity): Long

    @Update
    suspend fun updateStore(store: StoreEntity)

    @Delete
    suspend fun deleteStore(store: StoreEntity)

    @Query("UPDATE stores SET is_archived = :archived WHERE id = :id")
    suspend fun archiveStore(id: Long, archived: Boolean)

    @Query("SELECT COALESCE(MAX(display_order), -1) FROM stores")
    suspend fun getMaxOrder(): Int

    /**
     * Reorder a list of stores in a single transaction. The list passed in is the
     * desired left-to-right order; index 0 → display_order 0.
     */
    @Transaction
    suspend fun reorderStores(orderedIds: List<Long>) {
        orderedIds.forEachIndexed { index, id ->
            setDisplayOrder(id, index)
        }
    }

    @Query("UPDATE stores SET display_order = :order WHERE id = :id")
    suspend fun setDisplayOrder(id: Long, order: Int)
}
