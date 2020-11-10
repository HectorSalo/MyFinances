package com.skysam.hchirinos.myfinances.inicioSesionModule.interactor

import com.skysam.hchirinos.myfinances.common.model.firebase.FirebaseAuthentication
import com.skysam.hchirinos.myfinances.inicioSesionModule.presenter.RegistrarPresenter

class RegistrarInteractorClass(val registrarPresenter: RegistrarPresenter): RegistrarInteractor {
    override fun registerUser(email: String, password: String) {
        if (FirebaseAuthentication.getInstance().createUserWithEmailAndPassword(email, password).isSuccessful) {
            registrarPresenter.registerSuccess(true)
        } else {
            registrarPresenter.registerSuccess(false)
        }
    }
}