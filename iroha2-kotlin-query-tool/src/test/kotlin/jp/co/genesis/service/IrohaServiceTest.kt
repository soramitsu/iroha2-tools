package jp.co.genesis.service

import jp.co.genesis.config.Iroha2Config
import jp.co.soramitsu.iroha2.bytes
import jp.co.soramitsu.iroha2.client.Iroha2Client
import jp.co.soramitsu.iroha2.testengine.ALICE_ACCOUNT_ID_VALUE
import jp.co.soramitsu.iroha2.testengine.ALICE_KEYPAIR
import jp.co.soramitsu.iroha2.testengine.IrohaTest
import jp.co.soramitsu.iroha2.testengine.WithIroha
import jp.co.soramitsu.iroha2.toHex
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertNotNull

@ActiveProfiles("test")
@SpringBootTest(properties = ["command.line.runner.enabled=false"])
@Timeout(200)
class IrohaServiceTest : IrohaTest<Iroha2Client>() {

    @Autowired
    lateinit var irohaService: IrohaService

    @Test
    fun `query without url then error`(): Unit = runBlocking {
        val config = Iroha2Config()
        config.url = ""
        assertThrows<RuntimeException> { irohaService.query(config) }
    }

    @Test
    fun `query without adminAccount then error`(): Unit = runBlocking {
        val config = Iroha2Config()
        config.url = "http://localhost:8080/"
        config.adminAccount = ""
        assertThrows<RuntimeException> { irohaService.query(config) }
    }

    @Test
    fun `query without adminPublicKeyHex then error`(): Unit = runBlocking {
        val config = Iroha2Config()
        config.url = "http://localhost:8080/"
        config.adminAccount = ALICE_ACCOUNT_ID_VALUE
        config.adminPublicKeyHex = ""
        assertThrows<RuntimeException> { irohaService.query(config) }
    }

    @Test
    fun `query without adminPrivateKeyHex then error`(): Unit = runBlocking {
        val config = Iroha2Config()
        config.url = "http://localhost:8080/"
        config.adminAccount = ALICE_ACCOUNT_ID_VALUE
        config.adminPublicKeyHex = "aaaaaaaaaaaaaabbbbbbbbbbbbccccccccccccccdddddddddddddeeeeeeeeeee"
        config.adminPrivateKeyHex = ""
        assertThrows<RuntimeException> { irohaService.query(config) }
    }

    @Test
    @WithIroha([DefaultGenesis::class])
    fun `query Iroha2 then success`(): Unit = runBlocking {
        val config = Iroha2Config()
        config.url = client.urls[0].apiUrl.toString()
        config.adminAccount = ALICE_ACCOUNT_ID_VALUE
        config.adminPublicKeyHex = ALICE_KEYPAIR.public.bytes().toHex()
        config.adminPrivateKeyHex = ALICE_KEYPAIR.private.bytes().toHex()
        config.username = ""
        config.password = ""
        val result = irohaService.query(config)
        assertNotNull(result)
    }
}
