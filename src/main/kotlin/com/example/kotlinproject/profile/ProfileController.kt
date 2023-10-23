package com.example.kotlinproject.profile

import com.example.kotlinproject.auth.Auth
import com.example.kotlinproject.auth.AuthProfile
import com.example.kotlinproject.auth.Profiles
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.sql.Connection

@RestController
@RequestMapping("/user")
class ProfileController {

    @Auth
    @GetMapping
    fun fetch() = transaction {
        Profiles.selectAll().map { r ->
            ProfileResponse(
                r[Profiles.id].toString(),r[Profiles.userid], r[Profiles.username],
                r[Profiles.sex], r[Profiles.nickname],
                r[Profiles.birth]

            )
        }
    }

    @Auth
    @GetMapping("/profile/{id}")
    fun showProfile(@RequestAttribute authProfile: AuthProfile): List<ProfileResponse> =
        transaction(Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true) {

            val query = when {
                authProfile.userid != null -> Profiles.select {
                    (Profiles.id eq authProfile.id)
                }

                else -> Profiles.select {
                    (Profiles.username eq authProfile.username)

                };
            }

            query.map { r ->
                ProfileResponse(
                    r[Profiles.id].toString(),
                    r[Profiles.userid],
                    r[Profiles.username],
                    r[Profiles.sex],
                    r[Profiles.nickname],
                    r[Profiles.birth]
                )
            }
        }

}
