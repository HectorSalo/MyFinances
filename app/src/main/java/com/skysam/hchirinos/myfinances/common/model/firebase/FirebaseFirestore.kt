package com.skysam.hchirinos.myfinances.common.model.firebase

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.skysam.hchirinos.myfinances.common.utils.Constants


object FirebaseFirestore {
    private const val SEPARATOR = "-"

    private var mDatabaseReference: FirebaseFirestore? = null

    fun getInstance() : FirebaseFirestore {
        if (mDatabaseReference == null) {
            mDatabaseReference = FirebaseFirestore.getInstance()
        }
        return mDatabaseReference!!
    }

    fun getIngresosReference(uid: String, year: Int, month: Int): CollectionReference {
        return getInstance().collection(Constants.BD_INGRESOS).document(uid).collection("$year$SEPARATOR$month")
    }

    fun getAhorrosReference(uid: String, year: Int, month: Int): CollectionReference {
        return getInstance().collection(Constants.BD_AHORROS).document(uid).collection("$year$SEPARATOR$month")
    }

    fun getPrestamosReference(uid: String, year: Int, month: Int): CollectionReference {
        return getInstance().collection(Constants.BD_PRESTAMOS).document(uid).collection("$year$SEPARATOR$month")
    }

    fun getGastosReference(uid: String, year: Int, month: Int): CollectionReference {
        return getInstance().collection(Constants.BD_GASTOS).document(uid).collection("$year$SEPARATOR$month")
    }

    fun getDeudasReference(uid: String, year: Int, month: Int): CollectionReference {
        return getInstance().collection(Constants.BD_DEUDAS).document(uid).collection("$year$SEPARATOR$month")
    }

    fun getImages(): CollectionReference {
        return getInstance().collection(Constants.BD_IMAGENES_LISTAS)
    }

}