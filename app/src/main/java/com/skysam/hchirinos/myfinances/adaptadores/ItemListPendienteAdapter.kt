package com.skysam.hchirinos.myfinances.adaptadores

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.Utils.Constantes
import com.skysam.hchirinos.myfinances.constructores.ItemGastosConstructor
import com.skysam.hchirinos.myfinances.ui.agregar.AgregarActivity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ItemListPendienteAdapter(private var items: ArrayList<ItemGastosConstructor>, private val context: Context) :
    RecyclerView.Adapter<ItemListPendienteAdapter.ViewHolder>() {

    private val onLongClickListener: View.OnLongClickListener

    init {
        onLongClickListener = View.OnLongClickListener { v ->
            val itemLista = v.tag as ItemGastosConstructor
            crearOpciones(itemLista)
            return@OnLongClickListener true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemListPendienteAdapter.ViewHolder {
        val view = LayoutInflater.from(context)
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
            setOnLongClickListener(onLongClickListener)
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
        items = newList
        notifyDataSetChanged()
    }

    private fun crearOpciones(item: ItemGastosConstructor) {
        val dialog = AlertDialog.Builder(context)
        dialog.setTitle("¿Qué desea hacer?")
                .setItems(R.array.opciones_item_list_gasto) { _, i ->
                    when (i) {
                        0 -> {
                            moverToGastos(item)
                        }
                        //1 -> editarItem()
                        //2 -> eliminarItem()
                    }
                }
                .setNegativeButton(context.getString(R.string.btn_cancelar), null).show()
    }

    private fun moverToGastos(item: ItemGastosConstructor) {
        val intent = Intent(context, AgregarActivity::class.java)
        intent.putExtra(Constantes.BD_CONCEPTO, item.concepto)
        intent.putExtra(Constantes.BD_MONTO, item.montoAproximado)
        intent.putExtra("idItem", item.idItem)
        intent.putExtra("idLista", item.idListItem)
        intent.putExtra("cantidadItems", items.size)
        intent.putExtra("agregar", 3)
        context.startActivity(intent)
    }
}