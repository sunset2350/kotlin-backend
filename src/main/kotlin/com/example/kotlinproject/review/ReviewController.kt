package com.example.kotlinproject.review

import com.example.kotlinproject.auth.Auth
import com.example.kotlinproject.auth.AuthProfile
import com.example.kotlinproject.order.OrderMenu
import kotlinx.coroutines.selects.select
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
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

class ReviewController {


    @GetMapping("/{productId}")
    fun showReview(
        @PathVariable productId: String, @RequestParam page: Int,
        @RequestParam size: Int
    ): Map<String, Any?> =
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

    @Auth
    @GetMapping("/user")
    fun userReview(
        @RequestAttribute authProfile: AuthProfile,
        @RequestParam page: Int,
        @RequestParam size: Int

    ): Page<UserReviewRespone> = transaction(Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true) {
        val result = OrderMenu.select {
            OrderMenu.userLoginId eq authProfile.userLoginId and OrderMenu.Permission.eq("true")
        }.orderBy(OrderMenu.id to SortOrder.DESC)
            .limit(size, offset = (size * page).toLong())
            .map {
                UserReviewRespone(
                    it[OrderMenu.productId],
                    it[OrderMenu.reviewContent],
                    it[OrderMenu.reviewCount],
                    it[OrderMenu.OrderDate]
                )
            }
        val reviewCount = result.count()

        return@transaction PageImpl(
            result,
            PageRequest.of(page, size),
            reviewCount.toLong()
        )
    }

    @Auth
    @PutMapping("/user/{productId}")
    fun createReview(
        @RequestAttribute authProfile: AuthProfile,
        @RequestBody productReviewRequest: ProductReviewRequest
    ): ResponseEntity<Map<String, Any?>> {

        transaction {
            val result = OrderMenu.update({
                (OrderMenu.userLoginId eq authProfile.userLoginId) and (OrderMenu.Permission.eq("true")) and (OrderMenu.productId eq productReviewRequest.productId)
            }) {
                it[reviewContent] = productReviewRequest.reviewContent
                it[reviewCount] = productReviewRequest.reviewCount
            }
            val response = if (result > 0) {
                ResponseEntity.status(HttpStatus.OK).body("Updated $result records")
            } else {
                ResponseEntity.status(HttpStatus.CONFLICT).body("No records updated")
            }
            return@transaction response
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).build()
    }

}





