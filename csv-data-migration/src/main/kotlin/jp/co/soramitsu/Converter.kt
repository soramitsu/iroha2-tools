package jp.co.soramitsu

import jp.co.soramitsu.iroha2.Genesis
import jp.co.soramitsu.iroha2.JSON_SERDE
import jp.co.soramitsu.iroha2.asDomainId
import jp.co.soramitsu.iroha2.asName
import jp.co.soramitsu.iroha2.asValue
import jp.co.soramitsu.iroha2.cast
import jp.co.soramitsu.iroha2.client.Iroha2Client
import jp.co.soramitsu.iroha2.generated.core.genesis.GenesisTransaction
import jp.co.soramitsu.iroha2.generated.core.genesis.RawGenesisBlock
import jp.co.soramitsu.iroha2.generated.datamodel.account.AccountId
import jp.co.soramitsu.iroha2.generated.datamodel.asset.AssetDefinitionId
import jp.co.soramitsu.iroha2.generated.datamodel.asset.AssetId
import jp.co.soramitsu.iroha2.generated.datamodel.asset.AssetValueType
import jp.co.soramitsu.iroha2.generated.datamodel.isi.Instruction
import jp.co.soramitsu.iroha2.generated.datamodel.name.Name
import jp.co.soramitsu.iroha2.transaction.Instructions
import jp.co.soramitsu.iroha2.transaction.TransactionBuilder
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import java.io.File
import java.net.URL
import java.security.KeyPair
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.*

class Converter {
    companion object {
        private val ID = "id".asName() to 7
        private val FRAUD_TYPE = "ft".asName() to 2
        private val FRAUD_VALUE = "fv".asName() to 3
        private val ORIGINATION = "org".asName() to 4
        private val DESTINATION = "dst".asName() to 5
        private val TIMESTAMP = "ts".asName() to 6
        private val EXPIRY_DATE = "ed".asName() to TIMESTAMP.second
        private val STATUS = "sts".asName() to 8
        private val CONFIDENCE_INDEX = "ci".asName() to 13

        private val CONTRIBUTION_DOMAIN_ID = "contribution".asDomainId()

        private const val BUNCH_SIZE = 50

        internal val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        private val csvFormat = CSVFormat.DEFAULT
            .withFirstRecordAsHeader()
            .withSkipHeaderRecord()
    }

    suspend fun sendToIroha(
        csv: File,
        peerUrl: URL,
        admin: AccountId,
        keyPair: KeyPair,
        credentials: String? = null
    ) {
        val client = Iroha2Client(peerUrl, log = true, credentials = credentials)
        val isi = mutableListOf<Instruction>()

        csv.bufferedReader().use { reader ->
            CSVParser(reader, csvFormat).forEachIndexed { id, record ->
                isi.addAll(record.mapToAssetIsi(admin))
                println("ID: $id")
                if (id % BUNCH_SIZE == 0 && id != 0) {
                    client.send(*isi.toTypedArray(), account = admin, keyPair = keyPair)
                    println("$BUNCH_SIZE HAVE BEEN SENT")
                    isi.clear()
                }
            }
        }

        if (isi.isNotEmpty()) {
            client.send(*isi.toTypedArray(), account = admin, keyPair = keyPair)
        }
    }

    fun toGenesis(csv: File, genesis: File, admin: AccountId): Genesis {
        val node = JSON_SERDE.readTree(genesis)
        val block = JSON_SERDE.convertValue(node, RawGenesisBlock::class.java)
        val txs = block.transactions.cast<MutableList<GenesisTransaction>>()

        csv.bufferedReader().use { reader ->
            val parser = CSVParser(reader, csvFormat)
            val isi = mutableListOf<Instruction>()

            parser.forEach { isi.addAll(it.mapToAssetIsi(admin)) }
            txs.add(GenesisTransaction(isi))
        }

        return Genesis(RawGenesisBlock(txs))
    }

    private fun CSVRecord.mapToAssetIsi(accountId: AccountId): ArrayList<Instruction> {
        val isi = mutableListOf<Instruction>()

        val id = this.getId()
        val definitionId = AssetDefinitionId("${id}_${UUID.randomUUID()}".asName(), CONTRIBUTION_DOMAIN_ID)
        val assetId = AssetId(definitionId, accountId)

        Instructions.registerAsset(definitionId, AssetValueType.Store())
            .also { isi.add(it) }
        Instructions.setKeyValue(assetId, ID.first, id.asValue())
            .also { isi.add(it) }

        this.toSkvIsi<String>(assetId, FRAUD_TYPE)?.also { isi.add(it) }
        this.toSkvIsi<String>(assetId, ORIGINATION)?.also { isi.add(it) }
        this.toSkvIsi<String>(assetId, DESTINATION)?.also { isi.add(it) }
        this.toSkvIsi<Long>(assetId, STATUS)?.also { isi.add(it) }
        this.toSkvIsi<Date>(assetId, TIMESTAMP)?.also { isi.add(it) }
        this.toSkvIsi<Long>(assetId, CONFIDENCE_INDEX)?.also { isi.add(it) }

        dateFormatter
            .parse(this.get(EXPIRY_DATE.second))
            .toEpochSeconds(Duration.ofDays(120))
            .asValue()
            .let { date -> Instructions.setKeyValue(assetId, EXPIRY_DATE.first, date) }
            .also { isi.add(it) }

        Instructions.grantSetKeyValueAsset(assetId, accountId).also { isi.add(it) }

        return ArrayList(isi)
    }

    private fun CSVRecord.getId() = when (
        val id = this.get(ID.second)
    ) {
        "NULL" -> this.get(FRAUD_VALUE.second)
        else -> id.replace("#${CONTRIBUTION_DOMAIN_ID.name.string}", "")
    }

    private suspend fun Iroha2Client.send(
        vararg isi: Instruction,
        account: AccountId,
        keyPair: KeyPair
    ) = TransactionBuilder {
        account(account)
        this.instructions.value.addAll(isi)
    }.let { builder ->
        this.sendTransaction {
            builder.buildSigned(keyPair)
        }
    }
}

private inline fun <reified T> CSVRecord.toSkvIsi(
    assetId: AssetId,
    type: Pair<Name, Int>
) = this.get(type.second).let { v ->
    when (T::class) {
        Long::class -> v.toLongOrNull()
        Date::class ->
            Converter.dateFormatter
                .parse(v)
                .toInstant()
                .epochSecond.toString()

        else -> v
    }
}?.asValue()?.let { value ->
    Instructions.setKeyValue(assetId, type.first, value)
}

fun Date.toEpochSeconds(plus: Duration? = null) = this
    .toInstant().let { instant ->
        plus?.toSeconds()
            ?.let { instant.plusSeconds(it) }
            ?: instant
    }.epochSecond
