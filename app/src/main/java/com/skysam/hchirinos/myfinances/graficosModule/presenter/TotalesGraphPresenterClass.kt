package com.skysam.hchirinos.myfinances.graficosModule.presenter

import com.skysam.hchirinos.myfinances.graficosModule.interactor.TotalesGraphInteractor
import com.skysam.hchirinos.myfinances.graficosModule.interactor.TotalesGraphInteractorClass
import com.skysam.hchirinos.myfinances.graficosModule.ui.TotalesGraphView

class TotalesGraphPresenterClass(private val totalesView: TotalesGraphView): TotalesGraphPresenter {
    private val totalesGraphInteractor: TotalesGraphInteractor = TotalesGraphInteractorClass(this)

    override fun getIngresos(year: Int, month: Int) {
        totalesGraphInteractor.getIngresos(year, month)
    }

    override fun getGastos(year: Int, month: Int) {
        totalesGraphInteractor.getGastos(year, month)
    }

    override fun getDeudas(year: Int, month: Int) {
        totalesGraphInteractor.getDeudas(year, month)
    }

    override fun getPrestamos(year: Int, month: Int) {
        totalesGraphInteractor.getPrestamos(year, month)
    }

    override fun getAhorros(year: Int, month: Int) {
        totalesGraphInteractor.getAhorros(year, month)
    }

    override fun statusValorIngresos(statusOk: Boolean, ingresos: Float, message: String) {
        totalesView.statusValorIngresos(statusOk, ingresos, message)
    }

    override fun statusValorGastos(statusOk: Boolean, gastos: Float, message: String) {
        totalesView.statusValorGastos(statusOk, gastos, message)
    }

    override fun statusValorDeudas(statusOk: Boolean, deudas: Float, message: String) {
        totalesView.statusValorDeudas(statusOk, deudas, message)
    }

    override fun statusValorPrestamos(statusOk: Boolean, prestamos: Float, message: String) {
        totalesView.statusValorPrestamos(statusOk, prestamos, message)
    }

    override fun statusValorAhorros(statusOk: Boolean, ahorros: Float, message: String) {
        totalesView.statusValorAhorros(statusOk, ahorros, message)
    }
}