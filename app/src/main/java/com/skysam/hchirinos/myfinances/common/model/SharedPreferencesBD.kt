package com.skysam.hchirinos.myfinances.common.model

import android.content.Context
import android.content.SharedPreferences
import com.skysam.hchirinos.myfinances.common.utils.Constants

object SharedPreferencesBD {
    var sharedPreferences: SharedPreferences? = null

    fun getInstance(uid: String, context: Context): SharedPreferences {
        if (sharedPreferences == null) {
           sharedPreferences = context.getSharedPreferences(uid, Context.MODE_PRIVATE)
        }
        return sharedPreferences!!
    }

    fun getTipoBloqueo(uid: String, context: Context): String {
        val bloqueo = getInstance(uid, context).getString(Constants.PREFERENCE_TIPO_BLOQUEO, Constants.PREFERENCE_SIN_BLOQUEO)
        return bloqueo!!
    }
}