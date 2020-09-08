package com.skysam.hchirinos.myfinances.ui.general.listaGastos

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.Utils.Constantes
import com.skysam.hchirinos.myfinances.adaptadores.ListasPendientesAdapter
import com.skysam.hchirinos.myfinances.constructores.ListasConstructor
import com.skysam.hchirinos.myfinances.databinding.ActivityListapendientesListBinding


class ListaPendientesListActivity : AppCompatActivity() {

    private lateinit var activityListapendientesListBinding : ActivityListapendientesListBinding
    private var twoPane: Boolean = false
    private var listas: ArrayList<ListasConstructor> = ArrayList()
    private var user: FirebaseUser? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: ListasPendientesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityListapendientesListBinding = ActivityListapendientesListBinding.inflate(layoutInflater)
        setContentView(activityListapendientesListBinding.root)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        db = FirebaseFirestore.getInstance()
        user = FirebaseAuth.getInstance().currentUser

        if (findViewById<NestedScrollView>(R.id.listapendientes_detail_container) != null) {
            twoPane = true
        }

        adapter = ListasPendientesAdapter(listas, this, twoPane)

        setupRecyclerView(activityListapendientesListBinding.includeListapendientesList.rvListapendientesList)

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            val crearListaDialog = CrearEditarListaDialog(twoPane, true, listas, null, adapter)
            crearListaDialog.show(supportFragmentManager, title.toString())
        }

        if (twoPane) cargarListas()
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(DividerItemDecoration(applicationContext, LinearLayoutManager.VERTICAL))
        recyclerView.adapter = adapter
    }

    private fun cargarListas() {
        listas = ArrayList()
        activityListapendientesListBinding.includeListapendientesList.progressBar.visibility = View.VISIBLE
        val reference = db.collection(Constantes.BD_LISTA_GASTOS).document(user!!.uid).collection(Constantes.BD_TODAS_LISTAS)

        reference.orderBy(Constantes.BD_FECHA_INGRESO, Query.Direction.ASCENDING)
                .get().addOnSuccessListener { result ->
                    for (document in result) {
                        val lista = ListasConstructor()

                        lista.idLista = document.id
                        val cantidadD: Double? = document.getDouble(Constantes.BD_CANTIDAD_ITEMS)
                        val cantidad = cantidadD?.toInt()
                        lista.cantidadItems = cantidad!!
                        lista.nombreLista = document.getString(Constantes.BD_NOMBRE)

                        listas.add(lista)
                    }
                    adapter.updateList(listas)

                    activityListapendientesListBinding.includeListapendientesList.progressBar.visibility = View.GONE

                    if (listas.isNullOrEmpty()) {
                        activityListapendientesListBinding.includeListapendientesList.tvSinListas.visibility = View.VISIBLE
                        activityListapendientesListBinding.includeListapendientesList.rvListapendientesList.visibility = View.GONE
                    } else {
                        activityListapendientesListBinding.includeListapendientesList.tvSinListas.visibility = View.GONE
                        activityListapendientesListBinding.includeListapendientesList.rvListapendientesList.visibility = View.VISIBLE
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(applicationContext, getString(R.string.error_cargar_data), Toast.LENGTH_SHORT).show()
                    activityListapendientesListBinding.includeListapendientesList.progressBar.visibility = View.GONE
                }
    }

    override fun onOptionsItemSelected(item: MenuItem) =
            when (item.itemId) {
                android.R.id.home -> {
                    finish()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }

    override fun onResume() {
        super.onResume()
        if (!twoPane) cargarListas()
    }
}