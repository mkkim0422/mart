package com.rldjrgo.grocerynote.ui.screens.settings

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rldjrgo.grocerynote.data.billing.BillingRepository
import com.rldjrgo.grocerynote.data.local.AppDatabase
import com.rldjrgo.grocerynote.data.local.DarkModePref
import com.rldjrgo.grocerynote.data.local.SettingsDataStore
import com.rldjrgo.grocerynote.data.repository.StoreRepository
import com.rldjrgo.grocerynote.domain.model.Store
import com.rldjrgo.grocerynote.util.WidgetPinHelper
import com.rldjrgo.grocerynote.util.WidgetUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val darkMode: DarkModePref = DarkModePref.Off,
    val isAdRemoved: Boolean = false,
    val hasAddedWidget: Boolean = false,
    val stores: List<Store> = emptyList(),
    val largeWidgetStoreIds: List<Long> = emptyList(),
    val version: String = "1.0.0",
    val toast: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val settings: SettingsDataStore,
    private val db: AppDatabase,
    private val storeRepo: StoreRepository,
    private val widgetUpdater: WidgetUpdater,
    private val billing: BillingRepository,
    private val widgetPin: WidgetPinHelper,
) : AndroidViewModel(application) {

    private val toast = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch { runCatching { billing.start() } }
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        settings.darkMode,
        settings.isAdRemoved,
        settings.hasAddedWidget,
        toast,
    ) { dm, ad, hw, t ->
        SettingsUiState(darkMode = dm, isAdRemoved = ad, hasAddedWidget = hw, toast = t)
    }.combine(storeRepo.observeActiveStores()) { s, stores ->
        s.copy(stores = stores)
    }.combine(settings.largeWidgetStoreIds) { s, ids ->
        s.copy(largeWidgetStoreIds = ids)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SettingsUiState())

    fun setDarkMode(pref: DarkModePref) {
        viewModelScope.launch { settings.setDarkMode(pref) }
    }

    fun pinWidget(size: com.rldjrgo.grocerynote.util.WidgetSize): Boolean =
        widgetPin.pinWidget(size)

    fun purchaseRemoveAds(activity: Activity) {
        viewModelScope.launch { runCatching { billing.launchPurchase(activity) } }
    }

    fun wipeAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // CASCADE deletes items with their stores.
                db.clearAllTables()
                // Re-seed the defaults (RoomCallback only runs on first DB create).
                storeRepo.addStore("쿠팡", Color(0xFF3182F6), "emoji:🚀")
                storeRepo.addStore("다이소", Color(0xFFF04452), "store")
                widgetUpdater.updateAll()
                toast.value = "✓ 모두 삭제됨 · 기본 마트 복원"
            } catch (e: Exception) {
                Log.e("Settings", "Delete all failed", e)
                toast.value = "삭제 실패: ${e.message}"
            }
        }
    }

    fun saveLargeWidgetStoreIds(ids: List<Long>) {
        viewModelScope.launch {
            settings.setLargeWidgetStoreIds(ids)
            widgetUpdater.updateAll()
            toast.value = "✓ 위젯 표시 마트 저장됨"
        }
    }

    fun clearToast() { toast.value = null }
}
