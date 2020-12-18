package com.skysam.hchirinos.myfinances.ingresosModule.interactor

import android.widget.Toast
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.Query
import com.skysam.hchirinos.myfinances.common.model.constructores.IngresosGastosConstructor
import com.skysam.hchirinos.myfinances.common.model.firebase.FirebaseAuthentication
import com.skysam.hchirinos.myfinances.common.model.firebase.FirebaseFirestore
import com.skysam.hchirinos.myfinances.common.utils.Constants
import com.skysam.hchirinos.myfinances.ingresosModule.presenter.IngresosPresenter
import java.util.*

class IngresosInteractorClass(val ingresosPresenter: IngresosPresenter): IngresosInteractor {
    override fun getIngresos(year: Int, month: Int) {
        val listaIngresos = ArrayList<IngresosGastosConstructor>()
        val reference = FirebaseFirestore.getIngresosReference(FirebaseAuthentication.getCurrentUser()!!.uid, year, month)
        val query = reference.orderBy(Constants.BD_MONTO, Query.Direction.ASCENDING)
        query.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (doc in task.result!!) {
                    var perteneceMes = true
                    val calendarCobro = Calendar.getInstance()
                    val ingreso = IngresosGastosConstructor()
                    ingreso.idIngreso = doc.id
                    ingreso.concepto = doc.getString(Constants.BD_CONCEPTO)
                    ingreso.monto = doc.getDouble(Constants.BD_MONTO)!!
                    ingreso.isDolar = doc.getBoolean(Constants.BD_DOLAR)!!
                    val activo = doc.getBoolean(Constants.BD_MES_ACTIVO)
                    if (activo == null) {
                        ingreso.isMesActivo = true
                    } else {
                        ingreso.isMesActivo = activo
                    }
                    val tipoFrecuencia = doc.getString(Constants.BD_TIPO_FRECUENCIA)
                    if (tipoFrecuencia != null) {
                        val duracionFrecuencia = doc.getDouble(Constants.BD_DURACION_FRECUENCIA)!!
                        calendarCobro.time = doc.getDate(Constants.BD_FECHA_INCIAL)!!
                        val duracionFrecuenciaInt = duracionFrecuencia.toInt()
                        ingreso.duracionFrecuencia = duracionFrecuenciaInt
                        ingreso.fechaIncial = doc.getDate(Constants.BD_FECHA_INCIAL)!!
                        ingreso.tipoFrecuencia = doc.getString(Constants.BD_TIPO_FRECUENCIA)
                        ingreso.fechaFinal = doc.getDate(Constants.BD_FECHA_FINAL)


                        var mesCobro = calendarCobro[Calendar.MONTH]
                        var yearCobro = calendarCobro[Calendar.YEAR]

                        while (mesCobro <= month && yearCobro == year) {
                            perteneceMes = mesCobro == month

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

                            if (perteneceMes) {
                                mesCobro += 12
                            } else {
                                mesCobro = calendarCobro[Calendar.MONTH]
                                yearCobro = calendarCobro[Calendar.YEAR]
                            }
                        }
                    } else {
                        ingreso.tipoFrecuencia = null
                        ingreso.fechaFinal = null
                    }

                    if (perteneceMes) listaIngresos.add(ingreso)
                }
                ingresosPresenter.statusListaIngresos(true, listaIngresos, "")
            } else {
                ingresosPresenter.statusListaIngresos(false, listaIngresos, "Error al cargar lista")
            }
        }
    }

    override fun suspenderMes(year: Int, month: Int, id: String) {
        FirebaseFirestore.getIngresosReference(FirebaseAuthentication.getCurrentUser()!!.uid, year, month).document(id)
                .update(Constants.BD_MES_ACTIVO, false)
                .addOnSuccessListener(OnSuccessListener<Void?> { ingresosPresenter.statusSuspenderMes(true) })
                .addOnFailureListener(OnFailureListener { ingresosPresenter.statusSuspenderMes(false) })
    }


}