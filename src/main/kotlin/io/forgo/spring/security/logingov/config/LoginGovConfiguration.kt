package io.forgo.spring.security.logingov.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("logingov")
class LoginGovConfiguration {
    var keystore: MutableMap<String, String?> = mutableMapOf()
    lateinit var allowedOrigin: String
    lateinit var loginSuccessRedirect: String
    lateinit var logoutSuccessRedirect: String
}