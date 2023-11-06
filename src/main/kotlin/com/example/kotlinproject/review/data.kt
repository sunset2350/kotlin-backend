package com.example.kotlinproject.review

data class reviewRequest(
    val id : Long,
    val brandName : String,
    val productId: Long,
    val reviewContent: String?,
    val scope: Int?,
    val reviewAnswer : String?,
    val gender : String,
    val birth : String
)

data class ProductResponse(
    val nickname: String?,
    val reviewContent: String?,
    val reviewCount: Int?,
    val reviewResponse: String?
)

data class UserReviewCreate(
    val reviewContent: String,
    val reviewCount: Int,
    val productId: Long
)

data class UserReviewRespone(
    val productId: String,
    val reviewContent: String?,
    val reviewCount: Int?,
    val OrderDate: String
)

//data class ReviewRequest(
//    val userLoginId: String,
//    val
//)
