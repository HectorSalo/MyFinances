package com.skysam.hchirinos.myfinances.bloqueoFragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.google.android.material.textfield.TextInputLayout
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.Utils.Constantes
import com.skysam.hchirinos.myfinances.principal.HomeActivity
import kotlinx.android.synthetic.main.fragment_pin.*
import kotlinx.android.synthetic.main.fragment_pin.view.*

private const val ARG_PARAM1 = "user"
private const val ARG_PARAM2 = "inicio"


class PinFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var user: String? = null
    private var param2: String? = null
    private var inicio: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            user = it.getString(ARG_PARAM1)
            inicio = it.getBoolean(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_pin, container, false)

        val sharedPreferences = context?.getSharedPreferences(user, Constantes.CONTEXT_PREFERENCE)
        val pinAlmacenado = sharedPreferences?.getString(Constantes.PREFERENCE_PIN_ALMACENADO, "0000")

        view.input_pin.errorIconDrawable = null
        view.input_repetir_pin.errorIconDrawable = null

        if(inicio) {
            view.input_repetir_pin.visibility = View.GONE
            view.title_pin.text = "Ingrese PIN"
        } else {
            view.title_pin.text = "Ingrese PIN de Bloqueo"
            view.lottieAnimationView.visibility = View.GONE
        }

        view.button.setOnClickListener {
            button.hideKeyboard()
            if (inicio) {
                input_pin.error = null
                if (pinAlmacenado == et_registrar_pin.text.toString()) {
                    linearLayout.visibility = View.GONE
                    lottieAnimationView.visibility = View.VISIBLE
                    lottieAnimationView.setAnimation("huella_check.json")
                    lottieAnimationView.playAnimation()
                    Handler().postDelayed({
                        context?.startActivity(Intent(context, HomeActivity::class.java))
                    }, 2500)
                } else {
                    input_pin.error = "PIN incorrecto"
                    lottieAnimationView.visibility = View.VISIBLE
                    lottieAnimationView.setAnimation("huella_wrong.json")
                    lottieAnimationView.playAnimation()
                }
            } else {

            }
        }

        return view
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
}