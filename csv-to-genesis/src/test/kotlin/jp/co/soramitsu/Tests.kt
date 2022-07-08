package jp.co.soramitsu

import jp.co.soramitsu.iroha2.Genesis
import jp.co.soramitsu.iroha2.client.Iroha2Client
import jp.co.soramitsu.iroha2.engine.IrohaTest
import jp.co.soramitsu.iroha2.engine.WithIroha
import jp.co.soramitsu.iroha2.generated.core.genesis.RawGenesisBlock
import jp.co.soramitsu.iroha2.query.QueryBuilder
import jp.co.soramitsu.iroha2.testcontainers.IrohaContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.io.File

@Timeout(300)
class Tests : IrohaTest<Iroha2Client>() {

    private val converter = Converter()

    @Test
    fun `should convert csv to genesis and run Iroha with it`(): Unit = runBlocking {
        val genesis = converter.toGenesis(
            File("src/main/resources/example.csv"),
            File("src/main/resources/example_genesis.json")
        )
        startContainer(genesis).use { container ->
            val client = Iroha2Client(container.getApiUrl())

            QueryBuilder.findAllDomains()
                .account(Converter.ADMIN_ID)
                .buildSigned(Converter.ADMIN_KEYPAIR)
                .let { client.sendQuery(it) }
                .also { domains -> assert(domains.size == 3) }
        }
    }

    @Test
    @WithIroha(DefaultGenesis::class)
    fun `should send data from csv to Iroha`(): Unit = runBlocking {
        converter.sendToIroha(
            File("src/main/resources/example.csv"),
            client.peerUrl,
            ALICE_ACCOUNT_ID,
            ALICE_KEYPAIR
        )

        delay(5000)

        QueryBuilder.findAllAssets()
            .account(ALICE_ACCOUNT_ID)
            .buildSigned(ALICE_KEYPAIR)
            .let { client.sendQuery(it) }
            .also { assert(it.size == 653) }
    }

    private fun startContainer(genesis: RawGenesisBlock): IrohaContainer {
        return startContainer(Genesis(genesis))
    }

    private fun startContainer(genesis: Genesis): IrohaContainer {
        return IrohaContainer {
            this.genesis = genesis
        }.also { it.start() }
    }
}
