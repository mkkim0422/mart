package com.rldjrgo.grocerynote.data.repository

import androidx.room.withTransaction
import com.rldjrgo.grocerynote.data.local.AppDatabase
import com.rldjrgo.grocerynote.data.local.ItemDao
import com.rldjrgo.grocerynote.data.local.ItemEntity
import com.rldjrgo.grocerynote.data.local.StoreDao
import com.rldjrgo.grocerynote.data.local.StoreEntity
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONArray
import org.json.JSONObject

/**
 * 설정 → "내보내기 / 가져오기". 마트 + 항목을 JSON 한 파일로.
 *
 * Import is a MERGE, never a wipe: marts are matched by name (case/space
 * insensitive), items are skipped when the same name already exists in that
 * mart in the same state (active / completed). So restoring a backup on a
 * fresh install (seeded 쿠팡/다이소) or importing twice never duplicates.
 */
@Singleton
class BackupRepository @Inject constructor(
    private val db: AppDatabase,
    private val storeDao: StoreDao,
    private val itemDao: ItemDao,
) {

    data class ExportResult(val json: String, val stores: Int, val items: Int)

    data class ImportResult(
        val storesAdded: Int,
        val itemsAdded: Int,
        val itemsSkipped: Int,
        /** (new item id, reminderAt) for reminders still in the future — caller re-arms alarms. */
        val reminders: List<Pair<Long, Long>>,
    )

    suspend fun export(): ExportResult {
        val stores = storeDao.getAllStores()
        val items = itemDao.getAllItems().groupBy { it.storeId }
        val storesJson = JSONArray()
        stores.forEach { s ->
            val itemsJson = JSONArray()
            items[s.id].orEmpty().forEach { i ->
                itemsJson.put(
                    JSONObject()
                        .put("name", i.name)
                        .put("isCompleted", i.isCompleted)
                        .put("completedAt", i.completedAt ?: JSONObject.NULL)
                        .put("displayOrder", i.displayOrder)
                        .put("createdAt", i.createdAt)
                        .put("reminderAt", i.reminderAt ?: JSONObject.NULL),
                )
            }
            storesJson.put(
                JSONObject()
                    .put("name", s.name)
                    .put("colorHex", s.colorHex)
                    .put("iconKey", s.iconKey)
                    .put("displayOrder", s.displayOrder)
                    .put("isArchived", s.isArchived)
                    .put("createdAt", s.createdAt)
                    .put("items", itemsJson),
            )
        }
        val root = JSONObject()
            .put("app", APP_TAG)
            .put("format", FORMAT_VERSION)
            .put("exportedAt", System.currentTimeMillis())
            .put("stores", storesJson)
        return ExportResult(root.toString(2), stores.size, items.values.sumOf { it.size })
    }

    /** @throws IllegalArgumentException when [json] is not a MartNote backup. */
    suspend fun import(json: String): ImportResult {
        val root = runCatching { JSONObject(json) }
            .getOrElse { throw IllegalArgumentException("마트노트 백업 파일이 아니에요") }
        require(root.optString("app") == APP_TAG) { "마트노트 백업 파일이 아니에요" }
        val storesJson = root.optJSONArray("stores") ?: JSONArray()
        val now = System.currentTimeMillis()

        return db.withTransaction {
            var storesAdded = 0
            var itemsAdded = 0
            var itemsSkipped = 0
            val reminders = mutableListOf<Pair<Long, Long>>()

            val existingStores = storeDao.getAllStores().associateBy { it.name.key() }.toMutableMap()
            var nextStoreOrder = storeDao.getMaxOrder() + 1

            for (si in 0 until storesJson.length()) {
                val sj = storesJson.optJSONObject(si) ?: continue
                val name = sj.optString("name").trim()
                if (name.isEmpty()) continue

                val store = existingStores[name.key()] ?: run {
                    val entity = StoreEntity(
                        name = name,
                        colorHex = sj.optString("colorHex", "#3182F6"),
                        iconKey = sj.optString("iconKey", "store"),
                        displayOrder = nextStoreOrder++,
                        isArchived = sj.optBoolean("isArchived", false),
                        createdAt = sj.optLong("createdAt", now),
                    )
                    val id = storeDao.insertStore(entity)
                    storesAdded++
                    entity.copy(id = id).also { existingStores[name.key()] = it }
                }

                val existingItems = itemDao.getAllItems()
                    .filter { it.storeId == store.id }
                    .map { it.name.key() to it.isCompleted }
                    .toMutableSet()
                var nextItemOrder = itemDao.getMaxOrderInStore(store.id) + 1

                val itemsJson = sj.optJSONArray("items") ?: JSONArray()
                for (ii in 0 until itemsJson.length()) {
                    val ij = itemsJson.optJSONObject(ii) ?: continue
                    val itemName = ij.optString("name").trim()
                    if (itemName.isEmpty()) continue
                    val completed = ij.optBoolean("isCompleted", false)
                    if (!existingItems.add(itemName.key() to completed)) {
                        itemsSkipped++
                        continue
                    }
                    val reminderAt = ij.optLong("reminderAt", 0L).takeIf { !completed && it > now }
                    val id = itemDao.insertItem(
                        ItemEntity(
                            storeId = store.id,
                            name = itemName,
                            isCompleted = completed,
                            completedAt = ij.optLong("completedAt", 0L).takeIf { completed && it > 0 },
                            displayOrder = if (completed) ij.optInt("displayOrder", 0) else nextItemOrder++,
                            createdAt = ij.optLong("createdAt", now),
                            reminderAt = reminderAt,
                        ),
                    )
                    itemsAdded++
                    if (reminderAt != null) reminders += id to reminderAt
                }
            }
            ImportResult(storesAdded, itemsAdded, itemsSkipped, reminders)
        }
    }

    private fun String.key(): String = trim().replace(WHITESPACE, "").lowercase()

    private companion object {
        const val APP_TAG = "martnote"
        const val FORMAT_VERSION = 1
        val WHITESPACE = Regex("\\s+")
    }
}
