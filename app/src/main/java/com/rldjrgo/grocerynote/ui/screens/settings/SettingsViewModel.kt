package com.rldjrgo.grocerynote.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rldjrgo.grocerynote.data.local.AppDatabase
import com.rldjrgo.grocerynote.data.local.DarkModePref
import com.rldjrgo.grocerynote.data.local.SettingsDataStore
import com.rldjrgo.grocerynote.data.local.WidgetRefresh
import com.rldjrgo.grocerynote.data.repository.ItemRepository
import com.rldjrgo.grocerynote.data.repository.StoreRepository
import com.rldjrgo.grocerynote.domain.model.Item
import com.rldjrgo.grocerynote.domain.model.Store
import com.rldjrgo.grocerynote.util.WidgetUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val darkMode: DarkModePref = DarkModePref.Auto,
    val widgetRefresh: WidgetRefresh = WidgetRefresh.Immediate,
    val isAdRemoved: Boolean = false,
    val version: String = "1.0.0",
    val toast: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val settings: SettingsDataStore,
    private val storeRepo: StoreRepository,
    private val itemRepo: ItemRepository,
    private val db: AppDatabase,
) : AndroidViewModel(application) {

    private val toast = MutableStateFlow<String?>(null)
    private val ctx get() = getApplication<Application>()

    val uiState: StateFlow<SettingsUiState> = combine(
        settings.darkMode,
        settings.widgetRefresh,
        settings.isAdRemoved,
        toast,
    ) { dm, wr, ad, t ->
        SettingsUiState(darkMode = dm, widgetRefresh = wr, isAdRemoved = ad, toast = t)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SettingsUiState())

    fun setDarkMode(pref: DarkModePref) {
        viewModelScope.launch { settings.setDarkMode(pref) }
    }

    fun setWidgetRefresh(pref: WidgetRefresh) {
        viewModelScope.launch { settings.setWidgetRefresh(pref) }
    }

    fun wipeAllData() {
        viewModelScope.launch {
            db.clearAllTables()
            toast.value = "전체 삭제 완료"
            WidgetUpdater.requestUpdate(ctx)
        }
    }

    fun clearToast() { toast.value = null }

    /** Snapshot all data into a single JSON string. UI writes this to a SAF Uri. */
    suspend fun exportJson(): String {
        val storeList: List<Store> = storeRepo.observeActiveStores().first()
        val completedList: List<Item> = itemRepo.observeCompletedItems().first()
        val activeItems = mutableListOf<Item>()
        for (s in storeList) {
            activeItems += itemRepo.observeActiveItems(s.id).first()
        }
        val allItems = activeItems + completedList

        return buildString {
            append("{")
            append("\"stores\":[")
            storeList.forEachIndexed { i, s ->
                if (i > 0) append(",")
                append("{")
                append("\"id\":${s.id},")
                append("\"name\":\"${s.name.replace("\"", "\\\"")}\",")
                append("\"colorArgb\":${s.color.value.toString()},")
                append("\"iconKey\":\"${s.iconKey}\",")
                append("\"displayOrder\":${s.displayOrder}")
                append("}")
            }
            append("],\"items\":[")
            allItems.forEachIndexed { i, it ->
                if (i > 0) append(",")
                append("{")
                append("\"id\":${it.id},")
                append("\"storeId\":${it.storeId},")
                append("\"name\":\"${it.name.replace("\"", "\\\"")}\",")
                append("\"isCompleted\":${it.isCompleted},")
                append("\"completedAt\":${it.completedAt ?: "null"},")
                append("\"displayOrder\":${it.displayOrder},")
                append("\"createdAt\":${it.createdAt}")
                append("}")
            }
            append("]}")
        }
    }
}
