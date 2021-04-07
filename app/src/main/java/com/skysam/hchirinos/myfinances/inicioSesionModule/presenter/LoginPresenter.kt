package com.skysam.hchirinos.myfinances.inicioSesionModule.presenter

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseUser

interface LoginPresenter {
    fun getCurrentUser()
    fun sendEmailRecovery(email: String)
    fun authWithGoogle(acct: GoogleSignInAccount)
    fun getTipoBloqueo(uid: String)
    fun authWithEmail(email: String, password: String)

    fun userActive(active: Boolean, user: FirebaseUser?)
    fun emailRecoverySuccesfully()
    fun authWithGoogleStatus(ok: Boolean)
    fun tipoBloqueo(bloqueo: String)
    fun authWithEmailStatus(ok: Boolean, msg: String)
}