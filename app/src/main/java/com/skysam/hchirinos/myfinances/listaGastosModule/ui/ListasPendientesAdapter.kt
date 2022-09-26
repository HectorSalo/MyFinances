package com.skysam.hchirinos.myfinances.listaGastosModule.ui

import android.app.ActivityOptions
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.common.utils.Constants
import com.skysam.hchirinos.myfinances.common.model.constructores.ListasConstructor
import com.skysam.hchirinos.myfinances.common.model.firebase.Auth
import com.skysam.hchirinos.myfinances.common.model.firebase.FirebaseStorage

class ListasPendientesAdapter(private var listas: ArrayList<ListasConstructor>, private val parentActivity: ListaPendientesListActivity, private val twoPane: Boolean) :
    RecyclerView.Adapter<ListasPendientesAdapter.ViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val onClickListener: View.OnClickListener
    private val onLongClickListener: View.OnLongClickListener
    private var itemLista: ListasConstructor? = null
    private lateinit var imgPhoto: View


    init {
        onClickListener = View.OnClickListener { v ->
            itemLista = v.tag as ListasConstructor
            if (twoPane) {
                val fragment = ListaPendientesDetailFragment().apply {
                    arguments = Bundle().apply {
                        putString(ListaPendientesDetailFragment.ARG_ITEM_ID, itemLista!!.idLista)
                        putString(ListaPendientesDetailFragment.ARG_ITEM_NOMBRE, itemLista!!.nombreLista)
                        putString(ListaPendientesDetailFragment.ARG_ITEM_IMAGEN, itemLista!!.imagen)
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
                    putExtra(ListaPendientesDetailFragment.ARG_ITEM_IMAGEN, itemLista!!.imagen)
                }
                v.context.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(parentActivity,
                        imgPhoto, v.context.getString(R.string.transition_name_image)).toBundle())
            }
        }

        onLongClickListener = View.OnLongClickListener { v ->
            val itemLista = v.tag as ListasConstructor
            crearOpciones(itemLista.nombreLista, listas.indexOf(itemLista))
            return@OnLongClickListener true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.cardview_listas, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listas[position]
        holder.nombre.text = item.nombreLista

        val itemsCantidad =  item.cantidadItems
        if (itemsCantidad == 0) {
            holder.cantidad.text = "Sin ítems"
        } else {
            holder.cantidad.text = "Items: ${item.cantidadItems}"
        }

        val options = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .error(android.R.drawable.ic_menu_gallery)
                .placeholder(android.R.drawable.ic_menu_gallery)

        Glide.with(parentActivity.applicationContext).load(item.imagen)
                .apply(options).into(holder.imagen)

        with(holder.itemView) {
            tag = item
            imgPhoto = holder.imagen
            setOnClickListener(onClickListener)
            setOnLongClickListener(onLongClickListener)
        }
    }

    override fun getItemCount(): Int = listas.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.textView_nombre_lista)
        val cantidad: TextView = view.findViewById(R.id.textView_cantidad_items)
        val imagen: ImageView = view.findViewById(R.id.iv_listas)
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

        Handler(Looper.getMainLooper())
                .postDelayed({
            if (!listas.contains(lista)) {
                deleteLista(lista.idLista, lista.imagen)
            }
        }, 3000)
    }

    private fun deleteLista(id: String, url: String?) {
        db.collection(Constants.BD_LISTA_GASTOS).document(Auth.uidCurrentUser())
            .collection(Constants.BD_TODAS_LISTAS).document(id)
                .delete()
                .addOnSuccessListener(OnSuccessListener<Void?> {
                    Log.d("Delete", "DocumentSnapshot successfully deleted!")
                    deleteCollection(id)
                    if (url != null) FirebaseStorage.getPhotosReferenceByUrl(url).delete()
                })
                .addOnFailureListener(OnFailureListener { e ->
                    Log.w("Delete", "Error deleting document", e)
                    Toast.makeText(parentActivity.applicationContext, "Error al borrar la lista. Intente nuevamente.", Toast.LENGTH_SHORT).show()
                })
    }

    private fun deleteCollection(id: String) {
        db.collection(Constants.BD_LISTA_GASTOS).document(Auth.uidCurrentUser()).collection(id)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            Log.d(ContentValues.TAG, document.id + " => " + document.data)
                            db.collection(Constants.BD_LISTA_GASTOS).document(Auth.uidCurrentUser())
                                .collection(id).document(document.id)
                                    .delete()
                        }
                    } else {
                        Log.d(ContentValues.TAG, "Error getting documents: ", task.exception)
                    }
                }
    }

}