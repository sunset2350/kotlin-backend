package com.example.kotlinproject.profile

import com.example.kotlinproject.auth.Auth
import com.example.kotlinproject.auth.AuthProfile

import com.example.kotlinproject.auth.Profiles
import com.example.kotlinproject.order.OrderMenu
import org.jetbrains.exposed.sql.*

import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.core.io.ResourceLoader
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.sql.Connection
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.io.path.Path

@RestController
@RequestMapping("/api/user")
class ProfileController(private val resourceLoader: ResourceLoader) {

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
        @RequestParam("file", required = false) file: Optional<MultipartFile>,
        @RequestParam username: String,
        @RequestParam nickname : String,
        @RequestParam sex: String,
        @RequestParam birth: String,
        @RequestParam introduction: String,
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


        println("시작")



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
            println("여기")


            filesList.add(
                mapOf(
                    "uuidFileName" to uuidFileName,
                    "contentType" to file.get().contentType,
                    "originalFileName" to file.get().originalFilename
                )
            )


            println(uuidFileName)
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
//                it[Profiles.originalFileName] = originalFileName
//                it[Profiles.uuidFileName] = uuidFileName
//                it[Profiles.contentType] = contentType
            }
        }






        return ResponseEntity.status(HttpStatus.CREATED).build()
    }


    @GetMapping("/files/{uuidFilename}")
    fun getProfileimg(@PathVariable uuidFilename : String) : ResponseEntity<Any>{
        val file = Paths.get("$POST_FILE_PATH/$uuidFilename").toFile()

        if(!file.exists()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }

        val mimeType = Files.probeContentType(file.toPath())
        val mediaType = MediaType.parseMediaType(mimeType)

        val resource = resourceLoader.getResource("file:$file")
        return ResponseEntity.ok().contentType(mediaType).body(resource)
    }

    @Auth
    @GetMapping("/amount")
    fun totalAmount(@RequestAttribute authProfile: AuthProfile): ResponseEntity<Any> {
        val result = transaction {
            OrderMenu.select {
                OrderMenu.userLoginId eq authProfile.userLoginId and
                        OrderMenu.Permission.eq("true")
            }.toList()
                .sumOf {
                    it[OrderMenu.productPrice]
                }


        }
        return ResponseEntity.ok(mapOf("totalAmount" to result))
    }

    @Auth
    @GetMapping("/amount/month")
    fun monthAmount(@RequestAttribute authProfile: AuthProfile) : ResponseEntity<Any> {
        val result = transaction {
            val monthlyData =
            OrderMenu.select{
                OrderMenu.userLoginId eq authProfile.userLoginId and
                        OrderMenu.Permission.eq("true")

            }.toList()
                .groupBy {
                    it[OrderMenu.OrderDate].substring(0,7)
                }.mapValues { (_, orders) ->
                    orders.sumOf {
                        it[OrderMenu.productPrice]
                    }
                }

            val allMonth = (1..12).map {it.toString().padStart(2,'0')}
            val resultData = allMonth.map{ month ->
                val key = "2023-$month"
                key to (monthlyData[key] ?: 0)
            }.toMap()
            mapOf("monthAaount" to resultData)
        }
        return ResponseEntity.ok(result)
    }

    @Auth
    @GetMapping("count")
    fun countOrder(@RequestAttribute authProfile: AuthProfile) : ResponseEntity<Any>{
        val result = transaction {
            OrderMenu.select {
                OrderMenu.userLoginId eq authProfile.userLoginId and
                        OrderMenu.Permission.eq("true")
            }.count()
        }
        return ResponseEntity.ok(mapOf("totalCount" to result))
    }

}
