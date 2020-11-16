package com.skysam.hchirinos.myfinances.homeModule.ui

interface HomeView {
    fun valorCotizacionWebOk(valor: String, valorFloat: Float)
    fun valorCotizacionWebError(valorFloat: Float)
    fun statusValorIngresos(statusOk: Boolean, ingresos: Float, message: String)
    fun statusValorAhorros(statusOk: Boolean, ahorros: Float, message: String)
    fun statusValorDeudas(statusOk: Boolean, deudas: Float, message: String)
    fun statusValorPrestamos(statusOk: Boolean, prestamos: Float, message: String)
    fun statusValorGastos(statusOk: Boolean, gastos: Float, message: String)
}