package com.example.replace_application

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import com.example.replace_application.adapter.AddressLVAdapter
import com.example.replace_application.adapter.KeywordLVAdapter
import com.example.replace_application.api.KakaoApi
import com.example.replace_application.api.NaverApi
import com.example.replace_application.data.getadress.getAddress
import com.example.replace_application.data.getfindaddress.RoadAddress
import com.example.replace_application.data.getfindaddress.getFindadress
import com.example.replace_application.data.getkeyword.Document
import com.example.replace_application.databinding.ActivityMainBinding
import com.example.replace_application.resultmap.ResultMap
import com.example.replace_application.model.AddressModel
import com.example.replace_application.utils.FBAuth
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.widget.LocationButtonView
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.coroutines.*
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
    lateinit var geocoder: Geocoder
    val DocumentItems = mutableListOf<Document>()
    val KeywordItems = mutableListOf<com.example.replace_application.data.getkeyword.Document>()
    private lateinit var binding: ActivityMainBinding


    companion object {

        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000

        /*val API_KEY_ID = "4mnlwhbnbf"
         val API_KEY = "0s1NLYMxFqAkOROmp2PHNs3W9gxiAifoct6qaCpv"*/
        const val BASE_URL = "https://dapi.kakao.com/"
        const val API_KEY = "KakaoAK 640fb6c53b577568126ee0e851cecce0"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)


        findViewById<TextView>(R.id.logoutBtn).setOnClickListener {
            Firebase.auth.signOut()
            val intent = Intent(this, IntroActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        findViewById<Button>(R.id.connectCoupleBtn).setOnClickListener {
            val intent = Intent(this, FindCoupleActivity::class.java)
            startActivity(intent)
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

        binding.slideFrame.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
        binding.addressListArea.visibility = View.INVISIBLE

        binding.back.visibility = View.INVISIBLE





        binding.findAddress.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                if (p1?.action == MotionEvent.ACTION_DOWN) {
                    binding.findAddress.isCursorVisible = true
                    binding.slideFrame.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
                    binding.menu.visibility = View.INVISIBLE
                    binding.back.visibility = View.VISIBLE

                }

                return false
            }
        })


        binding.back.setOnClickListener {
            binding.findAddress.setText(null)
            binding.addressListArea.visibility = View.INVISIBLE
            binding.menu.visibility = View.VISIBLE
            binding.back.visibility = View.INVISIBLE
            binding.findAddress.isCursorVisible = false
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.findAddress.windowToken, 0)
        }

        binding.menu.setOnClickListener {
            binding.drawerLayout.openDrawer(findViewById(R.id.main_drawer))
        }

        binding.writeBtn.setOnClickListener {
            val intent = Intent(this, BoardWriteActivity::class.java)
            intent.putExtra("place", binding.placeName.text.toString())
            intent.putExtra("road", binding.roadAddress.text.toString())
            startActivity(intent)
        }


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


    @SuppressLint("MissingPermission")
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        val uiSettings = naverMap.uiSettings
        uiSettings.isZoomGesturesEnabled = true
        uiSettings.isZoomControlEnabled = false
        uiSettings.setLocationButtonEnabled(false)
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow)
        var currentButton = findViewById<LocationButtonView>(R.id.currentBtn)

        val lm : LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val current : Location?= lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)


        naverMap.addOnLocationChangeListener { location ->
            lat = location.latitude
            lng = location.longitude
        }


        var cameraUpdate = CameraUpdate.scrollTo(LatLng(lat, lng))
        naverMap.moveCamera(cameraUpdate)


        currentButton.map = naverMap

        var findMarker = Marker()


        val findAddress = findViewById<EditText>(R.id.findAddress)

        findAddress.addTextChangedListener(object : TextWatcher {


            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                KeywordItems.clear()
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                Log.d("mm", binding.findAddress.text.toString())
                if(!binding.findAddress.text.toString().equals(""))
                {
                    GlobalScope.launch(Dispatchers.IO) {

                        getKeyword(binding.findAddress.text.toString(), current?.latitude.toString(), current?.longitude.toString())

                        launch(Dispatchers.Main) {

                            binding.addressListArea.visibility = View.VISIBLE

                            val adapter = KeywordLVAdapter(KeywordItems, binding.findAddress.text.toString())
                            binding.addressRV.adapter = adapter
                            adapter.notifyDataSetChanged()

                        }
                    }
                }


            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })




        findAddress.setOnEditorActionListener { textView, action, event ->
            KeywordItems.clear()
            var handled = false
            if (action == EditorInfo.IME_ACTION_DONE) {
                val cor = geocoder.getFromLocationName(findAddress.text.toString(), 1)
                Log.d("mm", cor.toString())

                if(cor != null) {
                    findMarker.position = LatLng(cor[0].latitude, cor[0].longitude)
                    findMarker.map = naverMap
                    cameraUpdate =
                        CameraUpdate.scrollAndZoomTo(LatLng(cor[0].latitude, cor[0].longitude), 15.0)
                            .animate(CameraAnimation.Fly, 3000)
                    naverMap.moveCamera(cameraUpdate)
                    binding.menu.visibility = View.VISIBLE
                    binding.back.visibility = View.INVISIBLE
                    binding.addressListArea.visibility = View.INVISIBLE
                    binding.findAddress.isCursorVisible = false
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.findAddress.windowToken, 0)

                }

                GlobalScope.launch(Dispatchers.IO) {
                    getKeyword(binding.findAddress.text.toString(), current?.latitude.toString(), current?.longitude.toString())


                    launch(Dispatchers.Main) {
                        binding.placeName.text = KeywordItems[0].place_name
                        binding.roadAddress.text = KeywordItems[0].road_address_name

                        binding.slideFrame.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
                    }
                }
                handled = true
            }
            handled
        }

        naverMap.setOnMapClickListener { pointF, latLng ->
            val latitude = latLng.latitude
            val longitude = latLng.longitude


            Log.d("main", "$longitude,$latitude")


            //코루틴 사용
            GlobalScope.launch(Dispatchers.IO) {
                var (road, building) = getLoadAddress("$longitude", "$latitude")
                getKeyword(road, "$longitude", "$latitude")
                var place = ""
                if(KeywordItems.size != 0)
                {
                    place = KeywordItems[0].place_name.toString()
                }

                if (place.equals("")) {
                    launch(Dispatchers.Main) {
                        findMarker.map = null
                        binding.slideFrame.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
                    }
                } else {
                    launch(Dispatchers.Main) {
                        findMarker.position = LatLng(latitude, longitude)
                        findMarker.map = naverMap
                        binding.placeName.text = place
                        binding.roadAddress.text = road


                        binding.slideFrame.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
                        cameraUpdate = CameraUpdate.scrollTo(LatLng(latitude, longitude))
                        naverMap.moveCamera(cameraUpdate)
                    }
                }

            }

        }

        binding.addressRV.setOnItemClickListener { adapterView, view, position, id ->
            val item = adapterView.getItemAtPosition(position) as Document
            findMarker.position = LatLng(item.y.toDouble(), item.x.toDouble())
            findMarker.map = naverMap
            cameraUpdate =
                CameraUpdate.scrollAndZoomTo(LatLng(item.y.toDouble(), item.x.toDouble()), 15.0)
                    .animate(CameraAnimation.Fly, 3000)
            naverMap.moveCamera(cameraUpdate)
            binding.addressListArea.visibility = View.INVISIBLE
            binding.back.visibility = View.INVISIBLE
            binding.menu.visibility = View.VISIBLE

            binding.findAddress.setText(null)
            binding.findAddress.isCursorVisible = false
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.findAddress.windowToken, 0)


            binding.placeName.text = item.place_name
            binding.roadAddress.text = item.road_address_name
            binding.slideFrame.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED

        }

        findMarker.setOnClickListener {

            true
        }
    }

    private suspend fun getLoadAddress(longitude: String, latitude: String): Pair<String, String> {
        Log.d("MainActivity", longitude)
        Log.d("MainActivity", latitude)
        var roadAddress: String = ""
        var buildingName: String = ""
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(KakaoApi::class.java)

        val call = api.getLocation(API_KEY, longitude, latitude)

        val mapResponse = call.execute().body()


        if (mapResponse!!.documents[0].road_address != null) {
            roadAddress = mapResponse!!.documents[0].road_address.address_name
            buildingName = mapResponse.documents[0].road_address.building_name
        } else {
            roadAddress = ""
            buildingName = ""
        }

        return Pair(roadAddress, buildingName)
    }

    private suspend fun getFindAddress(text: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(KakaoApi::class.java)

        val call = api.getAddress(API_KEY, text)

        val mapResponse = call.execute().body()

        if(mapResponse != null) {
            val size = mapResponse!!.documents.size

            if(size != 0) {
                for(i in 0 until size) {
                    // DocumentItems.add(mapResponse.documents[i])
                }
            }
        }
    }

    private suspend fun getKeyword(text: String, latitude: String, longitude: String) {
        KeywordItems.clear()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(KakaoApi::class.java)

        val call = api.getKeyword(API_KEY, text, longitude, latitude, 20000, "distance")

        val mapResponse = call.execute().body()

        Log.d("main", mapResponse.toString())

        if(mapResponse != null) {

            val size = mapResponse!!.documents.size

            if (size != 0) {
                for (i in 0 until size) {
                    KeywordItems.add(mapResponse.documents[i])
                }
            }
        }

    }


}




