package io.forgo.spring.security.logingov.config

import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import javax.servlet.http.HttpServletRequest

class RequestUtil {
    companion object {
        fun getURL(request: HttpServletRequest?, path: String): String {
            val requestURI = URI(request?.requestURL.toString())
            val contextURI = URI(
                    requestURI.scheme,
                    requestURI.authority,
                    request?.contextPath,
                    null,
                    null
            )
            return UriComponentsBuilder.fromUri(contextURI).path(path).toUriString()
        }
    }
}