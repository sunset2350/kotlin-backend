package com.example.kotlinproject.review

data class ProductReviewRequest(
    val productId: String,
    val reviewContent : String?,
    val reviewCount : String?,
)

data class ProductResponse(
    val nickname: String?,
    val reviewContent: String?,
    val reviewCount: String?,
    val reviewResponse: String?
)

data class UserReviewRespone(
    val productId: String,
    val reviewContent : String?,
    val reviewCount : String?,
    val OrderDate : String
)
