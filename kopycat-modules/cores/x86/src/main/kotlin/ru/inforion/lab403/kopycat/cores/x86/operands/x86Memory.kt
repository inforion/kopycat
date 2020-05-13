package ru.inforion.lab403.kopycat.cores.x86.operands

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Access.ANY
import ru.inforion.lab403.kopycat.cores.base.operands.Memory
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.SSR.ds
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class x86Memory(dtyp: Datatype, addr: Long, override val ssr: x86Register = ds, access: Access = ANY) :
        Memory<x86Core>(dtyp, DWORD, addr, access) {
    override fun value(core: x86Core): Long = core.read(dtyp, effectiveAddress(core), ssr.reg)
    override fun value(core: x86Core, data: Long): Unit = core.write(dtyp, effectiveAddress(core), data, ssr.reg)
}