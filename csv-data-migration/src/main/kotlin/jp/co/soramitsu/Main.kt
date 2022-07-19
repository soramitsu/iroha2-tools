package jp.co.soramitsu

import jp.co.soramitsu.iroha2.asDomainId
import jp.co.soramitsu.iroha2.asName
import jp.co.soramitsu.iroha2.generated.datamodel.account.AccountId
import jp.co.soramitsu.iroha2.keyPairFromHex
import kotlinx.coroutines.runBlocking
import java.io.File
import java.net.URL

fun main(vararg args: String) = runBlocking {
    val converter = Converter()
    converter.sendToIroha(
        File(args[0]), // csv
        URL(args[1]), // peer URL
        AccountId(args[2].asName(), args[3].asDomainId()), // transactions send behalf of this account
        keyPairFromHex(args[4], args[5]), // key pair to sign transactions
        (args.getOrNull(6) != null && args.getOrNull(7) != null)
            .takeIf { it }
            ?.let { "${args[6]}:${args[7]}" } // credentials to basic auth
    )
}
