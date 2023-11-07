package com.example.kotlinproject.follow

import com.example.kotlinproject.auth.Auth
import com.example.kotlinproject.auth.AuthProfile
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.sql.Connection
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/follow")
class FollowController {

    @Auth
    @GetMapping
    fun showScrap(@RequestAttribute authProfile: AuthProfile): Map<String, Any?> =
        transaction(Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true) {
            val result = BrandFollow.select {
                BrandFollow.userLoginId eq authProfile.userLoginId and BrandFollow.brandName.isNotNull()
            }.orderBy(BrandFollow.id, SortOrder.DESC)
                .map {

                    followResponse(
                        it[BrandFollow.id].value,
                        it[BrandFollow.brandName],
                        it[BrandFollow.createTime]
                    )
                }

            mapOf("data" to result)

        }

    @Auth
    @PostMapping("/{brandname}")
    fun createScrap(
        @RequestAttribute authProfile: AuthProfile,
        @PathVariable brandname: String
    ): ResponseEntity<Map<String, Any?>> {

        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val checkScrap = transaction {
            BrandFollow.select {
                (BrandFollow.userLoginId eq authProfile.userLoginId) and
                        (BrandFollow.brandName eq brandname)
            }.count()
        }

        return if (checkScrap > 0) {
            ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("message" to "이미 존재합니다 (Already scrapped)"))
        } else {
            val insertResult = transaction {
                BrandFollow.insert {
                    it[userLoginId] = authProfile.userLoginId
                    it[brandName] = brandname
                    it[createTime] = currentDateTime.format(formatter)
                }
            }

            if (insertResult != null) {
                ResponseEntity.status(HttpStatus.CREATED)
                    .body(mapOf("message" to "팔로우 성공적으로 완료되었습니다 (Scrapped successfully)"))
            } else {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(mapOf("message" to "팔로우 취소중에 에러가 발생하였습니다 (Error while processing scrap)"))
            }
        }

    }

    @Auth
    @DeleteMapping("/delete/{brandname}")
    fun deleteScrap(@RequestAttribute authProfile: AuthProfile, @PathVariable brandname: String): ResponseEntity<Map<String, Any?>> {
        val checkScrap = transaction {
            BrandFollow.select {
                (BrandFollow.userLoginId eq authProfile.userLoginId) and (BrandFollow.brandName eq brandname)
            }.count()
        }

        return if (checkScrap <= 0) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("message" to "데이터가 존재하지 않습니다"))
        } else {
            val deleteResult = transaction {
                BrandFollow.deleteWhere {
                    (BrandFollow.userLoginId eq authProfile.userLoginId) and (BrandFollow.brandName eq brandname)
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