package com.skysam.hchirinos.myfinances.inicioSesionModule.interactor

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.GoogleAuthProvider
import com.skysam.hchirinos.myfinances.common.model.SharedPreferencesBD
import com.skysam.hchirinos.myfinances.common.model.firebase.FirebaseAuthentication
import com.skysam.hchirinos.myfinances.inicioSesionModule.presenter.LoginPresenter

class LoginInteractorClass(private val loginPresenter: LoginPresenter, val context: Context): LoginInteractor {

    override fun getCurrentUser() {
        val user = FirebaseAuthentication.getCurrentUser()
        if (user != null) {
            loginPresenter.userActive(true, user)
        } else {
            loginPresenter.userActive(false, user)
        }
    }

    override fun sendEmailRecovery(email: String) {
        if (FirebaseAuthentication.getInstance().sendPasswordResetEmail(email).isSuccessful) {
          loginPresenter.emailRecoverySuccesfully()
        }
    }

    override fun authWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        FirebaseAuthentication.getInstance().signInWithCredential(credential).addOnSuccessListener {
            loginPresenter.authWithGoogleStatus(true)
        }.addOnFailureListener {
            loginPresenter.authWithGoogleStatus(false)
        }
    }

    override fun getTipoBloqueo(uid: String) {
        val bloqueo = SharedPreferencesBD.getTipoBloqueo(uid, context)
        loginPresenter.tipoBloqueo(bloqueo)
    }

    override fun authWithEmail(email: String, password: String) {
        FirebaseAuthentication.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                loginPresenter.authWithEmailStatus(true)
            } else {
                loginPresenter.authWithEmailStatus(false)
            }
        }
    }
}