package com.skysam.hchirinos.myfinances.graficosModule.ui

interface ResumenConsolidadoView {
    fun statusIngresosAnual(month: Int, statusOk: Boolean, monto: Float, message: String)
    fun statusGastosAnual(month: Int, statusOk: Boolean, total: Float, normal: Float, ahorroCapitalizable: Float, pagoDeuda: Float, message: String)
    fun statusAhorrosAnual(month: Int, statusOk: Boolean, monto: Float, message: String)
    fun statusDeudasAnual(month: Int, statusOk: Boolean, monto: Float, message: String)
    fun statusPrestamosAnual(month: Int, statusOk: Boolean, monto: Float, message: String)
}
