package com.skysam.hchirinos.myfinances.ui.ajustes

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.Utils.Constantes
import java.util.concurrent.Executor


class BloqueoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bloqueo)

        val user = intent.getStringExtra(Constantes.USER)
        val tipoBloqueo = intent.getStringExtra(Constantes.PREFERENCE_TIPO_BLOQUEO)

        if (tipoBloqueo == Constantes.PREFERENCE_BLOQUEO_HUELLA) {
            crearDialogHuella();
            //supportFragmentManager.beginTransaction().add(R.id.nav_host_fragment, huellaFragment).commit()
        } else {
            //supportFragmentManager.beginTransaction().add(R.id.nav_host_fragment, pinFragment).commit()
        }
    }

    private fun crearDialogHuella() {
        lateinit var biometricPrompt: BiometricPrompt

        var executor: Executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int,
                                                       errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                    }

                    override fun onAuthenticationSucceeded(
                            result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                    }
                })

        var promptInfo: BiometricPrompt.PromptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Bienvenido a Mis Finanzas")
                .setSubtitle(getString(R.string.text_coloque_huella))
                .setNegativeButtonText("Acceder con PIN de respaldo")
                .build()

        biometricPrompt.authenticate(promptInfo)

        // Prompt appears when user clicks "Log in".
        // Consider integrating with the keystore to unlock cryptographic operations,
        // if needed by your app.
        /*val biometricLoginButton =
                    findViewById<Button>(R.id.biometric_login)
            biometricLoginButton.setOnClickListener {
                */

    }
}