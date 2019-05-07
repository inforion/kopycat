package ru.inforion.lab403.kopycat.cores.x86.operands

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Access.ANY
import ru.inforion.lab403.kopycat.cores.base.operands.Displacement
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.cores.x86.enums.SSR
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRDW.ebp
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRDW.esp
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRW.bp
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRW.sp
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.SSR.ds
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.SSR.ss
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class x86Displacement(
        dtyp: Datatype,
        reg: x86Register,
        off: Immediate<x86Core> = zero,
        ssr: x86Register = ds,
        access: Access = ANY
) : Displacement<x86Core>(dtyp, reg, off, access) {

    override val ssr: x86Register = if (reg != bp && reg != ebp && reg != sp && reg != esp) ssr else ss

    override fun value(core: x86Core): Long = core.read(dtyp, effectiveAddress(core), ssr.reg)
    override fun value(core: x86Core, data: Long): Unit = core.write(dtyp, effectiveAddress(core), data, ssr.reg)

    override fun toString(): String {
        val mspec = dtyp.name.toLowerCase()
        val sspec = if (ssr.reg != SSR.DS.id) "$ssr:" else ""
        val sign = if (off.isNegative) "" else "+"
        return if (off.value != 0L) "$mspec $sspec[$reg$sign$off]" else "$mspec $sspec[$reg]"
    }
}