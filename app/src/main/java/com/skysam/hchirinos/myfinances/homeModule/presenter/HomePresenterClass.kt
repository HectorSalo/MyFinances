package com.skysam.hchirinos.myfinances.homeModule.presenter

import android.content.Context
import com.skysam.hchirinos.myfinances.homeModule.interactor.HomeInteractor
import com.skysam.hchirinos.myfinances.homeModule.interactor.HomeInteractorClass
import com.skysam.hchirinos.myfinances.homeModule.interactor.RatesHistoryResult
import com.skysam.hchirinos.myfinances.homeModule.ui.HomeView

class HomePresenterClass(private val homeView: HomeView, context: Context): HomePresenter {
    private val homeInteractor: HomeInteractor = HomeInteractorClass(this, context)

    override fun obtenerCotizacionWeb() = homeInteractor.obtenerCotizacionWeb()

    override fun guardarCotizacionShared(valorBCV: Float, valorParalelo: Float, valorEuro: Float) {
        homeInteractor.guardarCotizacionShared(valorBCV, valorParalelo, valorEuro)
    }

    override fun moveDataNextYear(year: Int) = homeInteractor.moveDataNextYear(year)

    override fun valorCotizacionWebOk(
        valorBCV: Float,
        valorBCVPrev: Float,
        valorParalelo: Float,
        valorParaleloPrev: Float,
        valorEuro: Float,
        valorEuroPrev: Float,
        fechaBCV: String,
        fechaParalelo: String
    ) {
        homeView.valorCotizacionWebOk(
            valorBCV,
            valorBCVPrev,
            valorParalelo,
            valorParaleloPrev,
            valorEuro,
            valorEuroPrev,
            fechaBCV,
            fechaParalelo
        )
    }

    override fun valorCotizacionWebError(valorBCV: Float, valorParalelo: Float, valorEuro: Float) {
        homeView.valorCotizacionWebError(valorBCV, valorParalelo, valorEuro)
    }

    override fun statusMoveNextYear(statusOk: Boolean, message: String) {
        homeView.statusMoveNextYear(statusOk, message)
    }
    override fun obtenerHistorialTasas(from: String, to: String) {
        homeInteractor.obtenerHistorialTasas(from, to)
    }
    override fun historialTasasResult(result: RatesHistoryResult) {
        homeView.historialTasasResult(result)
    }
}
