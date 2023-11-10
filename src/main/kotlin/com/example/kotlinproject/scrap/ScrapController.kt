package com.example.kotlinproject.scrap

import com.example.kotlinproject.auth.Auth
import com.example.kotlinproject.auth.AuthProfile
import kotlinx.coroutines.selects.select
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.sql.Connection
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/scrap")
class ScrapController {

    @Auth
    @GetMapping
    fun showScrap(@RequestAttribute authProfile: AuthProfile): List<ScarpResponse> =
        transaction(Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true) {
            val result = ProductScrap.select {
                ProductScrap.userLoginId eq authProfile.userLoginId and ProductScrap.productId.isNotNull()
            }.orderBy(ProductScrap.id, SortOrder.DESC)
                .map {

                    ScarpResponse(
                        it[ProductScrap.productId],
                    )
                }

            return@transaction result

        }


    @Auth
    @PostMapping("/{productid}")
    fun createScrap(
        @RequestAttribute authProfile: AuthProfile,
        @PathVariable productid: Long
    ): ResponseEntity<Map<String, Any?>> {

        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val checkScrap = transaction {
            ProductScrap.select {
                (ProductScrap.userLoginId eq authProfile.userLoginId) and
                        (ProductScrap.productId eq productid)
            }.count()
        }

        return if (checkScrap > 0) {
            ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("message" to "이미 존재합니다 (Already scrapped)"))
        } else {
            val insertResult = transaction {
                ProductScrap.insert {
                    it[userLoginId] = authProfile.userLoginId
                    it[productId] = productid
                    it[createTime] = currentDateTime.format(formatter)
                }
            }

            if (insertResult != null) {
                ResponseEntity.status(HttpStatus.CREATED)
                    .body(mapOf("message" to "스크랩이 성공적으로 완료되었습니다 (Scrapped successfully)"))
            } else {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(mapOf("message" to "스크랩을 처리하는 중에 오류가 발생했습니다 (Error while processing scrap)"))
            }
        }
    }

    @Auth
    @DeleteMapping("/delete/{productid}")
    fun deleteScrap(@RequestAttribute authProfile: AuthProfile, @PathVariable productid: Long): ResponseEntity<Map<String, Any?>> {
        val checkScrap = transaction {
            ProductScrap.select {
                (ProductScrap.userLoginId eq authProfile.userLoginId) and (ProductScrap.productId eq productid)
            }.count()
        }

        return if (checkScrap <= 0) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("message" to "데이터가 존재하지 않습니다"))
        } else {
            val deleteResult = transaction {
                ProductScrap.deleteWhere {
                    (ProductScrap.userLoginId eq authProfile.userLoginId) and (ProductScrap.productId eq productid)
                }
            }
            if (deleteResult > 0) {
                ResponseEntity.status(HttpStatus.OK).body(mapOf("message" to "데이터가 정상 삭제되었습니다"))
            } else {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(mapOf("message" to "데이터 삭제 중에 오류가 발생했습니다"))
            }
        }
    }



}