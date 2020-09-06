package com.skysam.hchirinos.myfinances.adaptadores

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.constructores.ListasConstructor
import com.skysam.hchirinos.myfinances.ui.general.ListaPendientesDetailActivity
import com.skysam.hchirinos.myfinances.ui.general.ListaPendientesDetailFragment
import com.skysam.hchirinos.myfinances.ui.general.ListaPendientesListActivity

class ListasPendientesAdapter(private var listas: ArrayList<ListasConstructor>, private val parentActivity: ListaPendientesListActivity, private val twoPane: Boolean) :
    RecyclerView.Adapter<ListasPendientesAdapter.ViewHolder>() {

    private val onClickListener: View.OnClickListener

    init {
        onClickListener = View.OnClickListener { v ->
            val itemLista = v.tag as ListasConstructor
            if (twoPane) {
                val fragment = ListaPendientesDetailFragment().apply {
                    arguments = Bundle().apply {
                        putString(ListaPendientesDetailFragment.ARG_ITEM_ID, itemLista.idLista)
                        putString(ListaPendientesDetailFragment.ARG_ITEM_NOMBRE, itemLista.nombreLista)
                    }
                }
                parentActivity.supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.listapendientes_detail_container, fragment)
                        .commit()
            } else {
                val intent = Intent(v.context, ListaPendientesDetailActivity::class.java).apply {
                    putExtra(ListaPendientesDetailFragment.ARG_ITEM_ID, itemLista.idLista)
                    putExtra(ListaPendientesDetailFragment.ARG_ITEM_NOMBRE, itemLista.nombreLista)
                }
                v.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListasPendientesAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.cardview_listas, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListasPendientesAdapter.ViewHolder, position: Int) {
        val item = listas[position]
        holder.nombre.text = item.nombreLista
        val itemsCantidad =  item.cantidadItems
        if (itemsCantidad == 0) {
            holder.cantidad.text = "Sin ítems"
        } else {
            holder.cantidad.text = "Items: ${item.cantidadItems}"
        }

        with(holder.itemView) {
            tag = item
            setOnClickListener(onClickListener)
        }
    }

    override fun getItemCount(): Int = listas.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.textView_nombre_lista)
        val cantidad: TextView = view.findViewById(R.id.textView_cantidad_items)
    }

    fun updateList(newList: ArrayList<ListasConstructor>) {
        listas = ArrayList()
        listas.addAll(newList)
        notifyDataSetChanged()
    }

}