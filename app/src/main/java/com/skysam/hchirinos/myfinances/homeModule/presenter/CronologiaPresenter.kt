package com.skysam.hchirinos.myfinances.homeModule.presenter

import com.skysam.hchirinos.myfinances.common.model.constructores.ItemCronologiaConstructor

interface CronologiaPresenter {
    fun getCronologia(month: Int, year: Int)

    fun listCronologia(list: ArrayList<ItemCronologiaConstructor>)
}