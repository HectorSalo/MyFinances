package com.skysam.hchirinos.myfinances.graficosModule.presenter

interface ResumenConsolidadoPresenter {
    fun getIngresosAnual(year: Int, month: Int)
    fun getGastosAnual(year: Int, month: Int)
    fun getAhorrosAnual(year: Int, month: Int)
    fun getDeudasAnual(year: Int, month: Int)
    fun getPrestamosAnual(year: Int, month: Int)

    fun statusIngresosAnual(month: Int, statusOk: Boolean, monto: Float, message: String)
    fun statusGastosAnual(month: Int, statusOk: Boolean, total: Float, normal: Float, ahorroCapitalizable: Float, pagoDeuda: Float, message: String)
    fun statusAhorrosAnual(month: Int, statusOk: Boolean, total: Float, capital: Float, message: String)
    fun statusDeudasAnual(month: Int, statusOk: Boolean, monto: Float, message: String)
    fun statusPrestamosAnual(month: Int, statusOk: Boolean, monto: Float, message: String)
}
