package com.skysam.hchirinos.myfinances.inicioSesionModule.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.common.utils.Constants


class BloqueoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bloqueo)

        val tipoBloqueo = intent.getStringExtra(Constants.PREFERENCE_TIPO_BLOQUEO)

        if (tipoBloqueo == Constants.PREFERENCE_BLOQUEO_HUELLA) {
            supportFragmentManager.beginTransaction().add(R.id.container, HuellaFragment()).commit()
        } else {
            supportFragmentManager.beginTransaction().add(R.id.container, PinFragment()).commit()
        }
    }
}