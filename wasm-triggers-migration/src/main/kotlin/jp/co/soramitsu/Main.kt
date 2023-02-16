package jp.co.soramitsu

import jp.co.soramitsu.Mode.REGISTER
import jp.co.soramitsu.iroha2.asDomainId
import jp.co.soramitsu.iroha2.asName
import jp.co.soramitsu.iroha2.client.Iroha2Client
import jp.co.soramitsu.iroha2.generated.datamodel.account.AccountId
import jp.co.soramitsu.iroha2.keyPairFromHex
import kotlinx.coroutines.runBlocking
import java.net.URL

fun main(vararg args: String): Unit = runBlocking {
    val username = if (REGISTER.mode == args[6].toInt()) args.getOrNull(11) else args.getOrNull(7)
    val password = if (REGISTER.mode == args[6].toInt()) args.getOrNull(12) else args.getOrNull(8)
    val credentials = when (username != null && password != null) {
        true -> "${username}:${password}"
        false -> null
    }

    fun isRegisterMode(vararg args: String): Boolean = args.size == 11 && REGISTER.mode == args[6].toInt()

    Helper(Iroha2Client(URL(args[0]), log = true, credentials = credentials))
        .update(
            args[1], // directory with WASM contracts
            AccountId(args[2].asName(), args[3].asDomainId()),
            keyPairFromHex(args[4], args[5]),
            args[6].toInt(), //mode
            if (isRegisterMode(*args)) args[7].toInt() else -1, //Repeats
            if (isRegisterMode(*args)) args[8].toInt() else -1, //Trigger type
            if (isRegisterMode(*args)) args[9] else "", //Technical account
            if (isRegisterMode(*args)) args[10] else "" //Argument for specific trigger type
        ).map { triggerId ->
            triggerId.name.string
        }.also { results ->
            println("$results have been updated")
        }
}
