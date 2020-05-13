package ru.inforion.lab403.kopycat.modules.virtarm

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.modules.*
import java.util.logging.Level



class NANDCtrl(parent: Module, name: String) : Module(parent, name) {

    companion object {
        val log = logger(Level.FINER)
    }

    // bit[1]       - CLE, Command Latch Enable
    // bit[0]       - ALE, Address Latch Enable
    inner class Ports : ModulePorts(this) {
        val mem = Slave("inp", BUS02)
        val nand = Master("outp", NAND_BUS_SIZE)
    }

    override val ports = Ports()

    val area = object : Area(ports.mem, 0, 0b11L, "area") {

        override fun read(ea: Long, ss: Int, size: Int): Long = ports.nand.read(NAND_IO, 0, 1)

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            when (ea) {
                0b00L -> ports.nand.write(NAND_IO, 0, 1, value)
                0b01L -> ports.nand.write(NAND_ADDRESS, 0, 1, value)
                0b10L -> ports.nand.write(NAND_CMD, 0, 1, value)
                0b11L -> throw GeneralException("Can't be CLE and ALE at the same time")
            }
        }
    }
}