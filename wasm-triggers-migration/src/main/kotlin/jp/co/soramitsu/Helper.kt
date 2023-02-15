package jp.co.soramitsu

import jp.co.soramitsu.Mode.DEFAULT
import jp.co.soramitsu.Mode.REGISTER
import jp.co.soramitsu.Mode.UNREGISTER
import jp.co.soramitsu.iroha2.asAccountId
import jp.co.soramitsu.iroha2.asName
import jp.co.soramitsu.iroha2.client.Iroha2Client
import jp.co.soramitsu.iroha2.generated.datamodel.account.AccountId
import jp.co.soramitsu.iroha2.generated.datamodel.events.EventsFilterBox
import jp.co.soramitsu.iroha2.generated.datamodel.metadata.Metadata
import jp.co.soramitsu.iroha2.generated.datamodel.transaction.Executable
import jp.co.soramitsu.iroha2.generated.datamodel.transaction.WasmSmartContract
import jp.co.soramitsu.iroha2.generated.datamodel.trigger.Trigger
import jp.co.soramitsu.iroha2.generated.datamodel.trigger.TriggerId
import jp.co.soramitsu.iroha2.generated.datamodel.trigger.action.Action
import jp.co.soramitsu.iroha2.generated.datamodel.trigger.action.Repeats
import jp.co.soramitsu.iroha2.query.QueryBuilder
import kotlinx.coroutines.withTimeout
import java.io.File
import java.security.KeyPair

class Helper(private val client: Iroha2Client) {

    suspend fun update(
        filePath: String,
        admin: AccountId,
        keyPair: KeyPair,
        mode: Int,
        repeats: Int = -1,
        triggerType: Int = -1,
        technicalAccount: String = "",
        triggerArgument: String = ""
    ): List<TriggerId> {
        val wasmFiles = getWasmFiles(mode, filePath)
        if (wasmFiles.isEmpty()) {
            throw RuntimeException("Wasm files not found: $filePath")
        }
        val triggerIds = QueryBuilder.findAllActiveTriggerIds()
            .account(admin)
            .buildSigned(keyPair)
            .let { client.sendQuery(it) }

        val idsValues = triggerIds.map { it.name.string }
        val wasmFileNames = wasmFiles.map { it.key.removeSuffix(".wasm") }
        if ((mode == DEFAULT.mode || mode == UNREGISTER.mode)
            && !wasmFileNames.all { n -> idsValues.any { id -> id.startsWith(n) } }) {
            throw RuntimeException("Found triggers: $idsValues, provided files: $wasmFileNames")
        }

        return wasmFiles.map { file ->
            val trigger = getTrigger(triggerIds, admin, keyPair, file, mode, repeats,
                triggerType, technicalAccount, triggerArgument)
            client.sendTransaction {
                account(admin)
                if (mode == DEFAULT.mode || mode == UNREGISTER.mode) {
                    unregisterTrigger(trigger.id.name.string)
                }
                if (mode == DEFAULT.mode || mode == REGISTER.mode) {
                    registerWasmTrigger(
                        trigger.id,
                        file.value.readBytes(),
                        trigger.action.repeats,
                        trigger.action.technicalAccount,
                        trigger.action.metadata,
                        trigger.action.filter
                    )
                }
                buildSigned(keyPair)
            }.also {
                withTimeout(30000) {
                    it.await()
                }
            }.let { trigger.id }
        }
    }

    private fun getWasmFiles(mode: Int, filePath: String): Map<String, File> {
        return if (DEFAULT.mode == mode || UNREGISTER.mode == mode) {
            File(filePath).walk()
                .filter { !it.isDirectory }
                .filter { it.name.endsWith(".wasm") }
                .associateBy { it.name }
        } else if (REGISTER.mode == mode) {
            val file = File(filePath)
            listOf(file).filter { it.isFile }
                .associateBy { it.name }
        } else {
            throw RuntimeException("Mode $mode not supported")
        }
    }

    private suspend fun getTrigger(
        triggerIds: List<TriggerId>,
        admin: AccountId,
        keyPair: KeyPair,
        file: Map.Entry<String, File>,
        mode: Int,
        repeats: Int,
        triggerType: Int,
        technicalAccount: String,
        triggerArgument: String
    ): Trigger<*> {
        if (DEFAULT.mode == mode || UNREGISTER.mode == mode) {
            val id = getTriggerIdByPrefix(triggerIds, file.key.removeSuffix(".wasm"))[0]
            return QueryBuilder.findTriggerById(id)
                .account(admin)
                .buildSigned(keyPair)
                .let { client.sendQuery(it) }
        } else {
            val newTriggerId = file.key.removeSuffix(".wasm")
            val id = getTriggerIdByPrefix(triggerIds, newTriggerId)
            if (!id.isEmpty()) {
                throw RuntimeException("Trigger $id already exists")
            }
            return Trigger<Any>(
                TriggerId(newTriggerId.asName()),
                Action(
                    Executable.Wasm(WasmSmartContract(file.value.readBytes())),
                    getRepeats(repeats),
                    technicalAccount.asAccountId(),
                    getFilter(triggerType, triggerArgument),
                    Metadata(mapOf())
                )
            )
        }
    }

    private fun getTriggerIdByPrefix(triggerIds: List<TriggerId>, prefix: String): List<TriggerId> {
        return triggerIds.filter { id ->
            id.name.string.startsWith(prefix)
        }
    }

    private fun getRepeats(repeats: Int): Repeats {
        return if (RepeatsEnum.INDEFINITELY.num == repeats) {
            Repeats.Indefinitely()
        } else if (RepeatsEnum.EXACTLY.num == repeats) {
            Repeats.Exactly(repeats.toLong())
        } else {
            throw RuntimeException("Unsupported repeats type")
        }
    }

    private fun getFilter(triggerType: Int, triggerArgument: String): EventsFilterBox {
        return if (TriggerType.TIME.num == triggerType) {
            getTimeTrigger(triggerArgument.toLong())
        } else if (TriggerType.DATA_BY_ACCOUNT_METADATA.num == triggerType) {
            getDataTriggerByAccountMetadataInserted()
        } else {
            throw RuntimeException("Unsupported trigger type")
        }
    }
}
