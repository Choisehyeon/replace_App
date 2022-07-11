package com.example.replace_application.api

import com.example.replace_application.data.getadress.getAddress
import com.example.replace_application.data.getfindaddress.getFindadress
import com.example.replace_application.data.getkeyword.getKeyword
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KakaoApi {
    @GET("v2/local/geo/coord2address.json")
    fun getLocation(
        @Header("Authorization") key : String,
        @Query("x") longitude : String,
        @Query("y") latitude : String
    ): Call<getAddress>

    @GET("v2/local/search/address.json")
    fun getAddress(
        @Header("Authorization") key : String,
        @Query("query") address : String,
    ) : Call<getFindadress>

    @GET("v2/local/search/keyword.json")
    fun getKeyword(
        @Header("Authorization") key : String,
        @Query("query") keyword : String,
        @Query("x") longitude : String,
        @Query("y") latitude : String,
        @Query("radius") distance : Int
    ) : Call<getKeyword>
}