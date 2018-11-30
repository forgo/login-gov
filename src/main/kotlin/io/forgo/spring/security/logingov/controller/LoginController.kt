package io.forgo.spring.security.logingov.controller

import io.forgo.spring.security.logingov.config.LoginGovConfiguration
import io.forgo.spring.security.logingov.config.SecurityConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.client.RestTemplate
import org.thymeleaf.util.StringUtils
import javax.servlet.http.HttpServletResponse

@Controller
class LoginController {

    @Autowired
    lateinit var loginGovConfiguration: LoginGovConfiguration

    @Autowired
    lateinit var authorizedClientService: OAuth2AuthorizedClientService

    @GetMapping(SecurityConfig.LOGIN_PROFILE_ENDPOINT)
    @ResponseBody
    fun loginProfile(authentication: OAuth2AuthenticationToken?): HashMap<String, Any?> {
        if(authentication == null) {
            return hashMapOf()
        }
        else {
            val client: OAuth2AuthorizedClient = authorizedClientService.loadAuthorizedClient(authentication.authorizedClientRegistrationId, authentication.name)
            val userInfoEndpointUri = client.clientRegistration.providerDetails.userInfoEndpoint.uri
            var email: Any? = null
            if (!StringUtils.isEmpty(userInfoEndpointUri)) {
                var restTemplate = RestTemplate()
                var headers = HttpHeaders()
                headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + client.accessToken.tokenValue)

                val entity: HttpEntity<String> = HttpEntity("", headers)
                val response: ResponseEntity<Map<*, *>> = restTemplate.exchange(userInfoEndpointUri, HttpMethod.GET, entity, Map::class.java)
                val userAttributes: Map<*, *>? = response.body
                email = userAttributes?.get("email")
            }
            return hashMapOf("email" to email)
        }
    }

    @RequestMapping(SecurityConfig.LOGIN_SUCCESS_ENDPOINT)
    fun loginSuccess(httpServletResponse: HttpServletResponse) {
        httpServletResponse.sendRedirect(loginGovConfiguration.loginSuccessRedirect)
    }

    @RequestMapping(SecurityConfig.LOGOUT_SUCCESS_ENDPOINT)
    fun logoutSuccess(httpServletResponse: HttpServletResponse) {
        httpServletResponse.sendRedirect(loginGovConfiguration.logoutSuccessRedirect)
    }
}

