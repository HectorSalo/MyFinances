package com.skysam.hchirinos.myfinances.graficosModule.ui

interface PrestamosGraphView {
    fun statusMes(month: Int, statusOk: Boolean, monto: Float, message: String)
}