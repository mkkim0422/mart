package com.rldjrgo.grocerynote.ui.screens.settings

import android.app.Activity
import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rldjrgo.grocerynote.BuildConfig
import com.rldjrgo.grocerynote.data.billing.BillingRepository
import com.rldjrgo.grocerynote.data.local.AppDatabase
import com.rldjrgo.grocerynote.data.local.DarkModePref
import com.rldjrgo.grocerynote.data.local.SettingsDataStore
import com.rldjrgo.grocerynote.data.repository.BackupRepository
import com.rldjrgo.grocerynote.data.repository.StoreRepository
import com.rldjrgo.grocerynote.domain.model.Store
import com.rldjrgo.grocerynote.reminder.ReminderScheduler
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
    // 하드코딩 금지 — 빌드의 실제 versionName을 그대로 표시 (업데이트 때 자동 반영).
    val version: String = BuildConfig.VERSION_NAME,
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
    private val backup: BackupRepository,
    private val reminderScheduler: ReminderScheduler,
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

    /** 설정 → 내보내기: 마트+항목 JSON을 사용자가 고른 파일(uri)에 저장. */
    fun exportTo(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = backup.export()
                val resolver = getApplication<Application>().contentResolver
                resolver.openOutputStream(uri, "wt")?.use { out ->
                    out.write(result.json.toByteArray(Charsets.UTF_8))
                } ?: throw IllegalStateException("파일을 열 수 없어요")
                toast.value = "✓ 내보내기 완료 · 마트 ${result.stores}개, 항목 ${result.items}개"
            } catch (e: Exception) {
                Log.e("Settings", "Export failed", e)
                toast.value = "내보내기 실패: ${e.message}"
            }
        }
    }

    /** 설정 → 가져오기: 백업 JSON을 현재 데이터에 병합(덮어쓰기 아님, 중복 없음). */
    fun importFrom(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val resolver = getApplication<Application>().contentResolver
                val text = resolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                    ?: throw IllegalStateException("파일을 열 수 없어요")
                val r = backup.import(text)
                r.reminders.forEach { (id, at) -> reminderScheduler.schedule(id, at) }
                widgetUpdater.updateAll()
                toast.value = if (r.storesAdded == 0 && r.itemsAdded == 0) {
                    "이미 모두 있는 항목이에요 (건너뜀 ${r.itemsSkipped}개)"
                } else {
                    "✓ 가져오기 완료 · 마트 ${r.storesAdded}개, 항목 ${r.itemsAdded}개 추가"
                }
            } catch (e: Exception) {
                Log.e("Settings", "Import failed", e)
                toast.value = "가져오기 실패: ${e.message}"
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
