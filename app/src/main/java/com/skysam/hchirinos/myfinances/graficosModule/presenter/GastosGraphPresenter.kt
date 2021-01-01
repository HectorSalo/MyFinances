package com.skysam.hchirinos.myfinances.graficosModule.presenter

interface GastosGraphPresenter {
    fun getMes(year: Int, month: Int)

    fun statusMes(month: Int, statusOk: Boolean, monto: Float, message: String)
}