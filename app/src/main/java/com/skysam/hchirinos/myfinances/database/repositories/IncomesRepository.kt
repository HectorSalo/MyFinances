package com.skysam.hchirinos.myfinances.database.repositories

import android.content.ContentValues
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.skysam.hchirinos.myfinances.common.model.constructores.IngresosGastosConstructor
import com.skysam.hchirinos.myfinances.common.model.firebase.Auth
import com.skysam.hchirinos.myfinances.common.utils.Constants
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

                        }
                    }
        }
    }
}