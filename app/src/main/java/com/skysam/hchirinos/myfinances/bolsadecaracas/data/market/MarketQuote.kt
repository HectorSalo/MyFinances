package com.skysam.hchirinos.myfinances.bolsadecaracas.data.market

/**
 * Modelo para cotizaciones reales del mercado.
 */
data class MarketQuote(
    val symbol: String,
    val lastPrice: Double,
    val change: Double? = null,
    val percentChange: Double? = null,
    val volume: Double? = null,
    val updatedAt: Long? = null // timestamp unix opcional
)
