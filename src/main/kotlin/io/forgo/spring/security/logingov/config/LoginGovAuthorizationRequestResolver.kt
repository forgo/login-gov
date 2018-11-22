package io.forgo.spring.security.logingov.config

import org.springframework.security.crypto.keygen.Base64StringKeyGenerator
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.security.web.util.UrlUtils
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.stereotype.Component
import org.springframework.util.Assert
import org.springframework.web.util.UriComponentsBuilder
import javax.servlet.http.HttpServletRequest
import java.util.Base64
import java.util.HashMap

@Component
class LoginGovAuthorizationRequestResolver(clientRegistrationRepository:ClientRegistrationRepository)
    : OAuth2AuthorizationRequestResolver {

    private val clientRegistrationRepository:ClientRegistrationRepository
    private val authorizationRequestMatcher:AntPathRequestMatcher
    private val stateGenerator = Base64StringKeyGenerator(Base64.getUrlEncoder())

    init{
        val authorizationRequestBaseUri = OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI
        Assert.notNull(clientRegistrationRepository, "clientRegistrationRepository cannot be null")
        Assert.hasText(authorizationRequestBaseUri, "authorizationRequestBaseUri cannot be empty")
        this.clientRegistrationRepository = clientRegistrationRepository
        val pattern = authorizationRequestBaseUri + "/{" + REGISTRATION_ID_URI_VARIABLE_NAME + "}"
//        val new_pattern = "$authorizationRequestBaseUri/{$REGISTRATION_ID_URI_VARIABLE_NAME}"
        this.authorizationRequestMatcher = AntPathRequestMatcher(pattern)
    }

    override fun resolve(request:HttpServletRequest):OAuth2AuthorizationRequest {
        val registrationId = this.resolveRegistrationId(request)
        val redirectUriAction = getAction(request, "login")
        return resolve(request, registrationId!!, redirectUriAction)
    }

    override fun resolve(request:HttpServletRequest, registrationId:String):OAuth2AuthorizationRequest {
        val redirectUriAction = getAction(request, "authorize")
        return resolve(request, registrationId, redirectUriAction)
    }

    private fun getAction(request:HttpServletRequest, defaultAction:String):String {
        return request.getParameter("action") ?: defaultAction
    }

    private fun resolve(request:HttpServletRequest, registrationId:String, redirectUriAction:String):OAuth2AuthorizationRequest {
        val clientRegistration =
                this.clientRegistrationRepository.findByRegistrationId(registrationId)
                ?: throw IllegalArgumentException("Invalid Client Registration with Id: $registrationId")
        val builder:OAuth2AuthorizationRequest.Builder

        builder = when(clientRegistration.authorizationGrantType) {
            AuthorizationGrantType.AUTHORIZATION_CODE -> OAuth2AuthorizationRequest.authorizationCode()
            AuthorizationGrantType.IMPLICIT -> OAuth2AuthorizationRequest.implicit()
            else -> throw IllegalArgumentException(("Invalid Authorization Grant Type (" +
                    clientRegistration.authorizationGrantType.value +
                    ") for Client Registration with Id: " + clientRegistration.registrationId))
        }

        val redirectUriStr = this.expandRedirectUri(request, clientRegistration, redirectUriAction)
        val additionalParameters = HashMap<String, Any>()
        additionalParameters[OAuth2ParameterNames.REGISTRATION_ID] = clientRegistration.registrationId
        return builder
                .clientId(clientRegistration.clientId)
                .authorizationUri(clientRegistration.providerDetails.authorizationUri)
                .redirectUri(redirectUriStr)
                .scopes(clientRegistration.scopes)
                .state(this.stateGenerator.generateKey())
                .additionalParameters(additionalParameters)
                .build()
    }

    private fun resolveRegistrationId(request:HttpServletRequest):String? {
        if (this.authorizationRequestMatcher.matches(request))
        {
            return this.authorizationRequestMatcher
                    .extractUriTemplateVariables(request)[REGISTRATION_ID_URI_VARIABLE_NAME]
        }
        return null
    }

    private fun expandRedirectUri(request:HttpServletRequest, clientRegistration:ClientRegistration, action:String):String {
        // Supported URI variables -> baseUrl, action, registrationId
        // Used in -> CommonOAuth2Provider.DEFAULT_REDIRECT_URL = "{baseUrl}/{action}/oauth2/code/{registrationId}"
        val uriVariables = HashMap<String, String>()
        uriVariables["registrationId"] = clientRegistration.registrationId
        val baseUrl = UriComponentsBuilder.fromHttpUrl(UrlUtils.buildFullRequestUrl(request))
                .replaceQuery(null)
                .replacePath(request.contextPath)
                .build()
                .toUriString()
        uriVariables["baseUrl"] = baseUrl
        return UriComponentsBuilder.fromUriString(clientRegistration.redirectUriTemplate)
                .buildAndExpand(uriVariables)
                .toUriString()
    }
    companion object {
        const val REGISTRATION_ID_URI_VARIABLE_NAME = "registrationId"
    }
}