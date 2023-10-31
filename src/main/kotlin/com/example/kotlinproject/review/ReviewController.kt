package com.example.kotlinproject.review

import com.example.kotlinproject.auth.Auth
import com.example.kotlinproject.auth.AuthProfile
import com.example.kotlinproject.review.ProductReview.username
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.sql.Connection
import java.time.LocalDateTime

@RestController
@RequestMapping("/review")

class ReviewController {


    @Auth
    @GetMapping("/all")
    fun showReview(@RequestAttribute authProfile: AuthProfile): List<ProductResponse> {
        fun fetch() = transaction {
            ProductReview.selectAll().map { r ->
                ProductResponse(
                    r[ProductReview.id].toString(),
                    r[ProductReview.username],
                    r[ProductReview.productId],
                    r[ProductReview.reviewContent],
                    r[ProductReview.reviewCount],
                    r[ProductReview.reviewDate],
                )

            }
        }

        return fetch()
    }

    @Auth
    @PostMapping("/{productId}")
    fun createReview(
        @RequestAttribute authProfile: AuthProfile,
        @RequestBody productReviewResponse: ProductReviewResponse
    ): ResponseEntity<out Map<String, Any?>> {

        val (result, response) = transaction {
            val result = ProductReview.insert {
                it[username] = productReviewResponse.username
                it[productId] = productReviewResponse.productId
                it[reviewContent] = productReviewResponse.reviewContent
                it[reviewCount] = productReviewResponse.reviewCount
                it[userLoginId] = productReviewResponse.productId
                it[reviewDate] = LocalDateTime.now().toString()
            }.resultedValues
                ?: return@transaction Pair(false, null)

            val record = result.first()

            return@transaction Pair(
                true, ProductReviewResponse(
                    record[ProductReview.id].value,
                    record[ProductReview.userLoginId],
                    record[ProductReview.username],
                    record[ProductReview.productId],
                    record[ProductReview.reviewContent],
                    record[ProductReview.reviewCount],
                    record[ProductReview.reviewDate].toString(),
                )
            )
        }

        if (result) {
            return ResponseEntity
                .status(HttpStatus.CREATED).body(mapOf("data" to response))
        }

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(mapOf("data" to response,"error" to "conflict"))



    }
}



