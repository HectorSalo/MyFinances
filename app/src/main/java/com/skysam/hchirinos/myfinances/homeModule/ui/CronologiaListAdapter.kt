package com.skysam.hchirinos.myfinances.homeModule.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.common.model.constructores.ItemCronologiaConstructor
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CronologiaListAdapter(private var list: ArrayList<ItemCronologiaConstructor>): RecyclerView.Adapter<CronologiaListAdapter.ViewHolder> () {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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
        val tf = SimpleDateFormat("EEE d MMM yyyy", Locale.getDefault())
        val item = list[position]
        holder.concepto.text = item.concepto

        if (item.isDolar!!) {
            holder.monto.text = "$ ${item.monto.toString()}"
        } else {
            holder.monto.text = "Bs. ${item.monto.toString()}"
        }

        holder.fecha.text = tf.format(item.fecha!!)

        if (item.pasivo!!) {
            holder.imagen.setImageResource(R.drawable.ic_trending_down_24)
        } else {
            holder.imagen.setImageResource(R.drawable.ic_trending_up_24)
        }

    }

    override fun getItemCount(): Int = list.size
}