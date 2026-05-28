package com.skysam.hchirinos.myfinances.graficosModule.interactor

interface ResumenConsolidadoInteractor {
    fun getIngresosAnual(year: Int, month: Int)
    fun getGastosAnual(year: Int, month: Int)
    fun getAhorrosAnual(year: Int, month: Int)
    fun getDeudasAnual(year: Int, month: Int)
    fun getPrestamosAnual(year: Int, month: Int)
}
