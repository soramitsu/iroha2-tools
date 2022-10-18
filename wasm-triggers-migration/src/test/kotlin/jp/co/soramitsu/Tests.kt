package jp.co.soramitsu

import jp.co.soramitsu.iroha2.asName
import jp.co.soramitsu.iroha2.client.Iroha2Client
import jp.co.soramitsu.iroha2.engine.IrohaTest
import jp.co.soramitsu.iroha2.engine.WithIroha
import jp.co.soramitsu.iroha2.generateKeyPair
import jp.co.soramitsu.iroha2.generated.Duration
import jp.co.soramitsu.iroha2.generated.datamodel.account.AccountId
import jp.co.soramitsu.iroha2.generated.datamodel.events.data.filters.IdFilterAccountId
import jp.co.soramitsu.iroha2.generated.datamodel.events.data.filters.account.AccountEventFilter
import jp.co.soramitsu.iroha2.generated.datamodel.metadata.Metadata
import jp.co.soramitsu.iroha2.generated.datamodel.trigger.TriggerId
import jp.co.soramitsu.iroha2.generated.datamodel.trigger.action.Repeats
import jp.co.soramitsu.iroha2.toIrohaPublicKey
import jp.co.soramitsu.iroha2.transaction.EntityFilters
import jp.co.soramitsu.iroha2.transaction.EventFilters
import jp.co.soramitsu.iroha2.transaction.Filters
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.math.BigInteger
import java.util.*
import kotlin.test.assertEquals

@Timeout(60)
class Tests : IrohaTest<Iroha2Client>() {

    @Test
    @WithIroha(DefaultGenesis::class)
    fun `should update all smart contracts`(): Unit = runBlocking {
        registerNftStatsTrigger()
        registerMintCreditTrigger(
            AccountId("Bob".asName(), DEFAULT_DOMAIN_ID)
        )

        Helper(client).update(
            this.javaClass.classLoader
                .getResource("trigger_nft_stats.wasm")
                .path.substringBeforeLast("/"),
            ALICE_ACCOUNT_ID,
            ALICE_KEYPAIR
        ).also {
            assertEquals(2, it.size)
        }
    }

    private suspend fun registerMintCreditTrigger(newAccount: AccountId) {
        client.sendTransaction {
            account(ALICE_ACCOUNT_ID)
            registerAccount(
                newAccount,
                listOf(generateKeyPair().public.toIrohaPublicKey()),
                Metadata(mapOf())
            )
            registerWasmTrigger(
                TriggerId("trigger_mint_credit".asName()),
                this.javaClass.classLoader
                    .getResource("trigger_mint_credit.wasm")
                    .readBytes(),
                Repeats.Indefinitely(),
                ALICE_ACCOUNT_ID,
                Metadata(mapOf()),
                Filters.data(
                    EntityFilters.byAccount(
                        IdFilterAccountId(newAccount),
                        AccountEventFilter.ByMetadataInserted()
                    )
                )
            )
            buildSigned(ALICE_KEYPAIR)
        }.also {
            withTimeout(30000) {
                it.await()
            }
        }
    }

    private suspend fun registerNftStatsTrigger() {
        client.sendTransaction {
            account(ALICE_ACCOUNT_ID)
            registerWasmTrigger(
                TriggerId("trigger_nft_stats".asName()),
                this.javaClass.classLoader
                    .getResource("trigger_nft_stats.wasm")
                    .readBytes(),
                Repeats.Indefinitely(),
                ALICE_ACCOUNT_ID,
                Metadata(mapOf()),
                Filters.time(
                    EventFilters.timeEventFilter(
                        Duration(BigInteger.valueOf(Date().time / 1000), 0),
                        Duration(BigInteger.valueOf(3600), 0)
                    )
                )
            )
            buildSigned(ALICE_KEYPAIR)
        }.also {
            withTimeout(30000) {
                it.await()
            }
        }
    }
}
