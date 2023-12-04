package jp.co.soramitsu

import jp.co.soramitsu.iroha2.Genesis
import jp.co.soramitsu.iroha2.asDomainId
import jp.co.soramitsu.iroha2.asName
import jp.co.soramitsu.iroha2.generateKeyPair
import jp.co.soramitsu.iroha2.generated.AccountId
import jp.co.soramitsu.iroha2.generated.RawGenesisBlock
import jp.co.soramitsu.iroha2.toIrohaPublicKey
import jp.co.soramitsu.iroha2.transaction.Instructions

val DEFAULT_DOMAIN_ID = "wonderland".asDomainId()
val CONTRIBUTION_DOMAIN_ID = "contribution".asDomainId()
val ALICE_ACCOUNT_NAME = "alice".asName()
val ALICE_ACCOUNT_ID = AccountId(DEFAULT_DOMAIN_ID, ALICE_ACCOUNT_NAME)
val ALICE_KEYPAIR = generateKeyPair()

open class DefaultGenesis : Genesis(
    RawGenesisBlock(
        listOf(
            listOf(
                Instructions.registerDomain(DEFAULT_DOMAIN_ID),
                Instructions.registerDomain(CONTRIBUTION_DOMAIN_ID),
                Instructions.registerAccount(
                    ALICE_ACCOUNT_ID,
                    listOf(ALICE_KEYPAIR.public.toIrohaPublicKey()),
                ),
            ),
        ),
        executor = executorMode,
    ),
)
