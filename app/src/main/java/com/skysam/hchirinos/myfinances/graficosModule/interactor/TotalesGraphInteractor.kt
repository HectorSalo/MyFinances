package com.skysam.hchirinos.myfinances.graficosModule.interactor

interface TotalesGraphInteractor {
    fun getIngresos(year: Int, month: Int)
    fun getGastos(year: Int, month: Int)
    fun getDeudas(year: Int, month: Int)
    fun getPrestamos(year: Int, month: Int)
    fun getAhorros(year: Int, month: Int)
}