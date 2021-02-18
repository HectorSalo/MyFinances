package com.skysam.hchirinos.myfinances.homeModule.interactor

import com.skysam.hchirinos.myfinances.common.MyFinancesApp
import com.skysam.hchirinos.myfinances.common.model.SharedPreferencesBD
import com.skysam.hchirinos.myfinances.common.model.constructores.ItemCronologiaConstructor
import com.skysam.hchirinos.myfinances.common.model.firebase.FirebaseAuthentication
import com.skysam.hchirinos.myfinances.common.model.firebase.FirebaseFirestore
import com.skysam.hchirinos.myfinances.common.utils.ClassesCommon
import com.skysam.hchirinos.myfinances.common.utils.Constants
import com.skysam.hchirinos.myfinances.homeModule.presenter.CronologiaPresenter
import java.util.*

class CronologiaInteractorClass(private val cronologiaPresenter: CronologiaPresenter): CronologiaInteractor {

    val listaCronologia = ArrayList<ItemCronologiaConstructor>()

    override fun getCronologia(month: Int, year: Int) {
        getIngreso(month, year)
    }

    private fun getIngreso(month: Int, year: Int) {
        FirebaseFirestore.getIngresosReference(FirebaseAuthentication.getCurrentUser()!!.uid, year, month)
                .get()
                .addOnCompleteListener { task->
                    if (task.isSuccessful) {
                        for (doc in task.result!!) {
                            val activo = doc.getBoolean(Constants.BD_MES_ACTIVO)
                            if (activo == null || activo) {
                                val tipoFrecuencia = doc.getString(Constants.BD_TIPO_FRECUENCIA)
                                if (tipoFrecuencia != null) {
                                    val calendarCobro = Calendar.getInstance()
                                    val duracionFrecuencia = doc.getDouble(Constants.BD_DURACION_FRECUENCIA)!!
                                    val duracionFrecuenciaInt = duracionFrecuencia.toInt()
                                    calendarCobro.time = doc.getDate(Constants.BD_FECHA_INCIAL)!!
                                    var mesCobro = calendarCobro[Calendar.MONTH]
                                    var yearCobro = calendarCobro[Calendar.YEAR]

                                    while (mesCobro <= month && yearCobro == year) {
                                        if (mesCobro == month) {
                                            val ingreso = ItemCronologiaConstructor()
                                            ingreso.concepto = doc.getString(Constants.BD_CONCEPTO)
                                            ingreso.monto = doc.getDouble(Constants.BD_MONTO)!!
                                            ingreso.isDolar = doc.getBoolean(Constants.BD_DOLAR)!!
                                            ingreso.pasivo = false
                                            ingreso.fecha = calendarCobro.time
                                            listaCronologia.add(ingreso)
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
                                    val ingreso = ItemCronologiaConstructor()
                                    ingreso.concepto = doc.getString(Constants.BD_CONCEPTO)
                                    ingreso.monto = doc.getDouble(Constants.BD_MONTO)!!
                                    ingreso.isDolar = doc.getBoolean(Constants.BD_DOLAR)!!
                                    ingreso.fecha = doc.getDate(Constants.BD_FECHA_INCIAL)!!
                                    ingreso.pasivo = false
                                    listaCronologia.add(ingreso)
                                }
                            }
                        }
                        getGastos(year, month)
                    }
                }
    }

    private fun getGastos(year: Int, month: Int) {
        FirebaseFirestore.getGastosReference(FirebaseAuthentication.getCurrentUser()!!.uid, year, month)
                .get()
                .addOnCompleteListener { task->
                    if (task.isSuccessful) {
                        for (doc in task.result!!) {
                            val activo = doc.getBoolean(Constants.BD_MES_ACTIVO)
                            if (activo == null || activo) {
                                val tipoFrecuencia = doc.getString(Constants.BD_TIPO_FRECUENCIA)
                                if (tipoFrecuencia != null) {
                                    var programNotification = SharedPreferencesBD.getNotificationActive(FirebaseAuthentication.getCurrentUser()!!.uid,
                                            MyFinancesApp.appContext)
                                    val calendarPago = Calendar.getInstance()
                                    val duracionFrecuencia = doc.getDouble(Constants.BD_DURACION_FRECUENCIA)!!
                                    val duracionFrecuenciaInt = duracionFrecuencia.toInt()
                                    calendarPago.time = doc.getDate(Constants.BD_FECHA_INCIAL)!!
                                    var mesPago = calendarPago[Calendar.MONTH]
                                    var yearPago = calendarPago[Calendar.YEAR]

                                    while (mesPago <= month && yearPago == year) {
                                        if (mesPago == month) {
                                            val gasto = ItemCronologiaConstructor()
                                            gasto.concepto = doc.getString(Constants.BD_CONCEPTO)
                                            gasto.monto = doc.getDouble(Constants.BD_MONTO)!!
                                            gasto.isDolar = doc.getBoolean(Constants.BD_DOLAR)!!
                                            gasto.pasivo = true
                                            gasto.fecha = calendarPago.time
                                            listaCronologia.add(gasto)

                                            if (programNotification) {
                                                programNotification = programNotification(gasto.fecha!!,
                                                        doc.getDate(Constants.BD_FECHA_INCIAL)!!, gasto.concepto!!)
                                            }
                                        }

                                        when(tipoFrecuencia) {
                                            "Dias" -> {
                                                calendarPago.add(Calendar.DAY_OF_YEAR, duracionFrecuenciaInt)
                                            }
                                            "Semanas" -> {
                                                calendarPago.add(Calendar.DAY_OF_YEAR, duracionFrecuenciaInt * 7)
                                            }
                                            "Meses" -> {
                                                calendarPago.add(Calendar.MONTH, duracionFrecuenciaInt)
                                            }
                                        }

                                        mesPago = calendarPago[Calendar.MONTH]
                                        yearPago = calendarPago[Calendar.YEAR]
                                    }


                                } else {
                                    val gasto = ItemCronologiaConstructor()
                                    gasto.concepto = doc.getString(Constants.BD_CONCEPTO)
                                    gasto.monto = doc.getDouble(Constants.BD_MONTO)!!
                                    gasto.isDolar = doc.getBoolean(Constants.BD_DOLAR)!!
                                    gasto.fecha = doc.getDate(Constants.BD_FECHA_INCIAL)!!
                                    gasto.pasivo = true
                                    listaCronologia.add(gasto)
                                }
                            }
                        }
                        cronologiaPresenter.listCronologia(listaCronologia)
                    }
                }
    }

    private fun programNotification(fechaPago: Date, fechaIncial: Date, concepto: String): Boolean {
        val calendarNotification = Calendar.getInstance()
        val calendarRequestId = Calendar.getInstance()
        calendarNotification.time = fechaPago
        calendarNotification.set(Calendar.HOUR_OF_DAY, 10)
        calendarNotification.set(Calendar.MINUTE, 0)
        calendarNotification.add(Calendar.DAY_OF_YEAR, -7)

        calendarRequestId.time  = fechaIncial
        val requestId = calendarRequestId.timeInMillis.toInt()
        ClassesCommon.createNotification(concepto, true, requestId, calendarNotification.timeInMillis)
        return false
    }
}