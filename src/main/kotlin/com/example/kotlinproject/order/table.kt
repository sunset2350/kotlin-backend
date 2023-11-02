package com.example.kotlinproject.order


import com.example.kotlinproject.review.ProductReview
import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Configuration
import java.time.LocalDate

object OrderMenu : LongIdTable("OrderMenu") {
    val userLoginId = varchar("userLoginId" , 30)
    val userName = varchar("username",20)
    val nickname = varchar("nickname",30)
    val Permission = varchar("is_permission", 30)
    val productId = varchar("productId",100)
    val quantity = integer("quantity")
    val address = varchar("address",100)
    val reviewContent = largeText("reviewContent").nullable()
    val reviewCount = varchar("reviewCount",5).nullable()
    val reviewResponse = largeText("reviewResponse").nullable()
    val productPrice = integer("productPrice")
    val OrderDate = varchar("orderDate",100).default(LocalDate.now().toString())
}

@Configuration
class Order(private val database: Database) {
    @PostConstruct
    fun migrateSchema() {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(OrderMenu)
        }
    }
}