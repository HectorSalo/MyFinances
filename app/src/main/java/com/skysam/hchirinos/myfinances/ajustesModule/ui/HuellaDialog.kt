package com.skysam.hchirinos.myfinances.ajustesModule.ui

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.fragment.app.DialogFragment
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.common.model.firebase.Auth
import com.skysam.hchirinos.myfinances.common.utils.Constants
import com.skysam.hchirinos.myfinances.databinding.DialogHuellaSettingsBinding

class HuellaDialog(private val validarPinRespaldo: ValidarPinRespaldo, var fromPin: Boolean): DialogFragment() {
    private  var _binding: DialogHuellaSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var buttonPositive: Button
    private lateinit var buttonNegative: Button
    private var isCreatePinBackup: Boolean = false
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogHuellaSettingsBinding.inflate(layoutInflater)

        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding.root)
                .setNegativeButton(R.string.btn_cancelar, null)

        val biometricManager = BiometricManager.from(requireContext())
        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                binding.lottieAnimationView.setAnimation("huella_wrong.json")
                binding.lottieAnimationView.playAnimation()
                binding.tvInfoHuella.text = "No está disponible el bloqueo por huella en este dispositivo actualmente"
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                binding.lottieAnimationView.setAnimation("huella_wrong.json")
                binding.lottieAnimationView.playAnimation()
                binding.tvInfoHuella.text = "No tiene ninguna huella asociada a su dispositivo"
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                binding.lottieAnimationView.setAnimation("huella_wrong.json")
                binding.lottieAnimationView.playAnimation()
                binding.tvInfoHuella.text = "Este dispositivo no cuenta con lector de huella"
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
            }
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
            }
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
            }
            BiometricManager.BIOMETRIC_SUCCESS -> {
                binding.lottieAnimationView.setAnimation("huella_init.json")
                binding.lottieAnimationView.playAnimation()
                binding.tvInfoHuella.text = "¿Desea bloquear la App con su huella?"
                builder.setPositiveButton(getString(R.string.btn_bloquear), null)
            }
            else -> {
                binding.lottieAnimationView.setAnimation("huella_init.json")
                binding.lottieAnimationView.playAnimation()
                binding.tvInfoHuella.text = "Bloqueo por huella no disponible."
            }
        }

        val dialog = builder.create()
        dialog.show()
        dialog.setCanceledOnTouchOutside(false)

        buttonNegative = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        buttonPositive = dialog.getButton(DialogInterface.BUTTON_POSITIVE)

        buttonNegative.setOnClickListener {
            validarPinRespaldo.cancelDialog()
            dismiss()
        }

        buttonPositive.setOnClickListener {
            binding.inputPin.error = null
            binding.inputPin.errorIconDrawable = null
            binding.inputRepetirPin.errorIconDrawable = null
            binding.inputRepetirPin.error = null
            if (!fromPin) {
                if (isCreatePinBackup) {
                    createPinBackup()
                    return@setOnClickListener
                }
                layoutPinBackup()
            } else {
                if (isCreatePinBackup) {
                    validatePinBakcup()
                    return@setOnClickListener
                }
                layoutValidatePin()
            }
        }

        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun layoutPinBackup() {
        binding.tvInfoHuella.text = getString(R.string.text_ingrese_pin_respaldo)
        binding.linearLayout.visibility = View.VISIBLE
        binding.lottieAnimationView.visibility = View.GONE
        buttonPositive.text = getString(R.string.btn_ingresar)
        isCreatePinBackup = true
    }

    private fun createPinBackup() {
        val pin: String = binding.etRegistrarPin.text.toString()
        if (pin.isEmpty()) {
            binding.inputPin.error = getString(R.string.error_campo_vacio)
            return
        }
        val pinRepetir: String = binding.etPinRepetir.text.toString()
        if (pinRepetir.isEmpty()) {
            binding.inputRepetirPin.error = getString(R.string.error_campo_vacio)
            return
        }
        if (pin != pinRepetir) {
            binding.inputRepetirPin.error = getString(R.string.error_pin_match)
            return
        }
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)

        val sharedPreferences = requireActivity().getSharedPreferences(Auth.getCurrentUser()!!.uid, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(Constants.PREFERENCE_TIPO_BLOQUEO, Constants.PREFERENCE_BLOQUEO_HUELLA)
        editor.putString(Constants.PREFERENCE_PIN_ALMACENADO, pin)
        editor.apply()

        buttonPositive.visibility = View.GONE
        buttonNegative.visibility = View.GONE
        binding.linearLayout.visibility = View.GONE
        binding.lottieAnimationView.visibility = View.VISIBLE
        binding.lottieAnimationView.setAnimation("huella_check.json")
        binding.lottieAnimationView.playAnimation()
        binding.tvInfoHuella.text = "Guardando..."
        Handler(Looper.myLooper()!!).postDelayed({ binding.tvInfoHuella.text = "¡Listo!" }, 2500)
        Handler(Looper.myLooper()!!).postDelayed({
            validarPinRespaldo.changeTipoBloqueo(Constants.PREFERENCE_BLOQUEO_HUELLA)
            dialog!!.dismiss() }, 4000)
    }

    private fun layoutValidatePin() {
        binding.tvInfoHuella.text = getString(R.string.text_ingrese_pin_actual)
        binding.linearLayout.visibility = View.VISIBLE
        binding.inputRepetirPin.visibility = View.GONE
        binding.lottieAnimationView.visibility = View.GONE
        buttonPositive.text = getString(R.string.text_validar)
        isCreatePinBackup = true
    }

    private fun validatePinBakcup() {
        val sharedPreferences = requireActivity().getSharedPreferences(Auth.getCurrentUser()!!.uid, Context.MODE_PRIVATE)
        val pinRespaldo = sharedPreferences.getString(Constants.PREFERENCE_PIN_ALMACENADO, "0000")
        val pinIngresado = binding.etRegistrarPin.text.toString()

        if (pinIngresado.isEmpty()) {
            binding.inputPin.error = getString(R.string.error_campo_vacio)
            return
        }

        if (pinIngresado == pinRespaldo) {
            binding.tvInfoHuella.text = getString(R.string.text_ingrese_pin_respaldo)
            buttonPositive.text = getString(R.string.btn_guardar)
            binding.inputRepetirPin.visibility = View.VISIBLE
            binding.etRegistrarPin.requestFocus()
            binding.etRegistrarPin.text!!.clear()
            binding.etPinRepetir.text!!.clear()
            fromPin = false
        } else {
            binding.inputPin.error = getString(R.string.error_pin_code)
        }
    }
}