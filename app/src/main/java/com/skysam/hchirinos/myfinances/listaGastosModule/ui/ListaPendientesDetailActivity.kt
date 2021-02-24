package com.skysam.hchirinos.myfinances.listaGastosModule.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import com.skysam.hchirinos.myfinances.R


class ListaPendientesDetailActivity : AppCompatActivity() {

    private lateinit var idLista: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    putString(ListaPendientesDetailFragment.ARG_ITEM_IMAGEN,
                            intent.getStringExtra(ListaPendientesDetailFragment.ARG_ITEM_IMAGEN))
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