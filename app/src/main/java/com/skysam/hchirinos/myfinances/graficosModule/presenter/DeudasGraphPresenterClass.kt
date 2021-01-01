package com.skysam.hchirinos.myfinances.graficosModule.presenter

import com.skysam.hchirinos.myfinances.graficosModule.interactor.DeudasGraphInteractor
import com.skysam.hchirinos.myfinances.graficosModule.interactor.DeudasGraphInteractorClass
import com.skysam.hchirinos.myfinances.graficosModule.ui.DeudasGraphView

class DeudasGraphPresenterClass(private val deudasGraphView: DeudasGraphView): DeudasGraphPresenter {
    private val deudasGraphInteractor: DeudasGraphInteractor = DeudasGraphInteractorClass(this)
    override fun getMes(year: Int, month: Int) {
        deudasGraphInteractor.getMes(year, month)
    }

    override fun statusMes(month: Int, statusOk: Boolean, monto: Float, message: String) {
        deudasGraphView.statusMes(month, statusOk, monto, message)
    }
}