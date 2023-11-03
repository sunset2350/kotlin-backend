import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Configuration
import java.time.LocalDate

object ProductScrap : LongIdTable("ProductReview") {
    val userLoginId = varchar("userLoginId", 100)
    val username = varchar("username", 100)
    val productId = varchar("productId", 100)
    val reviewContent = largeText("reviewContent")
    val reviewCount = varchar("reviewCount",10)
    val reviewDate = varchar("reviewDate",50).default(LocalDate.now().toString())
}

@Configuration
class Review(private val database: Database) {
    @PostConstruct
    fun migrateSchema() {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(ProductScrap)
        }
    }
}