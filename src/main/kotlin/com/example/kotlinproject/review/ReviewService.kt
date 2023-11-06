package com.example.kotlinproject.review

import com.example.kotlinproject.auth.Auth
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

@Service

class ReviewService(private val rabbitTemplate: RabbitTemplate) {
    private val mapper = jacksonObjectMapper()

    @Auth
    fun createReview(review: reviewRequest){
        rabbitTemplate.convertAndSend("review-request",mapper.writeValueAsString(review))
    }


}