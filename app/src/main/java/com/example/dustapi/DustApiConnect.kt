package com.example.dustapi

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers

import retrofit2.http.Query
import retrofit2.http.QueryMap

interface DustApiConnect {
    @Headers("Accept: application/json")
    @GET("getMsrstnAcctoRltmMesureDnsty?&dataTerm=month&pageNo=1&numOfRows=10&ServiceKey=MUuuOiKqEneLjw5hXQ5LD6%2BmH9n3SHXpAa2Cn8RnqSM%2BC70WlVWicn6QFOuvM2ubGI42IFbOTGJRTg19%2B0QoLg%3D%3D&ver=1.3&_returnType=json")
    fun getDustData(

        @Query("stationName") stationName: String

    ): Call <DustData>
}