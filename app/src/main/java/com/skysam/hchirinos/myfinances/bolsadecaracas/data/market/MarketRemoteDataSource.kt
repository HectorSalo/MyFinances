package com.skysam.hchirinos.myfinances.bolsadecaracas.data.market

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fuente remota para obtener cotizaciones del mercado BVC.
 * Todo el scraping/parsing queda encapsulado aquí.
 */
@Singleton
class MarketRemoteDataSource @Inject constructor() {

    suspend fun fetchMarketQuotes(): Result<List<MarketQuote>> = withContext(Dispatchers.IO) {
        runCatching {
            val document = Jsoup.connect(MARKET_URL)
                .timeout(REQUEST_TIMEOUT_MS)
                .get()

            // Intenta selector específico del mercado real, luego fallback genérico
            val table = document.selectFirst("div#S\\:0 table")
                ?: document.selectFirst("#mercado table, .mercado table, [data-market] table")
                ?: document.selectFirst("table:has(th):has(td)")
                ?: throw IllegalStateException("No se encontró tabla de mercado en el HTML.")

            Log.d(BVC_MARKET_TAG, "Parser: tabla encontrada")

            val headerIndexMap = buildHeaderIndexMap(table)
            Log.d(BVC_MARKET_TAG, "Parser: headers mapeados = $headerIndexMap")
            val now = System.currentTimeMillis()

            val rows = extractRows(table)
            Log.d(BVC_MARKET_TAG, "Parser: ${rows.size} filas encontradas")

            val quotes = rows.mapNotNull { row ->
                parseRowToQuote(
                    row = row,
                    headerIndexMap = headerIndexMap,
                    fallbackUpdatedAt = now
                )
            }

            Log.d(BVC_MARKET_TAG, "Parser: ${quotes.size} cotizaciones válidas generadas")
            quotes.take(5).forEach { quote ->
                Log.d(BVC_MARKET_TAG, "Parser: ${quote.symbol} = ${quote.lastPrice}")
            }

            quotes
        }
    }

    private fun parseRowToQuote(
        row: Element,
        headerIndexMap: Map<String, Int>,
        fallbackUpdatedAt: Long
    ): MarketQuote? {
        val cells = row.select("td")
        if (cells.isEmpty()) return null

        val symbolIndex = headerIndexMap[COL_SYMBOL] ?: 0
        val symbol = valueAt(cells, symbolIndex)
            ?.replace("\\s+".toRegex(), "")
            ?.uppercase(Locale.getDefault())
            .orEmpty()
        if (symbol.isBlank()) return null

        // Requiere lastPrice explícito de columna identificada; sin fallback a firstNumericValue
        val lastPrice = valueAt(cells, headerIndexMap[COL_LAST_PRICE])
            ?.let(::parseDecimal)
            ?: return null

        val change = valueAt(cells, headerIndexMap[COL_CHANGE])?.let(::parseDecimal)
        val percentChange = valueAt(cells, headerIndexMap[COL_PERCENT_CHANGE])?.let(::parsePercent)
        val volume = valueAt(cells, headerIndexMap[COL_VOLUME])?.let(::parseDecimal)

        val updatedAtFromCell = valueAt(cells, headerIndexMap[COL_UPDATED_AT])?.let(::parseUpdatedAt)
        val updatedAt = updatedAtFromCell ?: fallbackUpdatedAt

        return MarketQuote(
            symbol = symbol,
            lastPrice = lastPrice,
            change = change,
            percentChange = percentChange,
            volume = volume,
            updatedAt = updatedAt
        )
    }

    private fun buildHeaderIndexMap(table: Element): Map<String, Int> {
        val headerCells = table.select("thead tr").last()?.select("th, td")
            ?: table.select("tr").firstOrNull()?.select("th, td")
            ?: return emptyMap()

        if (headerCells.isEmpty()) return emptyMap()

        val normalizedHeaders = headerCells.map { normalizeHeader(cleanCellText(it)) }
        val map = mutableMapOf<String, Int>()

        fun findIndex(vararg aliases: String): Int? {
            val normalizedAliases = aliases.map(::normalizeHeader)
            return normalizedHeaders.indexOfFirst { header -> header in normalizedAliases }
                .takeIf { it >= 0 }
        }

        findIndex("simbolo", "símbolo", "ticker", "especie")?.let { map[COL_SYMBOL] = it }
        findIndex("ultimo", "último", "precio", "precio cierre", "cierre")?.let {
            map[COL_LAST_PRICE] = it
        }
        findIndex("variacion", "variación", "cambio", "var abs")?.let { map[COL_CHANGE] = it }
        findIndex("variacion %", "variación %", "% variacion", "% variación", "cambio %", "var %")?.let {
            map[COL_PERCENT_CHANGE] = it
        }
        findIndex("volumen", "cantidad", "volumen negociado")?.let { map[COL_VOLUME] = it }
        findIndex("hora", "fecha", "actualizado", "actualización")?.let { map[COL_UPDATED_AT] = it }

        return map
    }

    private fun extractRows(table: Element): List<Element> {
        val tbodyRows = table.select("tbody tr")
        if (tbodyRows.isNotEmpty()) return tbodyRows

        return table.select("tr")
            .drop(1)
            .filter { row -> row.select("td").isNotEmpty() }
    }

    private fun valueAt(cells: List<Element>, index: Int?): String? {
        if (index == null || index < 0 || index >= cells.size) return null
        return cleanCellText(cells[index]).takeIf { it.isNotBlank() }
    }

    private fun cleanCellText(cell: Element): String {
        val clean = cell.clone()
        clean.select("svg, path, i, use").remove()
        return clean.text()
            .replace('\u00A0', ' ')
            .replace("\\s+".toRegex(), " ")
            .trim()
    }

    private fun normalizeHeader(value: String): String {
        val withoutAccents = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
        return withoutAccents.lowercase(Locale.getDefault())
            .replace("[^a-z0-9% ]".toRegex(), " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }

    private fun parseDecimal(raw: String?): Double? {
        if (raw.isNullOrBlank()) return null

        val trimmed = raw.trim()
        val negativeFromParentheses = trimmed.startsWith("(") && trimmed.endsWith(")")
        var value = trimmed
            .replace("[^0-9,.-]".toRegex(), "")
            .replace("\u2212", "-")

        if (value.isBlank() || value == "-" || value == "." || value == ",") return null

        val commaCount = value.count { it == ',' }
        val dotCount = value.count { it == '.' }

        value = when {
            commaCount > 0 && dotCount > 0 -> value.replace(".", "").replace(",", ".")
            commaCount > 0 -> value.replace(".", "").replace(",", ".")
            dotCount > 1 -> value.replace(".", "")
            else -> value
        }

        val parsed = value.toDoubleOrNull() ?: return null
        return if (negativeFromParentheses) -parsed else parsed
    }

    private fun parsePercent(raw: String?): Double? {
        if (raw.isNullOrBlank()) return null
        return parseDecimal(raw.replace("%", ""))
    }

    private fun parseUpdatedAt(raw: String?): Long? {
        if (raw.isNullOrBlank()) return null
        val value = raw.trim()

        val patterns = listOf(
            "dd/MM/yyyy HH:mm:ss",
            "dd/MM/yyyy HH:mm",
            "dd-MM-yyyy HH:mm:ss",
            "dd-MM-yyyy HH:mm",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "dd/MM/yyyy",
            "dd-MM-yyyy"
        )

        patterns.forEach { pattern ->
            runCatching {
                val locale = Locale.Builder().setLanguage("es").setRegion("VE").build()
                val sdf = SimpleDateFormat(pattern, locale)
                sdf.isLenient = true
                sdf.parse(value)?.time
            }.getOrNull()?.let { return it }
        }

        return null
    }

    companion object {
        private const val BVC_MARKET_TAG = "BVC_MARKET"
        private const val MARKET_URL = "https://www.bolsadecaracas.com/mercado"
        private const val REQUEST_TIMEOUT_MS = 15_000

        private const val COL_SYMBOL = "symbol"
        private const val COL_LAST_PRICE = "lastPrice"
        private const val COL_CHANGE = "change"
        private const val COL_PERCENT_CHANGE = "percentChange"
        private const val COL_VOLUME = "volume"
        private const val COL_UPDATED_AT = "updatedAt"
    }
}
