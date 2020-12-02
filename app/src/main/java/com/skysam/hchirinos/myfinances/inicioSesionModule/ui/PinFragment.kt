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
import com.google.firebase.auth.FirebaseAuth
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.common.utils.Constants
import com.skysam.hchirinos.myfinances.databinding.FragmentPinBinding
import com.skysam.hchirinos.myfinances.homeModule.ui.HomeActivity

class PinFragment : Fragment() {


    private var _binding: FragmentPinBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentPinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = FirebaseAuth.getInstance().currentUser

        val sharedPreferences = context?.getSharedPreferences(user!!.uid, Context.MODE_PRIVATE)
        val pinAlmacenado = sharedPreferences?.getString(Constants.PREFERENCE_PIN_ALMACENADO, "0000")

        binding.button.setOnClickListener {
            binding.button.hideKeyboard()
            binding.inputPin.error = null
            if (pinAlmacenado == binding.etRegistrarPin.text.toString()) {
                binding.linearLayout.visibility = View.GONE
                binding.lottieAnimationView.visibility = View.VISIBLE
                binding.lottieAnimationView.setAnimation("pin_check.json")
                binding.lottieAnimationView.playAnimation()
                Handler(Looper.getMainLooper())
                        .postDelayed({
                    context?.startActivity(Intent(context, HomeActivity::class.java))
                }, 2500)
            } else {
                binding.inputPin.error = getString(R.string.error_pass_code)
            }
        }
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}