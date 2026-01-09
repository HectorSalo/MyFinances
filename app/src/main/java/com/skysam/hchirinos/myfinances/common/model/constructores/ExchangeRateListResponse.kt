package com.skysam.hchirinos.myfinances.common.model.constructores

import com.google.gson.annotations.SerializedName

/**
 * Created by Hector Chirinos in the home office on 8 ene. 2026
 */
data class ExchangeRateListResponse(
    @SerializedName("rates")
    val rates: List<ExchangeRateDto> = emptyList()
)

data class ExchangeRateDto(
    @SerializedName("date")
    val date: String = "",          // "YYYY-MM-DD"

    @SerializedName("usd")
    val usd: Double = 0.0,

    @SerializedName("eur")
    val eur: Double = 0.0
)
