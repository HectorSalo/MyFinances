package com.skysam.hchirinos.myfinances.homeModule.presenter

import android.content.Context
import com.skysam.hchirinos.myfinances.homeModule.interactor.HomeInteractor
import com.skysam.hchirinos.myfinances.homeModule.interactor.HomeInteractorClass
import com.skysam.hchirinos.myfinances.homeModule.ui.HomeView

class HomePresenterClass(val homeView: HomeView, context: Context): HomePresenter {
    val homeInteractor: HomeInteractor = HomeInteractorClass(this, context)
    override fun obtenerCotizacionWeb() {
        homeInteractor.obtenerCotizacionWeb()
    }

    override fun guardarCotizacionShared(valorFloat: Float) {
        homeInteractor.guardarCotizacionShared(valorFloat)
    }

    override fun getIngresos(year: Int, month: Int) {
        homeInteractor.getIngresos(year, month)
    }

    override fun getGastos(year: Int, month: Int) {
        homeInteractor.getGastos(year, month)
    }

    override fun moveDataNextYear(year: Int) {
        homeInteractor.moveDataNextYear(year)
    }

    override fun valorCotizacionWebOk(valor: String, valorFloat: Float) {
        homeView.valorCotizacionWebOk(valor, valorFloat)
    }

    override fun valorCotizacionWebError(valorFloat: Float) {
        homeView.valorCotizacionWebError(valorFloat)
    }

    override fun statusValorIngresos(statusOk: Boolean, ingresos: Float, message: String) {
        homeView.statusValorIngresos(statusOk, ingresos, message)
    }

    override fun statusValorGastos(statusOk: Boolean, gastos: Float, message: String) {
        homeView.statusValorGastos(statusOk, gastos, message)
    }

    override fun statusMoveNextYear(statusOk: Boolean, message: String) {
        homeView.statusMoveNextYear(statusOk, message)
    }
}