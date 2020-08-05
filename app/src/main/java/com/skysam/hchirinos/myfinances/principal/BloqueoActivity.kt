package com.skysam.hchirinos.myfinances.principal

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.Utils.Constantes
import com.skysam.hchirinos.myfinances.bloqueoFragments.HuellaFragment
import com.skysam.hchirinos.myfinances.bloqueoFragments.PinFragment

class BloqueoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bloqueo)

        val user = intent.getStringExtra("user")
        val tipoBloqueo = intent.getStringExtra(Constantes.PREFERENCE_TIPO_BLOQUEO)
        val inicio = intent.getBooleanExtra("inicio", true)

        val pinFragment = PinFragment.newInstance(user.toString(), inicio)
        val huellaFragment = HuellaFragment()

        if (tipoBloqueo == Constantes.PREFERENCE_BLOQUEO_HUELLA) {
            supportFragmentManager.beginTransaction().add(R.id.container, huellaFragment).commit()
        } else {
            supportFragmentManager.beginTransaction().add(R.id.container, pinFragment).commit()
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, HomeActivity::class.java))
    }
}