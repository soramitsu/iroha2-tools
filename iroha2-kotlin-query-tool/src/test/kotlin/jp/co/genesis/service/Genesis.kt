package jp.co.genesis.service

import jp.co.genesis.service.DefaultGenesis.Companion.ALICE_ROLE_ID
import jp.co.genesis.service.DefaultGenesis.Companion.BOB_ROLE_ID
import jp.co.soramitsu.iroha2.Genesis
import jp.co.soramitsu.iroha2.Permissions
import jp.co.soramitsu.iroha2.asJsonString
import jp.co.soramitsu.iroha2.asName
import jp.co.soramitsu.iroha2.asStringWithJson
import jp.co.soramitsu.iroha2.generated.AssetId
import jp.co.soramitsu.iroha2.generated.AssetValue
import jp.co.soramitsu.iroha2.generated.AssetValueType
import jp.co.soramitsu.iroha2.generated.InstructionExpr
import jp.co.soramitsu.iroha2.generated.PermissionToken
import jp.co.soramitsu.iroha2.generated.RawGenesisBlock
import jp.co.soramitsu.iroha2.generated.RoleId
import jp.co.soramitsu.iroha2.testengine.ALICE_ACCOUNT_ID
import jp.co.soramitsu.iroha2.testengine.ALICE_KEYPAIR
import jp.co.soramitsu.iroha2.testengine.BOB_ACCOUNT_ID
import jp.co.soramitsu.iroha2.testengine.BOB_KEYPAIR
import jp.co.soramitsu.iroha2.testengine.DEFAULT_ASSET_DEFINITION_ID
import jp.co.soramitsu.iroha2.testengine.DEFAULT_ASSET_ID
import jp.co.soramitsu.iroha2.testengine.DEFAULT_DOMAIN_ID
import jp.co.soramitsu.iroha2.toIrohaPublicKey
import jp.co.soramitsu.iroha2.transaction.Instructions

open class DefaultGenesis : Genesis(rawGenesisBlock()) {
    companion object {
        val ALICE_ROLE_ID = RoleId("BOB_CAN_CHANGE_ACCOUNT".asName())
        val BOB_ROLE_ID = RoleId("ALICE_CAN_CHANGE_ACCOUNT".asName())
    }
}

fun rawGenesisBlock(vararg isi: InstructionExpr) = RawGenesisBlock(
    listOf(
        Instructions.registerDomain(DEFAULT_DOMAIN_ID),
        Instructions.registerAccount(BOB_ACCOUNT_ID, listOf(BOB_KEYPAIR.public.toIrohaPublicKey())),
        Instructions.registerAccount(ALICE_ACCOUNT_ID, listOf(ALICE_KEYPAIR.public.toIrohaPublicKey())),
        Instructions.registerAssetDefinition(DEFAULT_ASSET_DEFINITION_ID, AssetValueType.Quantity()),
        Instructions.registerAsset(DEFAULT_ASSET_ID, AssetValue.Quantity(10)),
        Instructions.registerAsset(AssetId(DEFAULT_ASSET_DEFINITION_ID, BOB_ACCOUNT_ID), AssetValue.Quantity(20)),
        Instructions.registerRole(
            BOB_ROLE_ID,
            PermissionToken(
                Permissions.CanSetKeyValueInUserAccount.type,
                BOB_ACCOUNT_ID.asJsonString().asStringWithJson(),
            ),
            PermissionToken(
                Permissions.CanRemoveKeyValueInUserAccount.type,
                BOB_ACCOUNT_ID.asJsonString().asStringWithJson(),
            ),
        ),
        Instructions.grantRole(BOB_ROLE_ID, ALICE_ACCOUNT_ID),
        Instructions.registerRole(
            ALICE_ROLE_ID,
            PermissionToken(
                Permissions.CanSetKeyValueInUserAccount.type,
                ALICE_ACCOUNT_ID.asJsonString().asStringWithJson(),
            ),
            PermissionToken(
                Permissions.CanRemoveKeyValueInUserAccount.type,
                ALICE_ACCOUNT_ID.asJsonString().asStringWithJson(),
            ),
        ),
        Instructions.grantRole(ALICE_ROLE_ID, BOB_ACCOUNT_ID),

    ).let { listOf(it) },
    Genesis.executorMode,
)
