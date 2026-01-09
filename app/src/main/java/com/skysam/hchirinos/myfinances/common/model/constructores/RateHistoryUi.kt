package com.skysam.hchirinos.myfinances.common.model.constructores

/**
 * Created by Hector Chirinos in the home office on 8 ene. 2026
 */
data class RateHistoryUi(
    val dateLabel: String,     // ya formateada para UI
    val usdLabel: String,      // ej: "USD 36,12"
    val eurLabel: String? = null
)
