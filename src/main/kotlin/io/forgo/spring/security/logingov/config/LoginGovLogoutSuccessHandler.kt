package io.forgo.spring.security.logingov.config

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler
import org.springframework.util.StringUtils
import org.springframework.web.client.RestTemplate
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class LoginGovLogoutSuccessHandler(authorizedClientService: OAuth2AuthorizedClientService) : SimpleUrlLogoutSuccessHandler() {

    private val authorizedClientService: OAuth2AuthorizedClientService = authorizedClientService

    // TODO: should we call the login.gov supported "RP-Initiated Logout" to invalidate login.gov's session too?
    // https://developers.login.gov/oidc/#logout
    override fun onLogoutSuccess(request: HttpServletRequest?, response: HttpServletResponse?, authentication: Authentication?) {
        val authenticationToken: OAuth2AuthenticationToken = authentication as OAuth2AuthenticationToken
        authenticationToken.principal
        val user: OidcUser = authenticationToken.principal as OidcUser
        val idToken: OidcIdToken = user.idToken

//        val logoutEndpointUri = "https://idp.int.identitysandbox.gov/openid_connect/logout"
//        if (!StringUtils.isEmpty(logoutEndpointUri)) {
//            val state: String = UUID.randomUUID().toString()
//            var restTemplate = RestTemplate()
//            var headers = HttpHeaders()
//            val entity: HttpEntity<String> = HttpEntity("", headers)
//            restTemplate.exchange(logoutEndpointUri, HttpMethod.GET, entity, )
//            val response: ResponseEntity<Map<*, *>> = restTemplate.exchange(logoutEndpointUri, HttpMethod.GET, entity, Map::class.java)
//        }

        redirectStrategy.sendRedirect(request, response, SecurityConfig.LOGOUT_SUCCESS_ENDPOINT)
    }

}