package com.skysam.hchirinos.myfinances.homeModule.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.skysam.hchirinos.myfinances.homeModule.repositories.MainRepository

/**
 * Created by Hector Chirinos on 26/07/2021.
 */
class MainViewModel: ViewModel() {
    val amountIngresos: LiveData<Double> = MainRepository.getIngresos().asLiveData()
}