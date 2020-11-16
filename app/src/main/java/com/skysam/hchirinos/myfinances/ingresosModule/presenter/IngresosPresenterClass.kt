package com.skysam.hchirinos.myfinances.ingresosModule.presenter

import com.skysam.hchirinos.myfinances.common.model.constructores.IngresosGastosConstructor
import com.skysam.hchirinos.myfinances.ingresosModule.interactor.IngresosInteractor
import com.skysam.hchirinos.myfinances.ingresosModule.interactor.IngresosInteractorClass
import com.skysam.hchirinos.myfinances.ingresosModule.ui.IngresosView
import java.util.ArrayList

class IngresosPresenterClass(val ingresosView: IngresosView): IngresosPresenter {
    val ingresosInteractor: IngresosInteractor = IngresosInteractorClass(this)
    override fun getIngresos(year: Int, month: Int) {
        ingresosInteractor.getIngresos(year, month)
    }

    override fun suspenderMes(year: Int, month: Int, id: String) {
        ingresosInteractor.suspenderMes(year, month, id)
    }

    override fun statusListaIngresos(statusOk: Boolean, ingresos: ArrayList<IngresosGastosConstructor>, message: String) {
        ingresosView.statusListaIngresos(statusOk, ingresos, message)
    }

    override fun statusSuspenderMes(statusOk: Boolean) {
        ingresosView.statusSuspenderMes(statusOk)
    }
}