package com.skysam.hchirinos.myfinances.graficosModule.presenter

import com.skysam.hchirinos.myfinances.graficosModule.interactor.AhorrosGraphInteractor
import com.skysam.hchirinos.myfinances.graficosModule.interactor.AhorrosGraphInteractorClass
import com.skysam.hchirinos.myfinances.graficosModule.ui.AhorrosGraphView

class AhorrosGraphPresenterClass(private val ahorrosGraphView: AhorrosGraphView): AhorrosGraphPresenter {
    private val ahorrosGraphInteractor: AhorrosGraphInteractor = AhorrosGraphInteractorClass(this)
    override fun getMes(year: Int, month: Int) {
       ahorrosGraphInteractor.getMes(year, month)
    }

    override fun statusMes(month: Int, statusOk: Boolean, monto: Float, message: String) {
        ahorrosGraphView.statusMes(month, statusOk, monto, message)
    }


}