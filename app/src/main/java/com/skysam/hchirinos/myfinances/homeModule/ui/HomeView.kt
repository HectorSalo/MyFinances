package com.skysam.hchirinos.myfinances.homeModule.ui

interface HomeView {
    fun valorCotizacionWebOk(valor: String, valorFloat: Float)
    fun valorCotizacionWebError(valorFloat: Float)
    fun statusValorIngresos(statusOk: Boolean, ingresos: Float, message: String)
    fun statusValorGastos(statusOk: Boolean, gastos: Float, message: String)
    fun statusMoveNextYear(statusOk: Boolean, message: String)
}