package com.example.kotlinproject.inquery


data class ProductInqueryResponse(
    val id: Long,
    val userLoginId: String,
    val username: String,
    val productId: Long,
    val inqueryCategory: String,
    val inqueryContent: String,
    val inqueryAnswer: String?
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

data class InqueryResponse(
    val id : Long,
    val productId: Long,
    val inqueryAnswer: String
)

data class ProductCreateRequest(
    val id : Long,
    val userLoginId: String,
    val username: String,
    val productId: Long,
    val inqueryCategory: String,
    val inqueryContent: String,
)

data class ProductInqeuryResponse(
    val id : Long,
    val productId: Long,
    val inqueryCategory: String,
    val inqueryAnswer: String
)


fun ProductCreateRequest.validate() =
    !(this.productId == null || this.inqueryContent.isEmpty()
            || this.userLoginId.isEmpty())

