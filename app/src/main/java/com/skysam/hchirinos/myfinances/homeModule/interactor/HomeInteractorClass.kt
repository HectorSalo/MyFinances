package com.skysam.hchirinos.myfinances.homeModule.interactor

import android.content.Context
import android.util.Log
import androidx.constraintlayout.widget.Constraints
import com.google.firebase.messaging.FirebaseMessaging
import com.skysam.hchirinos.myfinances.common.model.SharedPreferencesBD
import com.skysam.hchirinos.myfinances.common.model.firebase.FirebaseAuthentication
import com.skysam.hchirinos.myfinances.common.model.firebase.FirebaseFirestore
import com.skysam.hchirinos.myfinances.common.utils.Constants
import com.skysam.hchirinos.myfinances.homeModule.presenter.HomePresenter
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import java.util.*
import kotlin.coroutines.CoroutineContext

class HomeInteractorClass(private val homePresenter: HomePresenter, val context: Context): HomeInteractor, CoroutineScope {

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun obtenerCotizacionWeb() {

        getStatusNotification()

        launch {
            var valor: String? = null
            var valorCotizacion: Float? = null
            val url = "https://monitordolarvenezuela.com/"

            withContext(Dispatchers.IO) {
                try {
                    val doc = Jsoup.connect(url).get()
                    val data = doc.select("div.back-white-tabla")
                    valor = data.select("h6.text-center").text()
                } catch (e: Exception) {
                    Log.e("Error", e.toString())
                }
            }
            if (valor != null) {
                val valor1: String = valor!!.replace("Bs.S ", "")
                val valor2 = valor1.replace(".", "")
                val values: List<String> = valor2.split(" ")
                val valor3 = values[0]
                val valorNeto = valor3.replace(",", ".")
                valorCotizacion = valorNeto.toFloat()

                val values2: List<String> = valor1.split(" ")
                valor = values2[0]
            }

            if (valor != null) {
                homePresenter.valorCotizacionWebOk(valor!!, valorCotizacion!!)
            } else {
                obtenerCotizacionShared()
            }
        }

    }

    private fun getStatusNotification() {
        val notificationStatus = SharedPreferencesBD.getFirstSubscribeMainTopic(FirebaseAuthentication.getCurrentUser()!!.uid, context)
        if (!notificationStatus) {
            FirebaseMessaging.getInstance().subscribeToTopic(Constants.PREFERENCE_NOTIFICATION_MAIN_TOPIC)
                    .addOnSuccessListener {
                        SharedPreferencesBD.subscribeFirstMainTopicNotification(FirebaseAuthentication.getCurrentUser()!!.uid, context)
                    }
        }
    }

    private fun obtenerCotizacionShared() {
        homePresenter.valorCotizacionWebError(SharedPreferencesBD.getCotizacion(FirebaseAuthentication.getCurrentUser()!!.uid, context))
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
                                val calendarCobro = Calendar.getInstance()
                                val montoDetal = document.getDouble(Constants.BD_MONTO)!!
                                val dolar = document.getBoolean(Constants.BD_DOLAR)!!
                                val tipoFrecuencia = document.getString(Constants.BD_TIPO_FRECUENCIA)
                                if (tipoFrecuencia != null) {
                                    calendarCobro.time = document.getDate(Constants.BD_FECHA_INCIAL)!!
                                    val duracionFrecuencia = document.getDouble(Constants.BD_DURACION_FRECUENCIA)!!
                                    val duracionFrecuenciaInt = duracionFrecuencia.toInt()
                                    mesCobro = calendarCobro[Calendar.MONTH]
                                    yearCobro = calendarCobro[Calendar.YEAR]

                                    while (mesCobro <= month && yearCobro == year) {
                                        if (mesCobro == month) {
                                            montototal = if (dolar) {
                                                montototal + montoDetal
                                            } else {
                                                montototal + montoDetal / valorCotizacion
                                            }
                                        }

                                        when(tipoFrecuencia) {
                                            "Dias" -> {
                                                calendarCobro.add(Calendar.DAY_OF_YEAR, duracionFrecuenciaInt)
                                            }
                                            "Semanas" -> {
                                                calendarCobro.add(Calendar.DAY_OF_YEAR, duracionFrecuenciaInt * 7)
                                            }
                                            "Meses" -> {
                                                calendarCobro.add(Calendar.MONTH, duracionFrecuenciaInt)
                                            }
                                        }
                                        mesCobro = calendarCobro[Calendar.MONTH]
                                        yearCobro = calendarCobro[Calendar.YEAR]
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
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        var montototal = 0.0
                        for (document in task.result!!) {
                            val date = document.getDate(Constants.BD_FECHA_INGRESO)
                            val calendar = Calendar.getInstance()
                            calendar.time = date!!
                            val montoDetal = document.getDouble(Constants.BD_MONTO)!!
                            val dolar = document.getBoolean(Constants.BD_DOLAR)!!
                            montototal += if (dolar) {
                                montoDetal
                            } else {
                                montoDetal / valorCotizacion
                            }
                        }
                        homePresenter.statusValorAhorros(true, montototal.toFloat(), montototal.toString())
                    }
                }.addOnFailureListener {
                    homePresenter.statusValorAhorros(false, 0f, "Error al obtener ahorros")
                }
    }

    override fun getDeudas(year: Int, month: Int) {
        val valorCotizacion = SharedPreferencesBD.getCotizacion(FirebaseAuthentication.getCurrentUser()!!.uid, context)
        FirebaseFirestore.getDeudasReference(FirebaseAuthentication.getCurrentUser()!!.uid, year, month)
                .get()
                .addOnCompleteListener { task ->
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
                    }
                }.addOnFailureListener {
                    homePresenter.statusValorDeudas(false, 0f, "Error al obtener deudas")
                }
    }

    override fun getPrestamos(year: Int, month: Int) {
        val valorCotizacion = SharedPreferencesBD.getCotizacion(FirebaseAuthentication.getCurrentUser()!!.uid, context)
        FirebaseFirestore.getPrestamosReference(FirebaseAuthentication.getCurrentUser()!!.uid, year, month)
                .get()
                .addOnCompleteListener { task ->
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
                    }
                }.addOnFailureListener {
                    homePresenter.statusValorPrestamos(false, 0f, "Error al obtener préstamos")
                }
    }

    override fun getGastos(year: Int, month: Int) {
        val valorCotizacion = SharedPreferencesBD.getCotizacion(FirebaseAuthentication.getCurrentUser()!!.uid, context)
        FirebaseFirestore.getGastosReference(FirebaseAuthentication.getCurrentUser()!!.uid, year, month)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        var montototal = 0.0
                        var mesPago: Int
                        var yearPago: Int
                        for (document in task.result!!) {
                            val activo = document.getBoolean(Constants.BD_MES_ACTIVO)
                            if (activo == null || activo) {
                                Log.d(Constraints.TAG, document.id + " => " + document.data)
                                val montoDetal = document.getDouble(Constants.BD_MONTO)!!
                                val dolar = document.getBoolean(Constants.BD_DOLAR)!!
                                val tipoFrecuencia = document.getString(Constants.BD_TIPO_FRECUENCIA)
                                if (tipoFrecuencia != null) {
                                    val calendarPago = Calendar.getInstance()
                                    calendarPago.time = document.getDate(Constants.BD_FECHA_INCIAL)!!
                                    val duracionFrecuencia = document.getDouble(Constants.BD_DURACION_FRECUENCIA)!!
                                    val duracionFrecuenciaInt = duracionFrecuencia.toInt()
                                    mesPago = calendarPago[Calendar.MONTH]
                                    yearPago = calendarPago[Calendar.YEAR]

                                    while (mesPago <= month && yearPago == year) {
                                        if (mesPago == month) {
                                            montototal = if (dolar) {
                                                montototal + montoDetal
                                            } else {
                                                montototal + montoDetal / valorCotizacion
                                            }
                                        }

                                        when (tipoFrecuencia) {
                                            "Dias" -> {
                                                calendarPago.add(Calendar.DAY_OF_YEAR, duracionFrecuenciaInt)
                                            }
                                            "Semanas" -> {
                                                calendarPago.add(Calendar.DAY_OF_YEAR, duracionFrecuenciaInt * 7)
                                            }
                                            "Meses" -> {
                                                calendarPago.add(Calendar.MONTH, duracionFrecuenciaInt)
                                            }
                                        }
                                        mesPago = calendarPago[Calendar.MONTH]
                                        yearPago = calendarPago[Calendar.YEAR]
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
                }.addOnFailureListener {
                    homePresenter.statusValorGastos(false, 0f, "Error al obtener los Gastos")
                }
    }

    override fun moveDataNextYear(year: Int) {
        val user = FirebaseAuthentication.getCurrentUser()!!.uid
        FirebaseFirestore.getIngresosReference(user, year, 11)
                .get()
                .addOnCompleteListener { task->
                    if (task.isSuccessful) {
                        for (doc in task.result!!) {
                            val activo = doc.getBoolean(Constants.BD_MES_ACTIVO)
                            val tipoFrecuencia = doc.getString(Constants.BD_TIPO_FRECUENCIA)
                            if (activo == null || activo) {
                                if (tipoFrecuencia != null) {
                                    var mesC = 0
                                    var dayC = 0
                                    val calendar = Calendar.getInstance()
                                    val fechaFinal = doc.getDate(Constants.BD_FECHA_FINAL)
                                    if (fechaFinal != null) {
                                        calendar.time = fechaFinal
                                        mesC = calendar.get(Calendar.MONTH)
                                        dayC = calendar.get(Calendar.DAY_OF_MONTH)
                                    }

                                    if (fechaFinal == null || (mesC == 11 && dayC == 31)) {
                                        val calendarFinal = Calendar.getInstance()
                                        calendarFinal.set(year+1, 11, 31)
                                        var fechaInicial: Date? = null

                                        var mesInicial: Int
                                        var yearInicial: Int
                                        val calendarInicial = Calendar.getInstance()
                                        calendarInicial.time = doc.getDate(Constants.BD_FECHA_INCIAL)!!
                                        val duracionFrecuencia = doc.getDouble(Constants.BD_DURACION_FRECUENCIA)!!
                                        val duracionFrecuenciaInt = duracionFrecuencia.toInt()

                                        mesInicial = calendarInicial[Calendar.MONTH]
                                        yearInicial = calendarInicial[Calendar.YEAR]

                                        while (mesInicial <= 11 && yearInicial <= (year+1)) {
                                            if (yearInicial == (year+1)) {
                                                fechaInicial = calendarInicial.time
                                            }

                                            when(tipoFrecuencia) {
                                                "Dias" -> {
                                                    calendarInicial.add(Calendar.DAY_OF_YEAR, duracionFrecuenciaInt)
                                                }
                                                "Semanas" -> {
                                                    calendarInicial.add(Calendar.DAY_OF_YEAR, duracionFrecuenciaInt * 7)
                                                }
                                                "Meses" -> {
                                                    calendarInicial.add(Calendar.MONTH, duracionFrecuenciaInt)
                                                }
                                            }
                                            if (yearInicial != (year+1)) {
                                                mesInicial = calendarInicial[Calendar.MONTH]
                                                yearInicial = calendarInicial[Calendar.YEAR]
                                            } else {
                                                mesInicial = 13
                                            }
                                        }


                                        val docData: MutableMap<String, Any?> = HashMap()
                                        docData[Constants.BD_CONCEPTO] = doc.getString(Constants.BD_CONCEPTO)
                                        docData[Constants.BD_MONTO] = doc.getDouble(Constants.BD_MONTO)
                                        docData[Constants.BD_DOLAR] = doc.getBoolean(Constants.BD_DOLAR)
                                        docData[Constants.BD_DURACION_FRECUENCIA] = doc.getDouble(Constants.BD_DURACION_FRECUENCIA)
                                        docData[Constants.BD_TIPO_FRECUENCIA] = doc.getString(Constants.BD_TIPO_FRECUENCIA)
                                        docData[Constants.BD_MES_ACTIVO] = true
                                        docData[Constants.BD_FECHA_FINAL] = calendarFinal.time
                                        docData[Constants.BD_FECHA_INCIAL] = fechaInicial

                                        for (j in 0..11) {
                                            FirebaseFirestore.getIngresosReference(user, (year + 1), j).document(fechaInicial!!.time.toString())
                                                    .set(docData)
                                                    .addOnSuccessListener {  }
                                                    .addOnFailureListener {
                                                        homePresenter.statusMoveNextYear(false, "Error al copiar los Ingresos. Intente más tarde")
                                                    }
                                        }
                                    }
                                }
                            }
                        }
                        moveAhorros(user, year)
                    } else {
                        homePresenter.statusMoveNextYear(false, "Error al copiar los Ingresos. Intente más tarde")
                    }
                }
    }

    private fun moveAhorros(user: String, year: Int) {
        FirebaseFirestore.getAhorrosReference(user, year, 11)
                .get()
                .addOnCompleteListener { task->
                    if (task.isSuccessful) {
                        for (doc in task.result!!) {
                                val docData: MutableMap<String, Any?> = HashMap()
                                docData[Constants.BD_CONCEPTO] = doc.getString(Constants.BD_CONCEPTO)
                                docData[Constants.BD_MONTO] = doc.getDouble(Constants.BD_MONTO)
                                val fechaIngreso = doc.getDate(Constants.BD_FECHA_INGRESO)
                                docData[Constants.BD_FECHA_INGRESO] = fechaIngreso
                                docData[Constants.BD_DOLAR] = doc.getBoolean(Constants.BD_DOLAR)
                                docData[Constants.BD_ORIGEN] = doc.getString(Constants.BD_ORIGEN)

                                for (j in 0..11) {
                                    FirebaseFirestore.getAhorrosReference(user, (year + 1), j).document(fechaIngreso!!.time.toString())
                                            .set(docData)
                                            .addOnSuccessListener {  }
                                            .addOnFailureListener {
                                                homePresenter.statusMoveNextYear(false, "Error al copiar los Ahorros. Intente más tarde")
                                            }
                                }

                        }
                        movePrestamos(user, year)
                    } else {
                        homePresenter.statusMoveNextYear(false, "Error al copiar los Ahorros. Intente más tarde")
                    }
                }

    }

    private fun movePrestamos(user: String, year: Int) {
        FirebaseFirestore.getPrestamosReference(user, year, 11)
                .get()
                .addOnCompleteListener { task->
                    if (task.isSuccessful) {
                        for (doc in task.result!!) {
                                val docData: MutableMap<String, Any?> = HashMap()
                                docData[Constants.BD_CONCEPTO] = doc.getString(Constants.BD_CONCEPTO)
                                docData[Constants.BD_MONTO] = doc.getDouble(Constants.BD_MONTO)
                                val fechaIngreso = doc.getDate(Constants.BD_FECHA_INGRESO)
                                docData[Constants.BD_FECHA_INGRESO] = fechaIngreso
                                docData[Constants.BD_DOLAR] = doc.getBoolean(Constants.BD_DOLAR)


                                for (j in 0..11) {
                                    FirebaseFirestore.getPrestamosReference(user, (year + 1), j).document(fechaIngreso!!.time.toString())
                                            .set(docData)
                                            .addOnSuccessListener {  }
                                            .addOnFailureListener {
                                                homePresenter.statusMoveNextYear(false, "Error al copiar los Préstamos. Intente más tarde")
                                            }
                                }

                        }
                        moveGastos(user, year)
                    } else {
                        homePresenter.statusMoveNextYear(false, "Error al copiar los Préstamos. Intente más tarde")
                    }
                }
    }

    private fun moveGastos(user: String, year: Int) {
        FirebaseFirestore.getGastosReference(user, year, 11)
                .get()
                .addOnCompleteListener { task->
                    if (task.isSuccessful) {
                        for (doc in task.result!!) {
                            val activo = doc.getBoolean(Constants.BD_MES_ACTIVO)
                            val tipoFrecuencia = doc.getString(Constants.BD_TIPO_FRECUENCIA)
                            if (activo == null || activo) {
                                if (tipoFrecuencia != null) {
                                    var mesC = 0
                                    var dayC = 0
                                    val calendar = Calendar.getInstance()
                                    val fechaFinal = doc.getDate(Constants.BD_FECHA_FINAL)
                                    if (fechaFinal != null) {
                                        calendar.time = fechaFinal
                                        mesC = calendar.get(Calendar.MONTH)
                                        dayC = calendar.get(Calendar.DAY_OF_MONTH)
                                    }


                                    if (fechaFinal == null || (mesC == 11 && dayC == 31)) {
                                        val calendarFinal = Calendar.getInstance()
                                        calendarFinal.set(year+1, 11, 31)
                                        var fechaInicial: Date? = null

                                        var mesInicial: Int
                                        var yearInicial: Int
                                        val calendarInicial = Calendar.getInstance()
                                        calendarInicial.time = doc.getDate(Constants.BD_FECHA_INCIAL)!!
                                        val duracionFrecuencia = doc.getDouble(Constants.BD_DURACION_FRECUENCIA)!!
                                        val duracionFrecuenciaInt = duracionFrecuencia.toInt()

                                        mesInicial = calendarInicial[Calendar.MONTH]
                                        yearInicial = calendarInicial[Calendar.YEAR]

                                        while (mesInicial <= 11 && yearInicial <= (year+1)) {
                                            if (yearInicial == (year+1)) {
                                                fechaInicial = calendarInicial.time
                                            }

                                            when(tipoFrecuencia) {
                                                "Dias" -> {
                                                    calendarInicial.add(Calendar.DAY_OF_YEAR, duracionFrecuenciaInt)
                                                }
                                                "Semanas" -> {
                                                    calendarInicial.add(Calendar.DAY_OF_YEAR, duracionFrecuenciaInt * 7)
                                                }
                                                "Meses" -> {
                                                    calendarInicial.add(Calendar.MONTH, duracionFrecuenciaInt)
                                                }
                                            }
                                            if (yearInicial != (year+1)) {
                                                mesInicial = calendarInicial[Calendar.MONTH]
                                                yearInicial = calendarInicial[Calendar.YEAR]
                                            } else {
                                                mesInicial = 13
                                            }
                                        }


                                        val docData: MutableMap<String, Any?> = HashMap()
                                        docData[Constants.BD_CONCEPTO] = doc.getString(Constants.BD_CONCEPTO)
                                        docData[Constants.BD_MONTO] = doc.getDouble(Constants.BD_MONTO)
                                        docData[Constants.BD_DOLAR] = doc.getBoolean(Constants.BD_DOLAR)
                                        docData[Constants.BD_DURACION_FRECUENCIA] = doc.getDouble(Constants.BD_DURACION_FRECUENCIA)
                                        docData[Constants.BD_TIPO_FRECUENCIA] = doc.getString(Constants.BD_TIPO_FRECUENCIA)
                                        docData[Constants.BD_MES_ACTIVO] = true
                                        docData[Constants.BD_FECHA_FINAL] = calendarFinal.time
                                        docData[Constants.BD_FECHA_INCIAL] = fechaInicial

                                        for (j in 0..11) {
                                            FirebaseFirestore.getGastosReference(user, (year + 1), j).document(fechaInicial!!.time.toString())
                                                    .set(docData)
                                                    .addOnSuccessListener {  }
                                                    .addOnFailureListener {
                                                        homePresenter.statusMoveNextYear(false, "Error al copiar los Ingresos. Intente más tarde")
                                                    }
                                        }
                                    }
                                }
                            }
                        }
                        moveDeudas(user, year)
                    } else {
                        homePresenter.statusMoveNextYear(false, "Error al copiar los Gastos. Intente más tarde")
                    }
                }
    }

    private fun moveDeudas(user: String, year: Int) {
        FirebaseFirestore.getDeudasReference(user, year, 11)
                .get()
                .addOnCompleteListener { task->
                    if (task.isSuccessful) {
                        for (doc in task.result!!) {
                                    val docData: MutableMap<String, Any?> = HashMap()
                                    docData[Constants.BD_PRESTAMISTA] = doc.getString(Constants.BD_PRESTAMISTA)
                                    docData[Constants.BD_CONCEPTO] = doc.getString(Constants.BD_CONCEPTO)
                                    docData[Constants.BD_MONTO] = doc.getDouble(Constants.BD_MONTO)
                                    val fechaIngreso = doc.getDate(Constants.BD_FECHA_INGRESO)
                                    docData[Constants.BD_FECHA_INGRESO] = fechaIngreso
                                    docData[Constants.BD_DOLAR] = doc.getBoolean(Constants.BD_DOLAR)

                                    for (j in 0..11) {
                                        FirebaseFirestore.getDeudasReference(user, (year + 1), j).document(fechaIngreso!!.time.toString())
                                                .set(docData)
                                                .addOnSuccessListener {  }
                                                .addOnFailureListener {
                                                    homePresenter.statusMoveNextYear(false, "Error al copiar los Deudas. Intente más tarde")
                                                }
                                    }

                        }
                      homePresenter.statusMoveNextYear(true, "Elementos copiados exitosamente")
                    } else {
                        homePresenter.statusMoveNextYear(false, "Error al copiar los Deudas. Intente más tarde")
                    }
                }
    }

}