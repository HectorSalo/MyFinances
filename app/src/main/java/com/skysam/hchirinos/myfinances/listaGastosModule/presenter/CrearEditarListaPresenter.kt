package com.skysam.hchirinos.myfinances.listaGastosModule.presenter

import android.net.Uri
import com.skysam.hchirinos.myfinances.common.model.constructores.ImagenesListasConstructor

interface CrearEditarListaPresenter {
    fun getImages()
    fun uploadImage(uri: Uri)
    fun deleteOldImage(image: String?)

    fun cargarImagenes(imagenes: MutableList<ImagenesListasConstructor>)
    fun progressUploadImage(progress: Double)
    fun resultUploadImage(statusOk: Boolean, data: String)
}