package com.skysam.hchirinos.myfinances.listaGastosModule.ui

import com.skysam.hchirinos.myfinances.common.model.constructores.ImagenesListasConstructor

interface CrearEditarListaView {
    fun cargarImagenes(imagenes: ArrayList<ImagenesListasConstructor>)
    fun progressUploadImage(progress: Double)
    fun resultUploadImage(statusOk: Boolean, data: String)
}