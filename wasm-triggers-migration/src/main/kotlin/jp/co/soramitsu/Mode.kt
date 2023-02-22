package jp.co.soramitsu

enum class Mode(val mode: Int) {
    DEFAULT(0),
    REGISTER(1),
    UNREGISTER(2);

    companion object {
        fun from(mode: Int): Mode {
            return when (mode) {
                DEFAULT.mode -> DEFAULT
                REGISTER.mode -> REGISTER
                UNREGISTER.mode -> UNREGISTER
                else -> throw RuntimeException("Mode $mode not supported")
            }
        }
    }
}
