package com.skysam.hchirinos.myfinances.ui.ajustes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.Utils.Constantes


class BloqueoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bloqueo)

        val user = intent.getStringExtra(Constantes.USER)
        val tipoBloqueo = intent.getStringExtra(Constantes.PREFERENCE_TIPO_BLOQUEO)
        val inicio = intent.getBooleanExtra("inicio", true)

        val pinFragment = PinFragment.newInstance(user.toString(), inicio)
        val huellaFragment = HuellaFragment()

        val finalHost = NavHostFragment.create(R.navigation.nav_bloqueo)
        supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, finalHost)
                .setPrimaryNavigationFragment(finalHost)
                .commit()

        if (tipoBloqueo == Constantes.PREFERENCE_BLOQUEO_HUELLA) {
            supportFragmentManager.beginTransaction().add(R.id.container, huellaFragment).commit()
        } else {
            supportFragmentManager.beginTransaction().add(R.id.container, pinFragment).commit()
        }
    }
}