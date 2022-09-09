package jp.co.soramitsu

import jp.co.soramitsu.iroha2.asDomainId
import jp.co.soramitsu.iroha2.asName
import jp.co.soramitsu.iroha2.asValue
import jp.co.soramitsu.iroha2.client.Iroha2Client
import jp.co.soramitsu.iroha2.generated.datamodel.account.AccountId
import jp.co.soramitsu.iroha2.generated.datamodel.trigger.Trigger
import jp.co.soramitsu.iroha2.generated.datamodel.trigger.TriggerId
import jp.co.soramitsu.iroha2.keyPairFromHex
import jp.co.soramitsu.iroha2.query.QueryBuilder
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.io.File
import java.net.URL
import java.security.KeyPair

fun main(vararg args: String): Unit = runBlocking {
    val credentials = when (args.getOrNull(6) != null && args.getOrNull(7) != null) {
        true -> "${args[6]}:${args[7]}"
        false -> null
    }

    Iroha2Client(URL(args[0]), log = true, credentials = credentials).update(
        args[1], // directory with WASM contracts
        AccountId(args[2].asName(), args[3].asDomainId()),
        keyPairFromHex(args[4], args[5])
    ).also { results ->
        println("$results have been updated")
    }
}

private suspend fun Iroha2Client.update(
    directory: String,
    admin: AccountId,
    keyPair: KeyPair
): List<String> {
    val wasmFiles = File(directory).walk()
        .filter { !it.isDirectory }
        .filter { it.name.endsWith(".wasm") }
        .associateBy { it.name }
    val triggerIds = QueryBuilder.findAllActiveTriggerIds()
        .account(admin)
        .buildSigned(keyPair)
        .let { this.sendQuery(it) }

    val idsValues = triggerIds.map { it.name.string }
    val wasmFileNames = wasmFiles.map { it.key.removeSuffix(".wasm") }
    if (!idsValues.containsAll(wasmFileNames)) {
        throw RuntimeException("Found triggers: $idsValues, provided files: $wasmFileNames")
    }

    wasmFiles.forEach { file ->
        val triggerId = TriggerId(file.key.removeSuffix(".wasm").asName())
        val trigger = QueryBuilder.findTriggerById(triggerId)
            .account(admin)
            .buildSigned(keyPair)
            .let { this.sendQuery(it) }

        this.unregisterTrigger(admin, keyPair, triggerId)
        this.registerTrigger(admin, keyPair, trigger, file.value.readBytes())
    }

    return wasmFileNames
}

private suspend fun Iroha2Client.unregisterTrigger(
    admin: AccountId,
    keyPair: KeyPair,
    triggerId: TriggerId,
    timeout: Long = 30000
) = this.sendTransaction {
    account(admin)
    unregisterTrigger(triggerId.name.string)
    buildSigned(keyPair)
}.also {
    withTimeout(timeout) {
        it.await()
    }
}

private suspend fun Iroha2Client.registerTrigger(
    admin: AccountId,
    keyPair: KeyPair,
    trigger: Trigger<*>,
    wasm: ByteArray,
    timeout: Long = 30000
) = this.sendTransaction {
    account(admin)
    registerWasmTrigger(
        trigger.id,
        wasm,
        trigger.action.repeats,
        admin,
        trigger.action.metadata,
        trigger.action.filter
    )
    setKeyValue(admin, trigger.id.name, trigger.id.name.string.asValue())
    buildSigned(keyPair)
}.also {
    withTimeout(timeout) {
        it.await()
    }
}
