package ru.inforion.lab403.kopycat.modules.pic32mz

import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts


class GPIO(parent: Module, name: String) : Module(parent, name) {
    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem")
    }

    override val ports = Ports()

    inner class PORT_REGISTER(offset: Long, name: String) :
            ComplexRegister(ports.mem, offset, "PORT_$name")

    val ANSEL = PORT_REGISTER(0x0000, "ANSEL")
    val TRIS = PORT_REGISTER(0x0010, "TRIS")
    val PORT = PORT_REGISTER(0x0020, "PORT")
    val LAT = PORT_REGISTER(0x0030, "LAT")
    val ODC = PORT_REGISTER(0x0040, "ODC")
    val CNPU = PORT_REGISTER(0x0050, "CNPU")
    val CNPD = PORT_REGISTER(0x0060, "CNPD")
    val CNCON = PORT_REGISTER(0x0070, "CNCON")
    val CNEN = PORT_REGISTER(0x0080, "CNEN")
    val CNSTAT = PORT_REGISTER(0x0090, "CNSTAT")
    val CNNE = PORT_REGISTER(0x00A0, "CNNE")
    val CNF = PORT_REGISTER(0x00B0, "CNF")
}