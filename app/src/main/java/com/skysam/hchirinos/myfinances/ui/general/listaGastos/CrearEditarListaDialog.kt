package com.skysam.hchirinos.myfinances.ui.general.listaGastos

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.Constraints
import androidx.fragment.app.DialogFragment
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.Utils.Constantes
import com.skysam.hchirinos.myfinances.databinding.DialogCrearListaBinding
import java.util.*

class CrearEditarListaDialog(private val twoPane: Boolean, private val guardar: Boolean, private val nombre: String?, private val idLista: String?):
        DialogFragment() {
    private var _binding : DialogCrearListaBinding? = null
    private val binding get() = _binding!!
    private val user = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()
    private var dialog : AlertDialog? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogCrearListaBinding.inflate(layoutInflater)

        if (!guardar) {
            binding.etNombre.setText(nombre)
        }

        val builder = AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.btn_nueva_lista))
                .setView(binding.root)
                .setPositiveButton(getString(R.string.btn_guardar), null)
                .setNegativeButton(getString(R.string.btn_cancelar), null)

        dialog = builder.create()
        dialog?.show()
        dialog?.getButton(DialogInterface.BUTTON_POSITIVE)?.setOnClickListener {
            binding.inputNombre.error = null
            validarLista()
        }
        return dialog as AlertDialog
    }

    private fun validarLista() {
        val nombre = binding.etNombre.text.toString()
        if (nombre.isNullOrEmpty()) {
            binding.inputNombre.error = getString(R.string.error_campo_vacio)
            return
        }
        if (guardar) guardarLista(nombre) else editarLista(nombre)
    }

    private fun guardarLista(nombre: String) {
        val calendar = Calendar.getInstance()
        val fechaIngreso = calendar.time

        val docData: MutableMap<String, Any> = HashMap()
        docData[Constantes.BD_NOMBRE] = nombre
        docData[Constantes.BD_CANTIDAD_ITEMS] = 0
        docData[Constantes.BD_FECHA_INGRESO] = fechaIngreso

        db.collection(Constantes.BD_LISTA_GASTOS).document(user!!.uid).collection(Constantes.BD_TODAS_LISTAS)
                .add(docData)
                .addOnSuccessListener {document ->
                    val docId = document.id
                    Log.d(Constraints.TAG, "DocumentSnapshot written succesfully")

                    if (twoPane) {
                        val fragment = ListaPendientesDetailFragment().apply {
                            arguments = Bundle().apply {
                                putString(ListaPendientesDetailFragment.ARG_ITEM_ID, docId)
                                putString(ListaPendientesDetailFragment.ARG_ITEM_NOMBRE, nombre)
                            }
                        }
                        activity?.supportFragmentManager
                                ?.beginTransaction()
                                ?.replace(R.id.listapendientes_detail_container, fragment)
                                ?.commit()
                    } else {
                        val intent = Intent(context, ListaPendientesDetailActivity::class.java).apply {
                            putExtra(ListaPendientesDetailFragment.ARG_ITEM_ID, docId)
                            putExtra(ListaPendientesDetailFragment.ARG_ITEM_NOMBRE, nombre)
                        }
                        context?.startActivity(intent)
                    }
                    dialog?.dismiss()
                }
                .addOnFailureListener(OnFailureListener { e ->
                    Log.w(Constraints.TAG, "Error adding document", e)
                    Toast.makeText(context, "Error al guardar. Intente nuevamente", Toast.LENGTH_SHORT).show()
                    dialog?.dismiss()
                })
    }

    private fun editarLista(nombre: String) {
        db.collection(Constantes.BD_LISTA_GASTOS).document(user!!.uid).collection(Constantes.BD_TODAS_LISTAS).document(idLista!!)
                .update(Constantes.BD_NOMBRE, nombre)

    }

}