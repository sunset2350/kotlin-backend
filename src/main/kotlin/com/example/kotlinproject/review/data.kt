package com.example.kotlinproject.review

data class ProductReviewResponse(
    val id : Long,
    val userLoginId: String,
    val username: String,
    val productId : String,
    val reviewContent : String,
    val reviewCount : String,
    val reviewDate : String
)

data class ProductResponse(
    val id : String,
    val username : String,
    val productId: String,
    val reviewContent: String,
    val reviewCount : String,
    val reviewDate : String
)

