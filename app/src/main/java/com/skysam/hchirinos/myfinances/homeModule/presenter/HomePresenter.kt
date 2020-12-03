package com.skysam.hchirinos.myfinances.homeModule.presenter

interface HomePresenter {
    fun obtenerCotizacionWeb()
    fun guardarCotizacionShared(valorFloat: Float)
    fun getIngresos(year: Int, month: Int)
    fun getGastos(year: Int, month: Int)
    fun moveDataNextYear(year: Int)

    fun valorCotizacionWebOk(valor: String, valorFloat: Float)
    fun valorCotizacionWebError(valorFloat: Float)
    fun statusValorIngresos(statusOk: Boolean, ingresos: Float, message: String)
    fun statusValorGastos(statusOk: Boolean, gastos: Float, message: String)
    fun statusMoveNextYear(statusOk: Boolean, message: String)
}