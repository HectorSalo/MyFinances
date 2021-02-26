package com.skysam.hchirinos.myfinances.ajustesModule.ui

import android.content.Context
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.google.firebase.auth.FirebaseAuth
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.common.utils.Constants

class PreferenceSettingsFragment : PreferenceFragmentCompat(), ValidarPinRespaldo {

    private lateinit var listaBloqueo: ListPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferencias_preferences, rootKey)

        val user = FirebaseAuth.getInstance().currentUser
        val sharedPreferences = requireActivity().getSharedPreferences(user!!.uid, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val bloqueo = sharedPreferences.getString(Constants.PREFERENCE_TIPO_BLOQUEO, Constants.PREFERENCE_SIN_BLOQUEO)
        val temaInicial = sharedPreferences.getString(Constants.PREFERENCE_TEMA, Constants.PREFERENCE_TEMA_SISTEMA)
        val notificationActive = sharedPreferences.getBoolean(Constants.PREFERENCE_NOTIFICATION_ACTIVE, true)

        listaBloqueo = findPreference(Constants.PREFERENCE_TIPO_BLOQUEO)!!
        val notificacionesSwitch = findPreference<SwitchPreferenceCompat>(Constants.PREFERENCE_NOTIFICATION_MAIN_TOPIC)
        val listaTema = findPreference<ListPreference>(Constants.PREFERENCE_TEMA)

        when (bloqueo) {
            Constants.PREFERENCE_SIN_BLOQUEO -> listaBloqueo!!.value = Constants.PREFERENCE_SIN_BLOQUEO
            Constants.PREFERENCE_BLOQUEO_HUELLA -> listaBloqueo!!.value = Constants.PREFERENCE_BLOQUEO_HUELLA
            Constants.PREFERENCE_BLOQUEO_PIN -> listaBloqueo!!.value = Constants.PREFERENCE_BLOQUEO_PIN
        }

        notificacionesSwitch!!.isChecked = notificationActive

        when (temaInicial) {
            Constants.PREFERENCE_TEMA_SISTEMA -> listaTema!!.value = Constants.PREFERENCE_TEMA_SISTEMA
            Constants.PREFERENCE_TEMA_OSCURO -> listaTema!!.value = Constants.PREFERENCE_TEMA_OSCURO
            Constants.PREFERENCE_TEMA_CLARO -> listaTema!!.value = Constants.PREFERENCE_TEMA_CLARO
        }

        listaBloqueo!!.setOnPreferenceClickListener {
            val pinDialog = PinDialog(this)
            pinDialog.show(requireActivity().supportFragmentManager, tag)
            true
        }
    }

    override fun validarPinRespaldo(pinOk: Boolean) {
        if (!pinOk) {

        }
    }
}