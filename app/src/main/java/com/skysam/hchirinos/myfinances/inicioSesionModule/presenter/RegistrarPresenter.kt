package com.skysam.hchirinos.myfinances.inicioSesionModule.presenter

interface RegistrarPresenter {
    fun registerUser(email: String, password: String)

    fun registerSuccess(success: Boolean)
}