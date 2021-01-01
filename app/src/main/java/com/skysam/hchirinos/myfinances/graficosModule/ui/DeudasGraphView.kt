package com.skysam.hchirinos.myfinances.graficosModule.ui

interface DeudasGraphView {
    fun statusMes(month: Int, statusOk: Boolean, monto: Float, message: String)
}