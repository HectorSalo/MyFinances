package com.skysam.hchirinos.myfinances.bloqueoFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.textfield.TextInputLayout
import com.skysam.hchirinos.myfinances.R
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

        if(inicio) {
            view.input_repetir_pin.visibility = View.GONE
            view.title_pin.text = "Ingrese PIN"
        } else {
            view.title_pin.text = "Ingrese PIN Respaldo"
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
}