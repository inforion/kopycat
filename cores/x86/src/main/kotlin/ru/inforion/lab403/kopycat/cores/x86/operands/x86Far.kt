package ru.inforion.lab403.kopycat.cores.x86.operands

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.Far
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class x86Far(address: Long, val ss: Long) : Far<x86Core>(address, Datatype.DWORD) {
    override fun value(dev: x86Core): Long = address
    override fun toString(): String = "%04X:%08X".format(ss, address)
}