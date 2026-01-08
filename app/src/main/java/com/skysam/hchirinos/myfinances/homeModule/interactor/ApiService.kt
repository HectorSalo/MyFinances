package com.skysam.hchirinos.myfinances.homeModule.interactor

import retrofit2.Call
import retrofit2.http.GET

/**
 * Created by Hector Chirinos on 19/11/2024.
 */

interface DolarVzlaService {
    @GET("public/exchange-rate")
    fun getExchangeRate(): Call<DolarVzlaExchangeRateResponse>
}

interface DolarApiVeService {
    @GET("v1/dolares/paralelo")
    fun getParalelo(): Call<DolarApiParaleloResponse>
}