package com.skysam.hchirinos.myfinances.listaGastosModule.ui

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.Constraints
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.common.model.constructores.ImagenesListasConstructor
import com.skysam.hchirinos.myfinances.common.utils.Constants
import com.skysam.hchirinos.myfinances.common.model.constructores.ListasConstructor
import com.skysam.hchirinos.myfinances.databinding.DialogCrearListaBinding
import com.skysam.hchirinos.myfinances.listaGastosModule.interactor.CrearEditarListaInteractorClass
import com.skysam.hchirinos.myfinances.listaGastosModule.presenter.CrearEditarListaPresenter
import com.skysam.hchirinos.myfinances.listaGastosModule.presenter.CrearEditarListaPresenterClass
import java.util.*
import kotlin.collections.ArrayList

class CrearEditarListaDialog(private val twoPane: Boolean, private val guardar: Boolean, private val lista: ArrayList<ListasConstructor>, private val position: Int?,
                             private val adapter: ListasPendientesAdapter):
        DialogFragment(), CrearEditarListaView, CrearEditarListaClick {
    private var _binding : DialogCrearListaBinding? = null
    private val binding get() = _binding!!
    private val user = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()
    private var dialog : AlertDialog? = null
    private lateinit var imagenesListas: ArrayList<ImagenesListasConstructor>
    private lateinit var imagenesListasAdapter: ImagenesListasAdapter
    private var crearEditarListaPresenter: CrearEditarListaPresenter = CrearEditarListaPresenterClass(this)
    private var imagen: String? = null


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogCrearListaBinding.inflate(layoutInflater)

        crearEditarListaPresenter.getImages()

        var title = getString(R.string.btn_nueva_lista)
        if (!guardar) {
            binding.etNombre.setText(lista[position!!].nombreLista)
            title = getString(R.string.text_editar_lista)
        }

        val builder = AlertDialog.Builder(requireContext())
                .setTitle(title)
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
        Toast.makeText(context, "Guardando...", Toast.LENGTH_SHORT).show()
        val calendar = Calendar.getInstance()
        val fechaIngreso = calendar.time

        val docData: MutableMap<String, Any> = HashMap()
        docData[Constants.BD_NOMBRE] = nombre
        docData[Constants.BD_CANTIDAD_ITEMS] = 0
        docData[Constants.BD_FECHA_INGRESO] = fechaIngreso

        db.collection(Constants.BD_LISTA_GASTOS).document(user!!.uid).collection(Constants.BD_TODAS_LISTAS)
                .add(docData)
                .addOnSuccessListener {document ->
                    val docId = document.id
                    Log.d(Constraints.TAG, "DocumentSnapshot written succesfully")

                    if (twoPane) {
                        var itemNuevo: ListasConstructor?
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
                        itemNuevo = ListasConstructor()
                        itemNuevo.nombreLista = nombre
                        itemNuevo.idLista = docId
                        itemNuevo.cantidadItems = 0
                        itemNuevo.imagen = imagen
                        lista.add(itemNuevo)
                        adapter.updateList(lista)
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
                    Toast.makeText(context, getString(R.string.error_guardar_data), Toast.LENGTH_SHORT).show()
                    dialog?.dismiss()
                })
    }

    private fun editarLista(nombre: String) {
        Toast.makeText(context, "Actualizando...", Toast.LENGTH_SHORT).show()

        db.collection(Constants.BD_LISTA_GASTOS).document(user!!.uid).collection(Constants.BD_TODAS_LISTAS).document(lista[position!!].idLista)
                .update(Constants.BD_NOMBRE, nombre)
                .addOnSuccessListener {
                    Toast.makeText(context, getString(R.string.process_succes), Toast.LENGTH_SHORT).show()
                    dialog?.dismiss()
                    lista[position].nombreLista = nombre
                    adapter.updateList(lista)
                }
                .addOnFailureListener {
                    Toast.makeText(context, getString(R.string.error_guardar_data), Toast.LENGTH_SHORT).show()
                }
    }

    override fun cargarImagenes(imagenes: ArrayList<ImagenesListasConstructor>) {
        imagenesListas = ArrayList()
        imagenesListas = imagenes
        imagenesListasAdapter = ImagenesListasAdapter(imagenesListas, requireContext(), this)
        binding.rvImagenesListas.adapter = imagenesListasAdapter
    }

    override fun onImageClick(position: Int) {
        for (j in 0 until imagenesListas.size) {
            imagenesListas[j].imageSelected = false
        }
        imagen = imagenesListas[position].photoUrl
        imagenesListas[position].imageSelected = true
        imagenesListasAdapter.update(imagenesListas)
    }


}