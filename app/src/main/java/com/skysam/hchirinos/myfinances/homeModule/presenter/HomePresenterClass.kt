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

    override fun getAhoros(year: Int, month: Int) {
        homeInteractor.getAhoros(year, month)
    }

    override fun getDeudas(year: Int, month: Int) {
        homeInteractor.getDeudas(year, month)
    }

    override fun getPrestamos(year: Int, month: Int) {
        homeInteractor.getPrestamos(year, month)
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

    override fun statusValorAhorros(statusOk: Boolean, ahorros: Float, message: String) {
        homeView.statusValorAhorros(statusOk, ahorros, message)
    }

    override fun statusValorDeudas(statusOk: Boolean, deudas: Float, message: String) {
        homeView.statusValorDeudas(statusOk, deudas, message)
    }

    override fun statusValorPrestamos(statusOk: Boolean, prestamos: Float, message: String) {
        homeView.statusValorPrestamos(statusOk, prestamos, message)
    }

    override fun statusValorGastos(statusOk: Boolean, gastos: Float, message: String) {
        homeView.statusValorGastos(statusOk, gastos, message)
    }
}