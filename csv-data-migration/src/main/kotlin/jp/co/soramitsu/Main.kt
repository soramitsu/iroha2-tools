package jp.co.soramitsu

import jp.co.soramitsu.iroha2.asDomainId
import jp.co.soramitsu.iroha2.asName
import jp.co.soramitsu.iroha2.client.Iroha2Client
import jp.co.soramitsu.iroha2.generated.datamodel.account.AccountId
import jp.co.soramitsu.iroha2.keyPairFromHex
import jp.co.soramitsu.iroha2.query.QueryBuilder
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File
import java.net.URL

fun main(vararg args: String): Unit = runBlocking {
    val converter = Converter()
    val client = Iroha2Client(URL(args[1]), log = true)
    val admin = AccountId(args[2].asName(), args[3].asDomainId())
    val keyPair = keyPairFromHex(args[4], args[5])

    repeat(1) {
        converter.sendToIroha(
            client,
            File(args[0]), // csv
            admin, // transactions send behalf of this account
            keyPairFromHex(args[4], args[5]) // key pair to sign transactions
        )
    }

    delay(1000)
    QueryBuilder.findAllAssets()
        .account(admin)
        .buildSigned(keyPair)
        .let { client.sendQuery(it) }
        .also { println(it.size) }
}
