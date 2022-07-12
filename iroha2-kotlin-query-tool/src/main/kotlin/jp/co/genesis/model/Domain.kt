package jp.co.genesis.model

data class Domain(
    val id: String,
    val domainMetadata: Map<String, String>,
    val accounts: List<Account>,
    val assets: List<Asset>
)
