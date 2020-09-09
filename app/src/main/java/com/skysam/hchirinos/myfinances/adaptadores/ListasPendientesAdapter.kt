package com.skysam.hchirinos.myfinances.adaptadores

import android.content.ContentValues
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
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.Utils.Constantes
import com.skysam.hchirinos.myfinances.constructores.ListasConstructor
import com.skysam.hchirinos.myfinances.ui.general.listaGastos.CrearEditarListaDialog
import com.skysam.hchirinos.myfinances.ui.general.listaGastos.ListaPendientesDetailActivity
import com.skysam.hchirinos.myfinances.ui.general.listaGastos.ListaPendientesDetailFragment
import com.skysam.hchirinos.myfinances.ui.general.listaGastos.ListaPendientesListActivity

class ListasPendientesAdapter(private var listas: ArrayList<ListasConstructor>, private val parentActivity: ListaPendientesListActivity, private val twoPane: Boolean) :
    RecyclerView.Adapter<ListasPendientesAdapter.ViewHolder>() {

    private val user = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()
    private val onClickListener: View.OnClickListener
    private val onLongClickListener: View.OnLongClickListener
    private var itemLista: ListasConstructor? = null


    init {
        onClickListener = View.OnClickListener { v ->
            itemLista = v.tag as ListasConstructor
            if (twoPane) {
                val fragment = ListaPendientesDetailFragment().apply {
                    arguments = Bundle().apply {
                        putString(ListaPendientesDetailFragment.ARG_ITEM_ID, itemLista!!.idLista)
                        putString(ListaPendientesDetailFragment.ARG_ITEM_NOMBRE, itemLista!!.nombreLista)
                        putBoolean(ListaPendientesDetailFragment.ARG_TWO_PANE, twoPane)
                    }
                }
                parentActivity.supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.listapendientes_detail_container, fragment)
                        .commit()
            } else {
                val intent = Intent(v.context, ListaPendientesDetailActivity::class.java).apply {
                    putExtra(ListaPendientesDetailFragment.ARG_ITEM_ID, itemLista!!.idLista)
                    putExtra(ListaPendientesDetailFragment.ARG_ITEM_NOMBRE, itemLista!!.nombreLista)
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
                .setItems(R.array.opciones_list_gasto) { _, i ->
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
        val lista = listas[position]
        listas.removeAt(position)
        updateList(listas)

        if (twoPane) {
            if (itemLista != null) {
                if (itemLista!!.idLista == lista.idLista) {
                    val fragment = ListaPendientesDetailFragment()
                    parentActivity.supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.listapendientes_detail_container, fragment)
                            .commit()
                }
            }
        }

        val snackbar = Snackbar.make(parentActivity.findViewById(R.id.frameLayout), "Eliminando ${lista.nombreLista}", Snackbar.LENGTH_LONG).setAction("Deshacer") {
            listas.add(position, lista)
            updateList(listas)
        }
        snackbar.show()

        val handler = Handler()
        handler.postDelayed({
            if (!listas.contains(lista)) {
                deleteLista(lista.idLista)
            }
        }, 3000)
    }

    private fun deleteLista(id: String) {
        db.collection(Constantes.BD_LISTA_GASTOS).document(user!!.uid).collection(Constantes.BD_TODAS_LISTAS).document(id)
                .delete()
                .addOnSuccessListener(OnSuccessListener<Void?> {
                    Log.d("Delete", "DocumentSnapshot successfully deleted!")
                    deleteCollection(id)
                })
                .addOnFailureListener(OnFailureListener { e ->
                    Log.w("Delete", "Error deleting document", e)
                    Toast.makeText(parentActivity.applicationContext, "Error al borrar la lista. Intente nuevamente.", Toast.LENGTH_SHORT).show()
                })
    }

    private fun deleteCollection(id: String) {
        db.collection(Constantes.BD_LISTA_GASTOS).document(user!!.uid).collection(id)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            Log.d(ContentValues.TAG, document.id + " => " + document.data)
                            db.collection(Constantes.BD_LISTA_GASTOS).document(user.uid).collection(id).document(document.id)
                                    .delete()
                        }
                    } else {
                        Log.d(ContentValues.TAG, "Error getting documents: ", task.exception)
                    }
                }
    }

}