package com.skysam.hchirinos.myfinances.listaGastosModule.interactor

import android.util.Log
import androidx.constraintlayout.widget.Constraints.TAG
import com.skysam.hchirinos.myfinances.common.model.constructores.ImagenesListasConstructor
import com.skysam.hchirinos.myfinances.common.model.firebase.FirebaseFirestore
import com.skysam.hchirinos.myfinances.common.utils.Constants
import com.skysam.hchirinos.myfinances.listaGastosModule.presenter.CrearEditarListaPresenter

class CrearEditarListaInteractorClass(val crearEditarListaPresenter: CrearEditarListaPresenter): CrearEditarListaInteractor {
    override fun getImages() {
        val imagenes = ArrayList<ImagenesListasConstructor>()
        val imagenFirst = ImagenesListasConstructor()
        imagenFirst.photoUrl = null
        imagenFirst.imageSelected = true
        imagenes.add(0, imagenFirst)

        val imagenSecond = ImagenesListasConstructor()
        imagenSecond.photoUrl = null
        imagenSecond.imageSelected = false
        imagenes.add(1, imagenSecond)

        FirebaseFirestore.getImages().get()
                .addOnSuccessListener { result ->
            for (document in result) {
                val imagen = ImagenesListasConstructor()
                imagen.photoUrl = document.getString(Constants.BD_IMAGEN)
                imagen.imageSelected = false
                imagenes.add(imagen)
            }
            crearEditarListaPresenter.cargarImagenes(imagenes)
        }
                .addOnFailureListener { exception->
                    Log.d(TAG, "Error getting documents: ", exception)
                }
    }
}