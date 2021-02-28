package com.skysam.hchirinos.myfinances.ajustesModule.ui

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.fragment.app.DialogFragment
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.databinding.DialogHuellaSettingsBinding

class HuellaDialog(val fromPin: Boolean): DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogHuellaSettingsBinding = DialogHuellaSettingsBinding.inflate(layoutInflater)

        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(dialogHuellaSettingsBinding.root)
                .setNegativeButton(R.string.btn_cancelar, null)

        val biometricManager = BiometricManager.from(requireContext())
        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                dialogHuellaSettingsBinding.lottieAnimationView.setAnimation("huella_wrong.json")
                dialogHuellaSettingsBinding.lottieAnimationView.playAnimation()
                dialogHuellaSettingsBinding.tvInfoHuella.text = "No está disponible el bloqueo por huella en este dispositivo actualmente"
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                dialogHuellaSettingsBinding.lottieAnimationView.setAnimation("huella_wrong.json")
                dialogHuellaSettingsBinding.lottieAnimationView.playAnimation()
                dialogHuellaSettingsBinding.tvInfoHuella.text = "No tiene ninguna huella asociada a su dispositivo"
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                dialogHuellaSettingsBinding.lottieAnimationView.setAnimation("huella_wrong.json")
                dialogHuellaSettingsBinding.lottieAnimationView.playAnimation()
                dialogHuellaSettingsBinding.tvInfoHuella.text = "Este dispositivo no cuenta con lector de huella"
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
            }
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
            }
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
            }
            BiometricManager.BIOMETRIC_SUCCESS -> {
                dialogHuellaSettingsBinding.lottieAnimationView.setAnimation("huella_init.json")
                dialogHuellaSettingsBinding.lottieAnimationView.playAnimation()
                dialogHuellaSettingsBinding.tvInfoHuella.text = "¿Desea bloquear la App con su huella?"
                builder.setPositiveButton(getString(R.string.btn_bloquear), null)
            }
            else -> {
                dialogHuellaSettingsBinding.lottieAnimationView.setAnimation("huella_init.json")
                dialogHuellaSettingsBinding.lottieAnimationView.playAnimation()
                dialogHuellaSettingsBinding.tvInfoHuella.text = "Bloqueo por huella no disponible."
            }
        }

        val dialog = builder.create()
        dialog.show()

        val buttonNegative = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        val buttonPositive = dialog.getButton(DialogInterface.BUTTON_POSITIVE)

        return dialog
    }
}