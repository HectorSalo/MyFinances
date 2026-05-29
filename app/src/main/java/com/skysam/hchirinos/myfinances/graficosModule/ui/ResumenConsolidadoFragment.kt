package com.skysam.hchirinos.myfinances.graficosModule.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    // Siempre se usa el año actual; no hay selector de año
    private var yearSelected = 0
    private var monthCurrent = 0

    private var ingresosAcumulados = 0.0
    private var gastosAcumulados = 0.0
    // Desglose de gastos por tipoPresupuesto
    private var gastosNormalesAcumulados = 0.0
    private var ahorroCapitalizableAcumulado = 0.0
    private var pagoDeudaAcumulado = 0.0
    // Saldos mensuales: representan el estado en cada mes (no se acumulan)
    private val ahorrosMensuales  = DoubleArray(12)
    private val capitalMensual    = DoubleArray(12)
    private val deudasMensuales   = DoubleArray(12)
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
        yearSelected  = calendar[Calendar.YEAR]
        monthCurrent  = calendar[Calendar.MONTH]

        iniciarCarga()
    }

    private fun iniciarCarga() {
        ingresosAcumulados = 0.0
        gastosAcumulados = 0.0
        gastosNormalesAcumulados = 0.0
        ahorroCapitalizableAcumulado = 0.0
        pagoDeudaAcumulado = 0.0
        ahorrosMensuales.fill(0.0)
        capitalMensual.fill(0.0)
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
        // Helper para valores con signo (variaciones)
        fun fmtSigned(v: Double): String =
            if (v >= 0) "+ \$ ${fmt(v)}" else "- \$ ${fmt(-v)}"

        // Título fijo: siempre año actual
        binding.tvTituloConsolidado.text = getString(R.string.resumen_anual_titulo_actual)

        // ── Saldos (no flujos) ───────────────────────────────────────────────
        val capitalActual        = capitalMensual[mesesConsiderados - 1]
        val capitalInicial       = capitalMensual[0]
        val ahorroActual         = ahorrosMensuales[mesesConsiderados - 1]
        val ahorroGeneralActual  = ahorroActual - capitalActual

        val deudaActual  = deudasMensuales[mesesConsiderados - 1]
        val deudaInicial = deudasMensuales[0]
        val prestamoActual = prestamosMensuales[mesesConsiderados - 1]

        // Variación promedio mensual de saldos
        val divisorVar = maxOf(1, mesesConsiderados - 1).toDouble()
        val variacionPromedioCapital = (capitalActual - capitalInicial) / divisorVar
        val variacionPromedioDeuda   = (deudaActual   - deudaInicial)  / divisorVar

        // Estimaciones al cierre del año
        val mesesRestantes        = 12 - mesesConsiderados
        val capitalEstimadoCierre = capitalActual + variacionPromedioCapital * mesesRestantes
        val deudaEstimadaCierre   = maxOf(0.0, deudaActual + variacionPromedioDeuda * mesesRestantes)

        // Proyecciones de flujos
        val promIngresos       = if (mesesConsiderados > 0) ingresosAcumulados           / mesesConsiderados else 0.0
        val promGastosNormales = if (mesesConsiderados > 0) gastosNormalesAcumulados     / mesesConsiderados else 0.0
        val promAhorroCap      = if (mesesConsiderados > 0) ahorroCapitalizableAcumulado / mesesConsiderados else 0.0
        val promPagoDeuda      = if (mesesConsiderados > 0) pagoDeudaAcumulado           / mesesConsiderados else 0.0

        val ingresosProyectados          = promIngresos       * 12
        val gastosCotidianosProyectados  = promGastosNormales * 12
        val ahorroCapProyectado          = promAhorroCap      * 12
        val pagoDeudaProyectado          = promPagoDeuda      * 12

        // Cobertura proyectada de deuda
        val coberturaDeuda    = if (deudaEstimadaCierre > 0) pagoDeudaProyectado / deudaEstimadaCierre * 100 else 0.0
        val faltanteOSobrante = pagoDeudaProyectado - deudaEstimadaCierre

        // ── Card 1: Resumen hasta ahora ──────────────────────────────────────
        binding.tvTotalIngresos.text        = getString(R.string.consolidado_total_ingresos,        fmt(ingresosAcumulados))
        binding.tvTotalGastos.text          = getString(R.string.consolidado_total_gastos,          fmt(gastosAcumulados))
        binding.tvGastosNormales.text       = getString(R.string.consolidado_gastos_normales,       fmt(gastosNormalesAcumulados))
        binding.tvAhorroCapitalizable.text  = getString(R.string.consolidado_ahorro_capitalizable,  fmt(ahorroCapitalizableAcumulado))
        binding.tvPagoDeuda.text            = getString(R.string.consolidado_pago_deuda,            fmt(pagoDeudaAcumulado))
        binding.tvAhorroGeneral.text        = getString(R.string.consolidado_ahorro_general,        fmt(ahorroGeneralActual))
        binding.tvCapital.text              = getString(R.string.consolidado_capital,               fmt(capitalActual))
        binding.tvTotalDeudas.text          = getString(R.string.consolidado_deuda_actual,          fmt(deudaActual))
        binding.tvTotalPrestamos.text       = getString(R.string.consolidado_prestamo_actual,       fmt(prestamoActual))

        val margenCotidiano = ingresosAcumulados - gastosNormalesAcumulados
        binding.tvMargen.text = getString(R.string.consolidado_margen_operativo, fmt(margenCotidiano))

        // ── Card 2: Indicadores porcentuales ────────────────────────────────
        if (ingresosAcumulados > 0.0) {
            val pGastosCotidianos = (gastosNormalesAcumulados / ingresosAcumulados * 100).roundToInt()
            val pCapital          = (capitalActual             / ingresosAcumulados * 100).roundToInt()
            val pDeudas           = (deudaActual               / ingresosAcumulados * 100).roundToInt()
            val pPrestamos        = (prestamoActual             / ingresosAcumulados * 100).roundToInt()
            binding.tvPorcGastos.text    = getString(R.string.consolidado_porc_gastos,         pGastosCotidianos.toString())
            binding.tvPorcAhorros.text   = getString(R.string.consolidado_porc_ahorro_actual,  pCapital.toString())
            binding.tvPorcDeudas.text    = getString(R.string.consolidado_porc_deuda_actual,   pDeudas.toString())
            binding.tvPorcPrestamos.text = getString(R.string.consolidado_porc_prestamo_actual, pPrestamos.toString())
        } else {
            binding.tvPorcGastos.text    = getString(R.string.consolidado_sin_ingresos)
            binding.tvPorcAhorros.text   = ""
            binding.tvPorcDeudas.text    = ""
            binding.tvPorcPrestamos.text = ""
        }

        // ── Card 3: Promedios y variaciones mensuales ───────────────────────
        binding.tvPromIngresos.text = getString(R.string.consolidado_prom_ingresos,     fmt(promIngresos))
        binding.tvPromGastos.text   = getString(R.string.consolidado_prom_gastos,       fmt(promGastosNormales))
        binding.tvPromAhorros.text  = getString(R.string.consolidado_prom_saldo_ahorros, fmtSigned(variacionPromedioCapital))
        binding.tvPromDeudas.text   = getString(R.string.consolidado_prom_saldo_deudas,  fmtSigned(variacionPromedioDeuda))

        // ── Card 4: Proyección al cierre del año ─────────────────────────────
        binding.cardProyeccion.visibility = View.VISIBLE
        val diferenciaProyectada = ingresosProyectados - gastosCotidianosProyectados
        binding.tvProyIngresos.text   = getString(R.string.consolidado_proy_ingresos,                     fmt(ingresosProyectados))
        binding.tvProyGastos.text     = getString(R.string.consolidado_proy_gastos,                       fmt(gastosCotidianosProyectados))
        binding.tvProyDiferencia.text = getString(R.string.consolidado_proy_diferencia_ingresos_gastos, fmt(diferenciaProyectada))
        binding.tvProyAhorros.text    = getString(R.string.consolidado_tendencia_ahorros,                 fmt(capitalEstimadoCierre))

        // ── Card 5: Cobertura de deuda ────────────────────────────────────────
        binding.cardCobertura.visibility = View.VISIBLE
        if (deudaEstimadaCierre <= 0) {
            binding.tvCoberturaDeudaProyectada.text = getString(R.string.consolidado_sin_deuda_proyectada)
            binding.tvCoberturaPagoProyectado.text  = ""
            binding.tvCoberturaPorcentaje.text      = ""
            binding.tvCoberturaFaltante.text        = ""
        } else {
            binding.tvCoberturaDeudaProyectada.text = getString(R.string.consolidado_deuda_proyectada_cierre,     fmt(deudaEstimadaCierre))
            binding.tvCoberturaPagoProyectado.text  = getString(R.string.consolidado_pago_deuda_proyectado_cierre, fmt(pagoDeudaProyectado))
            binding.tvCoberturaPorcentaje.text      = getString(R.string.consolidado_cobertura_deuda, coberturaDeuda.roundToInt().toString())
            binding.tvCoberturaFaltante.text        = if (faltanteOSobrante < 0)
                getString(R.string.consolidado_faltante_deuda,  fmt(-faltanteOSobrante))
            else
                getString(R.string.consolidado_sobrante_deuda, fmt(faltanteOSobrante))
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

    override fun statusAhorrosAnual(month: Int, statusOk: Boolean, total: Float, capital: Float, message: String) {
        if (_binding == null) return
        if (statusOk) {
            ahorrosMensuales[month] = total.toDouble()
            capitalMensual[month]   = capital.toDouble()
        } else {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
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
