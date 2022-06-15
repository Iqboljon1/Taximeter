package com.iraimjanov.taxometr.models

data class History(
    val orderId: String = "",
    var kmSumma: String = "",
    var km: String = "",
    var summa: String = "",
    var time: String = "",
    var listLatLong: ArrayList<LatLong> = ArrayList(),
)