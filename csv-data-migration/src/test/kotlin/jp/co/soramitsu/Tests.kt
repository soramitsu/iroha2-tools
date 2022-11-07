package jp.co.soramitsu

import jp.co.soramitsu.iroha2.Genesis
import jp.co.soramitsu.iroha2.client.Iroha2Client
import jp.co.soramitsu.iroha2.query.QueryBuilder
import jp.co.soramitsu.iroha2.testengine.IrohaContainer
import jp.co.soramitsu.iroha2.testengine.IrohaTest
import jp.co.soramitsu.iroha2.testengine.WithIroha
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.io.File
import kotlin.test.assertEquals

@Timeout(300)
class Tests : IrohaTest<Iroha2Client>() {

    private val converter = Converter()

    @Test
    @Disabled // https://github.com/hyperledger/iroha/issues/2456
    fun `should convert csv to genesis and run Iroha with it`(): Unit = runBlocking {
        val genesis = converter.toGenesis(
            File("src/test/resources/example.csv"),
            File("src/test/resources/example_genesis.json"),
            ALICE_ACCOUNT_ID
        )
        startContainer(genesis).use { container ->
            Iroha2Client(container.getApiUrl())
                .checkAssetsSize(653)
        }
    }

    @Test
    @WithIroha(DefaultGenesis::class)
    fun `should send data from csv to Iroha`(): Unit = runBlocking {
        converter.sendToIroha(
            File("src/test/resources/example.csv"),
            client.peerUrl,
            ALICE_ACCOUNT_ID,
            ALICE_KEYPAIR
        )
        client.checkAssetsSize(233)
    }

    private fun startContainer(genesis: Genesis): IrohaContainer {
        return IrohaContainer {
            this.genesis = genesis
        }.also { it.start() }
    }

    private suspend fun Iroha2Client.checkAssetsSize(size: Int) {
        QueryBuilder.findAllAssets()
            .account(ALICE_ACCOUNT_ID)
            .buildSigned(ALICE_KEYPAIR)
            .let { this.sendQuery(it) }
            .also { assertEquals(it.size, size) }
    }
}
