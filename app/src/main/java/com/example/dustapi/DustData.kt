package com.example.dustapi


import com.squareup.moshi.Json


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


