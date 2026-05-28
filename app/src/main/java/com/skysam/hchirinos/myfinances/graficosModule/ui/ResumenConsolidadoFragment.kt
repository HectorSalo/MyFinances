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
    // Desglose de gastos por tipoPresupuesto
    private var gastosNormalesAcumulados = 0.0
    private var ahorroCapitalizableAcumulado = 0.0
    private var pagoDeudaAcumulado = 0.0
    // Saldos mensuales: no se acumulan, representan el estado en cada mes
    private val ahorrosMensuales = DoubleArray(12)
    private val deudasMensuales = DoubleArray(12)
    private val prestamosMensuales = DoubleArray(12)

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
        gastosNormalesAcumulados = 0.0
        ahorroCapitalizableAcumulado = 0.0
        pagoDeudaAcumulado = 0.0
        ahorrosMensuales.fill(0.0)
        deudasMensuales.fill(0.0)
        prestamosMensuales.fill(0.0)
        binding.progressBar.visibility = View.VISIBLE
        presenter.getIngresosAnual(yearSelected, 0)
    }

    private fun calcularYMostrarResumen() {
        if (_binding == null) return
        binding.progressBar.visibility = View.GONE

        val mesesConsiderados = monthCurrent + 1
        val fmt = { v: Double -> ClassesCommon.convertDoubleToString(v) }

        // Título
        binding.tvTituloConsolidado.text = getString(
            if (isCurrentYear) R.string.resumen_anual_titulo_actual
            else R.string.resumen_anual_titulo_historico
        )

        // ── Saldos (no flujos) ───────────────────────────────────────────────
        val ahorroActual  = ahorrosMensuales[mesesConsiderados - 1]
        val ahorroInicial = ahorrosMensuales[0]
        val promedioAhorros = ahorrosMensuales.slice(0 until mesesConsiderados).average()

        val deudaActual  = deudasMensuales[mesesConsiderados - 1]
        val deudaInicial = deudasMensuales[0]
        val promedioDeudas = deudasMensuales.slice(0 until mesesConsiderados).average()

        val prestamoActual = prestamosMensuales[mesesConsiderados - 1]

        // Tendencias saldos al cierre
        val mesesRestantes = 12 - mesesConsiderados
        val divisor = maxOf(1, mesesConsiderados - 1).toDouble()
        val tendAhorro = ahorroActual + ((ahorroActual - ahorroInicial) / divisor) * mesesRestantes
        val tendenciaMensualDeuda = (deudaActual - deudaInicial) / divisor
        val deudaProyectadaCierre = maxOf(0.0, deudaActual + tendenciaMensualDeuda * mesesRestantes)

        // Proyecciones de flujos por tipo (solo año actual)
        val promAhorroCap  = if (mesesConsiderados > 0) ahorroCapitalizableAcumulado / mesesConsiderados else 0.0
        val ahorroCapProyectado = promAhorroCap * 12
        val promPagoDeuda  = if (mesesConsiderados > 0) pagoDeudaAcumulado / mesesConsiderados else 0.0
        val pagoDeudaProyectado = promPagoDeuda * 12

        // Cobertura proyectada de deuda
        val coberturaDeuda = if (deudaProyectadaCierre > 0)
            pagoDeudaProyectado / deudaProyectadaCierre * 100 else 0.0
        val faltanteOSobrante = pagoDeudaProyectado - deudaProyectadaCierre

        // ── Card 1: Flujos del año ────────────────────────────────────────────
        binding.tvTotalIngresos.text = getString(R.string.consolidado_total_ingresos, fmt(ingresosAcumulados))
        binding.tvTotalGastos.text   = getString(R.string.consolidado_total_gastos,   fmt(gastosAcumulados))
        binding.tvGastosNormales.text       = getString(R.string.consolidado_gastos_normales,      fmt(gastosNormalesAcumulados))
        binding.tvAhorroCapitalizable.text  = getString(R.string.consolidado_ahorro_capitalizable, fmt(ahorroCapitalizableAcumulado))
        binding.tvPagoDeuda.text            = getString(R.string.consolidado_pago_deuda,           fmt(pagoDeudaAcumulado))

        binding.tvTotalAhorros.text = if (isCurrentYear)
            getString(R.string.consolidado_ahorro_actual, fmt(ahorroActual))
        else
            getString(R.string.consolidado_ahorro_cierre, fmt(ahorroActual))

        binding.tvTotalDeudas.text = if (isCurrentYear)
            getString(R.string.consolidado_deuda_actual,  fmt(deudaActual))
        else
            getString(R.string.consolidado_deuda_cierre,  fmt(deudaActual))

        binding.tvTotalPrestamos.text = if (isCurrentYear)
            getString(R.string.consolidado_prestamo_actual, fmt(prestamoActual))
        else
            getString(R.string.consolidado_prestamo_cierre, fmt(prestamoActual))

        val margenOperativo = ingresosAcumulados - gastosAcumulados
        binding.tvMargen.text = getString(R.string.consolidado_margen_operativo, fmt(margenOperativo))

        // ── Card 2: Indicadores porcentuales ────────────────────────────────
        if (ingresosAcumulados > 0.0) {
            val pGastos    = (gastosAcumulados  / ingresosAcumulados * 100).roundToInt()
            val pAhorros   = (ahorroActual       / ingresosAcumulados * 100).roundToInt()
            val pDeudas    = (deudaActual         / ingresosAcumulados * 100).roundToInt()
            val pPrestamos = (prestamoActual       / ingresosAcumulados * 100).roundToInt()
            binding.tvPorcGastos.text    = getString(R.string.consolidado_porc_gastos,         pGastos.toString())
            binding.tvPorcAhorros.text   = getString(R.string.consolidado_porc_ahorro_actual,   pAhorros.toString())
            binding.tvPorcDeudas.text    = getString(R.string.consolidado_porc_deuda_actual,    pDeudas.toString())
            binding.tvPorcPrestamos.text = getString(R.string.consolidado_porc_prestamo_actual, pPrestamos.toString())
            mostrarInsight(pGastos, deudaProyectadaCierre, coberturaDeuda, ahorroCapProyectado, margenOperativo)
        } else {
            binding.tvPorcGastos.text    = getString(R.string.consolidado_sin_ingresos)
            binding.tvPorcAhorros.text   = ""
            binding.tvPorcDeudas.text    = ""
            binding.tvPorcPrestamos.text = ""
            binding.tvInsightConsolidado.text = getString(R.string.consolidado_sin_ingresos)
        }

        // ── Card 3: Promedios ────────────────────────────────────────────────
        val promIngresos = if (mesesConsiderados > 0) ingresosAcumulados / mesesConsiderados else 0.0
        val promGastos   = if (mesesConsiderados > 0) gastosAcumulados   / mesesConsiderados else 0.0
        binding.tvPromIngresos.text = getString(R.string.consolidado_prom_ingresos,     fmt(promIngresos))
        binding.tvPromGastos.text   = getString(R.string.consolidado_prom_gastos,       fmt(promGastos))
        binding.tvPromAhorros.text  = getString(R.string.consolidado_prom_saldo_ahorros, fmt(promedioAhorros))
        binding.tvPromDeudas.text   = getString(R.string.consolidado_prom_saldo_deudas,  fmt(promedioDeudas))

        // ── Card 4: Proyección al cierre (solo año actual) ───────────────────
        if (isCurrentYear) {
            binding.cardProyeccion.visibility = View.VISIBLE
            binding.tvProyIngresos.text          = getString(R.string.consolidado_proy_ingresos,                fmt(promIngresos * 12))
            binding.tvProyGastos.text            = getString(R.string.consolidado_proy_gastos,                  fmt(promGastos * 12))
            binding.tvProyAhorroCapitalizable.text = getString(R.string.consolidado_ahorro_capitalizable_proyectado, fmt(ahorroCapProyectado))
            binding.tvProyPagoDeuda.text          = getString(R.string.consolidado_pago_deuda_proyectado,       fmt(pagoDeudaProyectado))
            binding.tvProyAhorros.text            = getString(R.string.consolidado_tendencia_ahorros,           fmt(tendAhorro))
            binding.tvProyDeudas.text             = getString(R.string.consolidado_tendencia_deudas,            fmt(deudaProyectadaCierre))
        } else {
            binding.cardProyeccion.visibility = View.GONE
        }

        // ── Card 5: Cobertura de deuda (solo año actual) ─────────────────────
        if (isCurrentYear) {
            binding.cardCobertura.visibility = View.VISIBLE
            if (deudaProyectadaCierre <= 0) {
                binding.tvCoberturaDeudaProyectada.text = getString(R.string.consolidado_sin_deuda_proyectada)
                binding.tvCoberturaPagoProyectado.text  = ""
                binding.tvCoberturaPorcentaje.text      = ""
                binding.tvCoberturaFaltante.text        = ""
            } else {
                binding.tvCoberturaDeudaProyectada.text = getString(R.string.consolidado_deuda_proyectada_cierre,    fmt(deudaProyectadaCierre))
                binding.tvCoberturaPagoProyectado.text  = getString(R.string.consolidado_pago_deuda_proyectado_cierre, fmt(pagoDeudaProyectado))
                binding.tvCoberturaPorcentaje.text      = getString(R.string.consolidado_cobertura_deuda, coberturaDeuda.roundToInt().toString())
                binding.tvCoberturaFaltante.text        = if (faltanteOSobrante < 0)
                    getString(R.string.consolidado_faltante_deuda,  fmt(-faltanteOSobrante))
                else
                    getString(R.string.consolidado_sobrante_deuda, fmt(faltanteOSobrante))
            }
        } else {
            binding.cardCobertura.visibility = View.GONE
        }
    }

    private fun mostrarInsight(
        pGastos: Int,
        deudaProyectadaCierre: Double,
        coberturaDeuda: Double,
        ahorroCapProyectado: Double,
        margenOperativo: Double
    ) {
        binding.tvInsightConsolidado.text = when {
            !isCurrentYear              -> getString(R.string.consolidado_insight_historico_neutro)
            ingresosAcumulados <= 0     -> getString(R.string.consolidado_sin_ingresos)
            deudaProyectadaCierre > 0 && coberturaDeuda < 100
                                        -> getString(R.string.consolidado_insight_deuda_sin_cubrir)
            deudaProyectadaCierre > 0 && coberturaDeuda >= 100
                                        -> getString(R.string.consolidado_insight_deuda_cubierta)
            ahorroCapProyectado > 0     -> getString(R.string.consolidado_insight_ahorro_cap)
            pGastos >= 80               -> getString(R.string.consolidado_insight_gastos_altos)
            margenOperativo > 0         -> getString(R.string.consolidado_insight_margen_positivo)
            else                        -> getString(R.string.consolidado_insight_neutro)
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

    override fun statusGastosAnual(month: Int, statusOk: Boolean, total: Float, normal: Float, ahorroCapitalizable: Float, pagoDeuda: Float, message: String) {
        if (_binding == null) return
        if (statusOk) {
            gastosAcumulados += total
            gastosNormalesAcumulados += normal
            ahorroCapitalizableAcumulado += ahorroCapitalizable
            pagoDeudaAcumulado += pagoDeuda
        } else {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
        if (month < monthCurrent) {
            presenter.getGastosAnual(yearSelected, month + 1)
        } else {
            presenter.getAhorrosAnual(yearSelected, 0)
        }
    }

    override fun statusAhorrosAnual(month: Int, statusOk: Boolean, monto: Float, message: String) {
        if (_binding == null) return
        if (statusOk) ahorrosMensuales[month] = monto.toDouble()
        else Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        if (month < monthCurrent) {
            presenter.getAhorrosAnual(yearSelected, month + 1)
        } else {
            presenter.getDeudasAnual(yearSelected, 0)
        }
    }

    override fun statusDeudasAnual(month: Int, statusOk: Boolean, monto: Float, message: String) {
        if (_binding == null) return
        if (statusOk) deudasMensuales[month] = monto.toDouble()
        else Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        if (month < monthCurrent) {
            presenter.getDeudasAnual(yearSelected, month + 1)
        } else {
            presenter.getPrestamosAnual(yearSelected, 0)
        }
    }

    override fun statusPrestamosAnual(month: Int, statusOk: Boolean, monto: Float, message: String) {
        if (_binding == null) return
        if (statusOk) prestamosMensuales[month] = monto.toDouble()
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
