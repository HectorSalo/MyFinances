package com.skysam.hchirinos.myfinances.homeModule.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.skysam.hchirinos.myfinances.common.model.constructores.ItemCronologiaConstructor
import com.skysam.hchirinos.myfinances.homeModule.repositories.CronologyRepository
import com.skysam.hchirinos.myfinances.homeModule.repositories.HomeRepository

/**
 * Created by Hector Chirinos on 26/07/2021.
 */
class MainViewModel: ViewModel() {
    val amountIngresos: LiveData<Double> = HomeRepository.getIngresos().asLiveData()
    val amountGastos: LiveData<Double> = HomeRepository.getGastos().asLiveData()
    val amountAhorros: LiveData<Double> = HomeRepository.getAhorros().asLiveData()
    val amountCapital: LiveData<Double> = HomeRepository.getCapital().asLiveData()
    val amountPrestamos: LiveData<Double> = HomeRepository.getPrestamos().asLiveData()
    val amountDeudas: LiveData<Double> = HomeRepository.getDeudas().asLiveData()
    val amountGastosNoFijos: LiveData<Double> = HomeRepository.getGastosNoFijos().asLiveData()

    val listIngresos: LiveData<List<ItemCronologiaConstructor>> = CronologyRepository.getIngresos().asLiveData()
    val listGastos: LiveData<List<ItemCronologiaConstructor>> = CronologyRepository.getGastos().asLiveData()
}