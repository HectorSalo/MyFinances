package com.skysam.hchirinos.myfinances.common

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.skysam.hchirinos.myfinances.common.model.firebase.Auth
import com.skysam.hchirinos.myfinances.common.utils.Constants

class MyFinancesApp: Application() {
    companion object {
        lateinit var appContext: Context
    }

    override fun onCreate() {
        super.onCreate()

        if (Auth.getUser() != null) {
            val sharedPreferences = getSharedPreferences(Auth.getUser()!!.uid, MODE_PRIVATE)

            when (sharedPreferences.getString(Constants.PREFERENCE_TEMA, Constants.PREFERENCE_TEMA_SISTEMA)) {
                Constants.PREFERENCE_TEMA_SISTEMA -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                Constants.PREFERENCE_TEMA_OSCURO -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                Constants.PREFERENCE_TEMA_CLARO -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        appContext = applicationContext
    }

    object MyFinancesAppObject {
        fun getContext(): Context {
            return appContext
        }
    }
}