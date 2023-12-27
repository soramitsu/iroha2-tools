package jp.co.soramitsu

import jp.co.soramitsu.Mode.REGISTER
import jp.co.soramitsu.iroha2.asDomainId
import jp.co.soramitsu.iroha2.asName
import jp.co.soramitsu.iroha2.client.Iroha2Client
import jp.co.soramitsu.iroha2.generated.AccountId
import jp.co.soramitsu.iroha2.keyPairFromHex
import kotlinx.coroutines.runBlocking
import java.net.URL

fun main(vararg args: String): Unit = runBlocking {
    val username = if (REGISTER.mode == args[8].toInt()) args.getOrNull(13) else args.getOrNull(9)
    val password = if (REGISTER.mode == args[8].toInt()) args.getOrNull(14) else args.getOrNull(10)
    val credentials = when (username != null && password != null) {
        true -> "$username:$password"
        false -> null
    }

    Helper(Iroha2Client(URL(args[0]), URL(args[1]), URL(args[2]), log = true, credentials = credentials)).update(
        args[3], // directory with WASM contracts
        AccountId(args[5].asDomainId(), args[4].asName()),
        keyPairFromHex(args[6], args[7]),
        args[8].toInt(), // mode
        if (isRegisterMode(*args)) args[9].toInt() else -1, // Repeats
        if (isRegisterMode(*args)) args[10].toInt() else -1, // Trigger type
        if (isRegisterMode(*args)) args[11] else "", // Technical account
        if (isRegisterMode(*args)) args[12] else "", // Argument for specific trigger type
    ).map { triggerId ->
        triggerId.name.string
    }.also { results ->
        println("$results have been updated")
    }
}

private fun isRegisterMode(vararg args: String): Boolean = args.size == 11 && REGISTER.mode == args[6].toInt()
