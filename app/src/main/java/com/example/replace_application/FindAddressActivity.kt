package com.example.replace_application

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.replace_application.api.KakaoApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FindAddressActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_address)
    }

    private suspend fun getFindAddress(text: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(MainActivity.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(KakaoApi::class.java)

        val call = api.getAddress(MainActivity.API_KEY, text)

        val mapResponse = call.execute().body()

        val size = mapResponse!!.documents.size

        if(size != 0) {
            for(i in 0 until size) {
               // DocumentItems.add(mapResponse.documents[i])
            }
        }
    }
}