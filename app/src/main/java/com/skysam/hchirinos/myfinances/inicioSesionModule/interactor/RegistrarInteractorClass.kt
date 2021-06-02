package com.skysam.hchirinos.myfinances.inicioSesionModule.interactor

import com.google.firebase.auth.FirebaseAuth
import com.skysam.hchirinos.myfinances.inicioSesionModule.presenter.RegistrarPresenter

class RegistrarInteractorClass(private val registrarPresenter: RegistrarPresenter): RegistrarInteractor {
    override fun registerUser(email: String, password: String) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener{
            registrarPresenter.registerSuccess(true)
        }
                .addOnFailureListener{
            registrarPresenter.registerSuccess(false)
        }
    }
}