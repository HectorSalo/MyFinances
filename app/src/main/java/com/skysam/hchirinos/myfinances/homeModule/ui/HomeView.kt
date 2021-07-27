package com.skysam.hchirinos.myfinances.homeModule.ui

interface HomeView {
    fun valorCotizacionWebOk(valor: String, valorFloat: Float)
    fun valorCotizacionWebError(valorFloat: Float)
    fun statusMoveNextYear(statusOk: Boolean, message: String)
}