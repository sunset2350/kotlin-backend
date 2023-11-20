package com.example.kotlinproject.order

data class OrderResponse(
        val id: Long,
        val userLoginId: String,
        val username: String,
        val nickname: String,
        val permission: String,
        val permissionContent: String?,
        val brandName: String,
        val productId: Long,
        val productName: String,
        val quantity: Int,
        val address: String,
        val reviewContent: String?,
        val reviewCount: Int?,
        val orderDate: String,
        val productPrice: Int,
        val reviewResponse: String?,
        val phonenumber: String,
        val detailaddress: String,
        val imp_uid: String
)

data class OrderRequest(
        val id: Long,
        val brandName: String,
        val username: String,
        val productId: Long,
        val productName: String,
        val quantity: Int,
        val address: String,
        val detailaddress: String,
        val phonenumber: String,
        val productPrice: Int,
        val imp_uid: String
)

data class Order(
        val userId: Long,
        val orderId: Long?,
        val productId: Long,
        val quantity: Int,
        val address: String,
)

data class OrderJudgment(
        val orderId: Long,
        val isPermission: String,
)

data class Review(
        val orderId: Long?,
        val userId: Long,
        val productId: Long,
        val quantity: Int,
        val address: String,
)

data class userOrder(
        val id: Long,
        val brandName: String,
        val productId: Long,
        val productName: String,
        val productPrice: Int,
        val permissionContent: String?
)


data class VerifyUid(
        val imp_uid: String
)

data class TokenRequest(val imp_key: String, val imp_secret: String)

data class TokenResponse(
        val code: Int,
        val message: String?,
        val response: AccessTokenResponse?
)

data class AccessTokenResponse(
        val access_token: String?,
        val now: Long?,
        val expired_at: Long?
)

data class cancelPaidInfo(
        val imp_uid: String
)

data class cancelResponse(
        val code: Int
)



