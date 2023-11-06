package com.example.kotlinproject.scrap

import ProductScrap
import com.example.kotlinproject.auth.Auth
import com.example.kotlinproject.auth.AuthProfile
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import java.sql.Connection

@Service
@RequestMapping("/scrap")

class ScrapController {
    @Auth
    fun showScrap(@RequestAttribute authProfile: AuthProfile): Map<String, Any?> =
        transaction(Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true) {
            val result = ProductScrap.select {
                ProductScrap.userLoginId eq authProfile.userLoginId and ProductScrap.productId.isNotNull()
            }.orderBy(ProductScrap.id, SortOrder.DESC)
                .map {

                ScarpResponse(
                    it[ProductScrap.id].value,
                    it[ProductScrap.productId],
                    it[ProductScrap.createTime]
                )
            }

            mapOf("data" to result)

        }


}