package com.skysam.hchirinos.myfinances.common.model.firebase

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.skysam.hchirinos.myfinances.common.utils.Constants


object FirebaseFirestore {
    private const val SEPARATOR = "-"

    private fun getInstance(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    fun getIngresosReference(year: Int, month: Int): CollectionReference {
        return getInstance().collection(Constants.BD_INGRESOS).document(Auth.uidCurrentUser())
            .collection("$year$SEPARATOR$month")
    }

    fun getAhorrosReference(year: Int, month: Int): CollectionReference {
        return getInstance().collection(Constants.BD_AHORROS).document(Auth.uidCurrentUser())
            .collection("$year$SEPARATOR$month")
    }

    fun getPrestamosReference(year: Int, month: Int): CollectionReference {
        return getInstance().collection(Constants.BD_PRESTAMOS).document(Auth.uidCurrentUser())
            .collection("$year$SEPARATOR$month")
    }

    fun getGastosReference(year: Int, month: Int): CollectionReference {
        return getInstance().collection(Constants.BD_GASTOS).document(Auth.uidCurrentUser())
            .collection("$year$SEPARATOR$month")
    }

    fun getDeudasReference(year: Int, month: Int): CollectionReference {
        return getInstance().collection(Constants.BD_DEUDAS).document(Auth.uidCurrentUser())
            .collection("$year$SEPARATOR$month")
    }

    fun getImages(): CollectionReference {
        return getInstance().collection(Constants.BD_IMAGENES_LISTAS)
    }

}