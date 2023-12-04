package com.example.kotlinproject.review

import org.jetbrains.exposed.dao.id.LongIdTable

data class reviewRequest(
    val id : Long,
    val brandName : String,
    val userId : Long,
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

data class noReivewResponse(
    val productId: Long,
    val createDate : String,
    val productPrice : Int,
    val productName : String,
    val brandName: String
)

data class inReviewResponse(
    val productId: Long,
    val productName: String,
    val createDate : String,
    val reviewCount : Int?,
    val reviewContent: String?,
    val reviewResponse: String?
)


data class UserReviewCreate(
    val reviewContent: String,
    val reviewCount: Int,
    val productId: Long
)

data class ReviewResponse(
    val id : Long,
    val productId: Long,
    val reviewAnswer: String,
)


