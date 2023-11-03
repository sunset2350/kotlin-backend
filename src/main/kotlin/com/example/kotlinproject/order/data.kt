package com.example.kotlinproject.order

data class OrderResponse(
    val id : Long,
    val userLoginId : String,
    val username : String,
    val nickname : String,
    val permission : String,
    val productId : String,
    val quantity : Int,
    val address : String,
    val reviewContent : String?,
    val reviewCount : String?,
    val orderDate : String,
    val productPrice : Int,
    val reviewResponse : String?
)

data class OrderRequest(
    val productId : String,
    val quantity : Int,
    val address : String,
    val productPrice : Int
)