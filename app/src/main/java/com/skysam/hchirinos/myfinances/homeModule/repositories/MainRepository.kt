package com.skysam.hchirinos.myfinances.homeModule.repositories

import android.content.ContentValues
import android.util.Log
import androidx.constraintlayout.widget.Constraints
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.skysam.hchirinos.myfinances.common.MyFinancesApp
import com.skysam.hchirinos.myfinances.common.model.SharedPreferencesBD
import com.skysam.hchirinos.myfinances.common.model.constructores.IngresosGastosConstructor
import com.skysam.hchirinos.myfinances.common.model.firebase.Auth
import com.skysam.hchirinos.myfinances.common.utils.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.*

/**
 * Created by Hector Chirinos on 26/07/2021.
 */
object MainRepository {
    val calendar: Calendar = Calendar.getInstance()
    val month = calendar.get(Calendar.MONTH)
    val year = calendar.get(Calendar.YEAR)
    val valorCotizacion = SharedPreferencesBD.getCotizacion(Auth.getCurrentUser()!!.uid, MyFinancesApp.MyFinancesAppObject.getContext())

    private fun getInstance(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    fun getIngresos(): Flow<Double> {
        return callbackFlow {
            val request = getInstance()
                .collection(Constants.BD_INGRESOS).document(Auth.getCurrentUser()!!.uid).collection("$year-$month")
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
                                mesCobro = calendarCobro[Calendar.MONTH]
                                yearCobro = calendarCobro[Calendar.YEAR]

                                while (mesCobro <= month && yearCobro == year) {
                                    if (mesCobro == month) {
                                        montototal = if (dolar) {
                                            montototal + montoDetal
                                        } else {
                                            montototal + montoDetal / valorCotizacion
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
                    offer(montototal)
                }
            awaitClose { request.remove() }
        }
    }
}