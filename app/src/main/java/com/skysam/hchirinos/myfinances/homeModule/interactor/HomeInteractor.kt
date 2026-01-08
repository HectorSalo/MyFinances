package com.skysam.hchirinos.myfinances.homeModule.interactor

interface HomeInteractor {
    fun obtenerCotizacionWeb()
    fun guardarCotizacionShared(valorBCV: Float, valorParalelo: Float, valorEuro: Float)
    fun moveDataNextYear(year: Int)
}
