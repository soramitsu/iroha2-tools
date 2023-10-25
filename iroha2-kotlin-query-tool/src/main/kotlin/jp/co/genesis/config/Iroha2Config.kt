package jp.co.genesis.config

import org.springframework.context.annotation.Configuration

@Configuration
class Iroha2Config {
    var url: String = ""
    var username: String = ""
    var password: String = ""
    var adminAccount: String = ""
    var adminPrivateKeyHex: String = ""
    var adminPublicKeyHex: String = ""
}
