package com.example.kotlinproject.inquery


data class ProductInqueryResponse(
    val id: Long,
    val userLoginId: String,
    val username: String,
    val productId: Long,
    val inqueryCategory: String,
    val inqueryContent: String,
    val inqueryAnswer: String
)

data class ProductInqueryRequest(
    val id: Long,
    val userLoginId: String,
    val productId: Long,
    val username: String,
    val inqueryCategory: String,
    val inqueryContent: String,
    val inqueryAnswer: String?,
    val inqueryDate: String
)

data class ProductCreateRequest(
    val userLoginId: String,
    val username: String,
    val productId: Long,
    val inqueryCategory: String,
    val inqueryContent: String,
)


fun ProductCreateRequest.validate() =
    !(this.productId == null || this.inqueryContent.isEmpty()
            || this.userLoginId.isEmpty())

