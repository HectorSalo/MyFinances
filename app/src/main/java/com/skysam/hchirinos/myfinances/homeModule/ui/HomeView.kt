package com.skysam.hchirinos.myfinances.homeModule.ui

import com.skysam.hchirinos.myfinances.homeModule.interactor.RatesHistoryResult

interface HomeView {
    fun valorCotizacionWebOk(
        valorBCV: Float,
        valorBCVPrev: Float,
        valorParalelo: Float,
        valorParaleloPrev: Float,
        valorEuro: Float,
        valorEuroPrev: Float,
        fechaBCV: String,
        fechaParalelo: String
    )

    fun valorCotizacionWebError(valorBCV: Float, valorParalelo: Float, valorEuro: Float)

    fun statusMoveNextYear(statusOk: Boolean, message: String)
    fun historialTasasResult(result: RatesHistoryResult)
}