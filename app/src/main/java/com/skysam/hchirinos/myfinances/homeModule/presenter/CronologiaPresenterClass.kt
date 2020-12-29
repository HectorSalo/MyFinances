package com.skysam.hchirinos.myfinances.homeModule.presenter

import com.skysam.hchirinos.myfinances.common.model.constructores.ItemCronologiaConstructor
import com.skysam.hchirinos.myfinances.homeModule.interactor.CronologiaInteractor
import com.skysam.hchirinos.myfinances.homeModule.interactor.CronologiaInteractorClass
import com.skysam.hchirinos.myfinances.homeModule.ui.CronologiaView

class CronologiaPresenterClass(private val cronologiaView: CronologiaView): CronologiaPresenter {
    private val cronologiaInteractor: CronologiaInteractor = CronologiaInteractorClass(this)

    override fun getCronologia(month: Int, year: Int) {
        cronologiaInteractor.getCronologia(month, year)
    }

    override fun listCronologia(list: ArrayList<ItemCronologiaConstructor>) {
        cronologiaView.listCronologia(list)
    }
}