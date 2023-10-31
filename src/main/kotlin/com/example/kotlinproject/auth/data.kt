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
    val userLoginId : String,
    val username: String,
)

data class Profile(
    val id: Long,
    val userid: String,
    val birth:String,
    val nickname: String,
    val username: String,
    val sex : String,
    val introduction:String
)


data class ProfileResponse(
    val id : Long,
    var uuidFileName : String,
    val originalFileName : String,
    val contentType: String,
)




