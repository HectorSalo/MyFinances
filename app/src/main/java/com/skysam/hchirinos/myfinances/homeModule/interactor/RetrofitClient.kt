package com.skysam.hchirinos.myfinances.homeModule.interactor

import com.skysam.hchirinos.myfinances.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by Hector Chirinos on 19/11/2024.
 */

object RetrofitClientDolarVzla {
    private const val BASE_URL = "https://api.dolarvzla.com/"
    private const val HEADER_KEY = "x-dolarvzla-key"
    private const val API_KEY = BuildConfig.DOLARVZLA_API_KEY

    private val okHttpClient = okhttp3.OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val builder = original.newBuilder()

            if (API_KEY.isNotBlank()) {
                builder.addHeader(HEADER_KEY, API_KEY)
            }

            chain.proceed(builder.build())
        }
        .build()
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
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