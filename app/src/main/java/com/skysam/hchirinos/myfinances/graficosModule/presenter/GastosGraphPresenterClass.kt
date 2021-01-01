package com.skysam.hchirinos.myfinances.graficosModule.presenter

import com.skysam.hchirinos.myfinances.graficosModule.interactor.GastosGraphInteractor
import com.skysam.hchirinos.myfinances.graficosModule.interactor.GastosGraphInteractorClass
import com.skysam.hchirinos.myfinances.graficosModule.ui.GastosGraphView

class GastosGraphPresenterClass(private val gastosGraphView: GastosGraphView): GastosGraphPresenter {
    private val gastosGraphInteractor: GastosGraphInteractor = GastosGraphInteractorClass(this)
    override fun getMes(year: Int, month: Int) {
        gastosGraphInteractor.getMes(year, month)
    }

    override fun statusMes(month: Int, statusOk: Boolean, monto: Float, message: String) {
        gastosGraphView.statusMes(month, statusOk, monto, message)
    }

}