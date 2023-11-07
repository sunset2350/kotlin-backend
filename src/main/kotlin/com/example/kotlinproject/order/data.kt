package com.example.kotlinproject.order

data class OrderResponse(
    val id: Long,
    val userLoginId: String,
    val username: String,
    val nickname: String,
    val permission: String,
    val permissionContent: String?,
    val brandName : String,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val address: String,
    val reviewContent: String?,
    val reviewCount: Int?,
    val orderDate: String,
    val productPrice: Int,
    val reviewResponse: String?
)

data class OrderRequest(
    val id: Long,
    val brandName: String,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val address: String,
    val productPrice: Int
)

data class Order(
    val userId: Long,
    val orderId: Long?,
    val productId: Long,
    val quantity: Int,
    val address:String,
)

data class OrderJudgment(
    val orderId : Long,
    val isPermission : String,
)

data class Review(
    val orderId: Long?,
    val userId: Long,
    val productId: Long,
    val quantity: Int,
    val address: String,

    )