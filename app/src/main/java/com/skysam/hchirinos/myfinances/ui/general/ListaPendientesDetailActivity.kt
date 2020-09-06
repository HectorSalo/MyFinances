package com.skysam.hchirinos.myfinances.ui.general

import android.content.Intent
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.Utils.Constantes

class ListaPendientesDetailActivity : AppCompatActivity() {

    private var user: FirebaseUser? = null
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
        user = FirebaseAuth.getInstance().currentUser
        val sharedPreferences = getSharedPreferences(user!!.uid, MODE_PRIVATE)

        when (sharedPreferences.getString(Constantes.PREFERENCE_TEMA, Constantes.PREFERENCE_TEMA_SISTEMA)) {
            Constantes.PREFERENCE_TEMA_SISTEMA -> setTheme(R.style.AppTheme)
            Constantes.PREFERENCE_TEMA_OSCURO -> setTheme(R.style.AppThemeNight)
            Constantes.PREFERENCE_TEMA_CLARO -> setTheme(R.style.AppThemeDay)
        }
        setContentView(R.layout.activity_listapendientes_detail)
        setSupportActionBar(findViewById(R.id.detail_toolbar))



        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own detail action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            val fragment = ListaPendientesDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ListaPendientesDetailFragment.ARG_ITEM_ID,
                            intent.getStringExtra(ListaPendientesDetailFragment.ARG_ITEM_ID))
                    putString(ListaPendientesDetailFragment.ARG_ITEM_NOMBRE,
                            intent.getStringExtra(ListaPendientesDetailFragment.ARG_ITEM_NOMBRE))
                }
            }

            supportFragmentManager.beginTransaction()
                    .add(R.id.listapendientes_detail_container, fragment)
                    .commit()
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
}