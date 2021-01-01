package com.skysam.hchirinos.myfinances.graficosModule.ui

interface GastosGraphView {
    fun statusMes(month: Int, statusOk: Boolean, monto: Float, message: String)
}