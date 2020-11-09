package com.skysam.hchirinos.myfinances.common.model.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object FirebaseAuthentication {
    private var mFirebaseAuth: FirebaseAuth? = null


    fun getInstance(): FirebaseAuth {
        if (mFirebaseAuth == null) {
            mFirebaseAuth = FirebaseAuth.getInstance()
        }
        return mFirebaseAuth!!
    }

    fun getCurrentUser() : FirebaseUser? {
        return getInstance().currentUser
    }
}