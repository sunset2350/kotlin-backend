package com.example.kotlinproject.review

import com.example.kotlinproject.auth.Auth
import com.example.kotlinproject.auth.AuthProfile
import com.example.kotlinproject.order.OrderMenu
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.sql.Connection
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/review")

class ReviewController (private val create: ReviewService){


    @GetMapping("/{productId}")
    fun showReview(@PathVariable productId: Long): Map<String, Any?> =
        transaction(Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true) {
            val result = OrderMenu.select {
                OrderMenu.productId eq productId and OrderMenu.reviewContent.isNotNull()
            }.map {
                ProductResponse(
                    it[OrderMenu.nickname],
                    it[OrderMenu.reviewContent],
                    it[OrderMenu.reviewCount],
                    it[OrderMenu.reviewResponse]
                )
            }


            mapOf("data" to result)
        }

//    @Auth
//    @GetMapping("/user")
//    fun userReview(
//        @RequestAttribute authProfile: AuthProfile,
//    ): Map<String, Any?> = transaction(Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true) {
//        val result = OrderMenu.select {
//            OrderMenu.userLoginId eq authProfile.userLoginId and OrderMenu.Permission.eq("true")
//        }.map {
//            UserReviewRespone(
//                it[OrderMenu.productId],
//                it[OrderMenu.reviewContent],
//                it[OrderMenu.reviewCount],
//                it[OrderMenu.OrderDate]
//            )
//
//        }
//        return@transaction mapOf("user review" to result)
//    }


    @Auth
    @PutMapping("/user/{productId}")
    fun createReview(
        @RequestAttribute authProfile: AuthProfile,
        @RequestBody userReviewCreate: UserReviewCreate
    ): ResponseEntity<Map<String, Any?>> {

        val result = transaction {
            val result = OrderMenu.update({
                (OrderMenu.userLoginId eq authProfile.userLoginId) and (OrderMenu.Permission.eq("true")) and (OrderMenu.productId eq userReviewCreate.productId)
            }) {
                it[reviewContent] = userReviewCreate.reviewContent
                it[reviewCount] = userReviewCreate.reviewCount
            }
            if (result > 0) {
                val data = OrderMenu.select {
                    (OrderMenu.userLoginId eq authProfile.userLoginId) and
                            (OrderMenu.Permission eq "true") and
                            (OrderMenu.productId eq userReviewCreate.productId)
                }.toList()


                val response = reviewRequest(
                    id = data.first()[OrderMenu.id].value,
                    brandName = data.first()[OrderMenu.brandName],
                    productId = data.first()[OrderMenu.productId],
                    reviewContent = data.first()[OrderMenu.reviewContent],
                    scope = data.first()[OrderMenu.reviewCount],
                    reviewAnswer = null,
                    gender = authProfile.sex,
                    birth = authProfile.birth
                )
                println(response)
                create.createReview(response)
            }


        }
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }
}









