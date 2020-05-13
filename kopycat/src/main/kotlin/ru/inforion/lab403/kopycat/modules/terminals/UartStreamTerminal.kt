package ru.inforion.lab403.kopycat.modules.terminals

import ru.inforion.lab403.common.extensions.asUInt
import ru.inforion.lab403.kopycat.cores.base.common.Module
import java.io.File
import java.io.OutputStream

class UartStreamTerminal(parent: Module, name: String, val stream: OutputStream) : UartTerminal(parent, name) {
    constructor(parent: Module, name: String, file: File) : this(parent, name, file.outputStream())
    constructor(parent: Module, name: String, path: String) : this(parent, name, File(path))

    private val writer = stream.writer()

    override fun onByteTransmitReady(byte: Byte) {
        writer.write(byte.asUInt)
        writer.flush()
    }

//    init {
//        terminalReceiveEnabled = false
//    }
}