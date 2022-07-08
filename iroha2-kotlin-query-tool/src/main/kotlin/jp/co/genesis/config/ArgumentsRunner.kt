package jp.co.genesis.config

import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.util.Base64

@Component
class ArgumentsRunner(val config: Iroha2Config) : CommandLineRunner {

    override fun run(vararg args: String?) {
        val username: String? = args[0]
        val password: String? = args[1]
        val peerUrl: String? = args[2]
        val adminAccount: String? = args[3]
        val adminPrivateKeyHex: String? = args[4]
        val adminPublicKeyHex: String? = args[5]

        if (username == null) {
            println("Specify username")
            return
        }
        if (password == null) {
            println("Specify password")
            return
        }
        if (peerUrl == null) {
            println("Specify peerUrl")
            return
        }
        if (adminAccount == null) {
            println("Specify adminAccount")
            return
        }
        if (adminPrivateKeyHex == null) {
            println("Specify adminPrivateKeyHex")
            return
        }
        if (adminPublicKeyHex == null) {
            println("Specify adminPublicKeyHex")
            return
        }

        val encode: String = try {
            Base64.getEncoder().encodeToString("$username:$password".toByteArray())
        } catch (e: Exception) {
            println("Failed to encode username and password: ${e.message}")
            return
        }

        val tokens = adminAccount.split(config.accountDelimiter)
        if (tokens.size != 2) {
            println("Specify adminAccount as USERNAME${config.accountDelimiter}DOMAIN")
            return
        }

        config.adminAccount = adminAccount
        config.adminPrivateKeyHex = adminPrivateKeyHex
        config.adminPublicKeyHex = adminPublicKeyHex
        config.basicAuth = encode
        config.url = peerUrl
    }
}
