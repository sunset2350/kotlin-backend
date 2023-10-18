package com.example.kotlinproject.auth

data class SignupRequest (
    val userId : String,
    val userName : String,
    val userPassword : String,
    val nickName : String,
    val userSex : String,
    val userBirth : String
);

data class AuthProfile(
    val id: Long = 0,
    val userId : String,
    val userName: String,
    val nickName: String
)




