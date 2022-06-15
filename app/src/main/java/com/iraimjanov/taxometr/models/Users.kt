package com.iraimjanov.taxometr.models

data class Users(
    var username: String = "",
    var password: String = "",
    var number: String = "",
    var address: String = "",
    var patronymic: String = "",
    var name: String = "",
    var surname: String = "",
    var imageLink: String = "",
)