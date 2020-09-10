package com.skysam.hchirinos.myfinances.adaptadores

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.Constraints
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.Utils.Constantes
import com.skysam.hchirinos.myfinances.constructores.ItemGastosConstructor
import com.skysam.hchirinos.myfinances.ui.agregar.AgregarActivity
import com.skysam.hchirinos.myfinances.ui.general.listaGastos.CrearEditarItemDialog
import com.skysam.hchirinos.myfinances.ui.general.listaGastos.ListaPendientesListActivity
import java.text.SimpleDateFormat
import java.util.*

class ItemListPendienteAdapter(private var items: ArrayList<ItemGastosConstructor>, private val activity: Activity, private val supportFragmentManager: FragmentManager, private val twoPane: Boolean) :
    RecyclerView.Adapter<ItemListPendienteAdapter.ViewHolder>() {

    private val user = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()
    private val onLongClickListener: View.OnLongClickListener

    init {
        onLongClickListener = View.OnLongClickListener { v ->
            val itemLista = v.tag as ItemGastosConstructor
            crearOpciones(itemLista)
            return@OnLongClickListener true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemListPendienteAdapter.ViewHolder {
        val view = LayoutInflater.from(activity)
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
        val dialog = AlertDialog.Builder(activity)
        dialog.setTitle("¿Qué desea hacer?")
                .setItems(R.array.opciones_item_list_gasto) { _, i ->
                    when (i) {
                        0 -> moverToGastos(item)
                        1 -> editarItem(item)
                        2 -> eliminarItem(item)
                    }
                }
                .setNegativeButton(activity.getString(R.string.btn_cancelar), null).show()
    }

    private fun moverToGastos(item: ItemGastosConstructor) {
        val intent = Intent(activity, AgregarActivity::class.java)
        intent.putExtra(Constantes.BD_CONCEPTO, item.concepto)
        intent.putExtra(Constantes.BD_MONTO, item.montoAproximado)
        intent.putExtra("idItem", item.idItem)
        intent.putExtra("idLista", item.idListItem)
        intent.putExtra("cantidadItems", items.size)
        intent.putExtra("agregar", 3)
        activity.startActivity(intent)
    }

    private fun editarItem(item: ItemGastosConstructor) {
        val editarItemDialog = CrearEditarItemDialog(this, items[0].idListItem, false, items, items.indexOf(item), twoPane)
        editarItemDialog.show(supportFragmentManager, items[0].idListItem)
    }

    private fun eliminarItem(item: ItemGastosConstructor) {
        val position = items.indexOf(item)
        items.removeAt(position)
        updateList(items)

        val snackbar = Snackbar.make(activity.findViewById(R.id.listapendientes_detail), "Eliminando ${item.concepto}", Snackbar.LENGTH_LONG).setAction("Deshacer") {
            items.add(position, item)
            updateList(items)
        }
        snackbar.show()

        val handler = Handler()
        handler.postDelayed({
            if (!items.contains(item)) {
                deleteItem(item)
            }
        }, 3000)
    }

    private fun deleteItem(item: ItemGastosConstructor) {
        db.collection(Constantes.BD_LISTA_GASTOS).document(user!!.uid).collection(item.idListItem).document(item.idItem)
                .delete()
                .addOnSuccessListener(OnSuccessListener<Void?> {
                    Log.d("Delete", "DocumentSnapshot successfully deleted!")
                    actualizarCantidadItems(item)
                })
                .addOnFailureListener(OnFailureListener { e ->
                    Log.w("Delete", "Error deleting document", e)
                    Toast.makeText(activity.applicationContext, activity.getString(R.string.error_eliminar_data), Toast.LENGTH_SHORT).show()
                })
    }

    private fun actualizarCantidadItems(item: ItemGastosConstructor) {
        db.collection(Constantes.BD_LISTA_GASTOS).document(user!!.uid).collection(Constantes.BD_TODAS_LISTAS).document(item.idListItem)
                .update(Constantes.BD_CANTIDAD_ITEMS, items.size)
                .addOnSuccessListener {
                    Log.d(Constraints.TAG, "DocumentSnapshot successfully updated!")
                    if (twoPane) {
                        activity.finish()
                        activity.startActivity(Intent(activity, ListaPendientesListActivity::class.java))
                        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                }
    }
}
