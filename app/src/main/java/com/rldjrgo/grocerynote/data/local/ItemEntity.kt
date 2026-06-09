package com.rldjrgo.grocerynote.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "items",
    foreignKeys = [
        ForeignKey(
            entity = StoreEntity::class,
            parentColumns = ["id"],
            childColumns = ["store_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["store_id"], name = "idx_items_store_id"),
        Index(value = ["is_completed"], name = "idx_items_is_completed"),
    ],
)
data class ItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "store_id") val storeId: Long,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean = false,
    @ColumnInfo(name = "completed_at") val completedAt: Long? = null,
    @ColumnInfo(name = "display_order") val displayOrder: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    /** Epoch millis of a one-shot purchase reminder, or null when none is set. */
    @ColumnInfo(name = "reminder_at") val reminderAt: Long? = null,
)
