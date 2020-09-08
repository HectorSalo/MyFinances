package com.skysam.hchirinos.myfinances.adaptadores

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.constructores.ListasConstructor
import com.skysam.hchirinos.myfinances.ui.general.listaGastos.CrearEditarListaDialog
import com.skysam.hchirinos.myfinances.ui.general.listaGastos.ListaPendientesDetailActivity
import com.skysam.hchirinos.myfinances.ui.general.listaGastos.ListaPendientesDetailFragment
import com.skysam.hchirinos.myfinances.ui.general.listaGastos.ListaPendientesListActivity

class ListasPendientesAdapter(private var listas: ArrayList<ListasConstructor>, private val parentActivity: ListaPendientesListActivity, private val twoPane: Boolean) :
    RecyclerView.Adapter<ListasPendientesAdapter.ViewHolder>() {


    private val onClickListener: View.OnClickListener
    private val onLongClickListener: View.OnLongClickListener


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

        onLongClickListener = View.OnLongClickListener { v ->
            val itemLista = v.tag as ListasConstructor
            crearOpciones(itemLista.nombreLista, listas.indexOf(itemLista))
            return@OnLongClickListener true
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
            setOnLongClickListener(onLongClickListener)
        }
    }

    override fun getItemCount(): Int = listas.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.textView_nombre_lista)
        val cantidad: TextView = view.findViewById(R.id.textView_cantidad_items)
    }

    fun updateList(newList: ArrayList<ListasConstructor>) {
        listas = newList
        notifyDataSetChanged()
    }

    private fun crearOpciones(nombre: String, position: Int) {
        val dialog = AlertDialog.Builder(parentActivity)
        dialog.setTitle("¿Qué desea hacer?")
                .setItems(R.array.opciones_list_gasto) { dialogInterface, i ->
                    when (i) {
                        0 -> {
                            val editarListaDialog = CrearEditarListaDialog(twoPane, false, listas, position, this)
                            editarListaDialog.show(parentActivity.supportFragmentManager, nombre)
                        }
                        1 -> eliminarItem(position)
                    }
                }
                .setNegativeButton(parentActivity.getString(R.string.btn_cancelar), null).show()
    }

    private fun eliminarItem(position: Int) {
        listas.removeAt(position)
        updateList(listas)

        /*val snackbar = Snackbar.make(view, lista.nombreLista + " borrado", Snackbar.LENGTH_LONG).setAction("Deshacer") {
            listListas.add(i, lista)
            listasAdapter.updateList(listListas)
        }
        snackbar.show()

        val handler = Handler()
        handler.postDelayed({
            if (!listListas.contains(lista)) {
                Toast.makeText(getApplicationContext(), "Eliminando lista", Toast.LENGTH_SHORT).show()
                deleteLista(lista.idLista)
            }
        }, 3000)*/
    }

}