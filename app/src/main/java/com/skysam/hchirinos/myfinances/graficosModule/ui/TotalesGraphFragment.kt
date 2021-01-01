package com.skysam.hchirinos.myfinances.graficosModule.ui

import android.content.res.Resources.Theme
import android.content.res.TypedArray
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.TintTypedArray.obtainStyledAttributes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.databinding.FragmentTotalesGraphBinding
import com.skysam.hchirinos.myfinances.graficosModule.presenter.TotalesGraphPresenter
import com.skysam.hchirinos.myfinances.graficosModule.presenter.TotalesGraphPresenterClass
import java.util.*


class TotalesGraphFragment : Fragment(), TotalesGraphView {

    private var _binding: FragmentTotalesGraphBinding? = null
    private val binding get() = _binding!!
    private var mesSelected = 0
    private var yearSelected = 0
    private var primaryColor = 0
    private var montoIngresos = 0f
    private var montoAhorros = 0f
    private var montoPrestamos = 0f
    private var montoGastos = 0f
    private var montoDeudas = 0f
    private lateinit var totalesGraphPresenter: TotalesGraphPresenter

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTotalesGraphBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        totalesGraphPresenter = TotalesGraphPresenterClass(this)

        val typedValue = TypedValue()
        val theme: Theme = requireContext().theme
        theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
        primaryColor = ContextCompat.getColor(requireContext(), typedValue.resourceId)

        val calendar = Calendar.getInstance()
        mesSelected = calendar[Calendar.MONTH]
        yearSelected = calendar[Calendar.YEAR]

        val listaMeses = listOf(*resources.getStringArray(R.array.meses))
        val adapterMeses = ArrayAdapter(requireContext(), R.layout.layout_spinner, listaMeses)
        binding.spMes.adapter = adapterMeses

        val listaYears = listOf(*resources.getStringArray(R.array.years))
        val adapterYears = ArrayAdapter(requireContext(), R.layout.layout_spinner, listaYears)
        binding.spYear.adapter = adapterYears

        binding.spMes.setSelection(mesSelected)

        if (yearSelected == 2020) {
            binding.spYear.setSelection(0)
        } else {
            binding.spYear.setSelection(1)
        }

        binding.spMes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                binding.progressBar.visibility = View.VISIBLE
                mesSelected = position
                totalesGraphPresenter.getIngresos(yearSelected, mesSelected)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        binding.spYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                yearSelected = if (position == 0) 2020 else 2021
                totalesGraphPresenter.getIngresos(yearSelected, mesSelected)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        cargarGraficos();
    }

    private fun cargarGraficos() {
        val datos = ArrayList<String>()
        datos.add(0, getString(R.string.menu_ingresos))
        datos.add(1, getString(R.string.menu_ahorros))
        datos.add(2, getString(R.string.pie_prestamos))
        datos.add(3, getString(R.string.menu_egresos))
        datos.add(4, getString(R.string.menu_deudas))

        val barEntries = ArrayList<BarEntry>()
        barEntries.add(BarEntry(0.0f, montoIngresos))
        barEntries.add(BarEntry(1.0f, montoAhorros))
        barEntries.add(BarEntry(2.0f, montoPrestamos))
        barEntries.add(BarEntry(3.0f, montoGastos))
        barEntries.add(BarEntry(4.0f, montoDeudas))

        val barDataSet = BarDataSet(barEntries, getString(R.string.label_nav_totales) + " ($)")
        barDataSet.setColors(Color.GREEN, Color.GREEN, Color.GREEN, Color.RED, Color.RED)
        barDataSet.valueTextColor = primaryColor
        barDataSet.valueTextSize = 14f

        val barData = BarData(barDataSet)

        val xAxis: XAxis = binding.barCharts.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(datos)
        xAxis.position = XAxis.XAxisPosition.TOP
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = 315f

        binding.barCharts.axisLeft.axisMinimum = 0f
        binding.barCharts.axisRight.axisMinimum = 0f
        binding.barCharts.axisLeft.textColor = primaryColor
        binding.barCharts.xAxis.textColor = primaryColor
        binding.barCharts.axisRight.textColor = primaryColor
        binding.barCharts.legend.textColor = primaryColor
        binding.barCharts.animateY(3000)
        binding.barCharts.description = null
        binding.barCharts.data = barData
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun statusValorIngresos(statusOk: Boolean, ingresos: Float, message: String) {
        if (statusOk) {
            montoIngresos = ingresos
            totalesGraphPresenter.getAhorros(yearSelected, mesSelected)
        } else {
            resultError(message)
        }
    }

    override fun statusValorGastos(statusOk: Boolean, gastos: Float, message: String) {
        if (statusOk) {
            montoGastos = gastos
            totalesGraphPresenter.getDeudas(yearSelected, mesSelected)
        } else {
            resultError(message)
        }
    }

    override fun statusValorDeudas(statusOk: Boolean, deudas: Float, message: String) {
        if (statusOk) {
            montoDeudas = deudas
        } else {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
        if (_binding != null) {
            binding.progressBar.visibility = View.GONE
            cargarGraficos()
        }
    }

    override fun statusValorPrestamos(statusOk: Boolean, prestamos: Float, message: String) {
        if (statusOk) {
            montoPrestamos = prestamos
            totalesGraphPresenter.getGastos(yearSelected, mesSelected)
        } else {
            resultError(message)
        }
    }

    override fun statusValorAhorros(statusOk: Boolean, ahorros: Float, message: String) {
        if (statusOk) {
            montoAhorros = ahorros
            totalesGraphPresenter.getPrestamos(yearSelected, mesSelected)
        } else {
            resultError(message)
        }
    }

    fun resultError(message: String) {
        if (_binding != null) {
            cargarGraficos()
            binding.progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }
}