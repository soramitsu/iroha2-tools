package jp.co.genesis.config

import jp.co.soramitsu.iroha2.ACCOUNT_ID_DELIMITER
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@ConditionalOnProperty(
    prefix = "command.line.runner",
    value = ["enabled"],
    havingValue = "true",
    matchIfMissing = true
)
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

        val tokens = adminAccount.split(ACCOUNT_ID_DELIMITER)
        if (tokens.size != 2) {
            println("Specify adminAccount as USERNAME${ACCOUNT_ID_DELIMITER}DOMAIN")
            return
        }

        config.adminAccount = adminAccount
        config.adminPrivateKeyHex = adminPrivateKeyHex
        config.adminPublicKeyHex = adminPublicKeyHex
        config.username = username
        config.password = password
        config.url = peerUrl
    }
}
