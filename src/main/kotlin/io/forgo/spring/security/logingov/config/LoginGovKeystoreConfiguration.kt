package io.forgo.spring.security.logingov.config

import io.forgo.spring.security.logingov.constants.LOGIN_GOV_REGISTRATION_ID
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("$LOGIN_GOV_REGISTRATION_ID.keystore")
class LoginGovKeystoreConfiguration {
    lateinit var alias: String
    lateinit var file: String
    lateinit var password: String
    lateinit var type: String
}