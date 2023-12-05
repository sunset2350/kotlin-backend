package com.example.kotlinproject.auth

import com.example.kotlinproject.auth.util.JwtUtil
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("/api/auth")
class AuthController(private val service: AuthService) {

    @PostMapping("/sign")
    fun signUp(@RequestBody req: SignupRequest): ResponseEntity<Long> {

        val profileId = service.createIdentity(req)
        if (profileId > 0) {
            return ResponseEntity.status(HttpStatus.CREATED).body(profileId)

        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(profileId)
        }
    }

    @PostMapping(value = ["/signin"])
    fun signIn(
        @RequestParam userLoginId: String,
        @RequestParam userPassword: String,
        res: HttpServletResponse,
        @RequestHeader(value = "referer", required = false) referer: String,

    ): ResponseEntity<*> {

        val home = "${referer.split("/")[0]}//${referer.split("/")[2]}"
        val (result, message) = service.authenticate(userLoginId, userPassword)
        println(home)


        if (result) {
            val cookie = Cookie("token", message)
            cookie.path = "/"
            cookie.maxAge = (JwtUtil.TOKEN_TIMEOUT / 1000L).toInt()
            cookie.domain = referer.split("/")[2].split(":")[0]

            res.addCookie(cookie)


            return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(
                    ServletUriComponentsBuilder
                        .fromHttpUrl(home)
                        .build().toUri()
                )

                .build<Any>()

        }
        return ResponseEntity
            .status(HttpStatus.FOUND)
            .location(
                ServletUriComponentsBuilder
                    .fromHttpUrl("$home?err=$message")
                    .build()
                    .toUri()
            ).build<Any>()


    }

}