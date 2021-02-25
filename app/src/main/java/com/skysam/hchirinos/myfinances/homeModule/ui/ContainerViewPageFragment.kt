package com.skysam.hchirinos.myfinances.homeModule.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.databinding.FragmentContainerViewPageBinding
import com.skysam.hchirinos.myfinances.homeModule.presenter.HomePresenter
import com.skysam.hchirinos.myfinances.homeModule.presenter.HomePresenterClass
import java.util.*


class ContainerViewPageFragment : Fragment(), HomeView {

    private var _binding: FragmentContainerViewPageBinding? = null
    private val binding get() = _binding!!
    private lateinit var moveToNextYearDialog: MoveToNextYearDialog
    private lateinit var homePresenter: HomePresenter
    private lateinit var title: String
    private lateinit var toolbar: Toolbar
    private lateinit var itemBuscar: MenuItem

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentContainerViewPageBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homePresenter = HomePresenterClass(this, requireContext())

        val sectionPageAdapter = SectionPageAdapter(childFragmentManager)
        binding.viewPager.adapter = sectionPageAdapter
        iniciarPuntosSlide(0)
        binding.viewPager.addOnPageChangeListener(viewListener)

        val calendar = Calendar.getInstance()
        val mesSelected = calendar[Calendar.MONTH]
        val yearSelected = calendar[Calendar.YEAR]
        val currentDay = calendar[Calendar.DAY_OF_MONTH]

        if (mesSelected == 11 && currentDay > 14) {
            binding.ibTransfer.visibility = View.VISIBLE
        }
        val mesString = when (mesSelected) {
            0 -> "Enero"
            1 -> "Febrero"
            2 -> "Marzo"
            3 -> "Abril"
            4 -> "Mayo"
            5 -> "Junio"
            6 -> "Julio"
            7 -> "Agosto"
            8 -> "Septiembre"
            9 -> "Octubre"
            10 -> "Noviembre"
            11 -> "Diciembre"
            else -> null
        }
        title = "$mesString, $yearSelected"
        configToolbar()

        binding.ibTransfer.setOnClickListener {
            moveToNextYearDialog = MoveToNextYearDialog(yearSelected, homePresenter)
            moveToNextYearDialog.show(requireActivity().supportFragmentManager, tag)
            moveToNextYearDialog.isCancelable = false
        }

    }

    private val viewListener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
        override fun onPageSelected(position: Int) {
            iniciarPuntosSlide(position)
        }

        override fun onPageScrollStateChanged(state: Int) {}
    }


    private fun iniciarPuntosSlide(pos: Int) {
        val puntosSlide = arrayOfNulls<TextView>(2)
        binding.linearPuntos.removeAllViews()
        for (i in puntosSlide.indices) {
            val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, (-80), 0, 0)

            puntosSlide[i] = TextView(context)
            puntosSlide[i]!!.text = "."
            puntosSlide[i]!!.textSize = 36f
            puntosSlide[i]!!.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
            puntosSlide[i]!!.layoutParams = params
            binding.linearPuntos.addView(puntosSlide[i])
        }
        if (puntosSlide.isNotEmpty()) {
            puntosSlide[pos]!!.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_blue_800))
        }
    }

    private fun configToolbar() {
        toolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.top_bar_menu)
        val menu = toolbar.menu
        itemBuscar = menu.findItem(R.id.menu_buscar)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        Handler(Looper.myLooper()!!).postDelayed({
            toolbar.animate().translationY(0f).duration = 500
            itemBuscar.isVisible = false
            toolbar.title = title
        }, 300)
    }

    override fun onPause() {
        super.onPause()
        toolbar.animate().translationY(toolbar.height.toFloat()).duration = 300
    }

    override fun valorCotizacionWebOk(valor: String, valorFloat: Float) {

    }

    override fun valorCotizacionWebError(valorFloat: Float) {

    }

    override fun statusValorIngresos(statusOk: Boolean, ingresos: Float, message: String) {

    }

    override fun statusValorGastos(statusOk: Boolean, gastos: Float, message: String) {

    }

    override fun statusValorDeudas(statusOk: Boolean, ingresos: Float, message: String) {

    }

    override fun statusValorPrestamos(statusOk: Boolean, gastos: Float, message: String) {

    }

    override fun statusValorAhorros(statusOk: Boolean, ingresos: Float, message: String) {

    }

    override fun statusMoveNextYear(statusOk: Boolean, message: String) {
        moveToNextYearDialog.dismiss()
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}