package com.rldjrgo.grocerynote.ui.screens.home

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rldjrgo.grocerynote.data.local.SettingsDataStore
import com.rldjrgo.grocerynote.data.repository.ItemRepository
import com.rldjrgo.grocerynote.data.repository.StoreRepository
import com.rldjrgo.grocerynote.domain.model.Item
import com.rldjrgo.grocerynote.domain.model.Store
import com.rldjrgo.grocerynote.reminder.ReminderScheduler
import com.rldjrgo.grocerynote.util.Analytics
import com.rldjrgo.grocerynote.util.WidgetPinHelper
import com.rldjrgo.grocerynote.util.WidgetUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val stores: List<Store> = emptyList(),
    val selectedStoreId: Long? = null,
    val activeItems: List<Item> = emptyList(),
    /** [activeItems]가 실제로 어느 마트의 리스트인지. 스와이프 직후 selectedStoreId만
     *  먼저 바뀌고 아이템은 한두 프레임 늦게 도착하는 구간을 UI가 구분할 수 있게 한다. */
    val activeItemsStoreId: Long? = null,
    val itemCounts: Map<Long, Int> = emptyMap(),
    val recentItemNames: List<String> = emptyList(),
    val highlightItemId: Long? = null,
    val showWidgetBanner: Boolean = false,
    /** One-time "🔍 = 쿠팡에서 바로 검색" tip on online-mart tabs. */
    val showSearchHint: Boolean = false,
    val isLoading: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
/** Completed items before the one-time in-app review ask. */
private const val REVIEW_AT = 10

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val storeRepo: StoreRepository,
    private val itemRepo: ItemRepository,
    private val widgetUpdater: WidgetUpdater,
    private val settings: SettingsDataStore,
    private val widgetPin: WidgetPinHelper,
    private val reminderScheduler: ReminderScheduler,
) : AndroidViewModel(application) {

    /** Emitted right after an item is completed so the UI can show an undo Snackbar. */
    data class UndoInfo(val itemId: Long, val name: String, val token: Long)

    private val _undoEvent = MutableStateFlow<UndoInfo?>(null)
    val undoEvent: StateFlow<UndoInfo?> = _undoEvent

    /** True once, at the [REVIEW_AT]th completed item → HomeScreen shows Google's in-app review. */
    private val _askReview = MutableStateFlow(false)
    val askReview: StateFlow<Boolean> = _askReview

    fun consumeReviewAsk() {
        _askReview.value = false
    }

    /** First-run mart picker (1.5.1): fresh installs only — anyone with items never sees it. */
    private val _showMartPicker = MutableStateFlow(false)
    val showMartPicker: StateFlow<Boolean> = _showMartPicker

    fun addPresetMarts(presets: List<com.rldjrgo.grocerynote.ui.components.MartPreset>) {
        _showMartPicker.value = false
        viewModelScope.launch {
            settings.setMartPickerSeen()
            presets.forEach { storeRepo.addStore(it.name, it.color, it.iconKey) }
            widgetUpdater.updateAll()
        }
    }

    fun dismissMartPicker() {
        _showMartPicker.value = false
        viewModelScope.launch { settings.setMartPickerSeen() }
    }

    /** Set briefly after addStore so the tab strip can auto-scroll + pulse the new pill. */
    private val _newlyAddedStoreId = MutableStateFlow<Long?>(null)
    val newlyAddedStoreId: StateFlow<Long?> = _newlyAddedStoreId

    /** 첫 목록 작성 직후 1회성 위젯 유도 시트 표시 신호. */
    private val _showWidgetNudge = MutableStateFlow(false)
    val showWidgetNudge: StateFlow<Boolean> = _showWidgetNudge
    private var nudgeChecked = false
    private var hasAddedItemThisSession = false

    private companion object {
        /** 위젯 유도 시트 재노출 간격 — 보름. */
        const val NUDGE_INTERVAL_MS = 15L * 24 * 60 * 60 * 1000
    }

    private val selectedStoreId = MutableStateFlow<Long?>(null)
    private val highlightItemId = MutableStateFlow<Long?>(null)
    private val recentNames = MutableStateFlow<List<String>>(emptyList())

    private val storesFlow = storeRepo.observeActiveStores()
    private val activeItemsFlow = selectedStoreId.flatMapLatest { id ->
        if (id == null) flowOf<Pair<Long?, List<Item>>>(null to emptyList())
        else itemRepo.observeActiveItems(id).map { id as Long? to it }
    }
    private val countsFlow = itemRepo.observeActiveCounts()
    private val bannerFlow = combine(
        settings.hasAddedWidget,
        settings.hasDismissedWidgetBanner,
    ) { added, dismissed -> !added && !dismissed }

    val uiState: StateFlow<HomeUiState> = combine(
        storesFlow,
        selectedStoreId,
        activeItemsFlow,
        recentNames,
        highlightItemId,
    ) { stores, selId, items, names, hl ->
        HomeUiState(
            stores = stores,
            selectedStoreId = selId ?: stores.firstOrNull()?.id,
            activeItems = items.second,
            activeItemsStoreId = items.first,
            recentItemNames = names,
            highlightItemId = hl,
            isLoading = false,
        )
    }.combine(countsFlow) { state, counts ->
        state.copy(itemCounts = counts)
    }.combine(bannerFlow) { state, showBanner ->
        state.copy(showWidgetBanner = showBanner)
    }.combine(settings.hasSeenSearchHint) { state, seen ->
        state.copy(showSearchHint = !seen)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, HomeUiState())

    /** Called when the tip is closed or the 🔍 is used for the first time. */
    fun dismissSearchHint() {
        viewModelScope.launch { settings.setSearchHintSeen() }
    }

    init {
        refreshRecentNames()
        // Keep selectedStoreId valid. Critical after "전체 삭제": the old store ids
        // are gone and replaced by new ones — if we kept the stale id, addItem()
        // would insert with a non-existent store_id and the FK constraint would
        // reject it (item silently never saved → widget/app stay empty).
        viewModelScope.launch {
            storesFlow.collect { stores ->
                val cur = selectedStoreId.value
                if (stores.isNotEmpty() && (cur == null || stores.none { it.id == cur })) {
                    selectedStoreId.value = stores.first().id
                }
            }
        }
        viewModelScope.launch {
            if (settings.hasSeenMartPicker.first()) return@launch
            val fresh = itemRepo.observeAllItems().first().isEmpty() && storesFlow.first().size <= 2
            if (fresh) _showMartPicker.value = true else settings.setMartPickerSeen()
        }
        // "자주 사는 항목" is per-mart → refresh whenever the selected store changes.
        viewModelScope.launch {
            selectedStoreId.collect { refreshRecentNames() }
        }
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

    fun pinWidget(kind: com.rldjrgo.grocerynote.util.WidgetKind, martId: Long?): Boolean =
        widgetPin.pinWidget(kind, martId)

    fun dismissWidgetBanner() {
        viewModelScope.launch { settings.setHasDismissedWidgetBanner(true) }
    }

    fun addItem(name: String) {
        if (name.isBlank()) return
        hasAddedItemThisSession = true
        viewModelScope.launch {
            val stores = storesFlow.first()
            if (stores.isEmpty()) return@launch
            val cur = selectedStoreId.value
            // Never trust a stale id (post-wipe it points at a deleted store → FK fail).
            val sid = if (cur != null && stores.any { it.id == cur }) cur else stores.first().id
            if (selectedStoreId.value != sid) selectedStoreId.value = sid
            itemRepo.addItem(sid, name)
            refreshRecentNames()
            widgetUpdater.updateAll()
        }
    }

    /** 공유받은 글(카톡 등)에서 뽑은 여러 항목을 [storeId] 마트에 한 번에 담는다. */
    /**
     * Add shared/received names to a mart, skipping ones already on that mart's list
     * (same name, ignoring case/spaces at the ends) — sharing the same list twice
     * shouldn't double it. [onDone] gets (added, skipped) for the snackbar.
     */
    fun addItems(storeId: Long, names: List<String>, onDone: (added: Int, skipped: Int) -> Unit = { _, _ -> }) {
        val clean = names.map { it.trim() }.filter { it.isNotEmpty() }
        if (clean.isEmpty()) return
        hasAddedItemThisSession = true
        viewModelScope.launch {
            val stores = storesFlow.first()
            val sid = if (stores.any { it.id == storeId }) storeId else stores.firstOrNull()?.id ?: return@launch
            selectedStoreId.value = sid
            val existing = itemRepo.observeActiveItems(sid).first().map { it.name.trim().lowercase() }.toSet()
            val fresh = clean.filter { it.lowercase() !in existing }
            fresh.forEach { itemRepo.addItem(sid, it) }
            onDone(fresh.size, clean.size - fresh.size)
            if (fresh.isEmpty()) return@launch
            Analytics.sharedItemsAdded(fresh.size)
            refreshRecentNames()
            widgetUpdater.updateAll()
        }
    }

    /** Persist a long-press drag reorder of the current mart's list. */
    fun reorderItems(orderedIds: List<Long>) {
        viewModelScope.launch {
            itemRepo.reorder(orderedIds)
            Analytics.itemsReordered()
            widgetUpdater.updateAll()
        }
    }

    fun completeItem(itemId: Long, itemName: String) {
        viewModelScope.launch {
            // A pending reminder on a now-bought item is pointless → drop it.
            itemRepo.setReminder(itemId, null)
            reminderScheduler.cancel(itemId)
            itemRepo.completeItem(itemId)
            _undoEvent.value = UndoInfo(itemId, itemName, System.currentTimeMillis())
            widgetUpdater.updateAll()
            // A finished shopping run is the happy moment to ask for a rating (once; Google
            // also rate-limits the dialog itself). Reviews were 13 → search rank stuck.
            if (settings.bumpCompletedCount() >= REVIEW_AT && !settings.reviewAsked.first()) {
                settings.setReviewAsked()
                _askReview.value = true
            }
        }
    }

    /** 추가 흐름이 끝났을 때(추가 시트 닫힘 / 음성추가 종료) 호출 — 방금 항목을
     *  등록했다면 위젯 유도 시트를 띄운다. "목록을 다 만든 직후"가 홈화면에서
     *  보고 싶어지는 순간이라 여기가 유도 타이밍 (완료 체크는 보통 마트 안이라 부적절). */
    fun onAddFlowFinished() {
        if (!hasAddedItemThisSession) return
        maybeShowWidgetNudge()
    }

    /** 위젯 유도 시트. 위젯이 이미 홈화면에 있으면 안 뜨고, 없으면 15일에
     *  한 번(그리고 앱 세션당 최대 1회)만 재노출 — 남발 금지(§3 정신). */
    private fun maybeShowWidgetNudge() {
        if (nudgeChecked) return
        nudgeChecked = true
        viewModelScope.launch {
            val last = settings.widgetNudgeLastShownAt.first()
            if (System.currentTimeMillis() - last < NUDGE_INTERVAL_MS) return@launch
            if (widgetPin.anyWidgetPlaced()) return@launch
            // 추가 시트 닫힘 애니메이션이 끝난 뒤에 떠야 자연스럽다.
            kotlinx.coroutines.delay(600)
            if (widgetPin.anyWidgetPlaced()) return@launch
            _showWidgetNudge.value = true
        }
    }

    fun dismissWidgetNudge() {
        _showWidgetNudge.value = false
        viewModelScope.launch {
            settings.setWidgetNudgeShownAt(System.currentTimeMillis())
        }
    }

    /** 홈화면의 실제 위젯 설치 여부를 DataStore에 반영 — 유도 배너가 설치되면
     *  자동으로 사라지고, 전부 제거되면(닫기 안 눌렀다면) 다시 나타난다. 런처에서
     *  직접 추가/제거한 경우까지 잡기 위해 onResume마다 호출된다. */
    fun syncWidgetPresence() {
        viewModelScope.launch { settings.setHasAddedWidget(widgetPin.anyWidgetPlaced()) }
    }

    /** Set a one-shot purchase reminder at [atMillis] (epoch). Caller ensures the
     *  POST_NOTIFICATIONS permission (13+) and that [atMillis] is in the future. */
    fun setReminder(itemId: Long, atMillis: Long) {
        viewModelScope.launch {
            itemRepo.setReminder(itemId, atMillis)
            reminderScheduler.schedule(itemId, atMillis)
            Analytics.reminderSet()
            widgetUpdater.updateAll()
        }
    }

    fun clearReminder(itemId: Long) {
        viewModelScope.launch {
            itemRepo.setReminder(itemId, null)
            reminderScheduler.cancel(itemId)
            widgetUpdater.updateAll()
        }
    }

    /** Snackbar [되돌리기] → put the item back into the active list. */
    fun undoComplete(itemId: Long) {
        viewModelScope.launch {
            itemRepo.reactivateItem(itemId)
            _undoEvent.value = null
            widgetUpdater.updateAll()
        }
    }

    fun consumeUndoEvent() {
        _undoEvent.value = null
    }

    fun updateItemName(itemId: Long, newName: String) {
        viewModelScope.launch {
            itemRepo.renameItem(itemId, newName)
            refreshRecentNames()
            widgetUpdater.updateAll()
        }
    }

    fun moveItem(itemId: Long, newStoreId: Long) {
        viewModelScope.launch {
            itemRepo.moveItemToStore(itemId, newStoreId)
            widgetUpdater.updateAll()
        }
    }

    fun deleteItem(itemId: Long) {
        viewModelScope.launch {
            reminderScheduler.cancel(itemId)
            itemRepo.deleteItem(itemId)
            widgetUpdater.updateAll()
        }
    }

    /** "쇼핑몰 앱 바로가기" switch at the top of a mart's list (per mart, default ON). */
    fun setShopAppEnabled(storeId: Long, enabled: Boolean) {
        viewModelScope.launch { storeRepo.setShopAppEnabled(storeId, enabled) }
    }

    /**
     * Received list link for a mart the receiver doesn't have (1.5.0): create it with the
     * sender's color/emoji, add the items, and land on its tab.
     */
    fun addStoreWithItems(name: String, color: Color, iconKey: String, names: List<String>) {
        val clean = names.map { it.trim() }.filter { it.isNotEmpty() }
        if (name.isBlank() || clean.isEmpty()) return
        hasAddedItemThisSession = true
        viewModelScope.launch {
            val newId = storeRepo.addStore(name, color, iconKey)
            clean.forEach { itemRepo.addItem(newId, it) }
            selectedStoreId.value = newId
            _newlyAddedStoreId.value = newId
            Analytics.sharedItemsAdded(clean.size)
            refreshRecentNames()
            widgetUpdater.updateAll()
            kotlinx.coroutines.delay(1_500)
            if (_newlyAddedStoreId.value == newId) _newlyAddedStoreId.value = null
        }
    }

    fun addStore(name: String, color: Color, iconKey: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val newId = storeRepo.addStore(name, color, iconKey)
            selectedStoreId.value = newId
            _newlyAddedStoreId.value = newId
            widgetUpdater.updateAll()
            kotlinx.coroutines.delay(1_500)
            if (_newlyAddedStoreId.value == newId) _newlyAddedStoreId.value = null
        }
    }

    /** Persist a tab-strip drag reorder (jiggle edit mode). */
    fun reorderStores(orderedIds: List<Long>) {
        viewModelScope.launch {
            storeRepo.reorder(orderedIds)
            widgetUpdater.updateAll()
        }
    }

    fun deleteStore(storeId: Long) {
        viewModelScope.launch {
            storeRepo.deleteStore(storeId)
            if (selectedStoreId.value == storeId) selectedStoreId.value = null
            widgetUpdater.updateAll()
        }
    }

    private fun refreshRecentNames() {
        viewModelScope.launch {
            val sid = selectedStoreId.value ?: storesFlow.first().firstOrNull()?.id
            recentNames.value =
                if (sid == null) emptyList() else itemRepo.recentItemNamesByStore(sid, 20)
        }
    }

    /** Delete a "자주 사는 항목" entry from the current mart. */
    fun deleteFrequentItem(name: String) {
        viewModelScope.launch {
            val sid = selectedStoreId.value ?: return@launch
            itemRepo.deleteByStoreAndName(sid, name)
            refreshRecentNames()
            widgetUpdater.updateAll()
        }
    }
}
