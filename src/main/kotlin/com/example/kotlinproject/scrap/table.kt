package com.example.kotlinproject.scrap
import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Configuration
import java.time.LocalDate

object ProductScrap : LongIdTable("ProductScrap") {
    val userLoginId = varchar("userLoginId", 100)
    val productId = long("productId")
    val createTime = varchar("date_time",50).default(LocalDate.now().toString())
}

@Configuration
class Scrap(private val database: Database) {
    @PostConstruct
    fun migrateSchema() {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(ProductScrap)
        }
    }
}