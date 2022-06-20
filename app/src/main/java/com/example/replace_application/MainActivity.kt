package com.example.replace_application

import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import com.example.replace_application.api.NaverApi
import com.example.replace_application.resultmap.ResultMap
import com.example.replace_application.databinding.ActivityMainBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.widget.LocationButtonView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap
    var lat: Double = 0.0
    var lng: Double = 0.0
    lateinit var geocoder : Geocoder
    var mapAddress : String = ""
    private lateinit var binding : ActivityMainBinding

    companion object {

        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        val API_KEY_ID = "4mnlwhbnbf"
        val API_KEY = "0s1NLYMxFqAkOROmp2PHNs3W9gxiAifoct6qaCpv"
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.menu.setOnClickListener {

        }

        geocoder = Geocoder(this)

        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.mapView) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.mapView, it).commit()
            }

        mapFragment.getMapAsync(this)

        locationSource =
            FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions,
                grantResults
            )
        ) {
            if (!locationSource.isActivated) { // 권한 거부됨
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        val uiSettings = naverMap.uiSettings
        uiSettings.isZoomGesturesEnabled = true
        uiSettings.isZoomControlEnabled = true
        uiSettings.setLocationButtonEnabled(false)
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow)
        var currentButton = findViewById<LocationButtonView>(R.id.currentBtn)

        naverMap.addOnLocationChangeListener { location ->
            lat = location.latitude
            lng = location.longitude
        }


        var cameraUpdate = CameraUpdate.scrollTo(LatLng(lat, lng))
        naverMap.moveCamera(cameraUpdate)


        currentButton.map = naverMap

        var findMarker = Marker()


        val findAddress = findViewById<EditText>(R.id.findAddress)
        findAddress.setOnEditorActionListener { textView, action, event ->
            var handled = false
            if (action == EditorInfo.IME_ACTION_DONE) {
                val cor = geocoder.getFromLocationName(findAddress.text.toString(), 1)
                findMarker.position = LatLng(cor[0].latitude, cor[0].longitude)
                findMarker.map = naverMap
                cameraUpdate =
                    CameraUpdate.scrollAndZoomTo(LatLng(cor[0].latitude, cor[0].longitude), 15.0)
                        .animate(CameraAnimation.Fly, 3000)
                naverMap.moveCamera(cameraUpdate)
                findAddress.setText(null)
                handled = true
            }
            handled
        }

        naverMap.setOnMapClickListener { pointF, latLng ->
            val latitude = latLng.latitude
            val longitude = latLng.longitude

            getLoadAddress("$longitude,$latitude")
            Log.d("main","$longitude,$latitude")

            findMarker.position = LatLng(latitude, longitude)
            findMarker.map = naverMap
            cameraUpdate = CameraUpdate.scrollTo(LatLng(latitude, longitude))
            naverMap.moveCamera(cameraUpdate)

        }
    }

    fun getLoadAddress(coord: String) {
        Log.d("main", coord)
        var check: Boolean = false
        val retrofit = Retrofit.Builder()
            .baseUrl("https://naveropenapi.apigw.ntruss.com/map-reversegeocode/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(NaverApi::class.java)

        val call = api.getAddress(API_KEY_ID, API_KEY,"coordsToaddr" ,coord, "epsg:4326", "addr,roadaddr", "json")
        call.enqueue(object : Callback<ResultMap> {
            override fun onResponse(call: Call<ResultMap>, response: Response<ResultMap>) {
                if(response.code() == 200) {
                    val mapResponse = response.body()
                    Log.d("MainActivity", "result: " + mapResponse.toString())
                    if(mapResponse!!.status.code == 0) {
                        mapAddress = mapResponse.results[0].region.area1.name + " " + mapResponse.results[0].region.area2.name + " " + mapResponse.results[0].region.area3.name

                        if(mapResponse.results.size == 1) {
                            mapAddress += " " + mapResponse.results[0].land.number1
                            if(mapResponse.results[0].land.number2.length != 0) {
                                mapAddress += "-" + mapResponse.results[0].land.number2
                            }
                        } else {
                            mapAddress += " " + mapResponse.results[0].land.number1
                            if(mapResponse.results[0].land.number2.length != 0) {
                                mapAddress += "-" + mapResponse.results[0].land.number2
                            }
                            mapAddress += "\n" + mapResponse.results[1].land.addition0.value
                            check = !mapResponse.results[1].land.addition0.value.equals("")
                        }


                        Log.d("main", mapAddress)
                        Log.d("main", check.toString())
                    }
                }
            }

            override fun onFailure(call: Call<ResultMap>, t: Throwable) {
                Log.d("MainActivity", "result :" + t.message)
            }

        })

    }

}
