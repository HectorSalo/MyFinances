package com.skysam.hchirinos.myfinances.homeModule.interactor

interface HomeInteractor {
    fun obtenerCotizacionWeb()
    fun guardarCotizacionShared(valorFloat: Float)
    fun moveDataNextYear(year: Int)
}