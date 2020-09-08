package com.skysam.hchirinos.myfinances.ui.general.listaGastos

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.Constraints
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.Utils.Constantes
import com.skysam.hchirinos.myfinances.adaptadores.ItemListPendienteAdapter
import com.skysam.hchirinos.myfinances.constructores.ItemGastosConstructor
import com.skysam.hchirinos.myfinances.databinding.DialogCrearEditarItemBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CrearEditarItemDialog(private val adapter: ItemListPendienteAdapter, private val idLista: String, private val guardar: Boolean, private val items: ArrayList<ItemGastosConstructor>):
        DialogFragment() {

    private var _binding : DialogCrearEditarItemBinding? = null
    private val binding get() = _binding!!
    private val user = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()
    private var dialog : AlertDialog? = null
    private var fechaSelec: Date? = null
    var calendar: Calendar = Calendar.getInstance()
    val fechaIngreso: Date = calendar.time

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogCrearEditarItemBinding.inflate(layoutInflater)

        binding.imageButton.setOnClickListener { selecFecha() }

        val builder = AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.btn_nuevo_item))
                .setView(binding.root)
                .setPositiveButton(getString(R.string.btn_guardar), null)
                .setNegativeButton(getString(R.string.btn_cancelar), null)

        dialog = builder.create()
        dialog?.show()
        dialog?.getButton(DialogInterface.BUTTON_POSITIVE)?.setOnClickListener {
            binding.outlinedConcepto.error = null
            binding.outlinedMonto.error = null
            validarLista()
        }
        return dialog as AlertDialog
    }

    private fun validarLista() {
        val concepto = binding.etConcepto.text.toString()
        val monto = binding.etMonto.text.toString()

        if (concepto.isEmpty()) {
            binding.outlinedConcepto.error = getString(R.string.error_campo_vacio)
            return
        }
        if (monto.isEmpty()) {
            binding.outlinedMonto.error = getString(R.string.error_campo_vacio)
            return
        }
        val montoDouble: Double = monto.toDouble()
        if (guardar) guardarItem(concepto, montoDouble)
    }

    private fun guardarItem(concepto: String, monto: Double) {
        Toast.makeText(context, "Guardando...", Toast.LENGTH_SHORT).show()
        val docData: MutableMap<String, Any?> = HashMap()
        docData[Constantes.BD_CONCEPTO] = concepto
        docData[Constantes.BD_MONTO] = monto
        docData[Constantes.BD_FECHA_APROXIMADA] = fechaSelec
        docData[Constantes.BD_FECHA_INGRESO] = fechaIngreso

        db.collection(Constantes.BD_LISTA_GASTOS).document(user!!.uid).collection(idLista)
                .add(docData)
                .addOnSuccessListener { document ->
                    Log.d(Constraints.TAG, "DocumentSnapshot written succesfully")
                    val item = ItemGastosConstructor()
                    item.idListItem = document.id
                    item.concepto = concepto
                    item.montoAproximado = monto
                    item.fechaAproximada = fechaSelec
                    item.fechaIngreso = fechaIngreso
                    items.add(item)
                    adapter.updateList(items)
                    actualizarCantidadItems()
                }
                .addOnFailureListener { e ->
                    Log.w(Constraints.TAG, "Error adding document", e)
                    Toast.makeText(context, getString(R.string.error_cargar_data), Toast.LENGTH_SHORT).show()
                    dialog?.dismiss()
                }
    }

    private fun actualizarCantidadItems() {
        db.collection(Constantes.BD_LISTA_GASTOS).document(user!!.uid).collection(Constantes.BD_TODAS_LISTAS).document(idLista)
                .update(Constantes.BD_CANTIDAD_ITEMS, items.size)
                .addOnSuccessListener {
                    Log.d(Constraints.TAG, "DocumentSnapshot successfully updated!")
                    Toast.makeText(context, getString(R.string.process_succes), Toast.LENGTH_SHORT).show()
                    dialog?.dismiss()
                }.addOnFailureListener { dialog?.dismiss() }
    }

    private fun selecFecha() {
        val calendarSelec = Calendar.getInstance()
        val calendar = Calendar.getInstance()
        val day = calendar[Calendar.DAY_OF_MONTH]
        val month = calendar[Calendar.MONTH]
        val year = calendar[Calendar.YEAR]

        val datePickerDialog = DatePickerDialog(requireContext(), OnDateSetListener { view, year, month, dayOfMonth ->
            calendarSelec.set(year, month, dayOfMonth)
            fechaSelec = calendarSelec.time
            binding.textViewFechaAproximada.text = SimpleDateFormat("EEE d MMM yyyy").format(fechaSelec!!)
        }, year, month, day)
        datePickerDialog.show()
    }
}