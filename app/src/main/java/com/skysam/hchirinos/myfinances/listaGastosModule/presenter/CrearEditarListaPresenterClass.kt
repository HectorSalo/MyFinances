package com.skysam.hchirinos.myfinances.listaGastosModule.presenter

import android.net.Uri
import com.skysam.hchirinos.myfinances.common.model.constructores.ImagenesListasConstructor
import com.skysam.hchirinos.myfinances.listaGastosModule.interactor.CrearEditarListaInteractor
import com.skysam.hchirinos.myfinances.listaGastosModule.interactor.CrearEditarListaInteractorClass
import com.skysam.hchirinos.myfinances.listaGastosModule.ui.CrearEditarListaView

class CrearEditarListaPresenterClass(private val crearEditarListaView: CrearEditarListaView): CrearEditarListaPresenter {
    private val crearEditarListaInteractor: CrearEditarListaInteractor = CrearEditarListaInteractorClass(this)
    override fun getImages() {
        crearEditarListaInteractor.getImages()
    }

    override fun uploadImage(uri: Uri) {
        crearEditarListaInteractor.uploadImage(uri)
    }

    override fun deleteOldImage(image: String?) {
        crearEditarListaInteractor.deleteOldImage(image)
    }

    override fun cargarImagenes(imagenes: MutableList<ImagenesListasConstructor>) {
        crearEditarListaView.cargarImagenes(imagenes)
    }

    override fun progressUploadImage(progress: Double) {
        crearEditarListaView.progressUploadImage(progress)
    }

    override fun resultUploadImage(statusOk: Boolean, data: String) {
        crearEditarListaView.resultUploadImage(statusOk, data)
    }



}