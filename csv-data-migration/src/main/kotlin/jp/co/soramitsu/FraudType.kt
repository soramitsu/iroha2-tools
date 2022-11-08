package jp.co.soramitsu

enum class FraudType(val code: Int) {
    Wangiri(0),
    IRSF(1),
    StolenDevice(2),
    IPFraud(3),
    SMSA2P(4);

    companion object {
        fun from(code: Int): FraudType {
            return when (code) {
                Wangiri.code -> Wangiri
                IRSF.code -> IRSF
                StolenDevice.code -> StolenDevice
                IPFraud.code -> IPFraud
                SMSA2P.code -> SMSA2P
                else -> throw RuntimeException("Could not resolver fraud type by code: $code")
            }
        }
    }
}
