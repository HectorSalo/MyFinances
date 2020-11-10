package com.skysam.hchirinos.myfinances.inicioSesionModule.presenter

import com.skysam.hchirinos.myfinances.inicioSesionModule.interactor.RegistrarInteractor
import com.skysam.hchirinos.myfinances.inicioSesionModule.interactor.RegistrarInteractorClass
import com.skysam.hchirinos.myfinances.inicioSesionModule.ui.RegistrarView

class RegistrarPresenterClass(val registrarView: RegistrarView): RegistrarPresenter {
    val registrarInteractor: RegistrarInteractor = RegistrarInteractorClass(this)

    override fun registerUser(email: String, password: String) {
        registrarInteractor.registerUser(email, password)
    }

    override fun registerSuccess(success: Boolean) {
        registrarView.registerSuccess(success)
    }
}