package com.example.kotlinproject.profile

data class ProfileResponse(
    val id: String,
    val userLoginId: String,
    val username: String,
    val sex: String,
    val nickname: String,
    val birth: String,
    val introduction: String,
)

data class ProfileFile(
    val id : Long,
    var uuidFileName : String,
    val originalFileName : String,
    val contentType: String,
)