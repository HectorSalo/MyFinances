package com.skysam.hchirinos.myfinances.common.model.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.skysam.hchirinos.myfinances.common.MyFinancesApp
import com.skysam.hchirinos.myfinances.common.model.SharedPreferencesBD

object Auth {
    private fun getCurrentUser(): FirebaseUser? {
        val mAuth = FirebaseAuth.getInstance()
        return mAuth.currentUser
    }

    fun getUser(): FirebaseUser? {
        return getCurrentUser()
    }

    fun uidCurrentUser(): String {
        return if (SharedPreferencesBD.getAccount(MyFinancesApp.MyFinancesAppObject.getContext()) == 1) {
            getCurrentUser()!!.uid
        } else {
            "${getCurrentUser()!!.uid}_2"
        }
    }
}