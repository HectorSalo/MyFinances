package com.skysam.hchirinos.myfinances.graficosModule.presenter

import com.skysam.hchirinos.myfinances.graficosModule.interactor.ResumenConsolidadoInteractor
import com.skysam.hchirinos.myfinances.graficosModule.interactor.ResumenConsolidadoInteractorClass
import com.skysam.hchirinos.myfinances.graficosModule.ui.ResumenConsolidadoView

class ResumenConsolidadoPresenterClass(
    private val view: ResumenConsolidadoView
) : ResumenConsolidadoPresenter {

    private val interactor: ResumenConsolidadoInteractor = ResumenConsolidadoInteractorClass(this)

    override fun getIngresosAnual(year: Int, month: Int) = interactor.getIngresosAnual(year, month)
    override fun getGastosAnual(year: Int, month: Int) = interactor.getGastosAnual(year, month)
    override fun getAhorrosAnual(year: Int, month: Int) = interactor.getAhorrosAnual(year, month)
    override fun getDeudasAnual(year: Int, month: Int) = interactor.getDeudasAnual(year, month)
    override fun getPrestamosAnual(year: Int, month: Int) = interactor.getPrestamosAnual(year, month)

    override fun statusIngresosAnual(month: Int, statusOk: Boolean, monto: Float, message: String) =
        view.statusIngresosAnual(month, statusOk, monto, message)

    override fun statusGastosAnual(month: Int, statusOk: Boolean, total: Float, normal: Float, ahorroCapitalizable: Float, pagoDeuda: Float, message: String) =
        view.statusGastosAnual(month, statusOk, total, normal, ahorroCapitalizable, pagoDeuda, message)

    override fun statusAhorrosAnual(month: Int, statusOk: Boolean, monto: Float, message: String) =
        view.statusAhorrosAnual(month, statusOk, monto, message)

    override fun statusDeudasAnual(month: Int, statusOk: Boolean, monto: Float, message: String) =
        view.statusDeudasAnual(month, statusOk, monto, message)

    override fun statusPrestamosAnual(month: Int, statusOk: Boolean, monto: Float, message: String) =
        view.statusPrestamosAnual(month, statusOk, monto, message)
}
