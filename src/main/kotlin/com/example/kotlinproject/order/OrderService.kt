package com.example.kotlinproject.order

import com.example.kotlinproject.auth.Auth
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException

@Service

class OrderService(private val rabbitTemplate: RabbitTemplate) {
    private val mapper = jacksonObjectMapper()

    @Auth
    fun createOrderMessage(order: Order){
        rabbitTemplate.convertAndSend("product-payment",mapper.writeValueAsString(order))
    }


}