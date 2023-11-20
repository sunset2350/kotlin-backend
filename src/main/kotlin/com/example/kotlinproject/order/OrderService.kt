package com.example.kotlinproject.order


import com.example.kotlinproject.auth.Auth
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service


@Service

class OrderService(private val rabbitTemplate: RabbitTemplate, private val iamportFeignClient: IamportFeignClient) {

    private val mapper = jacksonObjectMapper()


    @Auth
    fun createOrderMessage(order: Order) {
        println(order)
        rabbitTemplate.convertAndSend("product-payment", mapper.writeValueAsString(order))
    }


    @RabbitListener(queues = ["product-payment-result"])
    fun responseOrderMessage(message: String) {
        val request = TokenRequest("2420334878656867", "l8VEUS5fbiYIjyfppEdSJsBgIlCzC4xwsoOKvapi9GnQOIv2MAtpOpWtYI99INMm7p6BWfsEEP0pxdPn")
        val response = iamportFeignClient.getToken(request)


        val orderJudgment: OrderJudgment = mapper.readValue(message)
        println("Received Order: $orderJudgment")
        if (orderJudgment.isPermission == "false") {

            val orderResult = transaction {
                OrderMenu.select(OrderMenu.id eq orderJudgment.orderId).map {
                    VerifyUid(
                            imp_uid = it[OrderMenu.imp_uid]
                    )
                }
            }.single()

            val cancelResult = iamportFeignClient.cancelPaid(cancelPaidInfo(orderResult.imp_uid), response.response!!.access_token!!)
            transaction {
                OrderMenu.update({
                    (OrderMenu.id eq orderJudgment.orderId)
                })
                {
                    it[OrderMenu.Permission] = "false"
                    it[permissionContent] = "재고 부족"
                }
            }
        }
        if (orderJudgment.isPermission == "true") {
            transaction {
                OrderMenu.update({
                    (OrderMenu.id eq orderJudgment.orderId)
                })
                {
                    it[OrderMenu.Permission] = "true"
                    it[permissionContent] = "주문 완료"
                }
            }
        }
    }
}