package com.skysam.hchirinos.myfinances.homeModule.presenter

interface HomePresenter {
    fun obtenerCotizacionWeb()
    fun guardarCotizacionShared(valorBCV: Float, valorParalelo: Float, valorEuro: Float)
    fun moveDataNextYear(year: Int)

    fun valorCotizacionWebOk(
        valorBCV: Float,
        valorBCVPrev: Float,
        valorParalelo: Float,
        valorParaleloPrev: Float,
        valorEuro: Float,
        valorEuroPrev: Float,
        fechaBCV: String,
        fechaParalelo: String
    )

    fun valorCotizacionWebError(valorBCV: Float, valorParalelo: Float, valorEuro: Float)
    fun statusMoveNextYear(statusOk: Boolean, message: String)
}