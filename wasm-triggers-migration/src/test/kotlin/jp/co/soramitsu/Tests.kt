package jp.co.soramitsu

import jp.co.soramitsu.Mode.DEFAULT
import jp.co.soramitsu.Mode.REGISTER
import jp.co.soramitsu.Mode.UNREGISTER
import jp.co.soramitsu.iroha2.asName
import jp.co.soramitsu.iroha2.client.Iroha2Client
import jp.co.soramitsu.iroha2.generateKeyPair
import jp.co.soramitsu.iroha2.generated.AccountEventFilter
import jp.co.soramitsu.iroha2.generated.AccountId
import jp.co.soramitsu.iroha2.generated.Duration
import jp.co.soramitsu.iroha2.generated.FilterOptOfDataEntityFilter
import jp.co.soramitsu.iroha2.generated.Metadata
import jp.co.soramitsu.iroha2.generated.OriginFilterOfAccountEvent
import jp.co.soramitsu.iroha2.generated.Repeats
import jp.co.soramitsu.iroha2.generated.TriggerId
import jp.co.soramitsu.iroha2.generated.TriggeringFilterBox
import jp.co.soramitsu.iroha2.query.QueryBuilder
import jp.co.soramitsu.iroha2.testengine.IrohaTest
import jp.co.soramitsu.iroha2.testengine.WithIroha
import jp.co.soramitsu.iroha2.toIrohaPublicKey
import jp.co.soramitsu.iroha2.transaction.EntityFilters
import jp.co.soramitsu.iroha2.transaction.EventFilters
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.math.BigInteger
import java.util.*
import kotlin.test.assertEquals

@Timeout(300)
class Tests : IrohaTest<Iroha2Client>() {

    @Test
    @WithIroha(
        sources = [DefaultGenesis::class],
        configs = ["WSV_WASM_RUNTIME_CONFIG={\"FUEL_LIMIT\":500000000, \"MAX_MEMORY\": 4294967295}"],
    )
    fun `should update all smart contracts`(): Unit = runBlocking {
        registerNftStatsTrigger("trigger_nft_stats")
        registerNftStatsTrigger("trigger_nft_stats2")
        registerMintCreditTrigger(
            AccountId(DEFAULT_DOMAIN_ID, "Bob".asName()),
        )

        Helper(client).update(
            this.javaClass.classLoader
                .getResource("trigger_nft_stats.wasm")
                .path.substringBeforeLast("/"),
            ALICE_ACCOUNT_ID,
            ALICE_KEYPAIR,
            DEFAULT.mode,
        ).also {
            assertEquals(3, it.size)
        }
    }

    @Test
    @WithIroha([DefaultGenesis::class])
    fun `should only register new trigger`(): Unit = runBlocking {
        val repeats = 0
        val triggerType = 1
        val technicalAccount = "bob@admin"
        val triggerArg = ""
        Helper(client).update(
            this.javaClass.classLoader
                .getResource("trigger_mint_credit.wasm")
                .path,
            ALICE_ACCOUNT_ID,
            ALICE_KEYPAIR,
            REGISTER.mode,
            repeats,
            triggerType,
            technicalAccount,
            triggerArg,
        ).also {
            assertEquals(1, it.size)
            assertEquals("trigger_mint_credit", it[0].name.string)
        }

        getActiveTriggers().also {
            assertEquals(1, it.size)
            assertEquals("trigger_mint_credit", it[0].name.string)
        }
    }

    @Test
    @WithIroha([DefaultGenesis::class])
    fun `should only unregister triggers`(): Unit = runBlocking {
        registerNftStatsTrigger("trigger_nft_stats")
        registerNftStatsTrigger("trigger_nft_stats2")
        registerMintCreditTrigger(
            AccountId(DEFAULT_DOMAIN_ID, "Bob".asName()),
        )
        Helper(client).update(
            this.javaClass.classLoader
                .getResource("trigger_nft_stats.wasm")
                .path.substringBeforeLast("/"),
            ALICE_ACCOUNT_ID,
            ALICE_KEYPAIR,
            UNREGISTER.mode,
        ).also {
            assertEquals(3, it.size)
        }

        getActiveTriggers().also {
            assertEquals(0, it.size)
        }
    }

    private suspend fun getActiveTriggers(): List<TriggerId> {
        return QueryBuilder.findAllActiveTriggerIds()
            .account(ALICE_ACCOUNT_ID)
            .buildSigned(ALICE_KEYPAIR)
            .let { client.sendQuery(it) }
    }

    private suspend fun registerMintCreditTrigger(newAccount: AccountId) {
        client.sendTransaction {
            account(ALICE_ACCOUNT_ID)
            registerAccount(
                newAccount,
                listOf(generateKeyPair().public.toIrohaPublicKey()),
                Metadata(mapOf()),
            )
            registerWasmTrigger(
                TriggerId(name = "trigger_mint_credit".asName()),
                this.javaClass.classLoader
                    .getResource("trigger_mint_credit.wasm")
                    .readBytes(),
                Repeats.Indefinitely(),
                ALICE_ACCOUNT_ID,
                Metadata(mapOf()),
                TriggeringFilterBox.Data(
                    FilterOptOfDataEntityFilter.BySome(
                        EntityFilters.byAccount(
                            OriginFilterOfAccountEvent(newAccount),
                            AccountEventFilter.ByMetadataInserted(),
                        ),
                    ),
                ),
            )
            buildSigned(ALICE_KEYPAIR)
        }.also {
            withTimeout(30000) {
                it.await()
            }
        }
    }

    private suspend fun registerNftStatsTrigger(triggerId: String) {
        client.sendTransaction {
            account(ALICE_ACCOUNT_ID)
            registerWasmTrigger(
                TriggerId(name = triggerId.asName()),
                this.javaClass.classLoader
                    .getResource("trigger_nft_stats.wasm")
                    .readBytes(),
                Repeats.Indefinitely(),
                ALICE_ACCOUNT_ID,
                Metadata(mapOf()),
                TriggeringFilterBox.Time(
                    EventFilters.timeEventFilter(
                        Duration(BigInteger.valueOf(Date().time / 1000), 0),
                        Duration(BigInteger.valueOf(3600), 0),
                    ),
                ),
            )
            buildSigned(ALICE_KEYPAIR)
        }.also {
            withTimeout(30000) {
                it.await()
            }
        }
    }
}
