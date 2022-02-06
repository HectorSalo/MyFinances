package com.skysam.hchirinos.myfinances.graficosModule.interactor

import android.util.Log
import androidx.constraintlayout.widget.Constraints
import com.skysam.hchirinos.myfinances.common.MyFinancesApp
import com.skysam.hchirinos.myfinances.common.model.SharedPreferencesBD
import com.skysam.hchirinos.myfinances.common.model.firebase.Auth
import com.skysam.hchirinos.myfinances.common.model.firebase.FirebaseFirestore
import com.skysam.hchirinos.myfinances.common.utils.Constants
import com.skysam.hchirinos.myfinances.graficosModule.presenter.TotalesGraphPresenter
import java.util.*

class TotalesGraphInteractorClass(private val totalesGraphPresenter: TotalesGraphPresenter): TotalesGraphInteractor {
    override fun getIngresos(year: Int, month: Int) {
        val valorCotizacion = SharedPreferencesBD.getCotizacion(Auth.getCurrentUser()!!.uid, MyFinancesApp.MyFinancesAppObject.getContext())
        FirebaseFirestore.getIngresosReference(Auth.getCurrentUser()!!.uid, year, month)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        var montototal = 0.0
                        var mesCobro: Int
                        var yearCobro: Int

                        for (document in task.result!!) {
                            Log.d(Constraints.TAG, document.id + " => " + document.data)
                            val activo = document.getBoolean(Constants.BD_MES_ACTIVO)
                            if (activo == null || activo) {
                                val calendarCobro = Calendar.getInstance()
                                val montoDetal = document.getDouble(Constants.BD_MONTO)!!
                                val dolar = document.getBoolean(Constants.BD_DOLAR)!!
                                val tipoFrecuencia = document.getString(Constants.BD_TIPO_FRECUENCIA)
                                if (tipoFrecuencia != null) {
                                    calendarCobro.time = document.getDate(Constants.BD_FECHA_INCIAL)!!
                                    val duracionFrecuencia = document.getDouble(Constants.BD_DURACION_FRECUENCIA)!!
                                    val duracionFrecuenciaInt = duracionFrecuencia.toInt()
                                    val mesInicial = calendarCobro[Calendar.MONTH]
                                    mesCobro = calendarCobro[Calendar.MONTH]
                                    yearCobro = calendarCobro[Calendar.YEAR]

                                    while (mesCobro <= month && yearCobro == year) {
                                        if (mesCobro == month) {
                                            montototal = if (dolar) {
                                                montototal + montoDetal
                                            } else {
                                                if (mesInicial <= 8 && year <= 2021) {
                                                    montototal + (montoDetal / 1000000) / valorCotizacion
                                                } else {
                                                    montototal + montoDetal / valorCotizacion
                                                }
                                            }
                                        }

                                        when(tipoFrecuencia) {
                                            "Dias" -> {
                                                calendarCobro.add(Calendar.DAY_OF_YEAR, duracionFrecuenciaInt)
                                            }
                                            "Semanas" -> {
                                                calendarCobro.add(Calendar.DAY_OF_YEAR, duracionFrecuenciaInt * 7)
                                            }
                                            "Meses" -> {
                                                calendarCobro.add(Calendar.MONTH, duracionFrecuenciaInt)
                                            }
                                        }
                                        mesCobro = calendarCobro[Calendar.MONTH]
                                        yearCobro = calendarCobro[Calendar.YEAR]
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
                        totalesGraphPresenter.statusValorIngresos(true, montototal.toFloat(), montototal.toString())
                    } else {
                        totalesGraphPresenter.statusValorIngresos(true, 0f, "")
                    }
                }
                .addOnFailureListener {
                    totalesGraphPresenter.statusValorIngresos(false, 0f, "Error al obtener los Ingresos")
                }
    }

    override fun getGastos(year: Int, month: Int) {
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
                        totalesGraphPresenter.statusValorGastos(
                            true,
                            montototal.toFloat(),
                            montototal.toString()
                        )
                    } else {
                        totalesGraphPresenter.statusValorGastos(true, 0f, "")
                    }
                }.addOnFailureListener {
                totalesGraphPresenter.statusValorGastos(false, 0f, "Error al obtener los Gastos")
            }
    }

    override fun getDeudas(year: Int, month: Int) {
        val valorCotizacion = SharedPreferencesBD.getCotizacion(Auth.getCurrentUser()!!.uid, MyFinancesApp.MyFinancesAppObject.getContext())
        FirebaseFirestore.getDeudasReference(Auth.getCurrentUser()!!.uid, year, month)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        var montototal = 0.0
                        for (document in task.result!!) {
                            Log.d(Constraints.TAG, document.id + " => " + document.data)
                            val montoDetal = document.getDouble(Constants.BD_MONTO)!!
                            val dolar = document.getBoolean(Constants.BD_DOLAR)!!
                            montototal = if (dolar) {
                                montototal + montoDetal
                            } else {
                                montototal + montoDetal / valorCotizacion
                            }
                        }
                        totalesGraphPresenter.statusValorDeudas(
                            true,
                            montototal.toFloat(),
                            montototal.toString()
                        )
                    } else {
                        totalesGraphPresenter.statusValorDeudas(true, 0f, "")
                    }
                }.addOnFailureListener {
                totalesGraphPresenter.statusValorDeudas(false, 0f, "Error al obtener deudas")
            }
    }

    override fun getPrestamos(year: Int, month: Int) {
        val valorCotizacion = SharedPreferencesBD.getCotizacion(Auth.getCurrentUser()!!.uid, MyFinancesApp.MyFinancesAppObject.getContext())
        FirebaseFirestore.getPrestamosReference(Auth.getCurrentUser()!!.uid, year, month)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        var montototal = 0.0
                        for (document in task.result!!) {
                            Log.d(Constraints.TAG, document.id + " => " + document.data)
                            val montoDetal = document.getDouble(Constants.BD_MONTO)!!
                            val dolar = document.getBoolean(Constants.BD_DOLAR)!!
                            montototal = if (dolar) {
                                montototal + montoDetal
                            } else {
                                montototal + montoDetal / valorCotizacion
                            }
                        }
                        totalesGraphPresenter.statusValorPrestamos(
                            true,
                            montototal.toFloat(),
                            montototal.toString()
                        )
                    } else {
                        totalesGraphPresenter.statusValorPrestamos(true, 0f, "")
                    }
                }.addOnFailureListener {
                totalesGraphPresenter.statusValorPrestamos(false, 0f, "Error al obtener préstamos")
            }
    }

    override fun getAhorros(year: Int, month: Int) {
        val valorCotizacion = SharedPreferencesBD.getCotizacion(Auth.getCurrentUser()!!.uid, MyFinancesApp.MyFinancesAppObject.getContext())
        FirebaseFirestore.getAhorrosReference(Auth.getCurrentUser()!!.uid, year, month)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        var montototal = 0.0
                        for (document in task.result!!) {
                            val date = document.getDate(Constants.BD_FECHA_INGRESO)
                            val calendar = Calendar.getInstance()
                            calendar.time = date!!
                            val mesItemAhorro = calendar.get(Calendar.MONTH)
                            if (month >= mesItemAhorro) {
                                val montoDetal = document.getDouble(Constants.BD_MONTO)!!
                                val dolar = document.getBoolean(Constants.BD_DOLAR)!!
                                montototal += if (dolar) {
                                    montoDetal
                                } else {
                                    montoDetal / valorCotizacion
                                }
                            }
                        }
                        totalesGraphPresenter.statusValorAhorros(
                            true,
                            montototal.toFloat(),
                            montototal.toString()
                        )
                    } else {
                        totalesGraphPresenter.statusValorAhorros(true, 0f, "")
                    }
                }.addOnFailureListener {
                totalesGraphPresenter.statusValorAhorros(false, 0f, "Error al obtener ahorros")
            }
    }
}