package com.skysam.hchirinos.myfinances.ajustesModule.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.common.model.firebase.Auth
import com.skysam.hchirinos.myfinances.common.model.firebase.Auth.uidCurrentUser
import com.skysam.hchirinos.myfinances.common.utils.Constants
import com.skysam.hchirinos.myfinances.inicioSesionModule.ui.InicSesionActivity

class CerrarSesionDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmar")
        builder.setMessage("¿Desea cerrar la sesión?")
        builder.setPositiveButton("Si") { _, _ ->
            cerrarSesion()
        }.setNegativeButton("No", null)
        return builder.create()
    }

    private fun cerrarSesion() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(Constants.PREFERENCE_NOTIFICATION_MAIN_TOPIC)
        FirebaseAuth.getInstance().signOut()
        val sharedPreferences = requireActivity().getSharedPreferences(uidCurrentUser(), Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString(Constants.PREFERENCE_TIPO_BLOQUEO, Constants.PREFERENCE_SIN_BLOQUEO)
        editor.putString(Constants.PREFERENCE_PIN_ALMACENADO, "0000")
        editor.putBoolean(Constants.PREFERENCE_NOTIFICATION_ACTIVE, true)
        editor.putString(Constants.PREFERENCE_TEMA, Constants.PREFERENCE_TEMA_SISTEMA)
        editor.apply()

        var providerId = ""
        Auth.getUser()?.let {
            for (profile in it.providerData) {
                providerId = profile.providerId
            }
        }

        if (providerId == "google.com") {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()

            val googleSingInClient : GoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

            googleSingInClient.signOut()

        }
        close()
    }

    private fun close() {
        val intent = Intent(requireContext(), InicSesionActivity::class.java)
        startActivity(intent)
    }
}

