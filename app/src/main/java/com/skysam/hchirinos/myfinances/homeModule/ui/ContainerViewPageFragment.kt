package com.skysam.hchirinos.myfinances.homeModule.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.databinding.FragmentContainerViewPageBinding
import java.util.*


class ContainerViewPageFragment : Fragment() {

    private var _binding: FragmentContainerViewPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentContainerViewPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sectionPageAdapter = SectionPageAdapter(childFragmentManager)
        binding.viewPager.adapter = sectionPageAdapter
        iniciarPuntosSlide(0)
        binding.viewPager.addOnPageChangeListener(viewListener)

    }

    private val viewListener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
        override fun onPageSelected(position: Int) {
            iniciarPuntosSlide(position)
            Toast.makeText(requireContext(), "$position", Toast.LENGTH_SHORT).show()
        }

        override fun onPageScrollStateChanged(state: Int) {}
    }


    private fun iniciarPuntosSlide(pos: Int) {
        val puntosSlide = arrayOfNulls<TextView>(2)
        binding.linearPuntos.removeAllViews()
        for (i in puntosSlide.indices) {
            puntosSlide[i] = TextView(context)
            puntosSlide[i]!!.text = "."
            puntosSlide[i]!!.textSize = 35f
            puntosSlide[i]!!.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
            binding.linearPuntos.addView(puntosSlide[i])
        }
        if (puntosSlide.isNotEmpty()) {
            puntosSlide[pos]!!.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_blue_800))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}