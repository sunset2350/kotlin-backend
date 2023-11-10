package com.example.kotlinproject.order

import com.example.kotlinproject.auth.Auth
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class OrderService(private val rabbitTemplate: RabbitTemplate) {
    private val mapper = jacksonObjectMapper()

    @Auth
    fun createOrderMessage(order: Order) {
        println(order)
        rabbitTemplate.convertAndSend("product-payment", mapper.writeValueAsString(order))
    }

    @RabbitListener(queues = ["product-payment-result"])
    fun responseOrderMessage(message: String) {
        val orderJudgment: OrderJudgment = mapper.readValue(message)
        println("Received Order: $orderJudgment")
        val result = transaction {
            OrderMenu.update({
                (OrderMenu.id eq orderJudgment.orderId) and
                        (OrderMenu.Permission.eq("false"))
            }) {
                it[Permission] = orderJudgment.isPermission
            }
        }
        if(orderJudgment.isPermission == "false"){
            println("재고 부족")
            transaction {
                OrderMenu.update ({
                    (OrderMenu.id eq orderJudgment.orderId) and
                            (OrderMenu.Permission.eq("false"))
                })
                {
                    it[permissionContent] = "재고 부족"
                }
            }
        }
        if(orderJudgment.isPermission == "true") {
            println("주문완료")
            transaction {
                OrderMenu.update ({
                    (OrderMenu.id eq orderJudgment.orderId) and
                            (OrderMenu.Permission.eq("ture"))
                })
                {
                    it[permissionContent] = "주문 완료"
                }
            }
        }
    }
}