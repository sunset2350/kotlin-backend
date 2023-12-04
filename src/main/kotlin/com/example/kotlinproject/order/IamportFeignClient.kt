package com.example.kotlinproject.order

import org.jetbrains.exposed.sql.Column
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@FeignClient(name = "productClient2", url = "https://api.iamport.kr")
interface IamportFeignClient {

    @RequestMapping(value = ["/users/getToken"], method = [RequestMethod.POST], consumes = ["application/json"])
    fun getToken(@RequestBody request: TokenRequest) : TokenResponse


    @RequestMapping(value = ["/payments/cancel"], method = [RequestMethod.POST], consumes = ["application/json"])
    fun cancelPaid(@RequestBody cancelPaidInfo: cancelPaidInfo, @RequestHeader("Authorization") token: String) : cancelResponse

}
