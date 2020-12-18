package com.skysam.hchirinos.myfinances.homeModule.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.skysam.hchirinos.myfinances.R

class DetallesHomeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detalles_home, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
                DetallesHomeFragment()
    }
}