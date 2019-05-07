package ru.inforion.lab403.kopycat.cores.x86.operands

import ru.inforion.lab403.common.extensions.toULong
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.Near
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.SSR.cs
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class x86Near(dtyp: Datatype, offset: Int, override val ssr: x86Register = cs) : Near<x86Core>(offset, dtyp) {
    override fun value(core: x86Core): Long = offset.toULong()

    override fun toString(): String = if (offset > 0) {
        "0x%04X".format(offset)
    } else {
        "-0x%04X".format(offset)
    }
}