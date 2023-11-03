package com.example.kotlinproject.inquery

data class ProductInqueryResponse(
    val id: Long,
    val userLoginId: String,
    val nickname: String,
    val productId: String,
    val inqueryCategory: String,
    val inqueryContent: String,
    val inqueryAnswer: String
)


data class ProductCreateRequest(
    val userLoginId: String,
    val nickname: String,
    val productId: String,
    val inqueryCategory : String,
    val inqueryContent: String,
)


fun ProductCreateRequest.validate() =
    !(this.productId.isEmpty() || this.inqueryContent.isEmpty()
            || this.userLoginId.isEmpty() || this.nickname.isEmpty())

