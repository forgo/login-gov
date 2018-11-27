package io.forgo.spring.security.logingov.config

import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class LoginGovLogoutSuccessHandler : SimpleUrlLogoutSuccessHandler() {

    // TODO: should we call the login.gov supported "RP-Initiated Logout" to invalidate login.gov's session too?
    // https://developers.login.gov/oidc/#logout
    override fun onLogoutSuccess(request: HttpServletRequest?, response: HttpServletResponse?, authentication: Authentication?) {

        // call to https://idp.int.identitysandbox.gov/openid_connect/logout would probably go here...

        super.onLogoutSuccess(request, response, authentication)
    }

}