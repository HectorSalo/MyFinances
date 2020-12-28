package com.skysam.hchirinos.myfinances.homeModule.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.common.model.constructores.ItemCronologiaConstructor
import com.skysam.hchirinos.myfinances.listaGastosModule.ui.ImagenesListasAdapter
import org.w3c.dom.Text

class CronologiaListAdapter (private var list: ArrayList<ItemCronologiaConstructor>): RecyclerView.Adapter<CronologiaListAdapter.ViewHolder> () {
    class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        val concepto: TextView = view.findViewById(R.id.tv_concepto)
        val monto : TextView = view.findViewById(R.id.tv_monto)
        val fecha: TextView = view.findViewById(R.id.tv_fecha)
        val imagen: ImageView = view.findViewById(R.id.iv_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.cardview_cronologia, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int = list.size
}