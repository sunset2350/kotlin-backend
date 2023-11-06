package com.example.kotlinproject.inquery

import com.example.kotlinproject.auth.Auth
import com.example.kotlinproject.auth.AuthProfile
import com.example.kotlinproject.review.reviewRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

import org.springframework.web.bind.annotation.RestController

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/inquery")
class InqueryController(
    private val inqueryService: InqueryService,

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
                rs.getString("inqueryCategory"),
                rs.getString("inqueryContent"),
                rs.getString("inqueryAnswer")
            )

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
        val (userLoginId,username, productid, inqueryCategory, inqueryContent) = request


        val insertedId = SimpleJdbcInsert(template)
            .withTableName("ProductInquery")
            .usingGeneratedKeyColumns("id")
            .usingColumns(
                "userLoginId",
                "productid",
                "username",
                "inqueryCategory",
                "inqueryContent",
                "inqueryDate"
            )

            .executeAndReturnKey(
                mapOf(
                    "userLoginId" to authProfile.userLoginId,
                    "productid" to productid,
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
            inqueryCategory = request.inqueryCategory,
            inqueryContent = request.inqueryContent,
            inqueryAnswer = null,
            inqueryDate = currentDateTime.format(formatter)
        )

        inqueryService.CreateInquery(response)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                mapOf(
                    "inquery" to
                            ProductInqueryResponse(
                                insertedId.toLong(),
                                userLoginId, username,productid, inqueryCategory, inqueryContent, dateTime.toString())
                )
            )


    }


}


