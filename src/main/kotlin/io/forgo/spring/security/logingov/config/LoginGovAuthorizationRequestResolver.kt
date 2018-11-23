package io.forgo.spring.security.logingov.config

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import javax.servlet.http.HttpServletRequest
import java.util.LinkedHashMap


class LoginGovAuthorizationRequestResolver(clientRegistryRepository: ClientRegistrationRepository) : OAuth2AuthorizationRequestResolver {

    private val REGISTRATION_ID_URI_VARIABLE_NAME = "registrationId"
    private var defaultAuthorizationRequestResolver: OAuth2AuthorizationRequestResolver = DefaultOAuth2AuthorizationRequestResolver(
            clientRegistryRepository, OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI
    )
    private val authorizationRequestMatcher: AntPathRequestMatcher = AntPathRequestMatcher(
            OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI + "/{" + REGISTRATION_ID_URI_VARIABLE_NAME + "}")

    override fun resolve(request: HttpServletRequest?): OAuth2AuthorizationRequest {
        val authorizationRequest: OAuth2AuthorizationRequest = defaultAuthorizationRequestResolver.resolve(request)
        return customAuthorizationRequest(authorizationRequest)
    }

    override fun resolve(request: HttpServletRequest?, clientRegistrationId: String?): OAuth2AuthorizationRequest {
        val authorizationRequest: OAuth2AuthorizationRequest = defaultAuthorizationRequestResolver.resolve(request, clientRegistrationId)
        return customAuthorizationRequest(authorizationRequest)
    }

    private fun customAuthorizationRequest(authorizationRequest: OAuth2AuthorizationRequest): OAuth2AuthorizationRequest {

        val registrationId: String = this.resolveRegistrationId(authorizationRequest)
        val additionalParameters = LinkedHashMap(authorizationRequest.additionalParameters)

        // set login.gov specific params
        if(registrationId == "logingov") {
            additionalParameters["dude"] = "whatever"
        }

        return OAuth2AuthorizationRequest
            .from(authorizationRequest)
            .additionalParameters(additionalParameters)
            .build()
    }

    private fun resolveRegistrationId(authorizationRequest: OAuth2AuthorizationRequest): String {
        return authorizationRequest.additionalParameters[OAuth2ParameterNames.REGISTRATION_ID] as String
    }

}