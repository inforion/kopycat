package ru.inforion.lab403.kopycat.cores.mips.hardware.registers

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.cores.mips.enums.RSVD
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by batman on 28/06/16.
 */
class RSVDBank(core: MipsCore) : ARegistersBank<MipsCore, RSVD>(core, RSVD.values(), bits = 32) {
    override val name: String = "Reserved Registers Bank"
//    override fun get(op: ARegister<AMIPS>): Long = throw UnsupportedOperationException()
//    override fun set(op: ARegister<AMIPS>, value: Long): Unit = throw UnsupportedOperationException()
}