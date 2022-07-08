package jp.co.genesis.listener

import com.fasterxml.jackson.databind.ObjectMapper
import jp.co.soramitsu.iroha2.asDomainId
import jp.co.soramitsu.iroha2.asName
import jp.co.soramitsu.iroha2.client.Iroha2Client
import jp.co.soramitsu.iroha2.generated.datamodel.account.Id
import jp.co.soramitsu.iroha2.generated.datamodel.asset.Mintable
import jp.co.soramitsu.iroha2.privateKeyFromHex
import jp.co.soramitsu.iroha2.publicKeyFromHex
import jp.co.soramitsu.iroha2.query.QueryBuilder
import jp.co.soramitsu.iroha2.toHex
import jp.co.genesis.config.Iroha2Config
import jp.co.genesis.model.Account
import jp.co.genesis.model.AccountAsset
import jp.co.genesis.model.Asset
import jp.co.genesis.model.Domain
import jp.co.genesis.model.PermissionToken
import jp.co.genesis.model.Signature
import kotlinx.coroutines.runBlocking
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import java.security.KeyPair

@Component
class ContextListener(val config: Iroha2Config) : ApplicationListener<ApplicationReadyEvent> {

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        if (config.basicAuth.isEmpty() || config.url.isEmpty() ||
            config.adminAccount.isEmpty() || config.adminPrivateKeyHex.isEmpty()
        ) {
            event.applicationContext.close()
            return
        }

        try {
            val iroha2Client = Iroha2Client(config.proxyUrl, log = true)
            val tokens = config.adminAccount.split(config.accountDelimiter)
            val adminAccount = Id(tokens[0].asName(), tokens[1].asDomainId())

            QueryBuilder.findAllDomains()
                .account(adminAccount)
                .buildSigned(KeyPair(publicKeyFromHex(config.adminPublicKeyHex), privateKeyFromHex(config.adminPrivateKeyHex)))
                .let { query ->
                    runBlocking {
                        iroha2Client.sendQuery(query)
                    }
                }
                .let { domains ->
                    domains.map { domain ->
                        val accounts = domain.accounts.map { (t, u) ->
                            val accountAssets = u.assets.map { (t, u) ->
                                AccountAsset(
                                    id = t.definitionId.name.string,
                                    domainId = t.definitionId.domainId.name.string,
                                    value = u.value.toString()
                                )
                            }
                            val accountSignatories = u.signatories.map { publicKey ->
                                Signature(publicKeyHex = publicKey.payload.toHex())
                            }
                            val accountMetadata = u.metadata.map.entries.associate {
                                it.key.string to it.value.toString()
                            }.toMap()
                            val permissionTokens = u.permissionTokens.map { token ->
                                val params = token.params.entries.associate {
                                    it.key.string to it.value.toString()
                                }.toMap()
                                PermissionToken(
                                    name = token.name.string,
                                    params = params
                                )
                            }
                            Account(
                                name = t.name.string,
                                id = u.id.name.string,
                                domainId = t.domainId.name.string,
                                assets = accountAssets,
                                signatories = accountSignatories,
                                metadata = accountMetadata,
                                permissionTokens = permissionTokens
                            )
                        }
                        val domainMetadata = domain.metadata.map.entries.associate {
                            it.key.string to it.value.toString()
                        }.toMap()
                        val assets = domain.assetDefinitions.map { (t, u) ->
                            val assetMetadata = u.definition.metadata.map.entries.associate {
                                it.key.string to it.value.toString()
                            }.toMap()

                            val mintable = when (u.definition.mintable) {
                                is Mintable.Infinitely -> "Infinitely"
                                is Mintable.Not -> "Not"
                                is Mintable.Once -> "Once"
                            }

                            Asset(
                                name = t.name.string,
                                domainId = t.domainId.name.string,
                                valueType = u.definition.valueType.toString(),
                                metadata = assetMetadata,
                                mintable = mintable
                            )
                        }
                        Domain(
                            id = domain.id.name.string,
                            domainMetadata = domainMetadata,
                            accounts = accounts,
                            assets = assets
                        )
                    }
                }.let {
                    val mapper = ObjectMapper()
                    val result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(it)
                    println(result)
                }
        } catch (e: Exception) {
            println("Failed to query Iroha2: ${e.message}")
        }

        event.applicationContext.close()
    }
}
