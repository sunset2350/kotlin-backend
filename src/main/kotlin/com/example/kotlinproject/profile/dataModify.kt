package com.example.kotlinproject.profile



data class dataModify(
    val username: String,
    val sex: String,
    val nickname: String,
    val birth: String,
    val files : List<ProfileFile>,
    val introduction : String,
)
