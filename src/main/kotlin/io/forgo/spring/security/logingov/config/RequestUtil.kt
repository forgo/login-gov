package io.forgo.spring.security.logingov.config

import java.net.URI
import java.net.URL
import javax.servlet.http.HttpServletRequest

class RequestUtil {
    companion object {
        fun getURL(request: HttpServletRequest?, path: String): String {
            val url = URL(request?.requestURL.toString())
            val uri = URI(url.protocol, url.userInfo, url.host, url.port, path, null, null)
            return uri.toString()
        }
    }
}