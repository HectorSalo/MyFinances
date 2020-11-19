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

    fun saveCotizacion(uid: String, context: Context, valorFloat: Float) {
        val editor = getInstance(uid, context).edit()
        editor.putFloat(Constants.VALOR_COTIZACION, valorFloat)
        editor.apply()
    }

    fun getCotizacion(uid: String, context: Context): Float {
        return getInstance(uid, context).getFloat(Constants.VALOR_COTIZACION, 1f)
    }
}