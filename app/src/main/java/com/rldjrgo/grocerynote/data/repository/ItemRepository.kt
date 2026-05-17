package com.rldjrgo.grocerynote.data.repository

import com.rldjrgo.grocerynote.data.local.ItemDao
import com.rldjrgo.grocerynote.data.local.ItemEntity
import com.rldjrgo.grocerynote.domain.model.Item
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class ItemRepository @Inject constructor(
    private val dao: ItemDao,
) {

    fun observeActiveItems(storeId: Long): Flow<List<Item>> =
        dao.observeActiveItemsByStore(storeId).map { list -> list.map(ItemEntity::toDomain) }

    /** Emits on ANY items-table change — drives the widget auto-refresher. */
    fun observeAllItems(): Flow<List<Item>> =
        dao.observeAll().map { list -> list.map(ItemEntity::toDomain) }

    /** Per-mart active item counts for the tab badges. */
    fun observeActiveCounts(): Flow<Map<Long, Int>> =
        dao.observeActiveCounts().map { rows -> rows.associate { it.storeId to it.count } }

    /** Per-mart completed item counts for the completed-screen filter badges. */
    fun observeCompletedCounts(): Flow<Map<Long, Int>> =
        dao.observeCompletedCounts().map { rows -> rows.associate { it.storeId to it.count } }

    fun observeCompletedItems(): Flow<List<Item>> =
        dao.observeCompletedItems().map { list -> list.map(ItemEntity::toDomain) }

    fun observeCompletedItemsByStore(storeId: Long): Flow<List<Item>> =
        dao.observeCompletedItemsByStore(storeId).map { list -> list.map(ItemEntity::toDomain) }

    suspend fun getItem(id: Long): Item? = dao.getItemById(id)?.toDomain()

    suspend fun addItem(storeId: Long, name: String): Long {
        val nextOrder = dao.getMaxOrderInStore(storeId) + 1
        val entity = ItemEntity(
            storeId = storeId,
            name = name.trim(),
            displayOrder = nextOrder,
        )
        return dao.insertItem(entity)
    }

    suspend fun renameItem(id: Long, newName: String) {
        val current = dao.getItemById(id) ?: return
        dao.updateItem(current.copy(name = newName.trim()))
    }

    suspend fun moveItemToStore(id: Long, newStoreId: Long) {
        val current = dao.getItemById(id) ?: return
        val nextOrder = dao.getMaxOrderInStore(newStoreId) + 1
        dao.updateItem(current.copy(storeId = newStoreId, displayOrder = nextOrder))
    }

    suspend fun completeItem(id: Long) = dao.markCompleted(id)

    suspend fun reactivateItem(id: Long) {
        val current = dao.getItemById(id) ?: return
        val nextOrder = dao.getMaxOrderInStore(current.storeId) + 1
        dao.updateItem(current.copy(displayOrder = nextOrder))
        dao.markActive(id)
    }

    suspend fun deleteItem(id: Long) {
        val item = dao.getItemById(id) ?: return
        dao.deleteItem(item)
    }

    /** Re-insert a previously deleted item (Snackbar undo). Returns the new id. */
    suspend fun restoreItem(item: Item): Long = dao.insertItem(
        ItemEntity(
            storeId = item.storeId,
            name = item.name,
            isCompleted = item.isCompleted,
            completedAt = item.completedAt,
            displayOrder = item.displayOrder,
            createdAt = item.createdAt,
        ),
    )

    suspend fun recentItemNames(limit: Int = 50): List<String> = dao.getRecentItemNames(limit)

    /** Per-mart "frequently bought" names. */
    suspend fun recentItemNamesByStore(storeId: Long, limit: Int = 20): List<String> =
        dao.getRecentItemNamesByStore(storeId, limit)

    /** Remove a name from a store's frequently-bought list. */
    suspend fun deleteByStoreAndName(storeId: Long, name: String) =
        dao.deleteItemsByStoreAndName(storeId, name)
}

internal fun ItemEntity.toDomain(): Item = Item(
    id = id,
    storeId = storeId,
    name = name,
    isCompleted = isCompleted,
    completedAt = completedAt,
    displayOrder = displayOrder,
    createdAt = createdAt,
)
