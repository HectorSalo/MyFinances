package com.skysam.hchirinos.myfinances.ui.inicioSesion

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.Utils.Constantes
import com.skysam.hchirinos.myfinances.ui.inicio.HomeActivity
import java.util.concurrent.Executor


class BloqueoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bloqueo)

        val tipoBloqueo = intent.getStringExtra(Constantes.PREFERENCE_TIPO_BLOQUEO)

        if (tipoBloqueo == Constantes.PREFERENCE_BLOQUEO_HUELLA) {
            supportFragmentManager.beginTransaction().add(R.id.container, HuellaFragment()).commit()
        } else {
            supportFragmentManager.beginTransaction().add(R.id.container, PinFragment()).commit()
        }
    }
}