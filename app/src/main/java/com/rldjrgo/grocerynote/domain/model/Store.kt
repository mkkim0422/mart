package com.rldjrgo.grocerynote.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Domain representation of a mart. UI consumes this — never the Room entity.
 * The 8 default mart palette colors live in `ui/theme/Color.kt#MartPalette`.
 */
data class Store(
    val id: Long,
    val name: String,
    val color: Color,
    val iconKey: String,
    val displayOrder: Int,
    val isArchived: Boolean,
    val createdAt: Long,
)

/** "#3182F6" → Color */
internal fun String.toComposeColorOrDefault(default: Color = Color(0xFF3182F6)): Color {
    val hex = trim().removePrefix("#")
    return runCatching {
        when (hex.length) {
            6 -> Color(("FF$hex").toLong(radix = 16))
            8 -> Color(hex.toLong(radix = 16))
            else -> default
        }
    }.getOrDefault(default)
}

/** Color → "#RRGGBB" (alpha discarded; stores are opaque). */
internal fun Color.toHexString(): String {
    val argb = toArgb()
    return "#%06X".format(argb and 0x00FFFFFF)
}

private fun Color.toArgb(): Int {
    val a = (alpha * 255f).toInt() and 0xFF
    val r = (red * 255f).toInt() and 0xFF
    val g = (green * 255f).toInt() and 0xFF
    val b = (blue * 255f).toInt() and 0xFF
    return (a shl 24) or (r shl 16) or (g shl 8) or b
}
