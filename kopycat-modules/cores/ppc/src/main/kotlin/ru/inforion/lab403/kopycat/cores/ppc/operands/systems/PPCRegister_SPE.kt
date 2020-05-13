package ru.inforion.lab403.kopycat.cores.ppc.operands.systems

import ru.inforion.lab403.common.extensions.first
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.enums.Regtype
import ru.inforion.lab403.kopycat.cores.ppc.enums.systems.eUISA_SPE
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



abstract class PPCRegister_SPE(
        reg: Int,
        rtyp: Regtype,
        access: AOperand.Access = AOperand.Access.ANY) :
        PPCRegister(reg, rtyp, access) {

    override fun toString() = when (rtyp) {
        Regtype.SPE -> first<eUISA_SPE> { it.id == reg }.name
        else -> super.toString()
    }.toLowerCase()

    sealed class UISAext(id: Int) : PPCRegister_SPE(id, Regtype.SPE) {
        override fun value(core: PPCCore): Long = core.cpu.sprRegs.readIntern(reg)
        override fun value(core: PPCCore, data: Long) = core.cpu.sprRegs.writeIntern(reg, data)

        open class REG_DBG_DENIED(id: Int) : UISAext(id) {
            override fun value(core: PPCCore) = denied_read(reg)
            override fun value(core: PPCCore, data: Long) = denied_write(reg)
        }

        open class REG_DBG_READ(id: Int) : UISAext(id) {
            override fun value(core: PPCCore, data: Long) = denied_write(reg)
        }

        open class REG_DBG_WRITE(id: Int) : UISAext(id) {
            override fun value(core: PPCCore) = denied_read(reg)
        }

        //object Accumulator : REG_DBG_DENIED(eUISA_SPE.Accumulator.id)

        object SPEFSCR : UISAext(eUISA_SPE.SPEFSCR.id) {

            override fun value(core: PPCCore): Long {
                log.warning { "Read from SPEFSCR" }
                return super.value(core)
            }

            override fun value(core: PPCCore, data: Long) {
                super.value(core, data)
                log.warning { "Write to SPEFSCR" }
            }
        }
    }
}