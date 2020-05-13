package ru.inforion.lab403.kopycat.modules.p2020

import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.modules.BUS32
import java.nio.ByteOrder



class DDRMemory(parent: Module,
                name: String,
                val n: Int,
                val size: Long) : Module(parent, name) {

    inner class Ports : ModulePorts(this) {
        val inp = Slave("in", BUS32)
    }

    override val ports = Ports()

    fun startAddress(n: Int) = when (n) {
        0 -> 0x0000_0000L
        1 -> 0x0100_0000L
        2 -> 0x1000_0000L
        3 -> 0x1100_0000L
        else -> throw GeneralException("Wrong n: $n")
    }

    val mem = Memory(ports.inp, startAddress(n), startAddress(n) + size, "DDR${n}_MEMORY", ACCESS.R_W)


    init {
        mem.endian = ByteOrder.BIG_ENDIAN
    }

}