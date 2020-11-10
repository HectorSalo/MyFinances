package com.skysam.hchirinos.myfinances.homeModule.interactor

import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import com.skysam.hchirinos.myfinances.common.model.SharedPreferencesBD
import com.skysam.hchirinos.myfinances.common.model.firebase.FirebaseAuthentication
import com.skysam.hchirinos.myfinances.common.model.firebase.FirebaseFirestore
import com.skysam.hchirinos.myfinances.homeModule.presenter.HomePresenter
import org.jsoup.Jsoup
import java.io.IOException

class HomeInteractorClass(val homePresenter: HomePresenter, val context: Context): HomeInteractor {

    override fun obtenerCotizacionWeb() {
        val cotizacion: Cotizacion = Cotizacion(homePresenter, context)
        cotizacion.execute()
    }

    override fun guardarCotizacionShared(valorFloat: Float) {
        SharedPreferencesBD.saveCotizacion(FirebaseAuthentication.getCurrentUser()!!.uid, context, valorFloat)
    }

    override fun getIngresos(year: Int, month: Int) {
        FirebaseFirestore.getIngresosReference(FirebaseAuthentication.getCurrentUser()!!.uid, year, month)
                .get()
                .addOnCompleteListener {task ->
                    if (task.isSuccessful) {

                    } else {

                    }
                }
                .addOnFailureListener {  }
    }

    private class Cotizacion(val homePresenter: HomePresenter, val context: Context) : AsyncTask<Void?, Void?, Void?>() {
        var valor: String? = null
        var valorCotizacion: Float? = null

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            if (valor != null) {
                homePresenter.valorCotizacionWebOk(valor!!, valorCotizacion!!)
            } else {
                obtenerCotizacionShared()
            }
        }

        override fun onCancelled() {
            super.onCancelled()
            obtenerCotizacionShared()
        }

        override fun doInBackground(vararg voids: Void?): Void? {
            val url = "https://monitordolarvenezuela.com/"
            try {
                val doc = Jsoup.connect(url).get()
                val data = doc.select("div.back-white-tabla")
                valor = data.select("h6.text-center").text()

                if (valor != null) {
                    val valor1: String = valor!!.replace("Bs.S", "")
                    val valor2 = valor1.replace(".", "")
                    val valorNeto = valor2.replace(",", ".")
                    valorCotizacion = valorNeto.toFloat()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

        fun obtenerCotizacionShared() {
            homePresenter.valorCotizacionWebError(SharedPreferencesBD.getCotizacion(FirebaseAuthentication.getCurrentUser()!!.uid, context))
        }
    }



}