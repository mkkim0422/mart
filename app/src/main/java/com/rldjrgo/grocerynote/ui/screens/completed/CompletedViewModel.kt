package com.rldjrgo.grocerynote.ui.screens.completed

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rldjrgo.grocerynote.data.repository.ItemRepository
import com.rldjrgo.grocerynote.data.repository.StoreRepository
import com.rldjrgo.grocerynote.domain.model.Item
import com.rldjrgo.grocerynote.domain.model.Store
import com.rldjrgo.grocerynote.util.WidgetUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CompletedUiState(
    val items: List<Item> = emptyList(),
    val stores: List<Store> = emptyList(),
    val storesById: Map<Long, Store> = emptyMap(),
    val completedCounts: Map<Long, Int> = emptyMap(),
    val filterStoreId: Long? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CompletedViewModel @Inject constructor(
    application: Application,
    private val storeRepo: StoreRepository,
    private val itemRepo: ItemRepository,
    private val widgetUpdater: WidgetUpdater,
) : AndroidViewModel(application) {

    private val filterFlow = MutableStateFlow<Long?>(null)

    enum class UndoKind { Reactivated, Deleted }
    data class CompletedUndo(
        val kind: UndoKind,
        val itemId: Long,
        val name: String,
        val snapshot: Item?,
        val token: Long,
    )

    private val _undoEvent = MutableStateFlow<CompletedUndo?>(null)
    val undoEvent: StateFlow<CompletedUndo?> = _undoEvent

    private val itemsFlow = filterFlow.flatMapLatest { sid ->
        if (sid == null) itemRepo.observeCompletedItems()
        else itemRepo.observeCompletedItemsByStore(sid)
    }

    val uiState: StateFlow<CompletedUiState> = combine(
        itemsFlow,
        storeRepo.observeActiveStores(),
        filterFlow,
        itemRepo.observeCompletedCounts(),
    ) { items, stores, filter, counts ->
        CompletedUiState(
            items = items,
            stores = stores,
            storesById = stores.associateBy { it.id },
            completedCounts = counts,
            filterStoreId = filter,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CompletedUiState())

    fun setFilter(storeId: Long?) {
        filterFlow.value = storeId
    }

    fun reactivate(itemId: Long) {
        viewModelScope.launch {
            val item = itemRepo.getItem(itemId)
            itemRepo.reactivateItem(itemId)
            _undoEvent.value = CompletedUndo(
                UndoKind.Reactivated, itemId, item?.name ?: "", item, System.currentTimeMillis(),
            )
            widgetUpdater.updateAll()
        }
    }

    fun delete(itemId: Long) {
        viewModelScope.launch {
            val item = itemRepo.getItem(itemId)
            itemRepo.deleteItem(itemId)
            _undoEvent.value = CompletedUndo(
                UndoKind.Deleted, itemId, item?.name ?: "", item, System.currentTimeMillis(),
            )
            widgetUpdater.updateAll()
        }
    }

    fun undo(u: CompletedUndo) {
        viewModelScope.launch {
            when (u.kind) {
                UndoKind.Reactivated -> itemRepo.completeItem(u.itemId)
                UndoKind.Deleted -> u.snapshot?.let { itemRepo.restoreItem(it) }
            }
            _undoEvent.value = null
            widgetUpdater.updateAll()
        }
    }

    fun consumeUndoEvent() {
        _undoEvent.value = null
    }
}
