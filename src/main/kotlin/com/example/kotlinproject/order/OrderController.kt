package com.example.kotlinproject.order

import com.example.kotlinproject.auth.Auth
import com.example.kotlinproject.auth.AuthProfile
import kotlinx.coroutines.selects.select
import org.jetbrains.exposed.sql.and

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.HttpStatus

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.reflect.typeOf


@RestController
@RequestMapping("/order")


class OrderController(private val orderService: OrderService) {

    @Auth
    @GetMapping("/user")
    fun showOrderMenu(@RequestAttribute authProfile: AuthProfile) : List<userOrder>{
        val result = transaction {
            OrderMenu.select {
                OrderMenu.userLoginId eq authProfile.userLoginId and OrderMenu.Permission.eq("true")
            }.map {
                userOrder(
                    it[OrderMenu.brandName],
                    it[OrderMenu.productId],
                    it[OrderMenu.productName],
                    it[OrderMenu.productPrice]
                )
            }

            }
        return result
        }


    @Auth
    @PostMapping("/{productId}")
    fun orderMenu(
        @RequestBody request: OrderRequest, @RequestAttribute authProfile: AuthProfile,
    ): ResponseEntity<Map<String, Any?>> {

        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val permission = "false"



        val (result, response) = transaction {
            val result = OrderMenu.insert {
                it[userLoginId] = authProfile.userLoginId
                it[userName] = authProfile.username
                it[nickname] = authProfile.nickname
                it[Permission] = permission
                it[permissionContent] = null
                it[brandName] = request.brandName
                it[productId] = request.productId
                it[productName] = request.productName
                it[quantity] = request.quantity
                it[address] = request.address
                it[productPrice] = request.productPrice
                it[OrderDate] = currentDateTime.format(formatter)
            }.resultedValues ?: return@transaction Pair(false, null)

            val record = result.first()


            return@transaction Pair(
                true, OrderResponse(
                    record[OrderMenu.id].value,
                    record[OrderMenu.userLoginId],
                    record[OrderMenu.userName],
                    record[OrderMenu.nickname],
                    record[OrderMenu.Permission],
                    record[OrderMenu.permissionContent],
                    record[OrderMenu.brandName],
                    record[OrderMenu.productId],
                    record[OrderMenu.productName],
                    record[OrderMenu.quantity],
                    record[OrderMenu.address],
                    record[OrderMenu.reviewContent],
                    record[OrderMenu.reviewCount],
                    record[OrderMenu.OrderDate].toString(),
                    record[OrderMenu.productPrice],
                    record[OrderMenu.reviewResponse]
                )


            )

        }
        if (result) {


//            rabbitTemplate.convertAndSend("orderExchange","orderkey",result)


            val order = Order(
                orderId = response?.id,
                userId = authProfile.id,
                productId = request.productId,
                quantity = request.quantity,
                address = request.address
            )

            println(orderService.createOrderMessage(order))
            orderService.createOrderMessage(order)
            return ResponseEntity.status(HttpStatus.CREATED).body(mapOf("data" to response))

        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("data" to response, "error" to "conflict"))

    }
}
