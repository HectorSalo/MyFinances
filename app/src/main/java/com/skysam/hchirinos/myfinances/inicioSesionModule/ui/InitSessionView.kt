package com.skysam.hchirinos.myfinances.inicioSesionModule.ui

import com.google.firebase.auth.FirebaseUser

interface InitSessionView {
    fun userActive(active: Boolean, user: FirebaseUser?)
    fun emailRecoverySuccesfully()
    fun authWithGoogleStatus(ok: Boolean)
    fun tipoBloqueo(bloqueo: String)
    fun authWithEmailStatus(ok: Boolean)
}