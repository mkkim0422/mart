package com.rldjrgo.grocerynote.ui.screens.home

import android.app.Application
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val stores: List<Store> = emptyList(),
    val selectedStoreId: Long? = null,
    val activeItems: List<Item> = emptyList(),
    val recentItemNames: List<String> = emptyList(),
    val highlightItemId: Long? = null,
    val isLoading: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val storeRepo: StoreRepository,
    private val itemRepo: ItemRepository,
) : AndroidViewModel(application) {

    private val ctx get() = getApplication<Application>()

    private val selectedStoreId = MutableStateFlow<Long?>(null)
    private val highlightItemId = MutableStateFlow<Long?>(null)
    private val recentNames = MutableStateFlow<List<String>>(emptyList())

    private val storesFlow = storeRepo.observeActiveStores()
    private val activeItemsFlow = selectedStoreId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else itemRepo.observeActiveItems(id)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        storesFlow,
        selectedStoreId,
        activeItemsFlow,
        recentNames,
        highlightItemId,
    ) { stores, selId, items, names, hl ->
        val effective = selId ?: stores.firstOrNull()?.id
        if (selId == null && effective != null) selectedStoreId.value = effective
        HomeUiState(
            stores = stores,
            selectedStoreId = effective,
            activeItems = items,
            recentItemNames = names,
            highlightItemId = hl,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, HomeUiState())

    init {
        refreshRecentNames()
    }

    fun selectStore(id: Long) {
        selectedStoreId.value = id
    }

    /** Called by MainActivity when the widget deep-links into the app. */
    fun handleDeepLink(storeId: Long?, itemId: Long?) {
        storeId?.let { selectedStoreId.value = it }
        highlightItemId.value = itemId
    }

    fun clearHighlight() {
        highlightItemId.value = null
    }

    fun addItem(name: String) {
        val sid = selectedStoreId.value ?: return
        if (name.isBlank()) return
        viewModelScope.launch {
            itemRepo.addItem(sid, name)
            refreshRecentNames()
            WidgetUpdater.requestUpdate(ctx)
        }
    }

    fun completeItem(itemId: Long) {
        viewModelScope.launch {
            itemRepo.completeItem(itemId)
            WidgetUpdater.requestUpdate(ctx)
        }
    }

    fun updateItemName(itemId: Long, newName: String) {
        viewModelScope.launch {
            itemRepo.renameItem(itemId, newName)
            refreshRecentNames()
            WidgetUpdater.requestUpdate(ctx)
        }
    }

    fun moveItem(itemId: Long, newStoreId: Long) {
        viewModelScope.launch {
            itemRepo.moveItemToStore(itemId, newStoreId)
            WidgetUpdater.requestUpdate(ctx)
        }
    }

    fun deleteItem(itemId: Long) {
        viewModelScope.launch {
            itemRepo.deleteItem(itemId)
            WidgetUpdater.requestUpdate(ctx)
        }
    }

    fun addStore(name: String, color: Color, iconKey: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val newId = storeRepo.addStore(name, color, iconKey)
            selectedStoreId.value = newId
            WidgetUpdater.requestUpdate(ctx)
        }
    }

    fun deleteStore(storeId: Long) {
        viewModelScope.launch {
            storeRepo.deleteStore(storeId)
            if (selectedStoreId.value == storeId) selectedStoreId.value = null
            WidgetUpdater.requestUpdate(ctx)
        }
    }

    private fun refreshRecentNames() {
        viewModelScope.launch {
            recentNames.value = itemRepo.recentItemNames(limit = 50)
        }
    }
}
