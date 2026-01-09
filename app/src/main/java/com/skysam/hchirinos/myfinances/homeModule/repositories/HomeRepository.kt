package com.skysam.hchirinos.myfinances.homeModule.repositories

import android.content.ContentValues
import android.util.Log
import androidx.constraintlayout.widget.Constraints
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.skysam.hchirinos.myfinances.common.MyFinancesApp
import com.skysam.hchirinos.myfinances.common.model.SharedPreferencesBD
import com.skysam.hchirinos.myfinances.common.model.firebase.Auth
import com.skysam.hchirinos.myfinances.common.utils.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.*

/**
 * Created by Hector Chirinos on 26/07/2021.
 */
object HomeRepository {
    private val calendar: Calendar = Calendar.getInstance()
    private val month = calendar.get(Calendar.MONTH)
    private val year = calendar.get(Calendar.YEAR)
    private val valorCotizacion = SharedPreferencesBD.getCotizacion(MyFinancesApp.MyFinancesAppObject.getContext())

    private fun getInstance(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    fun getIngresos(): Flow<Double> {
        return callbackFlow {
            val request = getInstance()
                .collection(Constants.BD_INGRESOS).document(Auth.uidCurrentUser()).collection("$year-$month")
                .addSnapshotListener(MetadataChanges.INCLUDE) { value, error ->
                    if (error != null) {
                        Log.w(ContentValues.TAG, "Listen failed.", error)
                        return@addSnapshotListener
                    }

                    var montototal = 0.0
                    var mesCobro: Int
                    var yearCobro: Int

                    for (document in value!!) {
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
                                            if (mesInicial <= 8) {
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
                    trySend(montototal)
                }
            awaitClose { request.remove() }
        }
    }

    fun getGastos(): Flow<Double> {
        return callbackFlow {
            val request = getInstance()
                .collection(Constants.BD_GASTOS).document(Auth.uidCurrentUser()).collection("$year-$month")
                .addSnapshotListener(MetadataChanges.INCLUDE) { value, error ->
                    if (error != null) {
                        Log.w(ContentValues.TAG, "Listen failed.", error)
                        return@addSnapshotListener
                    }

                    var montototal = 0.0
                    var mesPago: Int
                    var yearPago: Int
                    for (document in value!!) {
                        val activo = document.getBoolean(Constants.BD_MES_ACTIVO)
                        if (activo == null || activo) {
                            Log.d(Constraints.TAG, document.id + " => " + document.data)
                            val montoDetal = document.getDouble(Constants.BD_MONTO)!!
                            val dolar = document.getBoolean(Constants.BD_DOLAR)!!
                            val tipoFrecuencia = document.getString(Constants.BD_TIPO_FRECUENCIA)
                            if (tipoFrecuencia != null) {
                                val calendarPago = Calendar.getInstance()
                                calendarPago.time = document.getDate(Constants.BD_FECHA_INCIAL)!!
                                val duracionFrecuencia = document.getDouble(Constants.BD_DURACION_FRECUENCIA)!!
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
                                montototal = if (dolar) {
                                    montototal + montoDetal
                                } else {
                                    montototal + montoDetal / valorCotizacion
                                }
                            }
                        }
                    }
                    trySend(montototal)
                }
            awaitClose { request.remove() }
        }
    }

    fun getAhorros(): Flow<Double> {
        return callbackFlow {
            val request = getInstance()
                .collection(Constants.BD_AHORROS).document(Auth.uidCurrentUser()).collection("$year-$month")
                .whereEqualTo(Constants.BD_CAPITAL, false)
                .addSnapshotListener(MetadataChanges.INCLUDE) { value, error ->
                    if (error != null) {
                        Log.w(ContentValues.TAG, "Listen failed.", error)
                        return@addSnapshotListener
                    }

                    var montototal = 0.0
                    for (document in value!!) {
                        val date = document.getDate(Constants.BD_FECHA_INGRESO)
                        val calendar = Calendar.getInstance()
                        calendar.time = date!!
                        val montoDetal = document.getDouble(Constants.BD_MONTO)!!
                        val dolar = document.getBoolean(Constants.BD_DOLAR)!!
                        montototal += if (dolar) {
                            montoDetal
                        } else {
                            montoDetal / valorCotizacion
                        }
                    }
                    trySend(montototal)
                }
            awaitClose { request.remove() }
        }
    }

    fun getCapital(): Flow<Double> {
        return callbackFlow {
            val request = getInstance()
                .collection(Constants.BD_AHORROS).document(Auth.uidCurrentUser()).collection("$year-$month")
                .whereEqualTo(Constants.BD_CAPITAL, true)
                .addSnapshotListener(MetadataChanges.INCLUDE) { value, error ->
                    if (error != null) {
                        Log.w(ContentValues.TAG, "Listen failed.", error)
                        return@addSnapshotListener
                    }

                    var montototal = 0.0
                    for (document in value!!) {
                        val date = document.getDate(Constants.BD_FECHA_INGRESO)
                        val calendar = Calendar.getInstance()
                        calendar.time = date!!
                        val montoDetal = document.getDouble(Constants.BD_MONTO)!!
                        val dolar = document.getBoolean(Constants.BD_DOLAR)!!
                        montototal += if (dolar) {
                            montoDetal
                        } else {
                            montoDetal / valorCotizacion
                        }
                    }
                    trySend(montototal)
                }
            awaitClose { request.remove() }
        }
    }

    fun getPrestamos(): Flow<Double> {
        return callbackFlow {
            val request = getInstance()
                .collection(Constants.BD_PRESTAMOS).document(Auth.uidCurrentUser()).collection("$year-$month")
                .addSnapshotListener(MetadataChanges.INCLUDE) { value, error ->
                    if (error != null) {
                        Log.w(ContentValues.TAG, "Listen failed.", error)
                        return@addSnapshotListener
                    }

                    var montototal = 0.0
                    for (document in value!!) {
                        Log.d(Constraints.TAG, document.id + " => " + document.data)
                        val montoDetal = document.getDouble(Constants.BD_MONTO)!!
                        val dolar = document.getBoolean(Constants.BD_DOLAR)!!
                        montototal = if (dolar) {
                            montototal + montoDetal
                        } else {
                            montototal + montoDetal / valorCotizacion
                        }
                    }
                    trySend(montototal)
                }
            awaitClose { request.remove() }
        }
    }

    fun getDeudas(): Flow<Double> {
        return callbackFlow {
            val request = getInstance()
                .collection(Constants.BD_DEUDAS).document(Auth.uidCurrentUser()).collection("$year-$month")
                .addSnapshotListener(MetadataChanges.INCLUDE) { value, error ->
                    if (error != null) {
                        Log.w(ContentValues.TAG, "Listen failed.", error)
                        return@addSnapshotListener
                    }

                    var montototal = 0.0
                    for (document in value!!) {
                        Log.d(Constraints.TAG, document.id + " => " + document.data)
                        val montoDetal = document.getDouble(Constants.BD_MONTO)!!
                        val dolar = document.getBoolean(Constants.BD_DOLAR)!!
                        montototal = if (dolar) {
                            montototal + montoDetal
                        } else {
                            montototal + montoDetal / valorCotizacion
                        }
                    }
                    trySend(montototal)
                }
            awaitClose { request.remove() }
        }
    }

    fun getGastosNoFijos(): Flow<Double> {
        return callbackFlow {
            val request = getInstance()
                .collection(Constants.BD_GASTOS).document(Auth.uidCurrentUser()).collection("$year-$month")
                .addSnapshotListener(MetadataChanges.INCLUDE) { value, error ->
                    if (error != null) {
                        Log.w(ContentValues.TAG, "Listen failed.", error)
                        return@addSnapshotListener
                    }

                    var montototal = 0.0
                    for (document in value!!) {
                        val activo = document.getBoolean(Constants.BD_MES_ACTIVO)
                        if (activo == null || activo) {
                            Log.d(Constraints.TAG, document.id + " => " + document.data)
                            val montoDetal = document.getDouble(Constants.BD_MONTO)!!
                            val dolar = document.getBoolean(Constants.BD_DOLAR)!!
                            val tipoFrecuencia = document.getString(Constants.BD_TIPO_FRECUENCIA)
                            if (tipoFrecuencia == null) {
                                montototal = if (dolar) {
                                    montototal + montoDetal
                                } else {
                                    montototal + montoDetal / valorCotizacion
                                }
                            }
                        }
                    }
                    trySend(montototal)
                }
            awaitClose { request.remove() }
        }
    }
}