package com.example.kotlinproject.profile

import com.example.kotlinproject.auth.Auth
import com.example.kotlinproject.auth.AuthProfile
import com.example.kotlinproject.auth.Profile
import com.example.kotlinproject.auth.Profiles
import jakarta.servlet.annotation.MultipartConfig
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.sql.Connection
import java.util.UUID

@RestController
@RequestMapping("/user")

class ProfileController {

    private val POST_FILE_PATH = "files/post"


//    @Auth
//    @GetMapping
//    fun fetch() = transaction {
//        Profiles.selectAll().map { r ->
//            ProfileResponse(
//                r[Profiles.id].toString(),r[Profiles.userid], r[Profiles.username],
//                r[Profiles.sex], r[Profiles.nickname],
//                r[Profiles.birth],r[Profiles.introduction],r[Profiles.image]
//
//            )
//        }
//    }

    @Auth
    @GetMapping("/profile")
    fun showProfile(@RequestAttribute authProfile: AuthProfile): List<ProfileResponse> =
        transaction(Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = false) {
            val query = when {
                authProfile.userLoginId != null -> Profiles.select {
                    (Profiles.id eq authProfile.id)
                }

                else -> Profiles.select {
                    (Profiles.id eq authProfile.id)
                }
            }

            query.map { r ->
                ProfileResponse(
                    r[Profiles.id].toString(),
                    r[Profiles.userLoginId],
                    r[Profiles.username],
                    r[Profiles.sex],
                    r[Profiles.nickname],
                    r[Profiles.birth],
                    r[Profiles.introduction]
                )
            }
        }

    @Auth
    @PostMapping("/{userid}")
    fun editProfile(
        @PathVariable userid: String,
        @RequestAttribute authProfile: AuthProfile,
        @RequestParam files: Array<MultipartFile>,
//        @RequestParam nickname : String,
//        @RequestParam username: String,
//        @RequestParam sex : String,
//        @RequestParam birth : String,
//        @RequestParam introduction : String

    ): ResponseEntity<ProfileResponse> {
        transaction {
            Profiles.select {
                (Profiles.userLoginId eq userid) and (Profiles.id eq authProfile.id)
            }.firstOrNull()
        } ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()


        val dirPath = Paths.get(POST_FILE_PATH)
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath)
        }


        val filesList = mutableListOf<Map<String, String?>>()

        runBlocking {
            files.forEach {
                launch {
                    println("filename: ${it.originalFilename}")

                    val uuidFileName =
                        buildString {
                            append(UUID.randomUUID().toString())
                            append(".") // 확장자
                            append(it.originalFilename!!.split(".").last())
                        }
                    val filePath = dirPath.resolve(uuidFileName)

                    it.inputStream.use {
                        Files.copy(it, filePath, StandardCopyOption.REPLACE_EXISTING)
                    }


                    filesList.add(
                        mapOf(
                            "uuidFileName" to uuidFileName,
                            "contentType" to it.contentType,
                            "originalFileName" to it.originalFilename
                        )
                    )
                }
            }

        }




        transaction {
            Profiles.replace  {

                it[this.username] = username
                it[this.nickname] = nickname
                it[this.sex] = sex
                it[this.birth] = birth
                it[this.introduction] = introduction
            }
        }

        transaction {
            Profiles.batchUpsert(filesList)  {
                this[Profiles.userLoginId] = Profiles.userLoginId
                this[Profiles.originalFileName] = it["originalFileName"] as String
                this[Profiles.uuidFileName] = it["uuidFileName"] as String
                this[Profiles.contentType] = it["contentType"] as String
            }
        }



        return ResponseEntity.status(HttpStatus.CREATED).build()
    }


}
