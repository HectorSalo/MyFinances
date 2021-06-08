package com.skysam.hchirinos.myfinances.graficosModule.interactor

import android.util.Log
import androidx.constraintlayout.widget.Constraints
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.firestore.QuerySnapshot
import com.skysam.hchirinos.myfinances.common.MyFinancesApp
import com.skysam.hchirinos.myfinances.common.model.SharedPreferencesBD
import com.skysam.hchirinos.myfinances.common.model.firebase.Auth
import com.skysam.hchirinos.myfinances.common.model.firebase.FirebaseFirestore
import com.skysam.hchirinos.myfinances.common.utils.Constants
import com.skysam.hchirinos.myfinances.graficosModule.presenter.DeudasGraphPresenter

class DeudasGraphInteractorClass(private val deudasGraphPresenter: DeudasGraphPresenter): DeudasGraphInteractor {
    override fun getMes(year: Int, month: Int) {
        val valorCotizacion = SharedPreferencesBD.getCotizacion(Auth.getCurrentUser()!!.uid, MyFinancesApp.MyFinancesAppObject.getContext())
        FirebaseFirestore.getDeudasReference(Auth.getCurrentUser()!!.uid, year, month)
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
                        deudasGraphPresenter.statusMes(month, true, montototal.toFloat(), montototal.toString())
                    } else {
                        deudasGraphPresenter.statusMes(month, true, 0f, "")
                    }
                }).addOnFailureListener(OnFailureListener {
                    deudasGraphPresenter.statusMes(month, false, 0f, "Error al obtener datos. Intente nuevamente")
                })
    }
}