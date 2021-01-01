package com.skysam.hchirinos.myfinances.graficosModule.presenter

import com.skysam.hchirinos.myfinances.graficosModule.interactor.IngresosGraphInteractor
import com.skysam.hchirinos.myfinances.graficosModule.interactor.IngresosGraphInteractorClass
import com.skysam.hchirinos.myfinances.graficosModule.ui.IngresosGraphView

class IngresosGraphPresenterClass(private val ingresosGraphView: IngresosGraphView): IngresosGraphPresenter {
    private val ingresosGraphInteractor: IngresosGraphInteractor = IngresosGraphInteractorClass(this)
    override fun getMes(year: Int, month: Int) {
        ingresosGraphInteractor.getMes(year, month)
    }

    override fun statusMes(month: Int, statusOk: Boolean, monto: Float, message: String) {
        ingresosGraphView.statusMes(month, statusOk, monto, message)
    }


}