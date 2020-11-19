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

    //위치요청 1. gps접근 권한 요청  2. 위치요청
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
    //미세먼지 공공api위한 moshi 라이브러리
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    //rest API 프로토콜 이용해서 API이용하기위한 retrofit
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://openapi.airkorea.or.kr/openapi/services/rest/ArpltnInforInqireSvc/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
    //pm10Grade 1 좋음 2 보통 3 나쁨 4 매우나쁨 파싱해서 텍스뷰로 현재 상태 보여줌.
    fun MiseStatus( status:String) {
        when(status){
            "1"-> tv_Status.text = "좋음"
            "2"-> tv_Status.text = "보통"
            "3"-> tv_Status.text = "나쁨"
            "4"-> tv_Status.text = "매우 나쁨"
        }

    }
    //현재 기기 위치 시군구 중에서 구에 해당하는 주소 추출해서 api함수로 전달 하기위한 클래스.
    // 코틀린은 전역변수가 없음.
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

        //위치요청
        locationRequest = LocationRequest.create()
        locationRequest.run {
            //위치요청 설정값
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 60 * 1000
        }
        //콜백 요청
        locationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?.let {
                    for((i, location) in it.locations.withIndex()) {
                        var mResultList: List<Address>?= null
                        var mGeocoder = Geocoder(applicationContext, Locale.getDefault())
                        try {
                            //현재 위도경도로 주소 가져옴.
                            mResultList = mGeocoder.getFromLocation(location.latitude, location.longitude,2)
                        }catch (e: IOException){
                            e.printStackTrace()
                            Log.d("실패" ,"주소변환 실패")
                        }
                        if (mResultList != null){
                            Log.d("성공", mResultList[0].getAddressLine(0))
                            var currentLocation = mResultList[0].getAddressLine(0)
                            //substring함으로써 대한민국 서울특별시 제외한 나머지.
                            currentLocation = currentLocation.substring(11)
                            val arr = currentLocation.split(" ")
                            //강서구 ~동만 출력하기 위한 split 처리 arr[0]는 강서구 arr[1]은 ~동 나타냄.
                            tv_Location.setText(arr[0]+ " " +arr[1])
                            //전역변수없어서 클래스 멤버로 현재 구정보 전달.
                            Sigungoo.goo = arr[0]




                        }
                        Log.d("성공", "#$i ${location.latitude} , ${location.longitude}")
                    }
                }
            }
        }
        //지속적인 위치 업데이트 요청. gps권한 접근 여부 한번더 확인.
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


        //미세먼지 api요청하는 부분
        val api = retrofit.create(DustApiConnect::class.java)
        val callDustData = api.getDustData(Sigungoo.goo)
        callDustData.enqueue(object : Callback <DustData>{
            override fun onResponse(call: Call<DustData>, response: Response <DustData>) {
                Log.d("결과", "성공 : ${response.raw()}")
                if (response.isSuccessful){
                    //DustResult에 Json형식 배열로 결과가 들어옴. 인덱스 0번째 있는 값이 가장 최근값.
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

