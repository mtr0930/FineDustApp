package com.example.dustapi


import com.squareup.moshi.Json

//미세먼지 파싱하기위한 데이터 클래스 이 형식으로 api응답을 처리.
data class DustData(
    @Json(name = "list")
    val items : List<item>?=null
)

data class item(
    @Json(name = "dataTime")
    val dataTime : String?=null,
    @Json(name = "pm10Grade")
    val pm10Grade : Int?=null,
    @Json(name = "pm10Value")
    val pm10Value : Int?=null
)


