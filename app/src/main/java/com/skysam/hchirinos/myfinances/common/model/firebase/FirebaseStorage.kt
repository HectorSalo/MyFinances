package com.skysam.hchirinos.myfinances.common.model.firebase

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

object FirebaseStorage {
    const val PATH_IMAGES_LISTS = "imagenesListas"

    private var mFirebaseStorage: FirebaseStorage? = null

    fun getInstance() : FirebaseStorage {
        if (mFirebaseStorage == null) {
            mFirebaseStorage = FirebaseStorage.getInstance()
        }
        return mFirebaseStorage!!
    }

    fun getPhotosReferenceByUid(uid: String) : StorageReference {
        return getInstance().reference.child(uid)
    }
}