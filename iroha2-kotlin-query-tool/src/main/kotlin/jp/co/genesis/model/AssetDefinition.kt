package jp.co.genesis.model

data class AssetDefinition(
    val name: String,
    val domainId: String,
    val valueType: String,
    val metadata: Map<String, String>,
    val mintable: String
)
