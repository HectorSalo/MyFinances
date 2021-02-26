package com.skysam.hchirinos.myfinances.ajustesModule.ui

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.databinding.DialogPinSettingsBinding

/**
 * Created by Hector Chirinos on 26/02/2021.
 */
class PinDialog(val validarPinRespaldo: ValidarPinRespaldo): DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogPinSettingsBinding = DialogPinSettingsBinding.inflate(layoutInflater)

        dialogPinSettingsBinding.inputRepetirPin.visibility = View.GONE

        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(getString(R.string.text_ingrese_pin_actual))
                .setView(dialogPinSettingsBinding.root)
                .setPositiveButton(R.string.text_validar, null)
                .setNegativeButton(R.string.btn_cancelar, null)

        val dialog = builder.create()
        dialog.show()

        val buttonNegative = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        val buttonPositive = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
        buttonPositive.setOnClickListener {

        }

        return dialog
    }
}