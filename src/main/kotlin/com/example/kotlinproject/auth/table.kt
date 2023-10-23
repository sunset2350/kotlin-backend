package com.example.kotlinproject.auth

import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Configuration

object Identities : LongIdTable("identity") {
    val userid = varchar("userid",  100)
    val secret = varchar("secret",  200)
}

object Profiles : LongIdTable("profile") {
    val userid = varchar("userid",  100)
    val birth = varchar("birth", 15)
    val nickname = varchar("nickname",  50)
    val username = varchar("username", 100)
    val sex = varchar("sex", 10)
//    val image = text("image")
}


@Configuration
class AuthTableSetup(private val database: Database) {
    @PostConstruct
    fun migrateSchema() {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(Identities, Profiles)
        }
    }
}