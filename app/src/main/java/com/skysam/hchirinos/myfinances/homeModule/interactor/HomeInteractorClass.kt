package com.skysam.hchirinos.myfinances.homeModule.interactor

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.skysam.hchirinos.myfinances.common.model.SharedPreferencesBD
import com.skysam.hchirinos.myfinances.common.model.firebase.Auth
import com.skysam.hchirinos.myfinances.common.model.firebase.FirebaseFirestore
import com.skysam.hchirinos.myfinances.common.utils.ClassesCommon
import com.skysam.hchirinos.myfinances.common.utils.Constants
import com.skysam.hchirinos.myfinances.homeModule.presenter.HomePresenter
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import java.security.KeyManagementException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
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
            val url = "http://www.bcv.org.ve/"

            withContext(Dispatchers.IO) {
                try {
                    val doc = Jsoup.connect(url).sslSocketFactory(socketFactory()).get()
                    val data = doc.select("div#dolar")
                    valor = data.select("strong").last()?.text()
                } catch (e: Exception) {
                    Log.e("Error", e.toString())
                }
            }
            if (valor != null) {
                val valorNeto = valor?.replace(",", ".")
                valorCotizacion = valorNeto?.toFloat()
                val valorRounded = String.format(Locale.US, "%.2f", valorCotizacion)
                valorCotizacion = valorRounded.toFloat()

                valor = ClassesCommon.convertFloatToString(valorCotizacion)
            }

            if (valor != null) {
                homePresenter.valorCotizacionWebOk(valor!!, valorCotizacion!!)
            } else {
                obtenerCotizacionShared()
            }
        }

    }

    private fun getStatusNotification() {
        val notificationStatus = SharedPreferencesBD.getFirstSubscribeMainTopic(context)
        if (!notificationStatus) {
            FirebaseMessaging.getInstance().subscribeToTopic(Constants.PREFERENCE_NOTIFICATION_MAIN_TOPIC)
                    .addOnSuccessListener {
                        SharedPreferencesBD.subscribeFirstMainTopicNotification(context)
                    }
        }
    }

    private fun obtenerCotizacionShared() {
        homePresenter.valorCotizacionWebError(SharedPreferencesBD.getCotizacion(context))
    }

    override fun guardarCotizacionShared(valorFloat: Float) {
        SharedPreferencesBD.saveCotizacion(context, valorFloat)
    }

    override fun moveDataNextYear(year: Int) {
        val user = Auth.uidCurrentUser()
        FirebaseFirestore.getIngresosReference(year, 11)
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
                                            FirebaseFirestore.getIngresosReference((year + 1), j).document(fechaInicial!!.time.toString())
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
        FirebaseFirestore.getAhorrosReference(year, 11)
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
                                    FirebaseFirestore.getAhorrosReference((year + 1), j).document(fechaIngreso!!.time.toString())
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
        FirebaseFirestore.getPrestamosReference(year, 11)
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
                                    FirebaseFirestore.getPrestamosReference((year + 1), j).document(fechaIngreso!!.time.toString())
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
        FirebaseFirestore.getGastosReference(year, 11)
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
                                        docData[Constants.BD_PAGADO] = false
                                        docData[Constants.BD_FECHA_FINAL] = calendarFinal.time
                                        docData[Constants.BD_FECHA_INCIAL] = fechaInicial

                                        for (j in 0..11) {
                                            FirebaseFirestore.getGastosReference((year + 1), j).document(fechaInicial!!.time.toString())
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
        FirebaseFirestore.getDeudasReference(year, 11)
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
                                        FirebaseFirestore.getDeudasReference((year + 1), j).document(fechaIngreso!!.time.toString())
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

    private fun socketFactory(): SSLSocketFactory {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        })

        try {
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            return sslContext.socketFactory
        } catch (e: Exception) {
            when (e) {
                is RuntimeException, is KeyManagementException -> {
                    throw RuntimeException("Failed to create a SSL socket factory", e)
                }
                else -> throw e
            }
        }
    }

}