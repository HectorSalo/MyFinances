package com.skysam.hchirinos.myfinances.homeModule.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.common.model.constructores.RateHistoryUi
import com.skysam.hchirinos.myfinances.databinding.BottomsheetRatesHistoryBinding
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Created by Hector Chirinos in the home office on 8 ene. 2026
 */
class RatesHistoryBottomSheet: BottomSheetDialogFragment() {
    interface Listener {
        fun onRangeSelected(from: String, to: String) // "YYYY-MM-DD"
    }

    private var listener: Listener? = null
    fun setListener(l: Listener) { listener = l }

    private var _binding: BottomsheetRatesHistoryBinding? = null
    private val binding get() = _binding!!

    private val adapter = RatesHistoryAdapter()

    private var fromIso: String? = null
    private var toIso: String? = null

    private var initialCache: ArrayList<RateHistoryUi> = arrayListOf()

    companion object {
        private const val ARG_CACHE = "arg_cache"

        fun newInstance(cache: ArrayList<RateHistoryUi> = arrayListOf()): RatesHistoryBottomSheet {
            return RatesHistoryBottomSheet().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_CACHE, cache.toParcelableList())
                }
            }
        }

        // RateHistoryUi no es Parcelable: lo empaquetamos en Bundles
        private fun ArrayList<RateHistoryUi>.toParcelableList(): ArrayList<Bundle> {
            val out = arrayListOf<Bundle>()
            for (it in this) {
                out.add(Bundle().apply {
                    putString("d", it.dateLabel)
                    putString("u", it.usdLabel)
                    putString("e", it.eurLabel)
                })
            }
            return out
        }

        private fun ArrayList<Bundle>.fromParcelableList(): ArrayList<RateHistoryUi> {
            val out = arrayListOf<RateHistoryUi>()
            for (b in this) {
                out.add(
                    RateHistoryUi(
                        dateLabel = b.getString("d").orEmpty(),
                        usdLabel = b.getString("u").orEmpty(),
                        eurLabel = b.getString("e")
                    )
                )
            }
            return out
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundles = arguments?.getParcelableArrayList<Bundle>(ARG_CACHE) ?: arrayListOf()
        initialCache = bundles.fromParcelableList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetRatesHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter

        binding.ibClose.setOnClickListener { dismissAllowingStateLoss() }

        // Render instantáneo con cache
        if (initialCache.isNotEmpty()) {
            hideStatus()
            adapter.submit(initialCache)
        } else {
            showStatus("Sin datos. Selecciona un rango para consultar.")
        }

        binding.btnFrom.setOnClickListener { pickDate(isFrom = true) }
        binding.btnTo.setOnClickListener { pickDate(isFrom = false) }

        binding.btnConsult.setOnClickListener {
            val f = fromIso
            val t = toIso
            if (f.isNullOrBlank() || t.isNullOrBlank()) {
                showStatus("Selecciona Desde y Hasta.")
                return@setOnClickListener
            }
            hideStatus()
            listener?.onRangeSelected(f, t)
        }
    }

    override fun getTheme(): Int = R.style.ThemeOverlay_MyApp_BottomSheetDialog

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Llama esto desde tu HomeFragment cuando llegue el resultado real del rango.
     */
    fun renderHistory(list: List<RateHistoryUi>) {
        if (!isAdded || _binding == null) return

        if (list.isEmpty()) {
            showStatus("Sin registro en las fechas seleccionadas.")
            adapter.submit(emptyList())
        } else {
            hideStatus()
            adapter.submit(list)
        }
    }

    fun renderError(message: String) {
        if (!isAdded || _binding == null) return
        showStatus(message)
    }

    private fun showStatus(text: String) {
        binding.tvStatus.text = text
        binding.tvStatus.visibility = View.VISIBLE
    }

    private fun hideStatus() {
        binding.tvStatus.visibility = View.GONE
    }

    private fun pickDate(isFrom: Boolean) {
        val picker = com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
            .setTitleText(if (isFrom) "Selecciona Desde" else "Selecciona Hasta")
            .build()

        picker.addOnPositiveButtonClickListener { selectionUtcMillis ->
            val (iso, label) = formatDate(selectionUtcMillis)
            if (isFrom) {
                fromIso = iso
                binding.btnFrom.text = label
            } else {
                toIso = iso
                binding.btnTo.text = label
            }
        }

        picker.show(parentFragmentManager, if (isFrom) "date_from" else "date_to")
    }

    /**
     * ISO para backend (YYYY-MM-DD), label con formato del dispositivo.
     */
    private fun formatDate(selectionUtcMillis: Long): Pair<String, String> {
        val utc = TimeZone.getTimeZone("UTC")
        val calUtc = Calendar.getInstance(utc).apply { timeInMillis = selectionUtcMillis }

        val y = calUtc.get(Calendar.YEAR)
        val m = calUtc.get(Calendar.MONTH) + 1
        val d = calUtc.get(Calendar.DAY_OF_MONTH)

        // ISO para backend (estable, correcto)
        val iso = String.format("%04d-%02d-%02d", y, m, d)

        // Label en formato del dispositivo, PERO usando el día seleccionado (Y/M/D),
        // no el millis convertido a zona local.
        val label = formatLabelWithDeviceLocale(y, m, d)

        return iso to label
    }

    private fun formatLabelWithDeviceLocale(year: Int, month1to12: Int, day: Int): String {
        // Construimos una fecha “segura” al mediodía LOCAL para evitar cruces por DST/offset.
        val calLocal = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault()).apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month1to12 - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 12) // mediodía evita cambios de día por DST
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Aquí puedes usar el formato que quieras (respetando Locale del dispositivo)
        val df = java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM, Locale.getDefault())
        return df.format(calLocal.time)
    }
}