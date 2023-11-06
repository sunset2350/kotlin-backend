package com.example.kotlinproject.inquery

import com.example.kotlinproject.auth.Auth
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

@Service
class InqueryService(private val rabbitTemplate : RabbitTemplate) {
    private val mapper = jacksonObjectMapper()



    @Auth
    fun CreateInquery(InqueryRequest : ProductInqueryRequest) {
        rabbitTemplate.convertAndSend("product-inquery",mapper.writeValueAsString(InqueryRequest))
    }
}