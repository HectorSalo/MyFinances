package com.skysam.hchirinos.myfinances.inicioSesionModule.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.common.utils.Constants
import com.skysam.hchirinos.myfinances.databinding.FragmentHuellaBinding
import com.skysam.hchirinos.myfinances.homeModule.ui.HomeActivity
import java.util.concurrent.Executor


class HuellaFragment : Fragment() {

    private var _fragmentHuellaBinding : FragmentHuellaBinding? = null
    private val fragmentHuellaBinding get() = _fragmentHuellaBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _fragmentHuellaBinding = FragmentHuellaBinding.inflate(inflater, container, false)
        return fragmentHuellaBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finishAffinity()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        fragmentHuellaBinding.tvInfoHuella.setOnClickListener {
            crearDialogHuella()
        }

        fragmentHuellaBinding.button.setOnClickListener { validarPin() }

        crearDialogHuella()

    }

    private fun crearDialogHuella() {
        lateinit var biometricPrompt: BiometricPrompt

        val executor: Executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int,
                                                       errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        when (errorCode) {
                            BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                                fragmentHuellaBinding.tvInfoHuella.visibility = View.GONE
                                fragmentHuellaBinding.lottieAnimationView.visibility = View.GONE
                                fragmentHuellaBinding.linearLayout.visibility = View.VISIBLE
                            }
                            BiometricPrompt.ERROR_LOCKOUT -> {
                                fragmentHuellaBinding.tvInfoHuella.visibility = View.GONE
                                fragmentHuellaBinding.lottieAnimationView.visibility = View.GONE
                                fragmentHuellaBinding.linearLayout.visibility = View.VISIBLE
                            }
                        }
                    }

                    override fun onAuthenticationSucceeded(
                            result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        fragmentHuellaBinding.tvInfoHuella.visibility = View.GONE
                        fragmentHuellaBinding.lottieAnimationView.setAnimation("huella_check.json")
                        fragmentHuellaBinding.lottieAnimationView.playAnimation()
                        Handler(Looper.myLooper()!!).postDelayed({
                            startActivity(Intent(requireContext(), HomeActivity::class.java)) }, 3500)
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                    }
                })

        val promptInfo: BiometricPrompt.PromptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Bienvenido a Mis Finanzas")
                .setSubtitle(getString(R.string.text_coloque_huella))
                .setNegativeButtonText("Acceder con PIN de respaldo")
                .build()

        biometricPrompt.authenticate(promptInfo)

    }

    private fun validarPin() {
        val user = FirebaseAuth.getInstance().currentUser

        val sharedPreferences = context?.getSharedPreferences(user!!.uid, Context.MODE_PRIVATE)
        val pinAlmacenado = sharedPreferences?.getString(Constants.PREFERENCE_PIN_ALMACENADO, "0000")

        fragmentHuellaBinding.button.hideKeyboard()
        fragmentHuellaBinding.inputPin.error = null
        if (pinAlmacenado == fragmentHuellaBinding.etRegistrarPin.text.toString()) {
            context?.startActivity(Intent(context, HomeActivity::class.java))
        } else {
            fragmentHuellaBinding.inputPin.error = getString(R.string.error_pass_code)
        }
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragmentHuellaBinding = null
    }
}




