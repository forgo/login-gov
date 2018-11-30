package io.forgo.spring.security.logingov.config

import io.forgo.spring.security.logingov.constants.LOGIN_GOV_LOGOUT_ENDPOINT
import io.forgo.spring.security.logingov.constants.LOGIN_GOV_LOGOUT_PARAM_ID_TOKEN_HINT
import io.forgo.spring.security.logingov.constants.LOGIN_GOV_LOGOUT_PARAM_POST_LOGOUT_REDIRECT_URI
import io.forgo.spring.security.logingov.constants.LOGIN_GOV_LOGOUT_PARAM_STATE
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler
import org.springframework.web.util.UriComponentsBuilder
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class LoginGovLogoutSuccessHandler : SimpleUrlLogoutSuccessHandler() {

    override fun onLogoutSuccess(request: HttpServletRequest?, response: HttpServletResponse?, authentication: Authentication?) {

        // extrapolate id_token from original token response (needed for logout request)
        // Authentication -> OAuth2AuthenticationToken -> OidcUser -> OidcIdToken
        // This cast is possible because of the 'openid' scope/flow in the client registration config
        val authenticationToken: OAuth2AuthenticationToken = authentication as OAuth2AuthenticationToken
        val user: OidcUser = authenticationToken.principal as OidcUser
        val idToken: OidcIdToken = user.idToken
        val idTokenHint: String = idToken.tokenValue

        // Call the login.gov supported "RP-Initiated Logout" to invalidate login.gov's session
        // https://developers.login.gov/oidc/#logout
        val state: String = UUID.randomUUID().toString()
        val postLogoutRedirectUri: String = RequestUtil.getURL(request, SecurityConfig.LOGOUT_SUCCESS_ENDPOINT)

        val builder: UriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(LOGIN_GOV_LOGOUT_ENDPOINT)
                .queryParam(LOGIN_GOV_LOGOUT_PARAM_ID_TOKEN_HINT, idTokenHint)
                .queryParam(LOGIN_GOV_LOGOUT_PARAM_POST_LOGOUT_REDIRECT_URI, postLogoutRedirectUri)
                .queryParam(LOGIN_GOV_LOGOUT_PARAM_STATE, state)

        redirectStrategy.sendRedirect(request, response, builder.toUriString())
    }

}