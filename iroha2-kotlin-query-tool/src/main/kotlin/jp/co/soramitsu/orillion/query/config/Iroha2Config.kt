package jp.co.soramitsu.orillion.query.config

import org.springframework.context.annotation.Configuration
import java.net.URL

@Configuration
class Iroha2Config {
    var url: String = ""
    var basicAuth: String = ""
    var adminAccount: String = ""
    var adminPrivateKeyHex: String = ""
    var adminPublicKeyHex: String = ""
    val proxyUrl: URL = URL("http://localhost:8080/domain")
    val accountDelimiter: String = "@"
}
