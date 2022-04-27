package com.skysam.hchirinos.myfinances.homeModule.repositories

import android.content.ContentValues
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.skysam.hchirinos.myfinances.common.model.constructores.ItemCronologiaConstructor
import com.skysam.hchirinos.myfinances.common.model.firebase.Auth
import com.skysam.hchirinos.myfinances.common.utils.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.*

/**
 * Created by Hector Chirinos (Home) on 27/7/2021.
 */
object CronologyRepository {
    private val calendar: Calendar = Calendar.getInstance()
    private val month = calendar.get(Calendar.MONTH)
    private val year = calendar.get(Calendar.YEAR)

    private fun getInstance(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    fun getIngresos(): Flow<List<ItemCronologiaConstructor>> {
        return callbackFlow {
            val request = getInstance()
                .collection(Constants.BD_INGRESOS).document(Auth.getCurrentUser()!!.uid).collection("$year-$month")
                .addSnapshotListener(MetadataChanges.INCLUDE) { value, error ->
                    if (error != null) {
                        Log.w(ContentValues.TAG, "Listen failed.", error)
                        return@addSnapshotListener
                    }

                    val listaCronologia = mutableListOf<ItemCronologiaConstructor>()

                    for (doc in value!!) {
                        val activo = doc.getBoolean(Constants.BD_MES_ACTIVO)
                        if (activo == null || activo) {
                            val tipoFrecuencia = doc.getString(Constants.BD_TIPO_FRECUENCIA)
                            if (tipoFrecuencia != null) {
                                val calendarCobro = Calendar.getInstance()
                                val duracionFrecuencia = doc.getDouble(Constants.BD_DURACION_FRECUENCIA)!!
                                val duracionFrecuenciaInt = duracionFrecuencia.toInt()
                                calendarCobro.time = doc.getDate(Constants.BD_FECHA_INCIAL)!!
                                val mesInicial = calendarCobro[Calendar.MONTH]
                                var mesCobro = calendarCobro[Calendar.MONTH]
                                var yearCobro = calendarCobro[Calendar.YEAR]

                                while (mesCobro <= month && yearCobro == year) {
                                    if (mesCobro == month) {
                                        val ingreso = ItemCronologiaConstructor()
                                        ingreso.concepto = doc.getString(Constants.BD_CONCEPTO)
                                        ingreso.isDolar = doc.getBoolean(Constants.BD_DOLAR)!!
                                        ingreso.pasivo = false
                                        ingreso.fecha = calendarCobro.time
                                        if (doc.getBoolean(Constants.BD_DOLAR)!!) {
                                            ingreso.monto = doc.getDouble(Constants.BD_MONTO)!!
                                        } else {
                                            if (mesInicial <= 8 && year <= 2021) {
                                                ingreso.monto = (doc.getDouble(Constants.BD_MONTO)!! / 1000000)
                                            } else {
                                                ingreso.monto = doc.getDouble(Constants.BD_MONTO)!!
                                            }
                                        }
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
                    trySend(listaCronologia)
                }
            awaitClose { request.remove() }
        }
    }

    fun getGastos(): Flow<List<ItemCronologiaConstructor>> {
        return callbackFlow {
            val request = getInstance()
                .collection(Constants.BD_GASTOS).document(Auth.getCurrentUser()!!.uid).collection("$year-$month")
                .addSnapshotListener(MetadataChanges.INCLUDE) { value, error ->
                    if (error != null) {
                        Log.w(ContentValues.TAG, "Listen failed.", error)
                        return@addSnapshotListener
                    }

                    val listaCronologia = mutableListOf<ItemCronologiaConstructor>()

                    for (doc in value!!) {
                        val activo = doc.getBoolean(Constants.BD_MES_ACTIVO)
                        if (activo == null || activo) {
                            val tipoFrecuencia = doc.getString(Constants.BD_TIPO_FRECUENCIA)
                            if (tipoFrecuencia != null) {
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
                                        gasto.isPaid = if (doc.getBoolean(Constants.BD_PAGADO) == null) false
                                        else doc.getBoolean(Constants.BD_PAGADO)!!
                                        listaCronologia.add(gasto)
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
                    trySend(listaCronologia)
                }
            awaitClose { request.remove() }
        }
    }
}