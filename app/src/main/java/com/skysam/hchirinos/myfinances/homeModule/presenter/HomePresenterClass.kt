package com.skysam.hchirinos.myfinances.homeModule.presenter

import android.content.Context
import com.skysam.hchirinos.myfinances.homeModule.interactor.HomeInteractor
import com.skysam.hchirinos.myfinances.homeModule.interactor.HomeInteractorClass
import com.skysam.hchirinos.myfinances.homeModule.ui.HomeView

class HomePresenterClass(private val homeView: HomeView, context: Context): HomePresenter {
    private val homeInteractor: HomeInteractor = HomeInteractorClass(this, context)
    override fun obtenerCotizacionWeb() {
        homeInteractor.obtenerCotizacionWeb()
    }

    override fun guardarCotizacionShared(valorBCV: Float, valorParalelo: Float) {
        homeInteractor.guardarCotizacionShared(valorBCV, valorParalelo)
    }

    override fun moveDataNextYear(year: Int) {
        homeInteractor.moveDataNextYear(year)
    }

    override fun valorCotizacionWebOk(valorBCV: Float, valorParalelo: Float, fechaBCV: String, fechaParalelo: String) {
        homeView.valorCotizacionWebOk(valorBCV, valorParalelo, fechaBCV, fechaParalelo)
    }

    override fun valorCotizacionWebError(valorBCV: Float, valorParalelo: Float) {
        homeView.valorCotizacionWebError(valorBCV, valorParalelo)
    }

    override fun statusMoveNextYear(statusOk: Boolean, message: String) {
        homeView.statusMoveNextYear(statusOk, message)
    }
}