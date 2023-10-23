package com.example.kotlinproject.auth

data class SignupRequest (
    val userid : String,
    val username : String,
    val userpassword : String,
    val nickname : String,
    val usersex : String,
    val userbirth : String
);

data class AuthProfile(
    val id: Long = 0,
    val userid : String,
    val username: String,
)




