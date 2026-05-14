package com.rldjrgo.grocerynote.ui.screens.devtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rldjrgo.grocerynote.data.repository.ItemRepository
import com.rldjrgo.grocerynote.data.repository.StoreRepository
import com.rldjrgo.grocerynote.domain.model.Item
import com.rldjrgo.grocerynote.domain.model.Store
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DevTestUiState(
    val stores: List<Store> = emptyList(),
    val selectedStoreId: Long? = null,
    val activeItems: List<Item> = emptyList(),
    val completedItems: List<Item> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DevTestViewModel @Inject constructor(
    private val storeRepo: StoreRepository,
    private val itemRepo: ItemRepository,
) : ViewModel() {

    private val selectedStoreId = MutableStateFlow<Long?>(null)

    private val storesFlow = storeRepo.observeActiveStores()

    private val activeItemsFlow = selectedStoreId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else itemRepo.observeActiveItems(id)
    }

    private val completedFlow = itemRepo.observeCompletedItems()

    val uiState: StateFlow<DevTestUiState> = combine(
        storesFlow,
        selectedStoreId,
        activeItemsFlow,
        completedFlow,
    ) { stores, selId, active, completed ->
        // Auto-select the first store when none is chosen yet.
        val effectiveId = selId ?: stores.firstOrNull()?.id
        if (selId == null && effectiveId != null) selectedStoreId.value = effectiveId
        DevTestUiState(
            stores = stores,
            selectedStoreId = effectiveId,
            activeItems = active,
            completedItems = completed,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, DevTestUiState())

    fun selectStore(id: Long) {
        selectedStoreId.value = id
    }

    fun addItem(storeId: Long, name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { itemRepo.addItem(storeId, name) }
    }

    fun toggleComplete(item: Item) {
        viewModelScope.launch {
            if (item.isCompleted) itemRepo.reactivateItem(item.id)
            else itemRepo.completeItem(item.id)
        }
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch { itemRepo.deleteItem(item.id) }
    }
}
