package com.example.kotlinproject.inquery

import com.example.kotlinproject.auth.Auth
import com.example.kotlinproject.auth.AuthProfile
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.web.bind.annotation.*

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/inquery")
class InqueryController(
    private val inqueryService: InqueryService,
    private val namedTemplate: NamedParameterJdbcTemplate,
    private val template: JdbcTemplate,
) {


    @GetMapping("/{productid}")
    fun showInquery(@PathVariable productid: Long): MutableList<ProductInqueryResponse> =
        template.query("SELECT * FROM ProductInquery where productid = '${productid}'")
        { rs, _ ->
            ProductInqueryResponse(
                rs.getLong("id"),
                rs.getString("userLoginId"),
                rs.getString("username"),
                rs.getLong("productId"),
                rs.getString("productName"),
                rs.getString("inqueryCategory"),
                rs.getString("inqueryContent"),
                rs.getString("inqueryAnswer")
            )

        }

    @Auth
    @DeleteMapping("/{productid}")
    fun deleteInquery(
        @RequestAttribute authProfile: AuthProfile,
        @PathVariable productid: Long
    ): ResponseEntity<Any> {
        val result = template.query(
            "SELECT id FROM ProductInquery where productid = ? AND profile_id = ?",
            arrayOf(productid,authProfile.userLoginId)) {rs, _ -> rs}

        if(result.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
        return ResponseEntity.ok().build()
    }

    @Auth
    @GetMapping("/user")
    fun paging(
        @RequestAttribute authProfile: AuthProfile,
        @RequestParam size: Int,
        @RequestParam page: Int
    ): PageImpl<ProductInqueryResponse> {
        val countFrom: String = "SELECT count(*) FROM ProductInquery where userLoginId = '${authProfile.userLoginId}'"

        val totalCount: Long? = namedTemplate.queryForObject(
            countFrom,
            mapOf("userLoginId" to authProfile.userLoginId),
            Long::class.java
        )


        if (totalCount == null || totalCount == 0L) {
            return PageImpl(listOf<ProductInqueryResponse>(), PageRequest.of(page, size), 0)
        }

        val selectFrom: String = "SELECT * FROM ProductInquery where userLoginId = :userLoginId"
        val orderByLimitOffset: String = """
        ORDER BY id DESC
        LIMIT $size OFFSET ${size * page}
    """.trimIndent()

        val content: List<ProductInqueryResponse> = namedTemplate.query(
            "$selectFrom $orderByLimitOffset",
            mapOf("userLoginId" to authProfile.userLoginId)
        ) { rs, _ ->
            ProductInqueryResponse(
                rs.getLong("id"),
                rs.getString("userLoginId"),
                rs.getString("username"),
                rs.getLong("productId"),
                rs.getString("productName"),
                rs.getString("inqueryCategory"),
                rs.getString("inqueryContent"),
                rs.getString("inqueryAnswer")
            )
        }


        return PageImpl(content, PageRequest.of(page, size), totalCount)
    }

    @Auth
    @PostMapping("/menu/{productid}")
    fun createInquery(
        @PathVariable productid: Long,
        @RequestBody request: ProductCreateRequest, @RequestAttribute authProfile: AuthProfile,

        ): ResponseEntity<Map<String, Any?>> {
        if (request.validate()) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to "title and content fields are request"))
        }

        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dateTime = LocalDateTime.now()
        val (id, userLoginId, username, productid, productName, inqueryCategory, inqueryContent) = request


        val insertedId = SimpleJdbcInsert(template)
            .withTableName("ProductInquery")
            .usingGeneratedKeyColumns("id")
            .usingColumns(
                "userLoginId",
                "productid",
                "productName",
                "username",
                "inqueryCategory",
                "inqueryContent",
                "inqueryDate"
            )

            .executeAndReturnKey(
                mapOf(
                    "userLoginId" to authProfile.userLoginId,
                    "productid" to productid,
                    "productName" to productName,
                    "username" to authProfile.username,
                    "inqueryCategory" to inqueryCategory,
                    "inqueryContent" to inqueryContent,
                    "inqueryDate" to currentDateTime.format((formatter))
                )
            )
        val response = ProductInqueryRequest(
            id = insertedId.toLong(),
            userLoginId = authProfile.userLoginId,
            username = authProfile.username,
            productId = productid,
            productName = productName,
            inqueryCategory = request.inqueryCategory,
            inqueryContent = request.inqueryContent,
            inqueryAnswer = null,
            inqueryDate = currentDateTime.format(formatter)
        )
        inqueryService.createInquery(response)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                mapOf(
                    "inquery" to
                            ProductInqueryResponse(
                                insertedId.toLong(),
                                userLoginId,
                                username,
                                productid,
                                productName,
                                inqueryCategory,
                                inqueryContent,
                                dateTime.toString()
                            )
                )
            )


    }


}


