package com.skysam.hchirinos.myfinances.ui.general

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.Utils.Constantes
import com.skysam.hchirinos.myfinances.adaptadores.ItemListPendienteAdapter
import com.skysam.hchirinos.myfinances.constructores.ItemGastosConstructor
import com.skysam.hchirinos.myfinances.databinding.ListapendientesDetailBinding


class ListaPendientesDetailFragment : Fragment() {

    private var _binding: ListapendientesDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var idLista: String
    private var items: ArrayList<ItemGastosConstructor> = ArrayList()
    private var user: FirebaseUser? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: ItemListPendienteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            if (it.containsKey(ARG_ITEM_ID)) {
                idLista = it.getString(ARG_ITEM_ID)!!
            }
            if (it.containsKey(ARG_ITEM_NOMBRE)) {
                activity?.findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)?.title = it.getString(ARG_ITEM_NOMBRE)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = ListapendientesDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        user = FirebaseAuth.getInstance().currentUser
        
        adapter = ItemListPendienteAdapter(items, requireContext())

        setupRecyclerView(binding.rvItemsLista)

        cargarLista()
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        recyclerView.adapter = adapter
    }

    private fun cargarLista() {
        binding.progressBar.visibility = View.VISIBLE
        val reference = db.collection(Constantes.BD_LISTA_GASTOS).document(user!!.uid).collection(idLista)

        reference.orderBy(Constantes.BD_FECHA_INGRESO, Query.Direction.ASCENDING)
                .get().addOnSuccessListener { result ->
                    for (document in result) {
                        val item = ItemGastosConstructor()

                        item.idItem = document.id
                        item.concepto = document.getString(Constantes.BD_CONCEPTO)
                        item.montoAproximado = document.getDouble(Constantes.BD_MONTO)!!
                        item.fechaIngreso = document.getDate(Constantes.BD_FECHA_INGRESO)
                        item.fechaAproximada = document.getDate(Constantes.BD_FECHA_APROXIMADA)

                        items.add(item)
                    }
                    adapter.updateList(items)

                    binding.progressBar.visibility = View.GONE

                    if (items.isNullOrEmpty()) {
                        binding.tvInfoLista.visibility = View.VISIBLE
                        binding.rvItemsLista.visibility = View.GONE
                    } else {
                        binding.tvInfoLista.visibility = View.GONE
                        binding.rvItemsLista.visibility = View.VISIBLE
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, getString(R.string.error_cargar_data), Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                }

    }

    companion object {
        const val ARG_ITEM_ID = "item_id"
        const val ARG_ITEM_NOMBRE = "item_nombre"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}