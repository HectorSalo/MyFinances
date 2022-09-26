package com.skysam.hchirinos.myfinances.graficosModule.interactor

import android.util.Log
import androidx.constraintlayout.widget.Constraints
import com.skysam.hchirinos.myfinances.common.MyFinancesApp
import com.skysam.hchirinos.myfinances.common.model.SharedPreferencesBD
import com.skysam.hchirinos.myfinances.common.model.firebase.FirebaseFirestore
import com.skysam.hchirinos.myfinances.common.utils.Constants
import com.skysam.hchirinos.myfinances.graficosModule.presenter.PrestamosGraphPresenter

class PrestamosGraphInteractorClass(private val prestamosGraphPresenter: PrestamosGraphPresenter): PrestamosGraphInteractor {
    override fun getMes(year: Int, month: Int) {
        val valorCotizacion = SharedPreferencesBD.getCotizacion(MyFinancesApp.MyFinancesAppObject.getContext())
        FirebaseFirestore.getPrestamosReference(year, month)
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
                        prestamosGraphPresenter.statusMes(
                            month,
                            true,
                            montototal.toFloat(),
                            montototal.toString()
                        )
                    } else {
                        prestamosGraphPresenter.statusMes(month, true, 0f, "")
                    }
                }.addOnFailureListener {
                prestamosGraphPresenter.statusMes(
                    month,
                    false,
                    0f,
                    "Error al obtener datos. Intente nuevamente"
                )
            }
    }
}