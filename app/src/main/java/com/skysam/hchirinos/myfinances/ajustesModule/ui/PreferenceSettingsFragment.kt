package com.skysam.hchirinos.myfinances.ajustesModule.ui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.google.firebase.auth.FirebaseAuth
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.common.utils.Constants

class PreferenceSettingsFragment : PreferenceFragmentCompat(), ValidarPinRespaldo {

    private lateinit var bloqueo: String
    private lateinit var listaBloqueo: ListPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferencias_preferences, rootKey)

        val user = FirebaseAuth.getInstance().currentUser
        val sharedPreferences = requireActivity().getSharedPreferences(user!!.uid, Context.MODE_PRIVATE)

        bloqueo = sharedPreferences.getString(Constants.PREFERENCE_TIPO_BLOQUEO, Constants.PREFERENCE_SIN_BLOQUEO)!!
        var temaInicial = sharedPreferences.getString(Constants.PREFERENCE_TEMA, Constants.PREFERENCE_TEMA_SISTEMA)
        val notificationActive = sharedPreferences.getBoolean(Constants.PREFERENCE_NOTIFICATION_ACTIVE, true)

        listaBloqueo = findPreference(Constants.PREFERENCE_TIPO_BLOQUEO)!!
        val notificacionesSwitch = findPreference<SwitchPreferenceCompat>(Constants.PREFERENCE_NOTIFICATION_MAIN_TOPIC)
        val listaTema = findPreference<ListPreference>(Constants.PREFERENCE_TEMA)

        when (bloqueo) {
            Constants.PREFERENCE_SIN_BLOQUEO -> listaBloqueo.value = Constants.PREFERENCE_SIN_BLOQUEO
            Constants.PREFERENCE_BLOQUEO_HUELLA -> listaBloqueo.value = Constants.PREFERENCE_BLOQUEO_HUELLA
            Constants.PREFERENCE_BLOQUEO_PIN -> listaBloqueo.value = Constants.PREFERENCE_BLOQUEO_PIN
        }

        notificacionesSwitch!!.isChecked = notificationActive

        when (temaInicial) {
            Constants.PREFERENCE_TEMA_SISTEMA -> listaTema!!.value = Constants.PREFERENCE_TEMA_SISTEMA
            Constants.PREFERENCE_TEMA_OSCURO -> listaTema!!.value = Constants.PREFERENCE_TEMA_OSCURO
            Constants.PREFERENCE_TEMA_CLARO -> listaTema!!.value = Constants.PREFERENCE_TEMA_CLARO
        }

        listaBloqueo.setOnPreferenceChangeListener { _, newValue ->
            bloqueo = listaBloqueo.value
            when (val bloqueoEscogido = newValue as String) {
                Constants.PREFERENCE_SIN_BLOQUEO -> {
                    if (bloqueo != bloqueoEscogido) {
                        val pinDialog = PinDialog(this, bloqueo, false)
                        pinDialog.show(requireActivity().supportFragmentManager, tag)
                    }
                }
                Constants.PREFERENCE_BLOQUEO_HUELLA -> {
                    if (bloqueo != bloqueoEscogido) {
                        if (bloqueo == Constants.PREFERENCE_BLOQUEO_PIN) {
                            val huellaDialog = HuellaDialog(this, true)
                            huellaDialog.show(requireActivity().supportFragmentManager, tag)
                        } else {
                            val huellaDialog = HuellaDialog(this, false)
                            huellaDialog.show(requireActivity().supportFragmentManager, tag)
                        }
                    }
                }
                Constants.PREFERENCE_BLOQUEO_PIN -> {
                    if (bloqueo != bloqueoEscogido) {
                        val pinDialog = PinDialog(this, bloqueo, true)
                        pinDialog.show(requireActivity().supportFragmentManager, tag)
                    }
                }
            }
            true
         }

        listaTema?.setOnPreferenceChangeListener { _, newValue ->
            val editor = sharedPreferences.edit()
            temaInicial = sharedPreferences.getString(Constants.PREFERENCE_TEMA, Constants.PREFERENCE_TEMA_SISTEMA)

            when (val temaEscogido = newValue as String) {
                Constants.PREFERENCE_TEMA_SISTEMA -> if (!temaEscogido.equals(temaInicial, ignoreCase = true)) {
                    editor.putString(Constants.PREFERENCE_TEMA, Constants.PREFERENCE_TEMA_SISTEMA)
                    editor.apply()
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
                Constants.PREFERENCE_TEMA_CLARO -> if (!temaEscogido.equals(temaInicial, ignoreCase = true)) {
                    editor.putString(Constants.PREFERENCE_TEMA, Constants.PREFERENCE_TEMA_CLARO)
                    editor.apply()
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                Constants.PREFERENCE_TEMA_OSCURO -> if (!temaEscogido.equals(temaInicial, ignoreCase = true)) {
                    editor.putString(Constants.PREFERENCE_TEMA, Constants.PREFERENCE_TEMA_OSCURO)
                    editor.apply()
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            }
            true
        }
    }

    override fun changeTipoBloqueo(newBloqueo: String) {
        listaBloqueo.value = newBloqueo
    }

    override fun cancelDialog() {
        listaBloqueo.value = bloqueo
    }
}