package com.skysam.hchirinos.myfinances.ui.ajustes

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.skysam.hchirinos.myfinances.Utils.Constantes
import com.skysam.hchirinos.myfinances.databinding.FragmentPinBinding
import com.skysam.hchirinos.myfinances.ui.inicio.HomeActivity
import kotlinx.android.synthetic.main.fragment_pin.*

private const val ARG_PARAM1 = "user"
private const val ARG_PARAM2 = "inicio"


class PinFragment : Fragment() {


    private var user: String? = null
    private var inicio: Boolean = true
    private var _binding: FragmentPinBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            user = it.getString(ARG_PARAM1)
            inicio = it.getBoolean(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentPinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = context?.getSharedPreferences(user, Constantes.CONTEXT_PREFERENCE)
        val pinAlmacenado = sharedPreferences?.getString(Constantes.PREFERENCE_PIN_ALMACENADO, "0000")

        binding.inputPin.errorIconDrawable = null
        binding.inputRepetirPin.errorIconDrawable = null

        if(inicio) {
            binding.inputRepetirPin.visibility = View.GONE
            binding.titlePin.text = "Ingrese PIN"
        } else {
            binding.titlePin.text = "Ingrese PIN de Bloqueo"
            binding.lottieAnimationView.visibility = View.GONE
        }

        binding.button.setOnClickListener {
            button.hideKeyboard()
            if (inicio) {
                input_pin.error = null
                if (pinAlmacenado == et_registrar_pin.text.toString()) {
                    linearLayout.visibility = View.GONE
                    lottieAnimationView.visibility = View.VISIBLE
                    lottieAnimationView.setAnimation("pin_check.json")
                    lottieAnimationView.playAnimation()
                    Handler().postDelayed({
                        context?.startActivity(Intent(context, HomeActivity::class.java))
                    }, 2500)
                } else {
                    input_pin.error = "PIN incorrecto"
                    lottieAnimationView.visibility = View.VISIBLE
                    lottieAnimationView.setAnimation("pin_wrong.json")
                    lottieAnimationView.playAnimation()
                }
            } else {

            }
        }

    }

    companion object {
        @JvmStatic
        fun newInstance(user: String, inicio: Boolean) =
                PinFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, user)
                        putBoolean(ARG_PARAM2, inicio)
                    }
                }
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}