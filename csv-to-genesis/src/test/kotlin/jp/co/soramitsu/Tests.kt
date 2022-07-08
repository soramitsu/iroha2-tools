package jp.co.soramitsu

import jp.co.soramitsu.iroha2.Genesis
import jp.co.soramitsu.iroha2.client.Iroha2Client
import jp.co.soramitsu.iroha2.engine.IrohaTest
import jp.co.soramitsu.iroha2.testcontainers.IrohaContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.io.File

@Timeout(150)
class Tests : IrohaTest<Iroha2Client>() {

    private val converter = Converter()

    @Test
    fun `should convert csv to genesis and run Iroha with it`() = runBlocking {
        val genesis = converter.convert(
            File("src/main/resources/example.csv"),
            File("src/main/resources/example_genesis.json")
        )
        startContainer(genesis)
        delay(50000)
    }

    private fun startContainer(genesis: Genesis): IrohaContainer {
        return IrohaContainer {
            this.genesis = genesis
        }.also { it.start() }
    }
}
