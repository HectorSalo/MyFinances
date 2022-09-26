package com.skysam.hchirinos.myfinances.common.model

import android.content.Context
import android.content.SharedPreferences
import com.skysam.hchirinos.myfinances.common.model.firebase.Auth
import com.skysam.hchirinos.myfinances.common.utils.Constants

object SharedPreferencesBD {
    var sharedPreferences: SharedPreferences? = null

    fun getInstance(context: Context): SharedPreferences {
        if (sharedPreferences == null) {
           sharedPreferences = context.getSharedPreferences(Auth.getUser()!!.uid, Context.MODE_PRIVATE)
        }
        return sharedPreferences!!
    }

    fun getTipoBloqueo(context: Context): String {
        val bloqueo = getInstance(context).getString(Constants.PREFERENCE_TIPO_BLOQUEO, Constants.PREFERENCE_SIN_BLOQUEO)
        return bloqueo!!
    }

    fun saveCotizacion(context: Context, valorFloat: Float) {
        val editor = getInstance(context).edit()
        editor.putFloat(Constants.VALOR_COTIZACION, valorFloat)
        editor.apply()
    }

    fun getCotizacion(context: Context): Float {
        return getInstance(context).getFloat(Constants.VALOR_COTIZACION, 1f)
    }

    fun saveAccount(context: Context, account: Int) {
        val editor = getInstance(context).edit()
        editor.putInt(Constants.USER, account)
        editor.apply()
    }

    fun getAccount(context: Context): Int {
        return getInstance(context).getInt(Constants.USER, 1)
    }

    fun getFirstSubscribeMainTopic(context: Context) : Boolean {
        return getInstance(context).getBoolean(Constants.SUBSCRIBE_FIRST_NOTIFICATION_MAIN_TOPIC,false)
    }

    fun subscribeFirstMainTopicNotification(context: Context) {
        val editor = getInstance(context).edit()
        editor.putBoolean(Constants.SUBSCRIBE_FIRST_NOTIFICATION_MAIN_TOPIC, true)
        editor.apply()
    }
}