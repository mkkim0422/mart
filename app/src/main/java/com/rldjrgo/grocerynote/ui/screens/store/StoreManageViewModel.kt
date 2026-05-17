package com.rldjrgo.grocerynote.ui.screens.store

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rldjrgo.grocerynote.data.repository.ItemRepository
import com.rldjrgo.grocerynote.data.repository.StoreRepository
import com.rldjrgo.grocerynote.domain.model.Store
import com.rldjrgo.grocerynote.util.WidgetUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** One row in the manage list: a store + its active-item count. */
data class StoreRow(
    val store: Store,
    val itemCount: Int,
)

data class StoreManageUiState(
    val rows: List<StoreRow> = emptyList(),
    val isLoading: Boolean = true,
)

/**
 * Backs [StoreManageScreen]. Every mutation also pokes [WidgetUpdater] so placed
 * widgets reflect mart changes within ~1s.
 *
 * Delete = soft delete: [requestDelete] archives the store (it instantly leaves
 * the active list / tabs / widget); a 5-second undo Snackbar then either
 * un-archives ([undoDelete]) or hard-deletes with item cascade ([finalizeDelete]).
 */
@HiltViewModel
class StoreManageViewModel @Inject constructor(
    private val storeRepo: StoreRepository,
    private val itemRepo: ItemRepository,
    private val widgetUpdater: WidgetUpdater,
) : ViewModel() {

    /** Emitted right after a store is archived so the UI can offer undo. */
    data class DeletedStore(val id: Long, val name: String, val token: Long)

    private val _deleted = MutableStateFlow<DeletedStore?>(null)
    val deleted: StateFlow<DeletedStore?> = _deleted

    val uiState: StateFlow<StoreManageUiState> = combine(
        storeRepo.observeActiveStores(),
        itemRepo.observeActiveCounts(),
    ) { stores, counts ->
        StoreManageUiState(
            rows = stores.map { StoreRow(it, counts[it.id] ?: 0) },
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, StoreManageUiState())

    private fun currentStore(id: Long): Store? =
        uiState.value.rows.firstOrNull { it.store.id == id }?.store

    /** Edit name + color + emoji in one shot (same fields as the add sheet). */
    fun updateStore(id: Long, name: String, color: Color, iconKey: String) {
        if (currentStore(id) == null || name.isBlank()) return
        viewModelScope.launch {
            storeRepo.renameStore(id, name.trim(), color, iconKey)
            widgetUpdater.updateAll()
        }
    }

    fun addStore(name: String, color: Color, iconKey: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            storeRepo.addStore(name.trim(), color, iconKey)
            widgetUpdater.updateAll()
        }
    }

    /** Commit a new left-to-right order. Called once on drag end (not per swap). */
    fun persistOrder(orderedIds: List<Long>) {
        viewModelScope.launch {
            storeRepo.reorder(orderedIds)
            widgetUpdater.updateAll()
        }
    }

    /** Soft delete: archive now, offer undo. */
    fun requestDelete(id: Long) {
        val s = currentStore(id) ?: return
        viewModelScope.launch {
            storeRepo.archiveStore(id)
            _deleted.value = DeletedStore(id, s.name, System.currentTimeMillis())
            widgetUpdater.updateAll()
        }
    }

    fun undoDelete(id: Long) {
        viewModelScope.launch {
            storeRepo.unarchiveStore(id)
            _deleted.value = null
            widgetUpdater.updateAll()
        }
    }

    /** Undo window elapsed → permanently remove (items cascade-delete). */
    fun finalizeDelete(id: Long) {
        viewModelScope.launch {
            storeRepo.deleteStore(id)
            _deleted.value = null
            widgetUpdater.updateAll()
        }
    }

    fun consumeDeletedEvent() {
        _deleted.value = null
    }
}
