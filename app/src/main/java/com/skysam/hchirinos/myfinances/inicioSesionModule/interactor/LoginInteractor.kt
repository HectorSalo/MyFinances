package com.skysam.hchirinos.myfinances.inicioSesionModule.interactor

import com.google.android.gms.auth.api.signin.GoogleSignInAccount

interface LoginInteractor {
    fun getCurrentUser()
    fun sendEmailRecovery(email: String)
    fun authWithGoogle(acct: GoogleSignInAccount)
    fun getTipoBloqueo(uid: String)
    fun authWithEmail(email: String, password: String)
}