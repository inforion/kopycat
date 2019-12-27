package ru.inforion.lab403.kopycat.cores.mips.exceptions

import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.kopycat.cores.base.abstracts.AInterrupt
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction.*
import ru.inforion.lab403.kopycat.cores.base.exceptions.HardwareException
import ru.inforion.lab403.kopycat.cores.mips.enums.ExcCode

/**
 * Created by a.gladkikh on 14/09/16.
 *
 * excCode - MIPS exception code
 * vAddr - is common field and may be not used
 */
open class MipsHardwareException(excCode: ExcCode, where: Long, val vAddr: Long = -1): HardwareException(excCode, where) {
    companion object {
        private fun excCodeMiss(LorS: AccessAction): ExcCode = when (LorS) {
            STORE -> ExcCode.TLBS_MISS
            LOAD, FETCH -> ExcCode.TLBL_MISS
        }

        private fun excCodeInv(LorS: AccessAction): ExcCode = when (LorS) {
            STORE -> ExcCode.TLBS_INVALID
            LOAD, FETCH -> ExcCode.TLBL_INVALID
        }
    }

    override fun toString(): String = "$prefix[${where.hex8}]: $excCode VA = ${vAddr.hex8}"

    class TLBMiss(LorS: AccessAction, where: Long, vAddr: Long) : MipsHardwareException(excCodeMiss(LorS), where, vAddr)
    class TLBInvalid(LorS: AccessAction, where: Long, vAddr: Long) : MipsHardwareException(excCodeInv(LorS), where, vAddr)

    class TLBModified(where: Long, vAddr: Long) : MipsHardwareException(ExcCode.MOD, where, vAddr)

    /**
     * EIC_Vector is common field for external interrupt controller support
     */
    class INT(where: Long, val interrupt: AInterrupt?) : MipsHardwareException(ExcCode.INT, where)

    class BP(where: Long) : MipsHardwareException(ExcCode.BP, where)
    class SYS(where: Long) : MipsHardwareException(ExcCode.SYS, where)
    class OV(where: Long) : MipsHardwareException(ExcCode.OV, where)
    class TR(where: Long) : MipsHardwareException(ExcCode.TR, where)
    class RI(where: Long) : MipsHardwareException(ExcCode.RI, where)
}