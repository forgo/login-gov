package io.forgo.spring.security.logingov.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
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
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@EnableWebSecurity
class SecurityConfig @Autowired constructor(loginGovConfiguration: LoginGovConfiguration) : WebSecurityConfigurerAdapter() {

    @Autowired
    lateinit var clientRegistrationRepository: ClientRegistrationRepository

    private final val keystore: MutableMap<String, String?> = loginGovConfiguration.keystore
    private final val keystoreUtil: KeystoreUtil = KeystoreUtil(
            keyStore = keystore["file"],
            keyStorePassword = keystore["password"],
            keyAlias = keystore["alias"],
            keyPassword = null,
            keyStoreType = keystore["type"]
    )
    private final val allowedOrigin: String = loginGovConfiguration.allowedOrigin

    companion object {
        const val LOGIN_ENDPOINT = DefaultLoginPageGeneratingFilter.DEFAULT_LOGIN_PAGE_URL
        const val LOGIN_SUCCESS_ENDPOINT = "/login_success"
        const val LOGIN_FAILURE_ENDPOINT = "/login_failure"
        const val LOGIN_PROFILE_ENDPOINT = "/login_profile"
        const val LOGOUT_ENDPOINT = "/logout"
        const val LOGOUT_SUCCESS_ENDPOINT = "/logout_success"
    }

    override fun configure(http: HttpSecurity) {
        http.authorizeRequests()
            // login, login failure, and index are allowed by anyone
            .antMatchers(
                    LOGIN_ENDPOINT,
                    LOGIN_SUCCESS_ENDPOINT,
                    LOGIN_PROFILE_ENDPOINT,
                    LOGIN_FAILURE_ENDPOINT,
                    LOGOUT_ENDPOINT,
                    LOGOUT_SUCCESS_ENDPOINT,
                    "/"
            )
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
                .logoutSuccessHandler(LoginGovLogoutSuccessHandler())
            .and()
            // configure authentication support using an OAuth 2.0 and/or OpenID Connect 1.0 Provider
            .oauth2Login()
                .authorizationEndpoint()
                .authorizationRequestResolver(LoginGovAuthorizationRequestResolver(clientRegistrationRepository))
                .authorizationRequestRepository(authorizationRequestRepository())
                .and()
                .tokenEndpoint()
                .accessTokenResponseClient(accessTokenResponseClient())
                .and()
                .failureUrl(LOGIN_FAILURE_ENDPOINT)
                .successHandler(LoginGovAuthenticationSuccessHandler())
    }

    @Bean
    fun corsFilter(): CorsFilter {
        // fix OPTIONS preflight login profile request failure with 403 Invalid CORS request
        val config = CorsConfiguration()
        config.addAllowedOrigin(allowedOrigin)
        config.allowCredentials = true
        config.allowedHeaders = listOf("x-auth-token", "Authorization", "cache", "Content-Type")
        config.addAllowedMethod(HttpMethod.OPTIONS)
        config.addAllowedMethod(HttpMethod.GET)

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration(LOGIN_PROFILE_ENDPOINT, config)

        return CorsFilter(source)
    }

    @Bean
    fun authorizationRequestRepository(): AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
        return HttpSessionOAuth2AuthorizationRequestRepository()
    }

    @Bean
    fun accessTokenResponseClient(): OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {
        val accessTokenResponseClient = DefaultAuthorizationCodeTokenResponseClient()
        accessTokenResponseClient.setRequestEntityConverter(LoginGovTokenRequestConverter(clientRegistrationRepository, keystoreUtil))
        return accessTokenResponseClient
    }
}