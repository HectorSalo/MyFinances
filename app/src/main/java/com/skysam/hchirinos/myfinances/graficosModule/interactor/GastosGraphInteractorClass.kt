package com.skysam.hchirinos.myfinances.graficosModule.interactor

import android.util.Log
import androidx.constraintlayout.widget.Constraints
import com.skysam.hchirinos.myfinances.common.MyFinancesApp
import com.skysam.hchirinos.myfinances.common.model.SharedPreferencesBD
import com.skysam.hchirinos.myfinances.common.model.firebase.Auth
import com.skysam.hchirinos.myfinances.common.model.firebase.FirebaseFirestore
import com.skysam.hchirinos.myfinances.common.utils.Constants
import com.skysam.hchirinos.myfinances.graficosModule.presenter.GastosGraphPresenter
import java.util.*

class GastosGraphInteractorClass(private val gastosGraphPresenter: GastosGraphPresenter): GastosGraphInteractor {
    override fun getMes(year: Int, month: Int) {
        val valorCotizacion = SharedPreferencesBD.getCotizacion(Auth.getCurrentUser()!!.uid, MyFinancesApp.MyFinancesAppObject.getContext())
        FirebaseFirestore.getGastosReference(Auth.getCurrentUser()!!.uid, year, month)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        var montototal = 0.0
                        var mesPago: Int
                        var yearPago: Int
                        for (document in task.result!!) {
                            val activo = document.getBoolean(Constants.BD_MES_ACTIVO)
                            if (activo == null || activo) {
                                Log.d(Constraints.TAG, document.id + " => " + document.data)
                                val montoDetal = document.getDouble(Constants.BD_MONTO)!!
                                val dolar = document.getBoolean(Constants.BD_DOLAR)!!
                                val tipoFrecuencia =
                                    document.getString(Constants.BD_TIPO_FRECUENCIA)
                                if (tipoFrecuencia != null) {
                                    val calendarPago = Calendar.getInstance()
                                    calendarPago.time =
                                        document.getDate(Constants.BD_FECHA_INCIAL)!!
                                    val duracionFrecuencia =
                                        document.getDouble(Constants.BD_DURACION_FRECUENCIA)!!
                                    val duracionFrecuenciaInt = duracionFrecuencia.toInt()
                                    mesPago = calendarPago[Calendar.MONTH]
                                    yearPago = calendarPago[Calendar.YEAR]

                                    while (mesPago <= month && yearPago == year) {
                                        if (mesPago == month) {
                                            montototal = if (dolar) {
                                                montototal + montoDetal
                                            } else {
                                                montototal + montoDetal / valorCotizacion
                                            }
                                        }

                                        when (tipoFrecuencia) {
                                            "Dias" -> {
                                                calendarPago.add(
                                                    Calendar.DAY_OF_YEAR,
                                                    duracionFrecuenciaInt
                                                )
                                            }
                                            "Semanas" -> {
                                                calendarPago.add(
                                                    Calendar.DAY_OF_YEAR,
                                                    duracionFrecuenciaInt * 7
                                                )
                                            }
                                            "Meses" -> {
                                                calendarPago.add(
                                                    Calendar.MONTH,
                                                    duracionFrecuenciaInt
                                                )
                                            }
                                        }
                                        mesPago = calendarPago[Calendar.MONTH]
                                        yearPago = calendarPago[Calendar.YEAR]
                                    }
                                } else {
                                    montototal = if (dolar) {
                                        montototal + montoDetal
                                    } else {
                                        montototal + montoDetal / valorCotizacion
                                    }
                                }
                            }
                        }
                        gastosGraphPresenter.statusMes(
                            month,
                            true,
                            montototal.toFloat(),
                            montototal.toString()
                        )
                    } else {
                        gastosGraphPresenter.statusMes(month, true, 0f, "")
                    }
                }.addOnFailureListener {
                gastosGraphPresenter.statusMes(
                    month,
                    false,
                    0f,
                    "Error al obtener datos. Intente nuevamente"
                )
            }
    }
}