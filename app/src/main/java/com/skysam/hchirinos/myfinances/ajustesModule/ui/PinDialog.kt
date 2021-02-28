package com.skysam.hchirinos.myfinances.ajustesModule.ui

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.common.utils.Constants
import com.skysam.hchirinos.myfinances.databinding.DialogPinSettingsBinding

/**
 * Created by Hector Chirinos on 26/02/2021.
 */
class PinDialog(val validarPinRespaldo: ValidarPinRespaldo, val fromBloqueo: String, val toPin: Boolean): DialogFragment() {

    private lateinit var dialogPinSettingsBinding: DialogPinSettingsBinding
    private lateinit var builder: AlertDialog.Builder
    private lateinit var buttonPositive: Button
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogPinSettingsBinding = DialogPinSettingsBinding.inflate(layoutInflater)
        var title = ""
        var textButtonPositive = ""

        when(fromBloqueo) {
            Constants.PREFERENCE_BLOQUEO_HUELLA -> {
                title = getString(R.string.text_ingrese_pin_respaldo)
                textButtonPositive = getString(R.string.text_validar)
                dialogPinSettingsBinding.inputRepetirPin.visibility = View.GONE
            }
            Constants.PREFERENCE_BLOQUEO_PIN -> {
                title = getString(R.string.text_ingrese_pin_actual)
                textButtonPositive = getString(R.string.text_validar)
                dialogPinSettingsBinding.inputRepetirPin.visibility = View.GONE
            }
            Constants.PREFERENCE_SIN_BLOQUEO -> {
                title = getString(R.string.text_ingrese_pin)
                textButtonPositive = getString(R.string.btn_guardar)
            }
        }

        builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(title)
                .setView(dialogPinSettingsBinding.root)
                .setPositiveButton(textButtonPositive, null)
                .setNegativeButton(R.string.btn_cancelar, null)

        val dialog = builder.create()
        dialog.show()

        val buttonNegative = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        buttonPositive = dialog.getButton(DialogInterface.BUTTON_POSITIVE)

        buttonPositive.setOnClickListener {
            dialogPinSettingsBinding.inputPin.error = null
            dialogPinSettingsBinding.inputRepetirPin.error = null
            if (toPin) {
                validarToPin()
            } else {
                validarToSinBloqueo()
            }
        }

        buttonNegative.setOnClickListener {
            validarPinRespaldo.cancelDialog()
            dialog.dismiss()
        }

        return dialog
    }

    private fun validarToPin() {

    }

    private fun validarToSinBloqueo() {
        val user = FirebaseAuth.getInstance().currentUser
        val sharedPreferences = requireActivity().getSharedPreferences(user!!.uid, Context.MODE_PRIVATE)
        val pinRespaldo = sharedPreferences.getString(Constants.PREFERENCE_PIN_ALMACENADO, "0000")
        val pinIngresado = dialogPinSettingsBinding.etRegistrarPin.text.toString()

        if (pinIngresado == pinRespaldo) {
            val editor = sharedPreferences.edit()
            editor.putString(Constants.PREFERENCE_TIPO_BLOQUEO, Constants.PREFERENCE_SIN_BLOQUEO)
            editor.putString(Constants.PREFERENCE_PIN_ALMACENADO, "0000")
            editor.apply()
            validarPinRespaldo.changeTipoBloqueo(Constants.PREFERENCE_SIN_BLOQUEO)
        } else {
            dialogPinSettingsBinding.inputPin.error = getString(R.string.error_pin_code)
        }
    }
}