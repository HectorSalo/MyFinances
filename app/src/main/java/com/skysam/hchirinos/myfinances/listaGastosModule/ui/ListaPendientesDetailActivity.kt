package com.skysam.hchirinos.myfinances.listaGastosModule.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.common.utils.Constants

class ListaPendientesDetailActivity : AppCompatActivity() {

    private var user: FirebaseUser? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var idLista: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
        user = FirebaseAuth.getInstance().currentUser
        val sharedPreferences = getSharedPreferences(user!!.uid, MODE_PRIVATE)

        when (sharedPreferences.getString(Constants.PREFERENCE_TEMA, Constants.PREFERENCE_TEMA_SISTEMA)) {
            Constants.PREFERENCE_TEMA_SISTEMA -> setTheme(R.style.AppTheme)
            Constants.PREFERENCE_TEMA_OSCURO -> setTheme(R.style.AppThemeNight)
            Constants.PREFERENCE_TEMA_CLARO -> setTheme(R.style.AppThemeDay)
        }
        setContentView(R.layout.activity_listapendientes_detail)
        setSupportActionBar(findViewById(R.id.detail_toolbar))

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            val fragment = ListaPendientesDetailFragment().apply {
                idLista = intent.getStringExtra(ListaPendientesDetailFragment.ARG_ITEM_ID)!!
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