package com.skysam.hchirinos.myfinances.homeModule.presenter

interface HomePresenter {
    fun obtenerCotizacionWeb()
    fun guardarCotizacionShared(valorBCV: Float, valorParalelo: Float)
    fun moveDataNextYear(year: Int)

    fun valorCotizacionWebOk(valorBCV: Float, valorParalelo: Float, fechaBCV: String, fechaParalelo: String)
    fun valorCotizacionWebError(valorBCV: Float, valorParalelo: Float)
    fun statusMoveNextYear(statusOk: Boolean, message: String)
}