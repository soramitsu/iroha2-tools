package jp.co.genesis

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Iroha2KotlinQueryToolApplication

fun main(args: Array<String>) {
    if (args.size < 6) {
        println("Specify username, password, peerUrl, adminAccount, adminPrivateKeyHex and adminPublicKeyHex")
        return
    }

    runApplication<Iroha2KotlinQueryToolApplication>(*args)
}
