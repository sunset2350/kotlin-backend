package com.example.kotlinproject.follow
import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Configuration
import java.time.LocalDate

object BrandFollow : LongIdTable("BrandNameFollow") {
    val userLoginId = varchar("userLoginId", 100)
    val brandName = varchar("brandName",30)
    val createTime = varchar("date_time",50).default(LocalDate.now().toString())
}

@Configuration
class Follow(private val database: Database) {
    @PostConstruct
    fun migrateSchema() {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(BrandFollow)
        }
    }
}