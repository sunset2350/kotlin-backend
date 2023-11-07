package com.example.kotlinproject.inquery

import com.example.kotlinproject.auth.Auth
import com.example.kotlinproject.auth.AuthProfile
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

@Service
class InqueryService(private val rabbitTemplate: RabbitTemplate) {
    private val mapper = jacksonObjectMapper()


    @Auth
    fun createInquery(InqueryRequest: ProductInqueryRequest) {

        rabbitTemplate.convertAndSend("product-inquery", mapper.writeValueAsString(InqueryRequest))
    }

    @Auth
    @RabbitListener(queues = ["inquery-response"])
    fun ResponseInquery(message: String) {
        val objectMapper = jacksonObjectMapper()
        val inqueryRequest: ProductInqueryRequest = objectMapper.readValue(message, ProductInqueryRequest::class.java)
        print("Received inquiry: $inqueryRequest")

        transaction {
            ProductInquery.update({
                (ProductInquery.id eq inqueryRequest.id) and
                        (ProductInquery.productId eq inqueryRequest.productId) and
                        (ProductInquery.inqueryAnswer.isNull())

            }) {
                it[ProductInquery.inqueryAnswer] = inqueryRequest.inqueryAnswer
            }
        }
    }
}
