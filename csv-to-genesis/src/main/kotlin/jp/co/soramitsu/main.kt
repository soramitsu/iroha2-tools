package jp.co.soramitsu

import java.io.File
import kotlin.io.path.Path

fun main(vararg args: String) {
    val converter = Converter()
    val genesis = converter.convert(File(args[0]), File(args[1]))

    genesis.writeToFile(Path(args[2]))
}
