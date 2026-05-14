package com.rldjrgo.grocerynote.data.repository

import androidx.compose.ui.graphics.Color
import com.rldjrgo.grocerynote.data.local.StoreDao
import com.rldjrgo.grocerynote.data.local.StoreEntity
import com.rldjrgo.grocerynote.domain.model.Store
import com.rldjrgo.grocerynote.domain.model.toComposeColorOrDefault
import com.rldjrgo.grocerynote.domain.model.toHexString
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class StoreRepository @Inject constructor(
    private val dao: StoreDao,
) {

    fun observeActiveStores(): Flow<List<Store>> =
        dao.observeActiveStores().map { list -> list.map(StoreEntity::toDomain) }

    suspend fun getStore(id: Long): Store? = dao.getStoreById(id)?.toDomain()

    suspend fun addStore(name: String, color: Color, iconKey: String): Long {
        val nextOrder = dao.getMaxOrder() + 1
        val entity = StoreEntity(
            name = name.trim(),
            colorHex = color.toHexString(),
            iconKey = iconKey,
            displayOrder = nextOrder,
        )
        return dao.insertStore(entity)
    }

    suspend fun renameStore(id: Long, newName: String, newColor: Color, newIconKey: String) {
        val current = dao.getStoreById(id) ?: return
        dao.updateStore(
            current.copy(
                name = newName.trim(),
                colorHex = newColor.toHexString(),
                iconKey = newIconKey,
            )
        )
    }

    suspend fun archiveStore(id: Long) = dao.archiveStore(id, archived = true)
    suspend fun unarchiveStore(id: Long) = dao.archiveStore(id, archived = false)

    suspend fun deleteStore(id: Long) {
        val store = dao.getStoreById(id) ?: return
        dao.deleteStore(store)
    }

    suspend fun reorder(orderedIds: List<Long>) = dao.reorderStores(orderedIds)
}

internal fun StoreEntity.toDomain(): Store = Store(
    id = id,
    name = name,
    color = colorHex.toComposeColorOrDefault(),
    iconKey = iconKey,
    displayOrder = displayOrder,
    isArchived = isArchived,
    createdAt = createdAt,
)
