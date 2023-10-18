package com.example.myapp.configuration

import com.example.kotlinproject.auth.AuthInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableWebMvc
class WebMvcConfig(val authInterceptor: AuthInterceptor) : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry
            .addMapping("/**") // 모든 경로에 대해
            .allowedOrigins(
                "http://localhost:5000",
                "http://localhost:8080",
                )
            .allowedMethods("*")
            .allowedHeaders("*")


    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authInterceptor)
    }
}