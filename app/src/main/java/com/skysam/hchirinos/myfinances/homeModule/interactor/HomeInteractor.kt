package com.skysam.hchirinos.myfinances.homeModule.interactor

interface HomeInteractor {
    fun obtenerCotizacionWeb()
    fun guardarCotizacionShared(valorBCV: Float, valorParalelo: Float)
    fun moveDataNextYear(year: Int)
}