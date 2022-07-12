package jp.co.genesis.proxy

import jp.co.genesis.config.Iroha2Config
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import javax.servlet.http.HttpServletRequest

@RestController
class ProxyController(val config: Iroha2Config) {

    @PostMapping("/domain/query")
    fun getDomains(request: HttpServletRequest): ResponseEntity<ByteArray> {
        val byteArray = request.inputStream.readAllBytes()
        val rest = RestTemplate()

        val httpHeaders = HttpHeaders()
        httpHeaders.setBasicAuth(config.basicAuth)
        val httpEntity = HttpEntity(byteArray, httpHeaders)

        return rest.exchange(
            "${config.url}/query",
            HttpMethod.POST,
            httpEntity,
            ByteArray::class.java
        )
    }
}
