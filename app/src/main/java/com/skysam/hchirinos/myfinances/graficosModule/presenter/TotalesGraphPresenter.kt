package com.skysam.hchirinos.myfinances.graficosModule.presenter

interface TotalesGraphPresenter {
    fun getIngresos(year: Int, month: Int)
    fun getGastos(year: Int, month: Int)
    fun getDeudas(year: Int, month: Int)
    fun getPrestamos(year: Int, month: Int)
    fun getAhorros(year: Int, month: Int)

    fun statusValorIngresos(statusOk: Boolean, ingresos: Float, message: String)
    fun statusValorGastos(statusOk: Boolean, gastos: Float, message: String)
    fun statusValorDeudas(statusOk: Boolean, deudas: Float, message: String)
    fun statusValorPrestamos(statusOk: Boolean, prestamos: Float, message: String)
    fun statusValorAhorros(statusOk: Boolean, ahorros: Float, message: String)
}