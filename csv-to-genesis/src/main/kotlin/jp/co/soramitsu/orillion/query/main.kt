package jp.co.soramitsu.orillion.query

import jp.co.soramitsu.iroha2.Genesis
import jp.co.soramitsu.iroha2.JSON_SERDE
import jp.co.soramitsu.iroha2.asDomainId
import jp.co.soramitsu.iroha2.asName
import jp.co.soramitsu.iroha2.asValue
import jp.co.soramitsu.iroha2.cast
import jp.co.soramitsu.iroha2.generateKeyPair
import jp.co.soramitsu.iroha2.generated.core.genesis.GenesisTransaction
import jp.co.soramitsu.iroha2.generated.core.genesis.RawGenesisBlock
import jp.co.soramitsu.iroha2.generated.datamodel.Name
import jp.co.soramitsu.iroha2.generated.datamodel.account.AccountId
import jp.co.soramitsu.iroha2.generated.datamodel.asset.AssetDefinitionId
import jp.co.soramitsu.iroha2.generated.datamodel.asset.AssetId
import jp.co.soramitsu.iroha2.generated.datamodel.asset.AssetValueType
import jp.co.soramitsu.iroha2.generated.datamodel.isi.Instruction
import jp.co.soramitsu.iroha2.toIrohaPublicKey
import jp.co.soramitsu.iroha2.transaction.Instructions
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import java.io.File
import java.math.BigInteger
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.*

private val DOMAIN_ID = "some_domain".asDomainId()
private val ADMIN_ID = AccountId("some_admin".asName(), DOMAIN_ID)
private val ADMIN_KEYPAIR = generateKeyPair()

private val ID = "id".asName() to 0
private val FRAUD_TYPE = "ft".asName() to 12
private val ORIGINATION = "org".asName() to 4
private val DESTINATION = "dst".asName() to 5
private val STATUS = "sts".asName() to 14
private val TIMESTAMP = "ts".asName() to 1
private val EXPIRY_DATE = "ed".asName() to TIMESTAMP.second

val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

fun main(vararg args: String) {
    val node = JSON_SERDE.readTree(File(args[0]))
    val block = JSON_SERDE.convertValue(node, RawGenesisBlock::class.java)
    val txs = block.transactions.cast<MutableList<GenesisTransaction>>()

    listOf(
        Instructions.registerDomain(DOMAIN_ID),
        Instructions.registerAccount(
            ADMIN_ID, listOf(ADMIN_KEYPAIR.public.toIrohaPublicKey())
        )
    ).let { isi ->
        GenesisTransaction(isi)
    }.let { tx -> txs.add(tx) }

    File(args[1]).bufferedReader().use { reader ->
        val parser = CSVParser(
            reader, CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withSkipHeaderRecord()
        )
        parser.forEach { txs.add(GenesisTransaction(it.mapToAssetIsi())) }
    }

    println(Genesis(RawGenesisBlock(txs)).asJson())
}

private fun CSVRecord.mapToAssetIsi(): List<Instruction> {
    val isi = mutableListOf<Instruction>()

    val id = this.get(ID.second)
    val definitionId = AssetDefinitionId(id.asName(), DOMAIN_ID)
    val assetId = AssetId(definitionId, ADMIN_ID)

    Instructions.registerAsset(definitionId, AssetValueType.Store())
        .also { isi.add(it) }
    Instructions.setKeyValue(definitionId, ID.first, id.asValue())
        .also { isi.add(it) }

    this.toSkvIsi<Long>(assetId, FRAUD_TYPE).also { isi.add(it) }
    this.toSkvIsi<String>(assetId, ORIGINATION).also { isi.add(it) }
    this.toSkvIsi<String>(assetId, DESTINATION).also { isi.add(it) }
    this.toSkvIsi<Long>(assetId, STATUS).also { isi.add(it) }
    this.toSkvIsi<Date>(assetId, TIMESTAMP).also { isi.add(it) }

    dateFormatter
        .parse(this.get(EXPIRY_DATE.second))
        .toEpochSeconds(Duration.ofDays(120))
        .asValue()
        .let { date -> Instructions.setKeyValue(assetId, EXPIRY_DATE.first, date) }
        .also { isi.add(it) }

    Instructions.grantSetKeyValueAsset(assetId, ADMIN_ID).also { isi.add(it) }

    return isi
}

private inline fun <reified T> CSVRecord.toSkvIsi(
    assetId: AssetId,
    type: Pair<Name, Int>
) = this.get(type.second).let { v ->
    when (T::class) {
        Long::class -> v.toLong()
        Date::class -> dateFormatter.parse(v).toInstant().epochSecond
        else -> v
    }
}.asValue().let { value ->
    Instructions.setKeyValue(assetId, type.first, value)
}

// todo move to Iroha SDK
private fun <T> T.asValue() = when (this) {
    is String -> this.asValue()
    is Long -> this.asValue()
    is Int -> this.asValue()
    is BigInteger -> this.asValue()
    is Boolean -> this.asValue()
    else -> throw RuntimeException("Unsupported type")
}

private fun Date.toEpochSeconds(plus: Duration? = null) = this
    .toInstant().let { instant ->
        plus?.toSeconds()
            ?.let { instant.plusSeconds(it) }
            ?: instant
    }.epochSecond
