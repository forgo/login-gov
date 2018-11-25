package io.forgo.spring.security.logingov.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.web.server.SslStoreProvider
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate
import javax.xml.bind.DatatypeConverter

@EnableWebSecurity
class SecurityConfig @Autowired constructor(serverProperties: ServerProperties) : WebSecurityConfigurerAdapter() {
    
    @Autowired
    lateinit var clientRegistrationRepository: ClientRegistrationRepository

    private final val sslUtil: SSLUtil = SSLUtil(serverProperties)

    companion object {
        const val LOGIN_ENDPOINT = "/oauth_login"
        const val LOGIN_SUCCESS_ENDPOINT = "/login_success"
        const val LOGIN_FAILURE_ENDPOINT = "/login_failure"
        const val AUTHORIZATION_ENDPOINT = "/oauth2/authorize_client"
        const val LOGOUT_ENDPOINT = "/logout"
        const val LOGOUT_SUCCESS_ENDPOINT = "/"
    }

    override fun configure(http: HttpSecurity) {
        http.authorizeRequests()
            // login, login failure, and index are allowed by anyone
            .antMatchers(LOGIN_ENDPOINT, LOGIN_FAILURE_ENDPOINT, "/")
                .permitAll()
            // any other requests are allowed by an authenticated user
            .anyRequest()
                .authenticated()
            .and()
            // custom logout behavior
            .logout()
                .logoutRequestMatcher(AntPathRequestMatcher(LOGOUT_ENDPOINT))
                .logoutSuccessUrl(LOGOUT_SUCCESS_ENDPOINT)
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
            .and()
            // configure authentication support using an OAuth 2.0 and/or OpenID Connect 1.0 Provider
            .oauth2Login()
//                .loginPage(LOGIN_ENDPOINT)
                .authorizationEndpoint()
                .authorizationRequestResolver(LoginGovAuthorizationRequestResolver(clientRegistrationRepository))
//                .baseUri(AUTHORIZATION_ENDPOINT)
                .authorizationRequestRepository(authorizationRequestRepository())
                .and()
                .tokenEndpoint()
                .accessTokenResponseClient(accessTokenResponseClient())
                .and()
                .defaultSuccessUrl(LOGIN_SUCCESS_ENDPOINT)
                .failureUrl(LOGIN_FAILURE_ENDPOINT)
    }

    @Bean
    fun authorizationRequestRepository(): AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
        return HttpSessionOAuth2AuthorizationRequestRepository()
    }

    @Bean
    fun accessTokenResponseClient(): OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {
        val accessTokenResponseClient = DefaultAuthorizationCodeTokenResponseClient()
        accessTokenResponseClient.setRequestEntityConverter(CustomTokenRequestConverter(clientRegistrationRepository, sslUtil))
        return accessTokenResponseClient
    }
}