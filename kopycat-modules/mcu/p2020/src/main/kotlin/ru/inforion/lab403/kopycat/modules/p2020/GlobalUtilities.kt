package ru.inforion.lab403.kopycat.modules.p2020

import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.field
import ru.inforion.lab403.kopycat.modules.BUS32


 
class GlobalUtilities(parent: Module, name: String) : Module(parent, name) {

    inner class Ports : ModulePorts(this) {
        val inp = Slave("in", BUS32)
        val ctrl = Slave("ctrl", BUS32)
    }

    override val ports = Ports()

    val GUTS_PORPLLSR = object : Register(ports.ctrl, 0xE_0000, Datatype.DWORD, "GUTS_PORPLLSR", writable = false) {
        var e500_1_Ratio by field(29..24)
        var e500_0_Ratio by field(21..16)
        var DDR_Ratio by field(13..9)
        var Plat_Ratio by field(5..1)

        override fun reset() {
            super.reset()
            e500_0_Ratio = 0b10 // 1:1
            e500_1_Ratio = 0b10 // 1:1
            DDR_Ratio = 0b11    // 3:1
            Plat_Ratio = 0b100  // 4:1
        }

    }


}