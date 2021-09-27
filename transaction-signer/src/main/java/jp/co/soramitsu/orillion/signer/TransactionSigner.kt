package jp.co.soramitsu.orillion.signer

import jp.co.soramitsu.iroha2.appendSignatures
import jp.co.soramitsu.iroha2.decode
import jp.co.soramitsu.iroha2.encode
import jp.co.soramitsu.iroha2.generated.datamodel.transaction.VersionedTransaction
import jp.co.soramitsu.iroha2.keyPairFromHex
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable
import org.bouncycastle.util.encoders.Hex

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
            publicKey, privateKey,
            EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519)
        )
        val decodedTransaction = transaction.decode(VersionedTransaction)
        val signedTransaction = decodedTransaction.appendSignatures(keyPair)
        println("Signed transaction content: $signedTransaction")

        val encoded = signedTransaction.encode(VersionedTransaction)
        println("Signed transaction (Hex): ${Hex.toHexString(encoded)}")
    }

}

fun main(args: Array<String>) {
    TransactionSigner().sign(args)
}
