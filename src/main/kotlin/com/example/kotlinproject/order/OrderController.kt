package com.example.kotlinproject.order

import com.example.kotlinproject.auth.Auth
import com.example.kotlinproject.auth.AuthProfile
import com.example.kotlinproject.auth.Profiles
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@RestController
@RequestMapping("/order")
class OrderController {


    @Auth
    @PostMapping("/{productId}")
    fun orderMenu(
        @RequestBody request: OrderRequest,
        @RequestAttribute authProfile: AuthProfile
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
                it[productId] = request.productId
                it[quantity] = request.quantity
                it[address] = request.address
                it[productPrice] = request.productPrice
                it[OrderDate] = currentDateTime.format(formatter)
            }.resultedValues
                ?: return@transaction Pair(false, null)

            println(result)
            val record = result.first()

            return@transaction Pair(
                true, OrderResponse(
                    record[OrderMenu.id].value,
                    record[OrderMenu.userLoginId],
                    record[OrderMenu.userName],
                    record[OrderMenu.nickname],
                    record[OrderMenu.Permission],
                    record[OrderMenu.productId],
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
            return ResponseEntity
                .status(HttpStatus.CREATED).body(mapOf("data" to response))
        }

        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(mapOf("data" to response, "error" to "conflict"))
    }

}