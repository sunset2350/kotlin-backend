package com.example.kotlinproject.inquery


import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Configuration
import java.time.LocalDate

object ProductInquery : LongIdTable("productinquery") {
    val userLoginId = varchar("userLoginId", 100)
    val username = varchar("username", 100)
    val productId = long("productId")
    val productName = varchar("productName",30)
    val inqueryCategory = varchar("inqueryCategory", 30)
    val inqueryContent = largeText("inqueryContent")
    val inqueryAnswer = largeText("inqueryAnswer").nullable()
    val inqueryDate = varchar("inqueryDate", 50).default(LocalDate.now().toString())
}

@Configuration
class Inquery(private val database: Database) {
    @PostConstruct
    fun migrateSchema() {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(ProductInquery)
        }
    }
}