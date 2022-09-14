package jp.co.soramitsu

import jp.co.soramitsu.iroha2.asName
import jp.co.soramitsu.iroha2.client.Iroha2Client
import jp.co.soramitsu.iroha2.engine.IrohaTest
import jp.co.soramitsu.iroha2.engine.WithIroha
import jp.co.soramitsu.iroha2.generated.Duration
import jp.co.soramitsu.iroha2.generated.datamodel.metadata.Metadata
import jp.co.soramitsu.iroha2.generated.datamodel.trigger.TriggerId
import jp.co.soramitsu.iroha2.generated.datamodel.trigger.action.Repeats
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

        Helper(client).update(
            this.javaClass.classLoader
                .getResource("trigger_nft_stats.wasm")
                .path.substringBeforeLast("/"),
            ALICE_ACCOUNT_ID,
            ALICE_KEYPAIR
        ).also {
            assertEquals(1, it.size)
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
