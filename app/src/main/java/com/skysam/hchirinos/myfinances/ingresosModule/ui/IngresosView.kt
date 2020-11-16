package com.skysam.hchirinos.myfinances.ingresosModule.ui

import com.skysam.hchirinos.myfinances.common.model.constructores.IngresosGastosConstructor
import java.util.ArrayList

interface IngresosView {
    fun statusListaIngresos(statusOk: Boolean, ingresos: ArrayList<IngresosGastosConstructor>, message: String)
    fun statusSuspenderMes(statusOk: Boolean)
}