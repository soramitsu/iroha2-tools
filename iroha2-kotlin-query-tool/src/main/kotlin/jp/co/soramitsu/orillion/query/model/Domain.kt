package jp.co.soramitsu.orillion.query.model

data class Domain(
    val id: String,
    val domainMetadata: Map<String, String>,
    val accounts: List<Account>,
    val assets: List<Asset>
)
