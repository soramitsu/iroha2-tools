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
        File(args[0]),
        URL(args[1]),
        AccountId(args[2].asName(), args[3].asDomainId()),
        keyPairFromHex(args[4], args[5])
    )
}
