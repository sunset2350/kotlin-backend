package com.example.kotlinproject.review

import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Configuration
import java.time.LocalDate

object ProductReview : LongIdTable("ProductReview") {
    val userLoginId = varchar("userLoginId", 100)
    val nickname = varchar("nickname",30)
    val productId = varchar("productId", 100)
    val birth = varchar("birth",30)
    val reviewContent = largeText("reviewContent")
    val scope = varchar("reviewCount",10)
    val reviewDate = varchar("reviewDate",50).default(LocalDate.now().toString())
}

@Configuration
class Review(private val database: Database) {
    @PostConstruct
    fun migrateSchema() {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(ProductReview)
        }
    }
}