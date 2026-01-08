package com.skysam.hchirinos.myfinances.homeModule.interactor

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by Hector Chirinos on 19/11/2024.
 */

object RetrofitClientDolarVzla {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.dolarvzla.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: DolarVzlaService = retrofit.create(DolarVzlaService::class.java)
}

object RetrofitClientDolarApiVe {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://ve.dolarapi.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: DolarApiVeService = retrofit.create(DolarApiVeService::class.java)
}