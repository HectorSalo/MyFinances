package com.skysam.hchirinos.myfinances.ingresosModule.interactor

interface IngresosInteractor {
    fun getIngresos(year: Int, month: Int)
    fun suspenderMes(year: Int, month: Int, id: String)
}