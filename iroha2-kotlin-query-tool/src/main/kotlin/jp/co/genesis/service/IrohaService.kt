package jp.co.genesis.service

import com.fasterxml.jackson.databind.ObjectMapper
import jp.co.genesis.config.Iroha2Config
import jp.co.genesis.model.Account
import jp.co.genesis.model.AccountAsset
import jp.co.genesis.model.AssetDefinition
import jp.co.genesis.model.Domain
import jp.co.genesis.model.PermissionToken
import jp.co.genesis.model.Signature
import jp.co.soramitsu.iroha2.asAccountId
import jp.co.soramitsu.iroha2.client.Iroha2Client
import jp.co.soramitsu.iroha2.generated.AccountId
import jp.co.soramitsu.iroha2.generated.Mintable
import jp.co.soramitsu.iroha2.privateKeyFromHex
import jp.co.soramitsu.iroha2.publicKeyFromHex
import jp.co.soramitsu.iroha2.query.QueryBuilder
import jp.co.soramitsu.iroha2.toHex
import org.springframework.stereotype.Service
import java.security.KeyPair

@Service
class IrohaService(
    val mapper: ObjectMapper = ObjectMapper(),
) {

    suspend fun query(config: Iroha2Config): String {
        when (true) {
            config.url.isEmpty() -> throw RuntimeException("Incorrect url")
            config.adminAccount.isEmpty() -> throw RuntimeException("Incorrect admin account")
            config.adminPublicKeyHex.isEmpty() -> throw RuntimeException("Incorrect admin public key hex")
            config.adminPrivateKeyHex.isEmpty() -> throw RuntimeException("Incorrect admin private key hex")
            else -> {}
        }

        val credentials = when (config.username.isNotEmpty() && config.password.isNotEmpty()) {
            true -> "${config.username}:${config.password}"
            false -> null
        }

        val iroha2Client = Iroha2Client(
            apiUrl = config.url,
            telemetryUrl = config.url,
            peerUrl = config.url,
            log = true,
            credentials,
        )
        val adminAccount = config.adminAccount.asAccountId()

        QueryBuilder.findAllDomains()
            .account(adminAccount)
            .buildSigned(
                KeyPair(
                    publicKeyFromHex(config.adminPublicKeyHex),
                    privateKeyFromHex(config.adminPrivateKeyHex),
                ),
            )
            .let { query ->
                iroha2Client.sendQuery(query)
            }
            .let { domains ->
                domains.map { domain ->
                    val accounts = domain.accounts.map { (accountId, account) ->
                        val accountAssets = account.assets.map { (t, u) ->
                            AccountAsset(
                                id = t.definitionId.name.string,
                                domainId = t.definitionId.domainId.name.string,
                                value = u.value.toString(),
                            )
                        }
                        val accountSignatories = account.signatories.map { publicKey ->
                            Signature(publicKeyHex = publicKey.payload.toHex())
                        }
                        val accountMetadata = account.metadata.map.entries.associate {
                            it.key.string to it.value.toString()
                        }.toMap()

                        val permissions = getPermissions(accountId, adminAccount, config, iroha2Client)

                        Account(
                            name = accountId.name.string,
                            id = account.id.name.string,
                            domainId = accountId.domainId.name.string,
                            assets = accountAssets,
                            signatories = accountSignatories,
                            metadata = accountMetadata,
                            permissions,
                        )
                    }
                    val domainMetadata = domain.metadata.map.entries.associate {
                        it.key.string to it.value.toString()
                    }.toMap()
                    val assetDefinitions = getAssetDefinitions(domain)
                    Domain(
                        id = domain.id.name.string,
                        domainMetadata = domainMetadata,
                        accounts = accounts,
                        assetDefinitions = assetDefinitions,
                    )
                }
            }.let {
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(it)
            }
    }

    suspend fun getPermissions(
        accountId: AccountId,
        adminAccount: AccountId,
        config: Iroha2Config,
        iroha2Client: Iroha2Client,
    ): List<PermissionToken> {
        return QueryBuilder.findPermissionTokensByAccountId(accountId)
            .account(adminAccount)
            .buildSigned(
                KeyPair(
                    publicKeyFromHex(config.adminPublicKeyHex),
                    privateKeyFromHex(config.adminPrivateKeyHex),
                ),
            )
            .let { query ->
                iroha2Client.sendQuery(query)
            }.map { PermissionToken(it.definitionId.string, it.payload.string) }
    }

    suspend fun getAssetDefinitions(domain: jp.co.soramitsu.iroha2.generated.Domain): List<AssetDefinition> {
        return domain.assetDefinitions.map { (t, u) ->

            val assetMetadata = u.metadata.map.entries.associate {
                it.key.string to it.value.toString()
            }.toMap()

            val mintable = when (u.mintable) {
                is Mintable.Infinitely -> "Infinitely"
                is Mintable.Not -> "Not"
                is Mintable.Once -> "Once"
            }

            AssetDefinition(
                name = t.name.string,
                domainId = t.domainId.name.string,
                valueType = u.valueType.toString(),
                metadata = assetMetadata,
                mintable = mintable,
            )
        }
    }
}
