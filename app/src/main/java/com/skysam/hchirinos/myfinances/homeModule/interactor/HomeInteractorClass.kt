package com.skysam.hchirinos.myfinances.homeModule.interactor

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.Constraints
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.firestore.QuerySnapshot
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

    override fun getAhoros(year: Int, month: Int) {
        val valorCotizacion = SharedPreferencesBD.getCotizacion(FirebaseAuthentication.getCurrentUser()!!.uid, context)
        FirebaseFirestore.getAhorrosReference(FirebaseAuthentication.getCurrentUser()!!.uid, year, month)
                .get()
                .addOnCompleteListener(OnCompleteListener<QuerySnapshot?> { task ->
                    if (task.isSuccessful) {
                        var montototal = 0.0
                        for (document in task.result!!) {
                            val date = document.getDate(Constants.BD_FECHA_INGRESO)
                            val calendar = Calendar.getInstance()
                            calendar.time = date!!
                            val mesItemAhorro = calendar.get(Calendar.MONTH)
                            if (month >= mesItemAhorro) {
                                val montoDetal = document.getDouble(Constants.BD_MONTO)!!
                                val dolar = document.getBoolean(Constants.BD_DOLAR)!!
                                montototal += if (dolar) {
                                    montoDetal
                                } else {
                                    montoDetal / valorCotizacion
                                }
                            }
                        }
                        homePresenter.statusValorAhorros(true, montototal.toFloat(), montototal.toString())
                    } else {
                        homePresenter.statusValorAhorros(false, 0f, "Error al obtener los Ahorros")
                    }
                }).addOnFailureListener(OnFailureListener { e ->
                    homePresenter.statusValorAhorros(false, 0f, "Error al obtener los Ahorros")
                })
    }

    override fun getDeudas(year: Int, month: Int) {
        val valorCotizacion = SharedPreferencesBD.getCotizacion(FirebaseAuthentication.getCurrentUser()!!.uid, context)
        FirebaseFirestore.getDeudasReference(FirebaseAuthentication.getCurrentUser()!!.uid, year, month)
                .get()
                .addOnCompleteListener(OnCompleteListener<QuerySnapshot?> { task ->
                    if (task.isSuccessful) {
                        var montototal = 0.0
                        for (document in task.result!!) {
                            Log.d(Constraints.TAG, document.id + " => " + document.data)
                            val montoDetal = document.getDouble(Constants.BD_MONTO)!!
                            val dolar = document.getBoolean(Constants.BD_DOLAR)!!
                            montototal = if (dolar) {
                                montototal + montoDetal
                            } else {
                                montototal + montoDetal / valorCotizacion
                            }
                        }
                        homePresenter.statusValorDeudas(true, montototal.toFloat(), montototal.toString())
                    } else {
                        homePresenter.statusValorDeudas(false, 0f, "Error al obtener las Deudas")
                    }
                }).addOnFailureListener(OnFailureListener { e ->
                    homePresenter.statusValorDeudas(false, 0f, "Error al obtener las Deudas")
                })
    }

    override fun getPrestamos(year: Int, month: Int) {
        val valorCotizacion = SharedPreferencesBD.getCotizacion(FirebaseAuthentication.getCurrentUser()!!.uid, context)
        FirebaseFirestore.getPrestamosReference(FirebaseAuthentication.getCurrentUser()!!.uid, year, month)
                .get()
                .addOnCompleteListener(OnCompleteListener<QuerySnapshot?> { task ->
                    if (task.isSuccessful) {
                        var montototal = 0.0
                        for (document in task.result!!) {
                            Log.d(Constraints.TAG, document.id + " => " + document.data)
                            val montoDetal = document.getDouble(Constants.BD_MONTO)!!
                            val dolar = document.getBoolean(Constants.BD_DOLAR)!!
                            montototal = if (dolar) {
                                montototal + montoDetal
                            } else {
                                montototal + montoDetal / valorCotizacion
                            }
                        }
                        homePresenter.statusValorPrestamos(true, montototal.toFloat(), montototal.toString())
                    } else {
                        homePresenter.statusValorPrestamos(false, 0f, "Error al obtener los Préstamos")
                    }
                }).addOnFailureListener(OnFailureListener { e ->
                    homePresenter.statusValorPrestamos(false, 0f, "Error al obtener los Préstamos")
                })
    }

    override fun getGastos(year: Int, month: Int) {
        val valorCotizacion = SharedPreferencesBD.getCotizacion(FirebaseAuthentication.getCurrentUser()!!.uid, context)
        FirebaseFirestore.getGastosReference(FirebaseAuthentication.getCurrentUser()!!.uid, year, month)
                .get()
                .addOnCompleteListener(OnCompleteListener<QuerySnapshot?> { task ->
                    if (task.isSuccessful) {
                        var montototal = 0.0
                        var mesPago = 0
                        var yearPago = 0
                        for (document in task.result!!) {
                            val activo = document.getBoolean(Constants.BD_MES_ACTIVO)
                            if (activo == null || activo) {
                                Log.d(Constraints.TAG, document.id + " => " + document.data)
                                val montoDetal = document.getDouble(Constants.BD_MONTO)!!
                                val dolar = document.getBoolean(Constants.BD_DOLAR)!!
                                val tipoFrecuencia = document.getString(Constants.BD_TIPO_FRECUENCIA)
                                if (tipoFrecuencia != null) {
                                    val calendarInicial = Calendar.getInstance()
                                    val calendarPago = Calendar.getInstance()
                                    val fechaInicial = document.getDate(Constants.BD_FECHA_INCIAL)
                                    val duracionFrecuencia = document.getDouble(Constants.BD_DURACION_FRECUENCIA)!!
                                    val duracionFrecuenciaInt = duracionFrecuencia.toInt()
                                    var multiploCobranza = 0
                                    calendarInicial.time = fechaInicial!!
                                    mesPago = calendarInicial[Calendar.MONTH]
                                    yearPago = calendarInicial[Calendar.YEAR]
                                    if (mesPago == month) {
                                        multiploCobranza = 1
                                    }
                                    if (tipoFrecuencia == "Dias") {
                                        var j = 1
                                        while (mesPago <= month && yearPago == year) {
                                            calendarInicial.add(Calendar.DAY_OF_YEAR, duracionFrecuenciaInt * j)
                                            calendarPago.time = calendarInicial.time
                                            mesPago = calendarPago[Calendar.MONTH]
                                            yearPago = calendarPago[Calendar.YEAR]
                                            calendarInicial.time = fechaInicial
                                            if (mesPago == month) {
                                                multiploCobranza += 1
                                            }
                                            j++
                                        }
                                    } else if (tipoFrecuencia == "Semanas") {
                                        var j = 1
                                        while (mesPago <= month && yearPago == year) {
                                            calendarInicial.add(Calendar.DAY_OF_YEAR, duracionFrecuenciaInt * j * 7)
                                            calendarPago.time = calendarInicial.time
                                            mesPago = calendarPago[Calendar.MONTH]
                                            yearPago = calendarPago[Calendar.YEAR]
                                            calendarInicial.time = fechaInicial
                                            if (mesPago == month) {
                                                multiploCobranza += 1
                                            }
                                            j++
                                        }
                                    } else if (tipoFrecuencia == "Meses") {
                                        var j = 1
                                        while (mesPago <= month && yearPago == year) {
                                            calendarInicial.add(Calendar.MONTH, duracionFrecuenciaInt * j)
                                            calendarPago.time = calendarInicial.time
                                            mesPago = calendarPago[Calendar.MONTH]
                                            yearPago = calendarPago[Calendar.YEAR]
                                            calendarInicial.time = fechaInicial
                                            if (mesPago == month) {
                                                multiploCobranza += 1
                                            }
                                            j++
                                        }
                                    }
                                    montototal = if (dolar) {
                                        montototal + montoDetal * multiploCobranza
                                    } else {
                                        montototal + montoDetal / valorCotizacion * multiploCobranza
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
                        homePresenter.statusValorGastos(true, montototal.toFloat(), montototal.toString())
                    } else {
                        homePresenter.statusValorGastos(false, 0f, "Error al obtener los Gastos")
                    }
                }).addOnFailureListener(OnFailureListener { e ->
                    homePresenter.statusValorGastos(false, 0f, "Error al obtener los Gastos")
                })
    }

    override fun moveDataNextYear(year: Int) {

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