package com.skysam.hchirinos.myfinances.inicioSesionModule.interactor

import com.skysam.hchirinos.myfinances.common.model.firebase.FirebaseAuthentication
import com.skysam.hchirinos.myfinances.inicioSesionModule.presenter.RegistrarPresenter

class RegistrarInteractorClass(private val registrarPresenter: RegistrarPresenter): RegistrarInteractor {
    override fun registerUser(email: String, password: String) {
        FirebaseAuthentication.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener{
            registrarPresenter.registerSuccess(true)
        }
                .addOnFailureListener{
            registrarPresenter.registerSuccess(false)
        }
    }
}