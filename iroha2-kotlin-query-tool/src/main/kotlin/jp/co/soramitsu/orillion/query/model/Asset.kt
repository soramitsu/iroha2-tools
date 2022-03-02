package jp.co.soramitsu.orillion.query.model

data class Asset(
    val name: String,
    val domainId: String,
    val valueType: String,
    val metadata: Map<String, String>,
    val mintable: Boolean
)
