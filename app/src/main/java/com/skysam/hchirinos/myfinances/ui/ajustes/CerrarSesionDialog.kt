package com.skysam.hchirinos.myfinances.ui.ajustes

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
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.Utils.Constantes
import com.skysam.hchirinos.myfinances.ui.inicioSesion.InicSesionActivity

class CerrarSesionDialog : DialogFragment() {
    val user = FirebaseAuth.getInstance().currentUser

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
        FirebaseAuth.getInstance().signOut()
        configurarPreferencesDefault()

        var providerId = ""
        user?.let {
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

            googleSingInClient.signOut().addOnSuccessListener {
                val intent = Intent(requireContext(), InicSesionActivity::class.java)
                startActivity(intent)
            }
        } else {
            val intent = Intent(requireContext(), InicSesionActivity::class.java)
            startActivity(intent)
        }
    }

    private fun configurarPreferencesDefault() {
        val sharedPreferences = requireActivity().getSharedPreferences(user!!.uid, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString(Constantes.PREFERENCE_TIPO_BLOQUEO, Constantes.PREFERENCE_SIN_BLOQUEO)
        editor.putString(Constantes.PREFERENCE_PIN_ALMACENADO, "0000")
        editor.putBoolean(Constantes.PREFERENCE_NOTIFICATION_ACTIVE, true)
        editor.putString(Constantes.PREFERENCE_TEMA, Constantes.PREFERENCE_TEMA_SISTEMA)
        editor.apply()
    }
}
