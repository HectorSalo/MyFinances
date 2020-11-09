package com.skysam.hchirinos.myfinances.inicioSesionModule.presenter

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseUser
import com.skysam.hchirinos.myfinances.inicioSesionModule.interactor.LoginInteractor
import com.skysam.hchirinos.myfinances.inicioSesionModule.interactor.LoginInteractorClass
import com.skysam.hchirinos.myfinances.inicioSesionModule.ui.InitSessionView

class LoginPresenterClass(val initSessionView: InitSessionView, val context: Context): LoginPresenter {
    val loginInteractor: LoginInteractor = LoginInteractorClass(this, context)
    override fun getCurrentUser() {
        loginInteractor.getCurrentUser()
    }

    override fun sendEmailRecovery(email: String) {
        loginInteractor.sendEmailRecovery(email)
    }

    override fun authWithGoogle(acct: GoogleSignInAccount) {
        loginInteractor.authWithGoogle(acct)
    }

    override fun getTipoBloqueo(uid: String) {
        loginInteractor.getTipoBloqueo(uid)
    }

    override fun authWithEmail(email: String, password: String) {
        loginInteractor.authWithEmail(email, password)
    }

    override fun userActive(active: Boolean, user: FirebaseUser?) {
        initSessionView.userActive(active, user)
    }

    override fun emailRecoverySuccesfully() {
        initSessionView.emailRecoverySuccesfully()
    }

    override fun authWithGoogleStatus(ok: Boolean) {
        initSessionView.authWithGoogleStatus(ok)
    }

    override fun tipoBloqueo(bloqueo: String) {
        initSessionView.tipoBloqueo(bloqueo)
    }

    override fun authWithEmailStatus(ok: Boolean) {
        initSessionView.authWithEmailStatus(ok)
    }
}