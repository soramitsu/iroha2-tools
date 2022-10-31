package jp.co.soramitsu.signer

import jp.co.soramitsu.iroha2.appendSignatures
import jp.co.soramitsu.iroha2.generated.datamodel.transaction.VersionedSignedTransaction
import jp.co.soramitsu.iroha2.keyPairFromHex
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable
import org.bouncycastle.util.encoders.Hex
import java.io.File

class TransactionSigner {

    fun sign(args: Array<String>) {
        if (args.size < 3) {
            println("Specify public_key, private_key and transaction (as Hex) parameters")
            return
        }
        val publicKey: String = args[0]
        val privateKey: String = args[1]
        val transactionBase64: String = args[2]

        val transaction: ByteArray = try {
            Hex.decode(transactionBase64)
        } catch (e: IllegalArgumentException) {
            println("Could not decode transaction from hex format: $e")
            return
        }

        val keyPair = keyPairFromHex(
            publicKey,
            privateKey,
            EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519)
        )
        val decodedTransaction = transaction.let { VersionedSignedTransaction.decode(it) }
        val signedTransaction = decodedTransaction.appendSignatures(keyPair)

        val encoded = signedTransaction.let { VersionedSignedTransaction.encode(it) }
        println("Signed transaction (Hex): ${Hex.toHexString(encoded)}")
    }
}

fun main(args: Array<String>) {
    val file = File("/env/env.txt")
    var envArgs = args
    if (file.isFile) {
        envArgs = file.readText().split(",").toTypedArray()
    }
    TransactionSigner().sign(envArgs)
}
