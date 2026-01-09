package com.skysam.hchirinos.myfinances.homeModule.interactor

import com.skysam.hchirinos.myfinances.common.model.constructores.ExchangeRateDto

/**
 * Created by Hector Chirinos in the home office on 8 ene. 2026
 */
sealed class RatesHistoryResult {
    data class Success(val items: List<ExchangeRateDto>) : RatesHistoryResult()
    object Empty : RatesHistoryResult()
    data class Error(val message: String, val code: Int? = null) : RatesHistoryResult()
}