package com.skysam.hchirinos.myfinances.homeModule.interactor

import retrofit2.Call
import retrofit2.http.GET

/**
 * Created by Hector Chirinos on 19/11/2024.
 */

interface ApiService {
 @GET("api/v1/dollar")
 fun getCotizacion(): Call<ApiResponse>

}