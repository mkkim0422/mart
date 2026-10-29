package com.rldjrgo.grocerynote.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stores",
    indices = [Index(value = ["display_order"], name = "idx_stores_display_order")],
)
data class StoreEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "color_hex") val colorHex: String,
    @ColumnInfo(name = "icon_key") val iconKey: String,
    @ColumnInfo(name = "display_order") val displayOrder: Int,
    @ColumnInfo(name = "is_archived") val isArchived: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    /** "쇼핑몰 앱 바로가기" switch for this mart (only matters if its name maps to an app). Default ON. Added in DB v3. */
    @ColumnInfo(name = "shop_app_enabled", defaultValue = "1") val shopAppEnabled: Boolean = true,
)
