package com.skysam.hchirinos.myfinances.listaGastosModule.presenter

import com.skysam.hchirinos.myfinances.common.model.constructores.ImagenesListasConstructor
import com.skysam.hchirinos.myfinances.listaGastosModule.interactor.CrearEditarListaInteractor
import com.skysam.hchirinos.myfinances.listaGastosModule.interactor.CrearEditarListaInteractorClass
import com.skysam.hchirinos.myfinances.listaGastosModule.ui.CrearEditarListaView

class CrearEditarListaPresenterClass(val crearEditarListaView: CrearEditarListaView): CrearEditarListaPresenter {
    private val crearEditarListaInteractor: CrearEditarListaInteractor = CrearEditarListaInteractorClass(this)
    override fun getImages() {
        crearEditarListaInteractor.getImages()
    }

    override fun cargarImagenes(imagenes: ArrayList<ImagenesListasConstructor>) {
        crearEditarListaView.cargarImagenes(imagenes)
    }
}