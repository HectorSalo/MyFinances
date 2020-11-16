package com.skysam.hchirinos.myfinances.homeModule.interactor

interface HomeInteractor {
    fun obtenerCotizacionWeb()
    fun guardarCotizacionShared(valorFloat: Float)
    fun getIngresos(year: Int, month: Int)
    fun getAhoros(year: Int, month: Int)
    fun getDeudas(year: Int, month: Int)
    fun getPrestamos(year: Int, month: Int)
    fun getGastos(year: Int, month: Int)
}