package com.skysam.hchirinos.myfinances.adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.constructores.ItemGastosConstructor
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ItemListPendienteAdapter(private var items: ArrayList<ItemGastosConstructor>, private val context: Context) :
    RecyclerView.Adapter<ItemListPendienteAdapter.ViewHolder>() {

    private val onClickListener: View.OnClickListener

    init {
        onClickListener = View.OnClickListener { v ->
            val itemLista = v.tag as ItemGastosConstructor
            Toast.makeText(context, itemLista.concepto, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemListPendienteAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.cardview_items_listas, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemListPendienteAdapter.ViewHolder, position: Int) {
        val df = SimpleDateFormat("EEE d MMM yyyy", Locale.getDefault())
        val item = items[position]

        holder.concepto.text = item.concepto
        holder.monto.text = "\$ ${item.montoAproximado}"
        if (item.fechaAproximada != null) {
            holder.fechaAproximada.text = "Realizar el gasto el: ${df.format(item.fechaAproximada)}"
        } else {
            holder.fechaAproximada.text = "Sin fecha aproximada"
        }

        holder.fechaIngreso.text = "Ítem agregado el: ${df.format(item.fechaIngreso)}"

        with(holder.itemView) {
            tag = item
            setOnClickListener(onClickListener)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val concepto: TextView = view.findViewById(R.id.textView_concepto)
        val monto: TextView = view.findViewById(R.id.textView_monto_aproximado)
        val fechaAproximada: TextView = view.findViewById(R.id.textView_fecha_aproximada)
        val fechaIngreso: TextView = view.findViewById(R.id.textView_fecha_ingreso)
    }

    fun updateList(newList: ArrayList<ItemGastosConstructor>) {
        items = ArrayList()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}