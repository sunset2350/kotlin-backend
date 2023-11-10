package com.example.kotlinproject.review

import com.example.kotlinproject.auth.Auth
import com.example.kotlinproject.auth.AuthProfile
import com.example.kotlinproject.order.OrderMenu
import kotlinx.coroutines.selects.select
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.sql.Connection
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/review")

class ReviewController(private val create: ReviewService) {


    @GetMapping("/{productId}")
    fun showReview(@PathVariable productId: Long): List<ProductResponse> =
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
            return@transaction result
        }

    @GetMapping("/review-total/{productId}")
    fun totalReview(@PathVariable productId: Long): ResponseEntity<Map<String,Any>> {
        return transaction {
            val reviewCount = OrderMenu.select {
                (OrderMenu.productId eq productId and OrderMenu.Permission.eq("true") and OrderMenu.reviewCount.isNotNull())
            }.count()

            val reviewSum = OrderMenu.select {
                (OrderMenu.productId eq productId and OrderMenu.Permission.eq("true") and OrderMenu.reviewCount.isNotNull())
            }.sumOf {
                it[OrderMenu.reviewCount] ?: 0
            }


            val reviewTotal = if (reviewCount > 0) {
                reviewSum / reviewCount
            } else {
                0
            }

            if (reviewCount > 0) {
                ResponseEntity.status(HttpStatus.OK).body(mapOf("reviewTotal" to reviewTotal))
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("reviewTotal" to 0))
            }
        }
    }


    @Auth
    @GetMapping("/no-review{key}")
    fun showNonReview(@RequestAttribute authProfile: AuthProfile, @RequestParam(required=false) key: String?): List<noReivewResponse> {
        println("${key}")
        val result = transaction {
            OrderMenu.select {
                (OrderMenu.userLoginId eq authProfile.userLoginId) and
                        (OrderMenu.Permission eq "true") and
                        (OrderMenu.reviewContent.isNull()) and
                        (OrderMenu.reviewCount.isNull()) and
                        if (key != null) {
                            ((OrderMenu.productName like "%${key}%") or (OrderMenu.brandName like "%${key}%"))
                        } else {
                            Op.TRUE
                        }
            }.map {
                noReivewResponse(
                    it[OrderMenu.productId],
                    it[OrderMenu.OrderDate],
                    it[OrderMenu.productPrice],
                    it[OrderMenu.productName],
                    it[OrderMenu.brandName]
                )
            }
        }
        return result
    }

    @Auth
    @GetMapping("/in-review")
    fun allReview(@RequestAttribute authProfile: AuthProfile): List<inReviewResponse> {
        val result = transaction {
            OrderMenu.select {
                (OrderMenu.userLoginId eq authProfile.userLoginId) and
                        (OrderMenu.Permission.eq("true")) and
                        (OrderMenu.reviewCount.isNotNull()) and
                        (OrderMenu.reviewContent.isNotNull())
            }.map {
                inReviewResponse(
                    it[OrderMenu.productId],
                    it[OrderMenu.productName],
                    it[OrderMenu.OrderDate],
                    it[OrderMenu.reviewCount],
                    it[OrderMenu.reviewContent],
                    it[OrderMenu.reviewResponse],
                )
            }

        }
        return result
    }


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
                    userId = authProfile.id,
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









