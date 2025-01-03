package com.skysam.hchirinos.myfinances.graficosModule.ui

import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.common.utils.ClassesCommon
import com.skysam.hchirinos.myfinances.databinding.FragmentDeudasGraphBinding
import com.skysam.hchirinos.myfinances.graficosModule.presenter.DeudasGraphPresenter
import com.skysam.hchirinos.myfinances.graficosModule.presenter.DeudasGraphPresenterClass
import java.util.*

class DeudasGraphFragment : Fragment(), DeudasGraphView {

    private var _binding: FragmentDeudasGraphBinding? = null
    private val binding get() = _binding!!
    private var primaryColor = 0
    private var yearSelected = 0
    private var montoEnero = 0f
    private var montoFebrero = 0f
    private var montoMarzo = 0f
    private var montoAbril = 0f
    private var montoMayo = 0f
    private var montoJunio = 0f
    private var montoJulio = 0f
    private var montoAgosto = 0f
    private var montoSeptiembre = 0f
    private var montoOctubre = 0f
    private var montoNoviembre = 0f
    private var montoDiciembre = 0f
    private var monthCurrent = 0
    private lateinit var deudasGraphPresenter: DeudasGraphPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentDeudasGraphBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        deudasGraphPresenter = DeudasGraphPresenterClass(this)

        val typedValue = TypedValue()
        val theme: Resources.Theme = requireContext().theme
        theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
        primaryColor = ContextCompat.getColor(requireContext(), typedValue.resourceId)

        val calendar = Calendar.getInstance()
        yearSelected = calendar[Calendar.YEAR]
        monthCurrent = calendar[Calendar.MONTH]

        val listaYears = listOf(*resources.getStringArray(R.array.years))
        val adapterYears = ArrayAdapter(requireContext(), R.layout.layout_spinner, listaYears)
        binding.spYear.adapter = adapterYears

        binding.spYear.setSelection(yearSelected - 2020)

        binding.spYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                montoEnero = 0f
                montoFebrero = 0f
                montoMarzo = 0f
                montoAbril = 0f
                montoMayo = 0f
                montoJunio = 0f
                montoJulio = 0f
                montoAgosto = 0f
                montoSeptiembre = 0f
                montoOctubre = 0f
                montoNoviembre = 0f
                montoDiciembre = 0f
                binding.progressBar.visibility = View.VISIBLE
                yearSelected = 2020 + position
                val yearCurrent = calendar[Calendar.YEAR]
                monthCurrent = if (yearCurrent == yearSelected) calendar[Calendar.MONTH] else 11
                deudasGraphPresenter.getMes(yearSelected, 0)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        cargarGraficos()
    }

    private fun cargarGraficos() {
        val datos = arrayListOf(*resources.getStringArray(R.array.meses))

        val barEntries = ArrayList<BarEntry>()
        barEntries.add(BarEntry(0.0f, montoEnero))
        barEntries.add(BarEntry(1.0f, montoFebrero))
        barEntries.add(BarEntry(2.0f, montoMarzo))
        barEntries.add(BarEntry(3.0f, montoAbril))
        barEntries.add(BarEntry(4.0f, montoMayo))
        barEntries.add(BarEntry(5.0f, montoJunio))
        barEntries.add(BarEntry(6.0f, montoJulio))
        barEntries.add(BarEntry(7.0f, montoAgosto))
        barEntries.add(BarEntry(8.0f, montoSeptiembre))
        barEntries.add(BarEntry(9.0f, montoOctubre))
        barEntries.add(BarEntry(10.0f, montoNoviembre))
        barEntries.add(BarEntry(11.0f, montoDiciembre))

        val barDataSet = BarDataSet(barEntries, getString(R.string.menu_deudas) + " ($)")
        barDataSet.color = ContextCompat.getColor(requireContext(), R.color.design_default_color_secondary)
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
        xAxis.textSize = 8f
        xAxis.labelCount = 12

        binding.barCharts.axisLeft.axisMinimum = 0f
        binding.barCharts.axisRight.axisMinimum = 0f
        binding.barCharts.axisLeft.textColor = primaryColor
        binding.barCharts.xAxis.textColor = primaryColor
        binding.barCharts.axisRight.textColor = primaryColor
        binding.barCharts.legend.textColor = primaryColor
        binding.barCharts.animateY(3000)
        binding.barCharts.description = null
        binding.barCharts.data = barData

        var amountTotal = 0.0
        for (i in 0..monthCurrent) {
            amountTotal += barEntries[i].y
        }
        val prom = amountTotal / (monthCurrent + 1)
        binding.tvProm.text = getString(R.string.text_prom_graphs,
                ClassesCommon.convertDoubleToString(prom))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun statusMes(month: Int, statusOk: Boolean, monto: Float, message: String) {
        if (_binding != null) {
            if (month == 11) {
                if (statusOk) {
                    montoDiciembre = monto
                } else {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
                binding.progressBar.visibility = View.GONE
                cargarGraficos()
            } else {
                if (statusOk) {
                    when(month) {
                        0 -> montoEnero = monto
                        1 -> montoFebrero = monto
                        2 -> montoMarzo = monto
                        3 -> montoAbril = monto
                        4 -> montoMayo = monto
                        5 -> montoJunio = monto
                        6 -> montoJulio = monto
                        7 -> montoAgosto = monto
                        8 -> montoSeptiembre = monto
                        9 -> montoOctubre = monto
                        10 -> montoNoviembre = monto
                    }
                    if (month < monthCurrent) {
                        deudasGraphPresenter.getMes(yearSelected, (month + 1))
                    } else {
                        binding.progressBar.visibility = View.GONE
                        cargarGraficos()
                    }
                } else {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                    cargarGraficos()
                }
            }
        }
    }

}