package com.skysam.hchirinos.myfinances.database.repositories

import android.content.ContentValues
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.skysam.hchirinos.myfinances.common.model.constructores.IngresosGastosConstructor
import com.skysam.hchirinos.myfinances.common.model.firebase.Auth
import com.skysam.hchirinos.myfinances.common.utils.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Created by Hector Chirinos (Home) on 4/5/2021.
 */
object IncomesRepository {
    private const val SEPARATOR = "-"
    private fun getInstance(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    fun getIncomes(year: Int, month: Int): Flow<List<IngresosGastosConstructor>> {
        return callbackFlow {
            val request = getInstance().
            collection(Constants.BD_INGRESOS).document(Auth.getCurrentUser()!!.uid)
                    .collection("$year${SEPARATOR}$month")
                    .orderBy(Constants.BD_MONTO, Query.Direction.ASCENDING)
                    .addSnapshotListener(MetadataChanges.INCLUDE) { value, error ->
                        if (error != null) {
                            Log.w(ContentValues.TAG, "Listen failed.", error)
                            return@addSnapshotListener
                        }

                        val incomes: MutableList<IngresosGastosConstructor> = mutableListOf()
                        for (doc in value!!) {
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
                                val duracionFrecuenciaInt = duracionFrecuencia.toInt()
                                ingreso.duracionFrecuencia = duracionFrecuenciaInt
                                ingreso.fechaIncial = doc.getDate(Constants.BD_FECHA_INCIAL)!!
                                ingreso.tipoFrecuencia = doc.getString(Constants.BD_TIPO_FRECUENCIA)
                                ingreso.fechaFinal = doc.getDate(Constants.BD_FECHA_FINAL)
                            } else {
                                ingreso.tipoFrecuencia = null
                                ingreso.fechaFinal = null
                            }
                            incomes.add(ingreso)
                        }
                        offer(incomes)
                    }
            awaitClose { request.remove() }
        }
    }
}