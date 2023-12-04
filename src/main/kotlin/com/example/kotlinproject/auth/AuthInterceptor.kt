package com.example.kotlinproject.auth


import com.example.kotlinproject.auth.util.JwtUtil
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component

import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import java.lang.reflect.Method

@Component
class AuthInterceptor : HandlerInterceptor {
    // 토큰 검증 및 subject/claim(토큰 내부의 데이터)를 객체화
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        // 1. 요청을 처리할 컨트롤 메서드에 @Auth 어노테이션이 있는지 확인
        // HTTP요청을 처리하는 메서드인지 확인
        if (handler is HandlerMethod) {
            val handlerMethod: HandlerMethod = handler
            val method: Method = handlerMethod.method
            println(method)
            if(method.getAnnotation(Auth::class.java) == null) {
                return true
            }

            val token: String = request.getHeader("Authorization")
            println(token)
            if (token.isEmpty()) {
                response.status = 401
                return false
            }

            val profile: AuthProfile? = JwtUtil.validateToken(token.replace("Bearer ", ""))
            if (profile == null) {
                response.status = 401
                return false
            }

            println(profile)
            request.setAttribute("authProfile", profile)
            return true
        }
        return true
    }
}
