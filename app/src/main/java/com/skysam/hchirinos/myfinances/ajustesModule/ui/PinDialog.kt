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
import androidx.fragment.app.DialogFragment
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.common.model.firebase.FirebaseAuthentication
import com.skysam.hchirinos.myfinances.common.utils.Constants
import com.skysam.hchirinos.myfinances.databinding.DialogPinSettingsBinding

/**
 * Created by Hector Chirinos on 26/02/2021.
 */
class PinDialog(private val validarPinRespaldo: ValidarPinRespaldo, private val fromBloqueo: String, private val toPin: Boolean): DialogFragment() {

    private var _binding: DialogPinSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var builder: AlertDialog.Builder
    private lateinit var buttonPositive: Button
    private lateinit var buttonNegative: Button
    private var fromHuella: Boolean = false
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogPinSettingsBinding.inflate(layoutInflater)
        var title = ""
        var textButtonPositive = ""

        when(fromBloqueo) {
            Constants.PREFERENCE_BLOQUEO_HUELLA -> {
                title = getString(R.string.text_ingrese_pin_respaldo)
                textButtonPositive = getString(R.string.text_validar)
                binding.inputRepetirPin.visibility = View.GONE
                fromHuella = true
            }
            Constants.PREFERENCE_BLOQUEO_PIN -> {
                title = getString(R.string.text_ingrese_pin_actual)
                textButtonPositive = getString(R.string.text_validar)
                binding.inputRepetirPin.visibility = View.GONE
            }
            Constants.PREFERENCE_SIN_BLOQUEO -> {
                title = getString(R.string.text_ingrese_pin)
                textButtonPositive = getString(R.string.btn_guardar)
            }
        }

        builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(title)
                .setView(binding.root)
                .setPositiveButton(textButtonPositive, null)
                .setNegativeButton(R.string.btn_cancelar, null)

        val dialog = builder.create()
        dialog.show()
        dialog.setCanceledOnTouchOutside(false)

        buttonNegative = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        buttonPositive = dialog.getButton(DialogInterface.BUTTON_POSITIVE)

        buttonPositive.setOnClickListener {
            binding.inputPin.error = null
            binding.inputRepetirPin.error = null
            binding.inputPin.errorIconDrawable = null
            binding.inputRepetirPin.errorIconDrawable = null
            if (toPin) {
                if (!fromHuella) {
                    validatePinNew()
                } else {
                    validatePinBakcup()
                }
            } else {
                validatePinBakcup()
            }
        }

        buttonNegative.setOnClickListener {
            validarPinRespaldo.cancelDialog()
            dismiss()
        }

        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun validatePinNew() {
        val pin = binding.etRegistrarPin.text.toString()
        if (pin.isEmpty()) {
            binding.inputPin.error = getString(R.string.error_campo_vacio)
            return
        }
        val pinRepeat = binding.etPinRepetir.text.toString()
        if (pinRepeat.isEmpty()) {
            binding.inputRepetirPin.error = getString(R.string.error_campo_vacio)
            return
        }
        if (pin != pinRepeat) {
            binding.inputRepetirPin.error = getString(R.string.error_pin_match)
            return
        }
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)

        val sharedPreferences = requireActivity().getSharedPreferences(FirebaseAuthentication.getCurrentUser()!!.uid, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(Constants.PREFERENCE_TIPO_BLOQUEO, Constants.PREFERENCE_BLOQUEO_PIN)
        editor.putString(Constants.PREFERENCE_PIN_ALMACENADO, pin)
        editor.apply()

        buttonPositive.visibility = View.GONE
        buttonNegative.visibility = View.GONE
        binding.linearLayout.visibility = View.GONE
        binding.lottieAnimationView.visibility = View.VISIBLE

        binding.lottieAnimationView.setAnimation("pin_check.json")
        binding.lottieAnimationView.playAnimation()
        Handler(Looper.myLooper()!!).postDelayed({
            validarPinRespaldo.changeTipoBloqueo(Constants.PREFERENCE_BLOQUEO_PIN)
            dialog!!.dismiss() }, 2500)
    }

    private fun validatePinBakcup() {
        val sharedPreferences = requireActivity().getSharedPreferences(FirebaseAuthentication.getCurrentUser()!!.uid, Context.MODE_PRIVATE)
        val pinRespaldo = sharedPreferences.getString(Constants.PREFERENCE_PIN_ALMACENADO, "0000")
        val pinIngresado = binding.etRegistrarPin.text.toString()

        if (pinIngresado.isEmpty()) {
            binding.inputPin.error = getString(R.string.error_campo_vacio)
            return
        }

        if (pinIngresado == pinRespaldo) {
            if (!fromHuella) {
                val editor = sharedPreferences.edit()
                editor.putString(Constants.PREFERENCE_TIPO_BLOQUEO, Constants.PREFERENCE_SIN_BLOQUEO)
                editor.putString(Constants.PREFERENCE_PIN_ALMACENADO, "0000")
                editor.apply()
                validarPinRespaldo.changeTipoBloqueo(Constants.PREFERENCE_SIN_BLOQUEO)
                dialog!!.dismiss()
            } else {
                dialog!!.setTitle(getString(R.string.text_ingrese_pin))
                buttonPositive.text = getString(R.string.btn_guardar)
                binding.inputRepetirPin.visibility = View.VISIBLE
                binding.etRegistrarPin.requestFocus()
                binding.etRegistrarPin.text!!.clear()
                binding.etPinRepetir.text!!.clear()
                fromHuella = false
            }
        } else {
            binding.inputPin.error = getString(R.string.error_pin_code)
        }
    }
}