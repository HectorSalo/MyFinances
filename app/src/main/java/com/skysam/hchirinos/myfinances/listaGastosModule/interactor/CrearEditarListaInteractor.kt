package com.skysam.hchirinos.myfinances.listaGastosModule.interactor

import android.net.Uri

interface CrearEditarListaInteractor {
    fun getImages()
    fun uploadImage(uri: Uri)
}