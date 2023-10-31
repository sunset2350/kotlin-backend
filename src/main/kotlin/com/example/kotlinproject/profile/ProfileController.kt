package com.example.kotlinproject.profile

import com.example.kotlinproject.auth.Auth
import com.example.kotlinproject.auth.AuthProfile

import com.example.kotlinproject.auth.Profiles
import org.jetbrains.exposed.sql.*

import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.sql.Connection
import java.util.*

@RestController
@RequestMapping("/user")

class ProfileController {

    private val POST_FILE_PATH = "files/post"


    @Auth
    @GetMapping("/profile")
    fun showProfile(@RequestAttribute authProfile: AuthProfile): List<ProfileResponse> =
        transaction(Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = false) {
            val query = when {
                authProfile.userLoginId != null -> Profiles.select {
                    (Profiles.userLoginId eq authProfile.userLoginId)
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
                    r[Profiles.introduction],
                    r[Profiles.uuidFileName]
                )
            }
        }

    @Auth
    @PutMapping("/update")
    fun editProfile(
        @RequestAttribute authProfile: AuthProfile,
        @RequestParam username: String,
        @RequestParam sex: String,
        @RequestParam birth: String,
        @RequestParam introduction: String,
        @RequestParam("file", required = false) file: Optional<MultipartFile>,

        ): ResponseEntity<ProfileResponse> {


        transaction {
            Profiles.select {
                (Profiles.userLoginId eq authProfile.userLoginId)
            }.firstOrNull()
        } ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()


        val dirPath = Paths.get(POST_FILE_PATH)

        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath)
        }





        if (file.isPresent && !file.get().isEmpty) {

            val filesList = mutableListOf<Map<String, String?>>()

            val uuidFileName =
                buildString {
                    append(UUID.randomUUID().toString())
                    append(".") // 확장자
                    append(file.get().originalFilename!!.split(".").last())
                }
            val filePath = dirPath.resolve(uuidFileName)

            val result = file.get().inputStream.use { stream ->
                Files.copy(stream, filePath, StandardCopyOption.REPLACE_EXISTING)
            }


            filesList.add(
                mapOf(
                    "uuidFileName" to uuidFileName,
                    "contentType" to file.get().contentType,
                    "originalFileName" to file.get().originalFilename
                )
            )


            transaction {

                for (fileInfo in filesList) {
                    val originalFileName = fileInfo["originalFileName"] as String
                    val uuidFileName = fileInfo["uuidFileName"] as String
                    val contentType = fileInfo["contentType"] as String


                    Profiles.update({ Profiles.userLoginId eq authProfile.userLoginId }) {
                        it[Profiles.username] = username
                        it[Profiles.nickname] = nickname
                        it[Profiles.sex] = sex
                        it[Profiles.birth] = birth
                        it[Profiles.introduction] = introduction
                        it[Profiles.originalFileName] = originalFileName
                        it[Profiles.uuidFileName] = uuidFileName
                        it[Profiles.contentType] = contentType
                    }
                }
            }
        }
        transaction {
            Profiles.update({ Profiles.userLoginId eq authProfile.userLoginId }) {
                it[Profiles.username] = username
                it[Profiles.nickname] = nickname
                it[Profiles.sex] = sex
                it[Profiles.birth] = birth
                it[Profiles.introduction] = introduction
            }
        }






        return ResponseEntity.status(HttpStatus.CREATED).build()
    }


}
