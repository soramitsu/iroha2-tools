package jp.co.soramitsu

import jp.co.soramitsu.iroha2.asDomainId
import jp.co.soramitsu.iroha2.asName
import jp.co.soramitsu.iroha2.client.Iroha2Client
import jp.co.soramitsu.iroha2.generated.datamodel.account.AccountId
import jp.co.soramitsu.iroha2.keyPairFromHex
import kotlinx.coroutines.runBlocking
import java.net.URL

fun main(vararg args: String): Unit = runBlocking {
    val credentials = when (args.getOrNull(6) != null && args.getOrNull(7) != null) {
        true -> "${args[6]}:${args[7]}"
        false -> null
    }
    Helper(Iroha2Client(URL(args[0]), log = true, credentials = credentials))
        .update(
            args[1], // directory with WASM contracts
            AccountId(args[2].asName(), args[3].asDomainId()),
            keyPairFromHex(args[4], args[5])
        ).map { triggerId ->
            triggerId.name.string
        }.also { results ->
            println("$results have been updated")
        }
}
