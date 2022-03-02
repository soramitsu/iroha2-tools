package jp.co.soramitsu.orillion.query.model

data class Account(
    val name: String,
    val id: String,
    val domainId: String,
    val assets: List<AccountAsset>,
    val signatories: List<Signature>,
    val metadata: Map<String, String>,
    val permissionTokens: List<PermissionToken>
)
