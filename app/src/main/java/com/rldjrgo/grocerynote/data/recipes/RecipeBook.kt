package com.rldjrgo.grocerynote.data.recipes

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * One dish (or occasion) and the things to buy for it. Names only — no amounts
 * (CLAUDE.md §3: quantity is the user's call).
 */
data class Recipe(
    val name: String,
    val emoji: String,
    val category: String,
    val aliases: List<String>,
    /** 재료 — what nearly everyone puts in. */
    val main: List<String>,
    /** 넣어도 좋은 것 — some households do, some don't. */
    val optional: List<String>,
    /** 양념 — seasonings/staples most kitchens already have. */
    val pantry: List<String>,
    /** 준비물 — non-food supplies (occasions only). */
    val supplies: List<String>,
) {
    /** Name + aliases, normalized for search. */
    val searchKeys: List<String> = (listOf(name) + aliases).map(::normalizeRecipeQuery)
}

/** Search normalization: ignore spaces/case and the common 찌게 misspelling. */
fun normalizeRecipeQuery(s: String): String =
    s.replace(" ", "").lowercase().replace("찌게", "찌개")

/**
 * The bundled "요리별 재료" table (`assets/recipes.txt`, ~60KB of text). Parsed
 * once, on first open of the tab, off the main thread — app start, Home and the
 * widgets never touch it.
 */
@Singleton
class RecipeBook @Inject constructor(@ApplicationContext private val context: Context) {

    @Volatile
    private var cached: List<Recipe>? = null

    suspend fun load(): List<Recipe> = cached ?: withContext(Dispatchers.IO) {
        cached ?: runCatching { parse() }.getOrDefault(emptyList()).also { cached = it }
    }

    private fun parse(): List<Recipe> {
        val out = ArrayList<Recipe>(320)
        var category = ""
        context.assets.open("recipes.txt").bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines.forEach { raw ->
                val line = raw.trim()
                when {
                    line.startsWith("## ") -> category = line.removePrefix("## ").trim()
                    line.isEmpty() || line.startsWith("#") -> Unit
                    else -> {
                        val p = line.split("|")
                        if (p.size >= 6) {
                            out += Recipe(
                                name = p[0].trim(),
                                emoji = p[1].trim(),
                                category = category,
                                aliases = p[2].csv(),
                                main = p[3].csv(),
                                optional = p[4].csv(),
                                pantry = p[5].csv(),
                                supplies = p.getOrNull(6)?.csv().orEmpty(),
                            )
                        }
                    }
                }
            }
        }
        return out
    }

    private fun String.csv(): List<String> = split(",").map { it.trim() }.filter { it.isNotEmpty() }
}
