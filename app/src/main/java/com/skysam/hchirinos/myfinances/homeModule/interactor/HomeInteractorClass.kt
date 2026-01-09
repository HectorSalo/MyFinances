package com.skysam.hchirinos.myfinances.homeModule.interactor

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.skysam.hchirinos.myfinances.common.model.SharedPreferencesBD
import com.skysam.hchirinos.myfinances.common.model.constructores.ExchangeRateListResponse
import com.skysam.hchirinos.myfinances.common.model.firebase.Auth
import com.skysam.hchirinos.myfinances.common.model.firebase.FirebaseFirestore
import com.skysam.hchirinos.myfinances.common.utils.ClassesCommon
import com.skysam.hchirinos.myfinances.common.utils.Constants
import com.skysam.hchirinos.myfinances.homeModule.presenter.HomePresenter
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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

        // 1) BCV + EURO (api.dolarvzla)
        RetrofitClientDolarVzla.service.getExchangeRate()
            .enqueue(object : Callback<DolarVzlaExchangeRateResponse> {

                override fun onResponse(
                    call: Call<DolarVzlaExchangeRateResponse>,
                    response: Response<DolarVzlaExchangeRateResponse>
                ) {
                    Log.i("Rates", "DolarVzla code=${response.code()} ok=${response.isSuccessful}")

                    if (!response.isSuccessful) {
                        val err = try { response.errorBody()?.string() } catch (e: Exception) { "errorBody-exception=$e" }
                        Log.e("Rates", "DolarVzla errorBody=$err")
                        fallbackAll()
                        return
                    }

                    val body = response.body()
                    Log.i("Rates", "DolarVzla body=$body")

                    if (body == null) {
                        Log.e("Rates", "DolarVzla body == null (posible parse/converter)")
                        fallbackAll()
                        return
                    }

                    val bcvCurrent = body.current.usd.toFloat()
                    val bcvPrev = body.previous.usd.toFloat()
                    val eurCurrent = body.current.eur.toFloat()
                    val eurPrev = body.previous.eur.toFloat()
                    val dateBcv = body.current.date

                    // Guardar Euro actual YA (y opcionalmente BCV aquí si quieres)
                    SharedPreferencesBD.saveCotizacionEuro(context, eurCurrent)

                    // 2) Paralelo (ve.dolarapi)
                    val paraleloOld = SharedPreferencesBD.getCotizacionParalelo(context)

                    RetrofitClientDolarApiVe.service.getParalelo()
                        .enqueue(object : Callback<DolarApiParaleloResponse> {

                            override fun onResponse(
                                call: Call<DolarApiParaleloResponse>,
                                response: Response<DolarApiParaleloResponse>
                            ) {
                                val pBody = response.body()
                                Log.i("Rates", "Paralelo code=${response.code()} ok=${response.isSuccessful}")
                                Log.i("Rates", "Paralelo body=$pBody")

                                // --- Caso 1: Paralelo OK ---
                                if (response.isSuccessful && pBody != null) {
                                    val paraleloCurrent = pBody.promedio.toFloat()
                                    val dateParalelo = pBody.fechaActualizacion

                                    homePresenter.valorCotizacionWebOk(
                                        bcvCurrent, bcvPrev,
                                        paraleloCurrent, paraleloOld,
                                        eurCurrent, eurPrev,
                                        dateBcv,
                                        dateParalelo
                                    )

                                    // Guardar SIEMPRE nuevos (BCV + Paralelo + Euro)
                                    homePresenter.guardarCotizacionShared(bcvCurrent, paraleloCurrent, eurCurrent)
                                    return
                                }

                                // --- Caso 2: Paralelo falla => parcial con paralelo guardado ---
                                val err = try { response.errorBody()?.string() } catch (e: Exception) { "errorBody-exception=$e" }
                                Log.e("Rates", "Paralelo errorBody=$err")

                                val paraleloSaved = paraleloOld

                                homePresenter.valorCotizacionWebOk(
                                    bcvCurrent, bcvPrev,
                                    paraleloSaved, paraleloOld, // delta 0
                                    eurCurrent, eurPrev,
                                    dateBcv,
                                    "" // sin fecha del paralelo
                                )

                                // Guardar BCV + paralelo (guardado) + Euro (actual)
                                homePresenter.guardarCotizacionShared(bcvCurrent, paraleloSaved, eurCurrent)
                            }

                            override fun onFailure(call: Call<DolarApiParaleloResponse>, t: Throwable) {
                                Log.e("Rates", "Paralelo onFailure", t)

                                // Parcial: BCV/EUR de red + Paralelo desde prefs
                                val paraleloSaved = paraleloOld

                                homePresenter.valorCotizacionWebOk(
                                    bcvCurrent, bcvPrev,
                                    paraleloSaved, paraleloOld,
                                    eurCurrent, eurPrev,
                                    dateBcv,
                                    ""
                                )

                                homePresenter.guardarCotizacionShared(bcvCurrent, paraleloSaved, eurCurrent)
                            }
                        })
                }

                override fun onFailure(call: Call<DolarVzlaExchangeRateResponse>, t: Throwable) {
                    Log.e("Rates", "DolarVzla onFailure", t)
                    fallbackAll()
                }
            })
    }
    private fun fallbackAll() {
        val bcv = SharedPreferencesBD.getCotizacion(context)
        val paralelo = SharedPreferencesBD.getCotizacionParalelo(context)
        val euro = SharedPreferencesBD.getCotizacionEuro(context)
        homePresenter.valorCotizacionWebError(bcv, paralelo, euro)
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

    override fun guardarCotizacionShared(valorBCV: Float, valorParalelo: Float, valorEuro: Float) {
        SharedPreferencesBD.saveCotizacion(context, valorBCV, valorParalelo, valorEuro)
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

    override fun obtenerHistorialTasas(from: String, to: String) {
        RetrofitClientDolarVzla.service.getExchangeRateHistory(from, to)
            .enqueue(object : Callback<ExchangeRateListResponse> {

                override fun onResponse(
                    call: Call<ExchangeRateListResponse>,
                    response: Response<ExchangeRateListResponse>
                ) {
                    Log.i("RatesHistory", "code=${response.code()} ok=${response.isSuccessful}")

                    if (!response.isSuccessful) {
                        val err = try { response.errorBody()?.string() }
                        catch (e: Exception) { "errorBody-exception=$e" }

                        Log.e("RatesHistory", "errorBody=$err")
                        homePresenter.historialTasasResult(
                            RatesHistoryResult.Error("HTTP ${response.code()}", response.code())
                        )
                        return
                    }

                    val body = response.body()
                    if (body == null) {
                        Log.e("RatesHistory", "body == null (posible parse/converter)")
                        homePresenter.historialTasasResult(RatesHistoryResult.Error("Respuesta vacía"))
                        return
                    }

                    val items = body.rates
                        .filter { it.date.isNotBlank() }
                        .sortedByDescending { it.date } // ISO "YYYY-MM-DD" ordena bien

                    if (items.isEmpty()) {
                        homePresenter.historialTasasResult(RatesHistoryResult.Empty)
                    } else {
                        homePresenter.historialTasasResult(RatesHistoryResult.Success(items))
                    }
                }

                override fun onFailure(call: Call<ExchangeRateListResponse>, t: Throwable) {
                    Log.e("RatesHistory", "onFailure", t)
                    homePresenter.historialTasasResult(
                        RatesHistoryResult.Error(t.message ?: "Error de conexión")
                    )
                }
            })
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

}