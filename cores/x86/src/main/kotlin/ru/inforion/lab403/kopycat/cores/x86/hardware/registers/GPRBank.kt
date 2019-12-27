package ru.inforion.lab403.kopycat.cores.x86.hardware.registers

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by v.davydov on 08.09.16.
 */

class GPRBank(core: x86Core) : ARegistersBank<x86Core, x86GPR>(core, x86GPR.values(), bits = 32) {
    override val name: String = "CPU General Purpose Registers"

    var eax by valueOf(x86Register.GPRDW.eax)
    var ecx by valueOf(x86Register.GPRDW.ecx)
    var ebx by valueOf(x86Register.GPRDW.ebx)
    var edx by valueOf(x86Register.GPRDW.edx)
    var esp by valueOf(x86Register.GPRDW.esp)
    var ebp by valueOf(x86Register.GPRDW.ebp)
    var esi by valueOf(x86Register.GPRDW.esi)
    var edi by valueOf(x86Register.GPRDW.edi)

    var ax by valueOf(x86Register.GPRW.ax)
    var cx by valueOf(x86Register.GPRW.cx)
    var bx by valueOf(x86Register.GPRW.bx)
    var dx by valueOf(x86Register.GPRW.dx)
    var sp by valueOf(x86Register.GPRW.sp)
    var bp by valueOf(x86Register.GPRW.bp)
    var si by valueOf(x86Register.GPRW.si)
    var di by valueOf(x86Register.GPRW.di)

    var al by valueOf(x86Register.GPRBL.al)
    var cl by valueOf(x86Register.GPRBL.cl)
    var bl by valueOf(x86Register.GPRBL.bl)
    var dl by valueOf(x86Register.GPRBL.dl)

    var ah by valueOf(x86Register.GPRBH.ah)
    var ch by valueOf(x86Register.GPRBH.ch)
    var bh by valueOf(x86Register.GPRBH.bh)
    var dh by valueOf(x86Register.GPRBH.dh)

    var eip by valueOf(x86Register.GPRDW.eip)
    var ip by valueOf(x86Register.GPRW.ip)

    override fun reset() {
        super.reset()
        eip = 0xFFF0
    }
}