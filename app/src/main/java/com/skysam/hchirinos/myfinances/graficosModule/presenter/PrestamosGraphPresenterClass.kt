package com.skysam.hchirinos.myfinances.graficosModule.presenter

import com.skysam.hchirinos.myfinances.graficosModule.interactor.PrestamosGraphInteractor
import com.skysam.hchirinos.myfinances.graficosModule.interactor.PrestamosGraphInteractorClass
import com.skysam.hchirinos.myfinances.graficosModule.ui.PrestamosGraphView

class PrestamosGraphPresenterClass(private val prestamosGraphView: PrestamosGraphView): PrestamosGraphPresenter {
    private val prestamosGraphInteractor: PrestamosGraphInteractor = PrestamosGraphInteractorClass(this)

    override fun getMes(year: Int, month: Int) {
        prestamosGraphInteractor.getMes(year, month)
    }

    override fun statusMes(month: Int, statusOk: Boolean, monto: Float, message: String) {
        prestamosGraphView.statusMes(month, statusOk, monto, message)
    }
}