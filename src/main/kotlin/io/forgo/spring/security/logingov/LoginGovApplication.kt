package io.forgo.spring.security.logingov

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class LoginGovApplication

fun main(args: Array<String>) {
    SpringApplication.run(LoginGovApplication::class.java, *args)
}