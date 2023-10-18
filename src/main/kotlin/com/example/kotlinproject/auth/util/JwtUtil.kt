package com.example.kotlinproject.auth.util

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import com.example.kotlinproject.auth.AuthProfile
import java.util.*

object JwtUtil {
    var secret = "your-secret"

    val TOKEN_TIMEOUT = (1000 * 60 * 60 * 24 * 7).toLong()

    fun createToken(id: Long,userId: String, userName: String, nickName: String): String? {
        val now = Date()

        val exp = Date(now.time+ TOKEN_TIMEOUT)
        val algorithm = Algorithm.HMAC256(secret)

        return JWT.create()
            .withSubject(id.toString())
            .withClaim("userId",userId)
            .withClaim("userName",userName)
            .withClaim("nickName",nickName)
            .withIssuedAt(now)
            .withExpiresAt(exp)
            .sign(algorithm)
    }

    fun validateToken(token : String) : AuthProfile? {
        val algorithm = Algorithm.HMAC256(secret)
        val verifier: JWTVerifier = JWT.require(algorithm).build()

        return try {
            val decodedJWT: DecodedJWT = verifier.verify(token)
            // 토큰 검증 제대로 된 상황
            // 토큰 페이로드(데이터, subject/claim)를 조회
            val id: Long = java.lang.Long.valueOf(decodedJWT.getSubject())
            val nickName: String = decodedJWT
                .getClaim("nickName").asString()
            val userName: String = decodedJWT
                .getClaim("userName").asString()
            val userId : String = decodedJWT
                .getClaim("userId").asString()

            AuthProfile(id, nickName, userName,userId)
        } catch (e: JWTVerificationException) {
            // 토큰 검증 오류 상황
            null
        }

    }
}