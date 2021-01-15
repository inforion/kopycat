/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2020 INFORION, LLC
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Non-free licenses may also be purchased from INFORION, LLC, 
 * for users who do not want their programs protected by the GPL. 
 * Contact us for details kopycat@inforion.ru
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package ru.inforion.lab403.kopycat.cores.mips.exceptions

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.kopycat.cores.base.abstracts.AInterrupt
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction.*
import ru.inforion.lab403.kopycat.cores.base.exceptions.HardwareException
import ru.inforion.lab403.kopycat.cores.mips.enums.ExcCode

/**
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

    inline val vpn2 get() = vAddr[31..13]

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