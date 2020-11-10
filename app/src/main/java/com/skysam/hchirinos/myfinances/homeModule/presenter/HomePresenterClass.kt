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

    override fun valorCotizacionWebOk(valor: String, valorFloat: Float) {
        homeView.valorCotizacionWebOk(valor, valorFloat)
    }

    override fun valorCotizacionWebError(valorFloat: Float) {
        homeView.valorCotizacionWebError(valorFloat)
    }
}