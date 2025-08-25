package com.skysam.hchirinos.myfinances.homeModule.interactor

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by Hector Chirinos on 19/11/2024.
 */

object RetrofitClient {
 private const val BASE_URL = "https://ve.dolarapi.com/"

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)
    }
}