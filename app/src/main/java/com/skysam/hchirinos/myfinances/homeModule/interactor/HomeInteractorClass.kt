package com.skysam.hchirinos.myfinances.homeModule.interactor

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import androidx.constraintlayout.widget.Constraints
import com.skysam.hchirinos.myfinances.common.model.SharedPreferencesBD
import com.skysam.hchirinos.myfinances.common.model.firebase.FirebaseAuthentication
import com.skysam.hchirinos.myfinances.common.model.firebase.FirebaseFirestore
import com.skysam.hchirinos.myfinances.common.utils.Constants
import com.skysam.hchirinos.myfinances.homeModule.presenter.HomePresenter
import org.jsoup.Jsoup
import java.io.IOException
import java.util.*

class HomeInteractorClass(val homePresenter: HomePresenter, val context: Context): HomeInteractor {

    override fun obtenerCotizacionWeb() {
        val cotizacion: Cotizacion = Cotizacion(homePresenter, context)
        cotizacion.execute()
    }

    override fun guardarCotizacionShared(valorFloat: Float) {
        SharedPreferencesBD.saveCotizacion(FirebaseAuthentication.getCurrentUser()!!.uid, context, valorFloat)
    }

    override fun getIngresos(year: Int, month: Int) {
        val valorCotizacion = SharedPreferencesBD.getCotizacion(FirebaseAuthentication.getCurrentUser()!!.uid, context)
        FirebaseFirestore.getIngresosReference(FirebaseAuthentication.getCurrentUser()!!.uid, year, month)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        var montototal = 0.0
                        var mesCobro: Int
                        var yearCobro: Int

                        for (document in task.result!!) {
                            Log.d(Constraints.TAG, document.id + " => " + document.data)
                            val activo = document.getBoolean(Constants.BD_MES_ACTIVO)
                            if (activo == null || activo) {
                                val calendarInicial = Calendar.getInstance()
                                val montoDetal = document.getDouble(Constants.BD_MONTO)!!
                                val dolar = document.getBoolean(Constants.BD_DOLAR)!!
                                val tipoFrecuencia = document.getString(Constants.BD_TIPO_FRECUENCIA)
                                if (tipoFrecuencia != null) {
                                    val fechaInicial = document.getDate(Constants.BD_FECHA_INCIAL)
                                    val duracionFrecuencia = document.getDouble(Constants.BD_DURACION_FRECUENCIA)!!
                                    val duracionFrecuenciaInt = duracionFrecuencia.toInt()
                                    var multiploIngreso = 0
                                    calendarInicial.time = fechaInicial
                                    mesCobro = calendarInicial[Calendar.MONTH]
                                    yearCobro = calendarInicial[Calendar.YEAR]
                                    if (mesCobro == month) {
                                        multiploIngreso = 1
                                    }
                                    if (tipoFrecuencia == "Dias") {
                                        var j = 1
                                        while (mesCobro <= month && yearCobro == year) {
                                            calendarInicial.add(Calendar.DAY_OF_YEAR, duracionFrecuenciaInt)
                                            mesCobro = calendarInicial[Calendar.MONTH]
                                            yearCobro = calendarInicial[Calendar.YEAR]
                                            if (mesCobro == month) {
                                                multiploIngreso += 1
                                            }
                                            j++
                                        }
                                    } else if (tipoFrecuencia == "Semanas") {
                                        var j = 1
                                        while (mesCobro <= month && yearCobro == year) {
                                            calendarInicial.add(Calendar.DAY_OF_YEAR, duracionFrecuenciaInt * 7)
                                            mesCobro = calendarInicial[Calendar.MONTH]
                                            yearCobro = calendarInicial[Calendar.YEAR]
                                            if (mesCobro == month) {
                                                multiploIngreso += 1
                                            }
                                            j++
                                        }
                                    } else if (tipoFrecuencia == "Meses") {
                                        var j = 1
                                        while (mesCobro <= month && yearCobro == year) {
                                            calendarInicial.add(Calendar.MONTH, duracionFrecuenciaInt)
                                            mesCobro = calendarInicial[Calendar.MONTH]
                                            yearCobro = calendarInicial[Calendar.YEAR]
                                            if (mesCobro == month) {
                                                multiploIngreso += 1
                                            }
                                            j++
                                        }
                                    }
                                    montototal = if (dolar) {
                                        montototal + montoDetal * multiploIngreso
                                    } else {
                                        montototal + montoDetal / valorCotizacion * multiploIngreso
                                    }
                                } else {
                                    montototal = if (dolar) {
                                        montototal + montoDetal
                                    } else {
                                        montototal + montoDetal / valorCotizacion
                                    }
                                }
                            }
                        }
                        homePresenter.statusValorIngresos(true, montototal.toFloat(), montototal.toString())
                    } else {
                        homePresenter.statusValorIngresos(false, 0f, "Error al obtener los Ingresos")
                    }
                }
                .addOnFailureListener {
                    homePresenter.statusValorIngresos(false, 0f, "Error al obtener los Ingresos")
                }
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