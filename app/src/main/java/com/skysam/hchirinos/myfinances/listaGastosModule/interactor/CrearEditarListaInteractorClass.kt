package com.skysam.hchirinos.myfinances.listaGastosModule.interactor

import android.net.Uri
import android.util.Log
import androidx.constraintlayout.widget.Constraints.TAG
import com.google.firebase.storage.StorageReference
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.common.MyFinancesApp
import com.skysam.hchirinos.myfinances.common.model.constructores.ImagenesListasConstructor
import com.skysam.hchirinos.myfinances.common.model.firebase.FirebaseAuthentication
import com.skysam.hchirinos.myfinances.common.model.firebase.FirebaseFirestore
import com.skysam.hchirinos.myfinances.common.model.firebase.FirebaseStorage
import com.skysam.hchirinos.myfinances.common.utils.Constants
import com.skysam.hchirinos.myfinances.listaGastosModule.presenter.CrearEditarListaPresenter

class CrearEditarListaInteractorClass(private val crearEditarListaPresenter: CrearEditarListaPresenter): CrearEditarListaInteractor {
    override fun getImages() {
        val imagenes = ArrayList<ImagenesListasConstructor>()
        val imagenFirst = ImagenesListasConstructor()
        imagenFirst.photoUrl = null
        imagenFirst.imageSelected = true
        imagenes.add(0, imagenFirst)

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

    override fun uploadImage(uri: Uri) {
        if (uri.lastPathSegment != null) {
            val photoRef: StorageReference = FirebaseStorage.getPhotosReferenceByUid(FirebaseAuthentication.getCurrentUser()!!.uid)
                    .child(FirebaseStorage.PATH_IMAGES_LISTS).child(uri.lastPathSegment!!)

            photoRef.putFile(uri).addOnSuccessListener { task ->
                task.storage.downloadUrl.addOnSuccessListener { uri ->
                    if (uri != null) {
                        crearEditarListaPresenter.resultUploadImage(true, uri.toString())
                    } else {
                        crearEditarListaPresenter.resultUploadImage(false,
                                MyFinancesApp.MyFinancesAppObject.getContext().getString(R.string.error_guardar_data))
                    }
                }
            }
                    .addOnFailureListener {
                        crearEditarListaPresenter.resultUploadImage(false,
                                MyFinancesApp.MyFinancesAppObject.getContext().getString(R.string.error_guardar_data))
                    }
                    .addOnProgressListener { task ->
                        val progress = (100.0 * task.bytesTransferred) / task.totalByteCount
                        crearEditarListaPresenter.progressUploadImage(progress)
                    }
        }
    }
}