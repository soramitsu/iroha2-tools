package jp.co.soramitsu

import jp.co.soramitsu.iroha2.client.Iroha2Client
import jp.co.soramitsu.iroha2.generated.datamodel.account.AccountId
import jp.co.soramitsu.iroha2.generated.datamodel.trigger.TriggerId
import jp.co.soramitsu.iroha2.query.QueryBuilder
import kotlinx.coroutines.withTimeout
import java.io.File
import java.security.KeyPair

class Helper(private val client: Iroha2Client) {

    suspend fun update(
        directory: String,
        admin: AccountId,
        keyPair: KeyPair
    ): List<TriggerId> {
        val wasmFiles = File(directory).walk()
            .filter { !it.isDirectory }
            .filter { it.name.endsWith(".wasm") }
            .associateBy { it.name }
        val triggerIds = QueryBuilder.findAllActiveTriggerIds()
            .account(admin)
            .buildSigned(keyPair)
            .let { client.sendQuery(it) }

        val idsValues = triggerIds.map { it.name.string }
        val wasmFileNames = wasmFiles.map { it.key.removeSuffix(".wasm") }
        if (!wasmFileNames.all { n -> idsValues.any { id -> id.startsWith(n) } }) {
            throw RuntimeException("Found triggers: $idsValues, provided files: $wasmFileNames")
        }

        return wasmFiles.map { file ->
            triggerIds.filter { id ->
                id.name.string.startsWith(file.key.removeSuffix(".wasm"))
            }.map { id ->
                val trigger = QueryBuilder.findTriggerById(id)
                    .account(admin)
                    .buildSigned(keyPair)
                    .let { client.sendQuery(it) }

                client.sendTransaction {
                    account(admin)
                    unregisterTrigger(id.name.string)
                    registerWasmTrigger(
                        trigger.id,
                        file.value.readBytes(),
                        trigger.action.repeats,
                        trigger.action.technicalAccount,,
                        trigger.action.metadata,
                        trigger.action.filter
                    )
                    buildSigned(keyPair)
                }.also {
                    withTimeout(30000) {
                        it.await()
                    }
                }.let { id }
            }
        }.flatten()
    }
}
