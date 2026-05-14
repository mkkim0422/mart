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
    val filterStoreId: Long? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CompletedViewModel @Inject constructor(
    application: Application,
    private val storeRepo: StoreRepository,
    private val itemRepo: ItemRepository,
) : AndroidViewModel(application) {

    private val ctx get() = getApplication<Application>()
    private val filterFlow = MutableStateFlow<Long?>(null)

    private val itemsFlow = filterFlow.flatMapLatest { sid ->
        if (sid == null) itemRepo.observeCompletedItems()
        else itemRepo.observeCompletedItemsByStore(sid)
    }

    val uiState: StateFlow<CompletedUiState> = combine(
        itemsFlow,
        storeRepo.observeActiveStores(),
        filterFlow,
    ) { items, stores, filter ->
        CompletedUiState(
            items = items,
            stores = stores,
            storesById = stores.associateBy { it.id },
            filterStoreId = filter,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CompletedUiState())

    fun setFilter(storeId: Long?) {
        filterFlow.value = storeId
    }

    fun reactivate(itemId: Long) {
        viewModelScope.launch {
            itemRepo.reactivateItem(itemId)
            WidgetUpdater.requestUpdate(ctx)
        }
    }

    fun delete(itemId: Long) {
        viewModelScope.launch {
            itemRepo.deleteItem(itemId)
            WidgetUpdater.requestUpdate(ctx)
        }
    }
}
