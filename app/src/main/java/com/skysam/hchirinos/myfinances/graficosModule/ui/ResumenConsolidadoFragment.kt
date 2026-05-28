package com.skysam.hchirinos.myfinances.graficosModule.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.common.utils.ClassesCommon
import com.skysam.hchirinos.myfinances.databinding.FragmentResumenConsolidadoBinding
import com.skysam.hchirinos.myfinances.graficosModule.presenter.ResumenConsolidadoPresenter
import com.skysam.hchirinos.myfinances.graficosModule.presenter.ResumenConsolidadoPresenterClass
import java.util.*
import kotlin.math.roundToInt

class ResumenConsolidadoFragment : Fragment(), ResumenConsolidadoView {

    private var _binding: FragmentResumenConsolidadoBinding? = null
    private val binding get() = _binding!!

    private var yearSelected = 0
    private var monthCurrent = 0
    private var isCurrentYear = false

    private var ingresosAcumulados = 0.0
    private var gastosAcumulados = 0.0
    private var ahorrosAcumulados = 0.0
    private var deudasAcumuladas = 0.0
    private var prestamosAcumulados = 0.0

    private lateinit var presenter: ResumenConsolidadoPresenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResumenConsolidadoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        presenter = ResumenConsolidadoPresenterClass(this)

        val calendar = Calendar.getInstance()
        yearSelected = calendar[Calendar.YEAR]
        monthCurrent = calendar[Calendar.MONTH]
        isCurrentYear = true

        val listaYears = listOf(*resources.getStringArray(R.array.years))
        val adapterYears = ArrayAdapter(requireContext(), R.layout.layout_spinner, listaYears)
        binding.spYear.adapter = adapterYears
        binding.spYear.setSelection(yearSelected - 2020)

        binding.spYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                val yearCurrent = calendar[Calendar.YEAR]
                yearSelected = 2020 + position
                isCurrentYear = yearSelected == yearCurrent
                monthCurrent = if (isCurrentYear) calendar[Calendar.MONTH] else 11
                iniciarCarga()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun iniciarCarga() {
        ingresosAcumulados = 0.0
        gastosAcumulados = 0.0
        ahorrosAcumulados = 0.0
        deudasAcumuladas = 0.0
        prestamosAcumulados = 0.0
        binding.progressBar.visibility = View.VISIBLE
        presenter.getIngresosAnual(yearSelected, 0)
    }

    private fun calcularYMostrarResumen() {
        if (_binding == null) return
        binding.progressBar.visibility = View.GONE

        val mesesConsiderados = monthCurrent + 1

        // Título
        binding.tvTituloConsolidado.text = getString(
            if (isCurrentYear) R.string.resumen_anual_titulo_actual
            else R.string.resumen_anual_titulo_historico
        )

        // Card 1: Totales
        val fmt = { v: Double -> ClassesCommon.convertDoubleToString(v) }
        binding.tvTotalIngresos.text = getString(R.string.consolidado_total_ingresos, fmt(ingresosAcumulados))
        binding.tvTotalGastos.text = getString(R.string.consolidado_total_gastos, fmt(gastosAcumulados))
        binding.tvTotalAhorros.text = getString(R.string.consolidado_total_ahorros, fmt(ahorrosAcumulados))
        binding.tvTotalDeudas.text = getString(R.string.consolidado_total_deudas, fmt(deudasAcumuladas))
        binding.tvTotalPrestamos.text = getString(R.string.consolidado_total_prestamos, fmt(prestamosAcumulados))
        val margen = ingresosAcumulados - gastosAcumulados - deudasAcumuladas
        binding.tvMargen.text = getString(R.string.consolidado_margen, fmt(margen))

        // Card 2: Indicadores
        if (ingresosAcumulados > 0.0) {
            val pGastos = (gastosAcumulados / ingresosAcumulados * 100).roundToInt()
            val pAhorros = (ahorrosAcumulados / ingresosAcumulados * 100).roundToInt()
            val pDeudas = (deudasAcumuladas / ingresosAcumulados * 100).roundToInt()
            val pPrestamos = (prestamosAcumulados / ingresosAcumulados * 100).roundToInt()
            binding.tvPorcGastos.text = getString(R.string.consolidado_porc_gastos, pGastos.toString())
            binding.tvPorcAhorros.text = getString(R.string.consolidado_porc_ahorros, pAhorros.toString())
            binding.tvPorcDeudas.text = getString(R.string.consolidado_porc_deudas, pDeudas.toString())
            binding.tvPorcPrestamos.text = getString(R.string.consolidado_porc_prestamos, pPrestamos.toString())
            mostrarInsight(pGastos, pAhorros, pDeudas)
        } else {
            binding.tvPorcGastos.text = getString(R.string.consolidado_sin_ingresos)
            binding.tvPorcAhorros.text = ""
            binding.tvPorcDeudas.text = ""
            binding.tvPorcPrestamos.text = ""
            binding.tvInsightConsolidado.text = getString(R.string.consolidado_sin_ingresos)
        }

        // Card 3: Promedios
        val pIngresos = if (mesesConsiderados > 0) ingresosAcumulados / mesesConsiderados else 0.0
        val pGastosM = if (mesesConsiderados > 0) gastosAcumulados / mesesConsiderados else 0.0
        val pAhorrosM = if (mesesConsiderados > 0) ahorrosAcumulados / mesesConsiderados else 0.0
        val pDeudasM = if (mesesConsiderados > 0) deudasAcumuladas / mesesConsiderados else 0.0
        binding.tvPromIngresos.text = getString(R.string.consolidado_prom_ingresos, fmt(pIngresos))
        binding.tvPromGastos.text = getString(R.string.consolidado_prom_gastos, fmt(pGastosM))
        binding.tvPromAhorros.text = getString(R.string.consolidado_prom_ahorros, fmt(pAhorrosM))
        binding.tvPromDeudas.text = getString(R.string.consolidado_prom_deudas, fmt(pDeudasM))

        // Card 4: Proyección (solo año actual)
        if (isCurrentYear) {
            binding.cardProyeccion.visibility = View.VISIBLE
            binding.tvProyIngresos.text = getString(R.string.consolidado_proy_ingresos, fmt(pIngresos * 12))
            binding.tvProyGastos.text = getString(R.string.consolidado_proy_gastos, fmt(pGastosM * 12))
            binding.tvProyAhorros.text = getString(R.string.consolidado_proy_ahorros, fmt(pAhorrosM * 12))
            binding.tvProyDeudas.text = getString(R.string.consolidado_proy_deudas, fmt(pDeudasM * 12))
        } else {
            binding.cardProyeccion.visibility = View.GONE
        }
    }

    private fun mostrarInsight(pGastos: Int, pAhorros: Int, pDeudas: Int) {
        binding.tvInsightConsolidado.text = when {
            !isCurrentYear -> getString(R.string.consolidado_insight_historico_neutro)
            pGastos >= 80 -> getString(R.string.consolidado_insight_gastos_altos)
            pAhorros >= 20 -> getString(R.string.consolidado_insight_ahorro_saludable)
            pDeudas >= 40 -> getString(R.string.consolidado_insight_deudas_altas)
            else -> getString(R.string.consolidado_insight_neutro)
        }
    }

    // ── View callbacks ──────────────────────────────────────────────────────────

    override fun statusIngresosAnual(month: Int, statusOk: Boolean, monto: Float, message: String) {
        if (_binding == null) return
        if (statusOk) ingresosAcumulados += monto
        else Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        if (month < monthCurrent) {
            presenter.getIngresosAnual(yearSelected, month + 1)
        } else {
            presenter.getGastosAnual(yearSelected, 0)
        }
    }

    override fun statusGastosAnual(month: Int, statusOk: Boolean, monto: Float, message: String) {
        if (_binding == null) return
        if (statusOk) gastosAcumulados += monto
        else Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        if (month < monthCurrent) {
            presenter.getGastosAnual(yearSelected, month + 1)
        } else {
            presenter.getAhorrosAnual(yearSelected, 0)
        }
    }

    override fun statusAhorrosAnual(month: Int, statusOk: Boolean, monto: Float, message: String) {
        if (_binding == null) return
        if (statusOk) ahorrosAcumulados += monto
        else Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        if (month < monthCurrent) {
            presenter.getAhorrosAnual(yearSelected, month + 1)
        } else {
            presenter.getDeudasAnual(yearSelected, 0)
        }
    }

    override fun statusDeudasAnual(month: Int, statusOk: Boolean, monto: Float, message: String) {
        if (_binding == null) return
        if (statusOk) deudasAcumuladas += monto
        else Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        if (month < monthCurrent) {
            presenter.getDeudasAnual(yearSelected, month + 1)
        } else {
            presenter.getPrestamosAnual(yearSelected, 0)
        }
    }

    override fun statusPrestamosAnual(month: Int, statusOk: Boolean, monto: Float, message: String) {
        if (_binding == null) return
        if (statusOk) prestamosAcumulados += monto
        else Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        if (month < monthCurrent) {
            presenter.getPrestamosAnual(yearSelected, month + 1)
        } else {
            calcularYMostrarResumen()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
