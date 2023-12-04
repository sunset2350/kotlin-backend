package com.example.kotlinproject.review

import com.example.kotlinproject.auth.Auth
import com.example.kotlinproject.order.OrderMenu
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException

@Service
class ReviewService(private val rabbitTemplate: RabbitTemplate) {
    private val mapper = jacksonObjectMapper()
    private val emitters = mutableListOf<SseEmitter>()

    @Auth
    fun createReview(review: reviewRequest) {
        rabbitTemplate.convertAndSend("review-request", mapper.writeValueAsString(review))
    }

    @RabbitListener(queues = ["review-response"])
    fun responseReview(message: String) {
        val reviewResponse: ReviewResponse = mapper.readValue(message)


        println("Received review: $reviewResponse")
        val result = transaction {
            OrderMenu.update({
                (OrderMenu.productId eq reviewResponse.productId) and (OrderMenu.id eq reviewResponse.id)
            }) {
                it[OrderMenu.reviewResponse] = reviewResponse.reviewAnswer
            }
        }
    }
}

