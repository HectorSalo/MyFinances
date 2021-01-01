package com.skysam.hchirinos.myfinances.graficosModule.ui

interface IngresosGraphView {
    fun statusMes(month: Int, statusOk: Boolean, monto: Float, message: String)
}