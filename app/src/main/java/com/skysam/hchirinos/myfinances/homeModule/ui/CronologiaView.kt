package com.skysam.hchirinos.myfinances.homeModule.ui

import com.skysam.hchirinos.myfinances.common.model.constructores.ItemCronologiaConstructor

interface CronologiaView {
    fun listCronologia(list: ArrayList<ItemCronologiaConstructor>)
}