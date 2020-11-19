package com.example.dustapi

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.*


import kotlinx.serialization.*
import kotlinx.serialization.json.*
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import java.io.IOException
import java.util.*



class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback


    private fun initLocation(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION),111)
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener {  location:Location? ->
                if (location == null){
                    Log.e("실패", "location get fail")
                }
                else{
                    Log.d("성공", "location succeed")
                }
            }
            .addOnFailureListener{
                Log.e("실패", "location error is ${it.message}")
                it.printStackTrace()
            }
    }

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://openapi.airkorea.or.kr/openapi/services/rest/ArpltnInforInqireSvc/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    fun MiseStatus( status:String) {
        when(status){
            "1"-> tv_Status.text = "좋음"
            "2"-> tv_Status.text = "보통"
            "3"-> tv_Status.text = "나쁨"
            "4"-> tv_Status.text = "매우 나쁨"
        }

    }
    class Sigungoo {
        companion object {
            var goo = "강서구"
            fun getMoreInfo():String { return "This is more fun" }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        initLocation()


        locationRequest = LocationRequest.create()
        locationRequest.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 60 * 1000
        }

        locationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?.let {
                    for((i, location) in it.locations.withIndex()) {
                        var mResultList: List<Address>?= null
                        var mGeocoder = Geocoder(applicationContext, Locale.getDefault())
                        try {
                            mResultList = mGeocoder.getFromLocation(location.latitude, location.longitude,2)
                        }catch (e: IOException){
                            e.printStackTrace()
                            Log.d("실패" ,"주소변환 실패")
                        }
                        if (mResultList != null){
                            Log.d("성공", mResultList[0].getAddressLine(0))
                            var currentLocation = mResultList[0].getAddressLine(0)
                            currentLocation = currentLocation.substring(11)
                            val arr = currentLocation.split(" ")
                            println(arr[0])
                            tv_Location.setText(arr[0]+ " " +arr[1])
                            Sigungoo.goo = arr[0]




                        }
                        Log.d("성공", "#$i ${location.latitude} , ${location.longitude}")
                    }
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION),111)
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())



        val api = retrofit.create(DustApiConnect::class.java)
        val callDustData = api.getDustData(Sigungoo.goo)
        callDustData.enqueue(object : Callback <DustData>{
            override fun onResponse(call: Call<DustData>, response: Response <DustData>) {
                Log.d("결과", "성공 : ${response.raw()}")
                if (response.isSuccessful){
                    val DustResult = response.body()?.items
                    println(DustResult?.get(0))
                    val pm10Status = DustResult?.get(0)?.pm10Grade.toString()
                    tv_Value.text = DustResult?.get(0)?.pm10Value.toString() + "㎍/㎥"
                    tv_time.text = DustResult?.get(0)?.dataTime.toString()
                    MiseStatus(pm10Status)
                }
            }

            override fun onFailure(call: Call<DustData>, t: Throwable) {
                Log.d("결과", "실패: $t")
            }
        })



    }
    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }




}

