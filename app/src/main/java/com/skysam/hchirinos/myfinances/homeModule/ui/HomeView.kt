package com.skysam.hchirinos.myfinances.homeModule.ui

interface HomeView {
    fun valorCotizacionWebOk(valorBCV: Float, valorParalelo: Float, fechaBCV: String, fechaParalelo: String)
    fun valorCotizacionWebError(valorBCV: Float, valorParalelo: Float)
    fun statusMoveNextYear(statusOk: Boolean, message: String)
}