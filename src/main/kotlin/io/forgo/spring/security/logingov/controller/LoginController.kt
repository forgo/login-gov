package io.forgo.spring.security.logingov.controller

import io.forgo.spring.security.logingov.config.SecurityConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ResolvableType
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import java.util.concurrent.atomic.AtomicLong
import org.springframework.ui.Model
import org.springframework.web.client.RestTemplate
import org.thymeleaf.util.StringUtils

@Controller
class LoginController {

    var clientAuthenticationURLs = mutableMapOf<String, String>()

    @Autowired
    lateinit var clientRegistrationRepository: ClientRegistrationRepository

    @Autowired
    lateinit var authorizedClientService: OAuth2AuthorizedClientService

    @GetMapping("/oauth_login")
    fun login(model: Model): String {
        var clientRegistrations: Iterable<ClientRegistration> = emptyList()
        val type: ResolvableType = ResolvableType.forInstance(clientRegistrationRepository).`as`(Iterable::class.java)
        if (type !== ResolvableType.NONE && ClientRegistration::class.java.isAssignableFrom(type.resolveGenerics()[0])) {
            @Suppress("UNCHECKED_CAST")
            clientRegistrations = clientRegistrationRepository as Iterable<ClientRegistration>
        }
        clientRegistrations.forEach { registration ->
            clientAuthenticationURLs[registration.clientName] = SecurityConfig.AUTHORIZATION_ENDPOINT + "/" + registration.registrationId
        }
        model.addAttribute("urls", clientAuthenticationURLs)
        return "oauth_login"
    }

    @GetMapping("/login_success")
    fun loginSuccess(model: Model, authentication: OAuth2AuthenticationToken): String {
        val client: OAuth2AuthorizedClient = authorizedClientService.loadAuthorizedClient(authentication.authorizedClientRegistrationId, authentication.name)
        val userInfoEndpointUri = client.clientRegistration.providerDetails.userInfoEndpoint.uri
        if(!StringUtils.isEmpty(userInfoEndpointUri)) {
            var restTemplate: RestTemplate = RestTemplate()
            var headers: HttpHeaders = HttpHeaders()
            headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + client.accessToken.tokenValue)

            val entity: HttpEntity<String> = HttpEntity("", headers)
            val response: ResponseEntity<Map<*,*>> = restTemplate.exchange(userInfoEndpointUri, HttpMethod.GET, entity, Map::class.java)
            val userAttributes: Map<*,*>? = response.body
            model.addAttribute("name", userAttributes?.get("name"))
        }
        return "login_success"
    }

    val counter = AtomicLong()

    @GetMapping("/")
    fun index(
            model: Model,
            @RegisteredOAuth2AuthorizedClient("logingov") authorizedClient: OAuth2AuthorizedClient,
            @AuthenticationPrincipal oAuth2User: OAuth2User
    ): String
    {
        model.addAttribute("userName", oAuth2User.name)
        model.addAttribute("clientName", authorizedClient.clientRegistration.clientName)
        model.addAttribute("userAttributes", oAuth2User.attributes)
        model.addAttribute("counter", counter.incrementAndGet().toString())
        return "index"
    }
}

