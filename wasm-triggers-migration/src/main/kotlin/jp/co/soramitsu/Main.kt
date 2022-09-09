package jp.co.soramitsu

import jp.co.soramitsu.iroha2.asDomainId
import jp.co.soramitsu.iroha2.asName
import jp.co.soramitsu.iroha2.asValue
import jp.co.soramitsu.iroha2.client.Iroha2Client
import jp.co.soramitsu.iroha2.generated.datamodel.account.AccountId
import jp.co.soramitsu.iroha2.keyPairFromHex
import jp.co.soramitsu.iroha2.query.QueryBuilder
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.io.File
import java.net.URL
import java.security.KeyPair

fun main(vararg args: String) = runBlocking {
    val credentials = when (args.getOrNull(6) != null && args.getOrNull(7) != null) {
        true -> "${args[6]}:${args[7]}"
        false -> null
    }

    Iroha2Client(URL(args[0]), log = true, credentials = credentials).update(
        args[1], // directory with WASM contracts
        AccountId(args[2].asName(), args[3].asDomainId()),
        keyPairFromHex(args[4], args[5])
    )
}

suspend fun Iroha2Client.update(
    directory: String,
    admin: AccountId,
    keyPair: KeyPair
) {
    val wasmFiles = File(directory).walk()
        .filter { !it.isDirectory }
        .filter { it.name.endsWith(".wasm") }
        .associateBy { it.name }
    val triggerIds = QueryBuilder.findAllActiveTriggerIds()
        .account(admin)
        .buildSigned(keyPair)
        .let { this.sendQuery(it) }

    val idsValues = triggerIds.map { it.name.string }
    val wasmFileNames = wasmFiles.map { it.key }
    if (idsValues.containsAll(wasmFileNames)) {
        throw RuntimeException("Found triggers: $idsValues, provided files: $wasmFileNames")
    }

    triggerIds.forEach { triggerId ->
        val trigger = QueryBuilder.findTriggerById(triggerId)
            .account(admin)
            .buildSigned(keyPair)
            .let { this.sendQuery(it) }

        this.sendTransaction {
            account(admin)
            unregisterTrigger(triggerId.name.string)
            buildSigned(keyPair)
        }.also {
            withTimeout(30000) {
                it.await()
            }
        }

        this.sendTransaction {
            account(admin)
            registerWasmTrigger(
                triggerId,
                wasmFiles["${triggerId.name.string}.wasm"]!!.readBytes(),
                trigger.action.repeats,
                admin,
                trigger.action.metadata,
                trigger.action.filter
            )
            setKeyValue(admin, triggerId.name, triggerId.name.string.asValue())
            buildSigned(keyPair)
        }.also {
            withTimeout(30000) {
                it.await()
            }
        }
    }
}
