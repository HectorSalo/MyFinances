package com.skysam.hchirinos.myfinances.homeModule.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skysam.hchirinos.myfinances.common.model.constructores.RateHistoryUi
import com.skysam.hchirinos.myfinances.databinding.ItemRateHistoryBinding

/**
 * Created by Hector Chirinos in the home office on 8 ene. 2026
 */
class RatesHistoryAdapter: RecyclerView.Adapter<RatesHistoryAdapter.VH>() {
    private var items: List<RateHistoryUi> = emptyList()

    fun submit(list: List<RateHistoryUi>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemRateHistoryBinding.inflate(inflater, parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    class VH(private val binding: ItemRateHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RateHistoryUi) = with(binding) {
            tvDate.text = item.dateLabel
            tvUsd.text = item.usdLabel

            val eur = item.eurLabel
            if (eur.isNullOrBlank()) {
                tvEur.visibility = View.GONE
            } else {
                tvEur.visibility = View.VISIBLE
                tvEur.text = eur
            }
        }
    }
}