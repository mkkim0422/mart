package com.rldjrgo.grocerynote.domain.model

/**
 * Domain representation of a shopping item. Pure data — no Room/Compose deps.
 */
data class Item(
    val id: Long,
    val storeId: Long,
    val name: String,
    val isCompleted: Boolean,
    val completedAt: Long?,
    val displayOrder: Int,
    val createdAt: Long,
    /** Epoch millis of a one-shot purchase reminder, or null when none is set. */
    val reminderAt: Long? = null,
)
