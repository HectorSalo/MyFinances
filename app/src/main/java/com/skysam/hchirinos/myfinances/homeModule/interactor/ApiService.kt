package com.skysam.hchirinos.myfinances.homeModule.interactor

import com.skysam.hchirinos.myfinances.common.model.constructores.ExchangeRateListResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by Hector Chirinos on 19/11/2024.
 */

interface DolarVzlaService {
    @GET("public/exchange-rate")
    fun getExchangeRate(): Call<DolarVzlaExchangeRateResponse>

    @GET("public/exchange-rate/list")
    fun getExchangeRateHistory(
        @Query("from") from: String? = null, // "YYYY-MM-DD"
        @Query("to") to: String? = null      // "YYYY-MM-DD"
    ): Call<ExchangeRateListResponse>
}

interface DolarApiVeService {
    @GET("v1/dolares/paralelo")
    fun getParalelo(): Call<DolarApiParaleloResponse>
}