package com.example.kotlinproject.order

import com.example.kotlinproject.auth.Auth
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

@Service
class OrderService(private val rabbitTemplate: RabbitTemplate) {
    private val mapper = jacksonObjectMapper()

    @Auth
    fun createOrderMessage(order: Order){
        rabbitTemplate.convertAndSend("product-payment",mapper.writeValueAsString(order))
    }

//    @RabbitListener(queues = ["product-payment-response"])
//    fun responseOrderMessage(permission : String){
//        val updateResponse = OrderMenu.update({
//            (OrderMenu.userLoginId eq authProfile.userLoginId) and (OrderMenu.Permission.eq("true")) and (OrderMenu.productId eq userReviewCreate.productId)
//        })
//
//    }


}