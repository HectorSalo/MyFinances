package com.skysam.hchirinos.myfinances.graficosModule.interactor

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.firestore.QuerySnapshot
import com.skysam.hchirinos.myfinances.common.MyFinancesApp
import com.skysam.hchirinos.myfinances.common.model.SharedPreferencesBD
import com.skysam.hchirinos.myfinances.common.model.firebase.FirebaseAuthentication
import com.skysam.hchirinos.myfinances.common.model.firebase.FirebaseFirestore
import com.skysam.hchirinos.myfinances.common.utils.Constants
import com.skysam.hchirinos.myfinances.graficosModule.presenter.AhorrosGraphPresenter
import java.util.*

class AhorrosGraphInteractorClass(private val ahorrosGraphPresenter: AhorrosGraphPresenter): AhorrosGraphInteractor {
    override fun getMes(year: Int, month: Int) {
        val valorCotizacion = SharedPreferencesBD.getCotizacion(FirebaseAuthentication.getCurrentUser()!!.uid, MyFinancesApp.MyFinancesAppObject.getContext())
        FirebaseFirestore.getAhorrosReference(FirebaseAuthentication.getCurrentUser()!!.uid, year, month)
                .get()
                .addOnCompleteListener(OnCompleteListener<QuerySnapshot?> { task ->
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
                        ahorrosGraphPresenter.statusMes(month, true, montototal.toFloat(), montototal.toString())
                    } else {
                        ahorrosGraphPresenter.statusMes(month, true, 0f, "")
                    }
                }).addOnFailureListener(OnFailureListener {
                    ahorrosGraphPresenter.statusMes(month, false, 0f, "Error al obtener datos. Intente nuevamente")
                })
    }

}