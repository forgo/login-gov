package io.forgo.spring.security.logingov.config

import org.springframework.security.core.Authentication
import org.springframework.security.web.DefaultRedirectStrategy
import org.springframework.security.web.RedirectStrategy
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class LoginGovAuthenticationSuccessHandler : AuthenticationSuccessHandler {

    private val redirectStrategy: RedirectStrategy = DefaultRedirectStrategy()

    override fun onAuthenticationSuccess(request: HttpServletRequest?, response: HttpServletResponse?, authentication: Authentication?) {
        redirectStrategy.sendRedirect(request, response, SecurityConfig.LOGIN_SUCCESS_ENDPOINT)
    }
}