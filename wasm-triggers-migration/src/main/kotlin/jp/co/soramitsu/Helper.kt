package jp.co.soramitsu

import jp.co.soramitsu.Mode.DEFAULT
import jp.co.soramitsu.Mode.REGISTER
import jp.co.soramitsu.Mode.UNREGISTER
import jp.co.soramitsu.RepeatsEnum.EXACTLY
import jp.co.soramitsu.RepeatsEnum.INDEFINITELY
import jp.co.soramitsu.TriggerType.DATA_BY_ACCOUNT_METADATA
import jp.co.soramitsu.TriggerType.TIME
import jp.co.soramitsu.iroha2.asAccountId
import jp.co.soramitsu.iroha2.asName
import jp.co.soramitsu.iroha2.client.Iroha2Client
import jp.co.soramitsu.iroha2.generated.datamodel.account.AccountId
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
        if ((mode == DEFAULT.mode || mode == UNREGISTER.mode) &&
            !wasmFileNames.all { n -> idsValues.any { id -> id.startsWith(n) } }
        ) {
            throw RuntimeException("Found triggers: $idsValues, provided files: $wasmFileNames")
        }

        val returningList = mutableListOf<TriggerId>()

        wasmFiles.map { file ->
            val triggers = getTriggers(
                triggerIds, admin, keyPair, file, mode, repeats,
                triggerType, technicalAccount, triggerArgument
            )
            client.sendTransaction {
                account(admin)
                triggers.forEach {
                    if (mode == DEFAULT.mode || mode == UNREGISTER.mode) {
                        unregisterTrigger(it.id.name.string)
                    }
                    if (mode == DEFAULT.mode || mode == REGISTER.mode) {
                        registerWasmTrigger(
                            it.id,
                            file.value.readBytes(),
                            it.action.repeats,
                            it.action.technicalAccount,
                            it.action.metadata,
                            it.action.filter
                        )
                    }
                    returningList.add(it.id)
                }
                buildSigned(keyPair)
            }.also {
                withTimeout(30000) {
                    it.await()
                }
            }
        }

        return returningList
    }

    private fun getWasmFiles(mode: Int, filePath: String): Map<String, File> {
        return when (Mode.from(mode)) {
            DEFAULT -> getFilesFromPath(filePath)
            UNREGISTER -> getFilesFromPath(filePath)
            REGISTER -> listOf(File(filePath)).filter { it.isFile }.associateBy { it.name }
        }
    }

    private fun getFilesFromPath(filePath: String): Map<String, File> {
        return File(filePath).walk()
            .filter { !it.isDirectory }
            .filter { it.name.endsWith(".wasm") }
            .associateBy { it.name }
    }

    private suspend fun getTriggers(
        triggerIds: List<TriggerId>,
        admin: AccountId,
        keyPair: KeyPair,
        file: Map.Entry<String, File>,
        mode: Int,
        repeats: Int,
        triggerType: Int,
        technicalAccount: String,
        triggerArgument: String
    ): List<Trigger<*>> {
        if (DEFAULT.mode == mode || UNREGISTER.mode == mode) {
            val ids = getTriggerIdByPrefix(triggerIds, file.key.removeSuffix(".wasm"))
            return ids.map { id ->
                QueryBuilder.findTriggerById(id)
                    .account(admin)
                    .buildSigned(keyPair)
                    .let { client.sendQuery(it) }
            }
        } else {
            val newTriggerId = file.key.removeSuffix(".wasm")
            val id = getTriggerIdByPrefix(triggerIds, newTriggerId)
            if (id.isNotEmpty()) {
                throw RuntimeException("Trigger $id already exists")
            }
            return listOf(
                Trigger<Any>(
                    TriggerId(newTriggerId.asName()),
                    Action(
                        Executable.Wasm(WasmSmartContract(file.value.readBytes())),
                        getRepeats(repeats),
                        technicalAccount.asAccountId(),
                        getFilter(triggerType, triggerArgument),
                        Metadata(mapOf())
                    )
                )
            )
        }
    }

    private fun getTriggerIdByPrefix(
        triggerIds: List<TriggerId>,
        prefix: String
    ) = triggerIds.filter { id -> id.name.string.startsWith(prefix) }

    private fun getRepeats(repeats: Int) = when (RepeatsEnum.from(repeats)) {
        INDEFINITELY -> Repeats.Indefinitely()
        EXACTLY -> Repeats.Exactly(repeats.toLong())
    }

    private fun getFilter(triggerType: Int, triggerArgument: String) = when (TriggerType.from(triggerType)) {
        TIME -> getTimeTrigger(triggerArgument.toLong())
        DATA_BY_ACCOUNT_METADATA -> getDataTriggerByAccountMetadataInserted()
    }
}
