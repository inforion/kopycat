package ru.inforion.lab403.kopycat.cores.ppc.operands.systems

import ru.inforion.lab403.common.extensions.first
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.enums.Regtype
import ru.inforion.lab403.kopycat.cores.ppc.enums.systems.embedded.eOEA_Embedded
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore
import ru.inforion.lab403.kopycat.modules.cores.PPCCoreEmbedded

abstract class PPCRegister_Embedded(
        reg: Int,
        rtyp: Regtype,
        access: AOperand.Access = AOperand.Access.ANY) :
        PPCRegister(reg, rtyp, access) {

    override fun toString() = when (rtyp) {
        Regtype.Embedded -> first<eOEA_Embedded> { it.id == reg }.name
        else -> super.toString()
    }.toLowerCase()

    sealed class OEAext(id: Int) : PPCRegister_Embedded(id, Regtype.Embedded) {
        override fun value(core: PPCCore): Long = core.cpu.sprRegs.readIntern(reg)
        override fun value(core: PPCCore, data: Long) = core.cpu.sprRegs.writeIntern(reg, data)

        open class REG_DBG_DENIED(id: Int) : OEAext(id) {
            override fun value(core: PPCCore) = denied_read(reg)
            override fun value(core: PPCCore, data: Long) = denied_write(reg)
        }

        open class REG_DBG_READ(id: Int) : OEAext(id) {
            override fun value(core: PPCCore, data: Long) = denied_write(reg)
        }

        open class REG_DBG_WRITE(id: Int) : OEAext(id) {
            override fun value(core: PPCCore) = denied_read(reg)
        }


        //Interrupt registers

        //Interrupt vector prefix
        object IVPR : OEAext(eOEA_Embedded.IVPR.id)

        //Process ID registers 0
        object PID0 : OEAext(eOEA_Embedded.PID0.id)

        //Timer control
        //TODO: WATCHDOG ROUTINE
        //Waiting for read point
        object TCR : OEAext(eOEA_Embedded.TCR.id) {

            override fun value(core: PPCCore): Long {
                log.warning { "Warning: read from TCR" }
                //TODO("Catch for read")
                return super.value(core)
            }

            override fun value(core: PPCCore , data: Long) {
                super.value(core, data)
                (core as PPCCoreEmbedded).timebase.operateTCR(data)
            }

        }

        // Timer status register
        object TSR : REG_DBG_WRITE(eOEA_Embedded.TSR.id) {
            override fun value(core: PPCCore, data: Long) {
                //TODO: replace to "value":
                val oldData = core.cpu.sprRegs.readIntern(reg)
                super.value(core, oldData and data)
            }
        }

        object DECAR : OEAext(eOEA_Embedded.DECAR.id) {
            override fun value(core: PPCCore): Long = throw GeneralException("DECAR can't be read")
        }
    }
}