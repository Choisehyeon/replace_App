package com.example.replace_application.api

import com.example.replace_application.resultmap.ResultMap
import retrofit2.Call

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NaverApi {
    @GET("v2/gc")
    fun getAddress (
        @Header("X-NCP-APIGW-API-KEY-ID") apiKeyID: String,
        @Header("X-NCP-APIGW-API-KEY") apiKey: String,
        @Query("request") request: String,
        @Query("coords") coords: String,
        @Query("sources") sources: String,
        @Query("orders") orders : String,
        @Query("output") output:String
    ) : Call<ResultMap>

}