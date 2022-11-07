package jp.co.soramitsu

import jp.co.soramitsu.iroha2.Genesis
import jp.co.soramitsu.iroha2.IdKey
import jp.co.soramitsu.iroha2.Permissions
import jp.co.soramitsu.iroha2.asDomainId
import jp.co.soramitsu.iroha2.asName
import jp.co.soramitsu.iroha2.generateKeyPair
import jp.co.soramitsu.iroha2.generated.core.genesis.GenesisTransaction
import jp.co.soramitsu.iroha2.generated.core.genesis.RawGenesisBlock
import jp.co.soramitsu.iroha2.generated.datamodel.account.AccountId
import jp.co.soramitsu.iroha2.toIrohaPublicKey
import jp.co.soramitsu.iroha2.transaction.Instructions

val DEFAULT_DOMAIN_ID = "wonderland".asDomainId()
val CONTRIBUTION_DOMAIN_ID = "contribution".asDomainId()
val ALICE_ACCOUNT_NAME = "alice".asName()
val ALICE_ACCOUNT_ID = AccountId(ALICE_ACCOUNT_NAME, DEFAULT_DOMAIN_ID)
val ALICE_KEYPAIR = generateKeyPair()

open class DefaultGenesis : Genesis(
    RawGenesisBlock(
        listOf(
            GenesisTransaction(
                listOf(
                    Instructions.registerDomain(DEFAULT_DOMAIN_ID),
                    Instructions.registerDomain(CONTRIBUTION_DOMAIN_ID),
                    Instructions.registerAccount(
                        ALICE_ACCOUNT_ID,
                        listOf(ALICE_KEYPAIR.public.toIrohaPublicKey())
                    ),
                    Instructions.registerPermissionToken(
                        Permissions.CanSetKeyValueUserAssetsToken.type,
                        IdKey.AssetId
                    )
                )
            )
        )
    )
)
