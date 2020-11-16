package com.skysam.hchirinos.myfinances.ingresosModule.presenter

import com.skysam.hchirinos.myfinances.common.model.constructores.IngresosGastosConstructor
import java.util.ArrayList

interface IngresosPresenter {
    fun getIngresos(year: Int, month: Int)
    fun suspenderMes(year: Int, month: Int, id: String)

    fun statusListaIngresos(statusOk: Boolean, ingresos: ArrayList<IngresosGastosConstructor>, message: String)
    fun statusSuspenderMes(statusOk: Boolean)
}