package com.skysam.hchirinos.myfinances.homeModule.presenter

interface HomePresenter {
    fun obtenerCotizacionWeb()
    fun guardarCotizacionShared(valorFloat: Float)
    fun moveDataNextYear(year: Int)

    fun valorCotizacionWebOk(valor: String, valorFloat: Float)
    fun valorCotizacionWebError(valorFloat: Float)
    fun statusMoveNextYear(statusOk: Boolean, message: String)
}