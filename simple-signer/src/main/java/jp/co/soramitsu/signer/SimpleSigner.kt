package jp.co.soramitsu.signer

import jp.co.soramitsu.iroha2.keyPairFromHex
import jp.co.soramitsu.iroha2.sign
import jp.co.soramitsu.iroha2.toHex
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class SimpleSigner

fun main(args: Array<String>) {
    if (args.size != 3) {
        println("Specify public_key, private_key and text to sign")
        return
    }
    val publicKey = args[0]
    val privateKey = args[1]
    val toSign = args[2]

    val keyPair = keyPairFromHex(publicKey, privateKey)

    val signature = keyPair.private.sign(toSign.toByteArray(Charsets.UTF_8)).toHex()

    println("Signed message (Hex): $signature")
}
