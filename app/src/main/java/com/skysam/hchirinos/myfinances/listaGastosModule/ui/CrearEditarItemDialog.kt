package com.skysam.hchirinos.myfinances.listaGastosModule.ui

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.Constraints
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.common.utils.Constants
import com.skysam.hchirinos.myfinances.common.model.constructores.ItemGastosConstructor
import com.skysam.hchirinos.myfinances.databinding.DialogCrearEditarItemBinding
import java.text.SimpleDateFormat
import java.util.*

class CrearEditarItemDialog(private val adapter: ItemListPendienteAdapter, private val idLista: String, private val guardar: Boolean, private val items: ArrayList<ItemGastosConstructor>,
                            private val position: Int?, private val twoPane: Boolean):
        DialogFragment() {

    private var _binding : DialogCrearEditarItemBinding? = null
    private val binding get() = _binding!!
    private val user = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()
    private var dialog : AlertDialog? = null
    private var fechaSelec: Date? = null
    private var calendar: Calendar = Calendar.getInstance()
    private val fechaIngreso: Date = calendar.time
    private var conceptoViejo: String? = null
    private var montoViejo: Double? = null
    private var fechaViejaAproximada: Date? = null


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
        if (!guardar) cargarItem()
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
        if (guardar) guardarItem(concepto, montoDouble) else actualizarItem(concepto, montoDouble)
    }


    private fun guardarItem(concepto: String, monto: Double) {
        Toast.makeText(context, "Guardando...", Toast.LENGTH_SHORT).show()

        val docData: MutableMap<String, Any?> = HashMap()
        docData[Constants.BD_CONCEPTO] = concepto
        docData[Constants.BD_MONTO] = monto
        docData[Constants.BD_FECHA_APROXIMADA] = fechaSelec
        docData[Constants.BD_FECHA_INGRESO] = fechaIngreso

        db.collection(Constants.BD_LISTA_GASTOS).document(user!!.uid).collection(idLista)
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
        db.collection(Constants.BD_LISTA_GASTOS).document(user!!.uid).collection(Constants.BD_TODAS_LISTAS).document(idLista)
                .update(Constants.BD_CANTIDAD_ITEMS, items.size)
                .addOnSuccessListener {
                    Log.d(Constraints.TAG, "DocumentSnapshot successfully updated!")
                    Toast.makeText(context, getString(R.string.process_succes), Toast.LENGTH_SHORT).show()
                    if (twoPane) {
                        requireActivity().finish()
                        requireActivity().startActivity(Intent(context, ListaPendientesListActivity::class.java))
                        requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    } else {
                        dialog?.dismiss()
                    }

                }.addOnFailureListener { dialog?.dismiss() }
    }

    private fun cargarItem() {
        db.collection(Constants.BD_LISTA_GASTOS).document(user!!.uid).collection(idLista).document(items[position!!].idItem).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document!!.exists()) {
                    Log.d(Constraints.TAG, "DocumentSnapshot data: " + document.data)
                    conceptoViejo = document.getString(Constants.BD_CONCEPTO)
                    binding.etConcepto.setText(conceptoViejo)
                    montoViejo = document.getDouble(Constants.BD_MONTO)
                    binding.etMonto.setText("$montoViejo")
                    fechaViejaAproximada = document.getDate(Constants.BD_FECHA_APROXIMADA)
                    if (fechaViejaAproximada != null) {
                        binding.textViewFechaAproximada.text = SimpleDateFormat("EEE d MMM yyyy", Locale.getDefault()).format(fechaViejaAproximada!!)
                    }
                }
            } else {
                Log.d(Constraints.TAG, "get failed with ", task.exception)
                Toast.makeText(context, getString(R.string.error_cargar_data), Toast.LENGTH_SHORT).show()
                dialog?.dismiss()
            }
        }
    }

    private fun actualizarItem(concepto: String, monto: Double) {
        Toast.makeText(context, "Actualizando...", Toast.LENGTH_SHORT).show()

        val item: MutableMap<String, Any> = HashMap()

        if (conceptoViejo != concepto) {
            item[Constants.BD_CONCEPTO] = concepto
            items[position!!].concepto = concepto
        }
        if (monto != montoViejo) {
            item[Constants.BD_MONTO] = monto
            items[position!!].montoAproximado = monto
        }

        if (fechaSelec != null) {
            if (fechaSelec != fechaViejaAproximada) {
                item[Constants.BD_FECHA_APROXIMADA] = fechaSelec!!
                items[position!!].fechaAproximada = fechaSelec!!
            }
        }

        db.collection(Constants.BD_LISTA_GASTOS).document(user!!.uid).collection(idLista).document(items[position!!].idItem)
                .update(item)
                .addOnSuccessListener {
                    Log.d(Constraints.TAG, "DocumentSnapshot successfully updated!")
                    Toast.makeText(context, getString(R.string.process_succes), Toast.LENGTH_SHORT).show()
                    adapter.updateList(items)
                    dialog?.dismiss()
                }
                .addOnFailureListener { e ->
                    Log.w(Constraints.TAG, "Error updating document", e)
                    Toast.makeText(context, getString(R.string.error_cargar_data), Toast.LENGTH_SHORT).show()
                }
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
            binding.textViewFechaAproximada.text = SimpleDateFormat("EEE d MMM yyyy", Locale.getDefault()).format(fechaSelec!!)
        }, year, month, day)
        datePickerDialog.show()
    }
}