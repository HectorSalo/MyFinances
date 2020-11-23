package com.skysam.hchirinos.myfinances.listaGastosModule.presenter

import com.skysam.hchirinos.myfinances.common.model.constructores.ImagenesListasConstructor

interface CrearEditarListaPresenter {
    fun getImages()

    fun cargarImagenes(imagenes: ArrayList<ImagenesListasConstructor>)
}