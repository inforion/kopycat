/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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
import ru.inforion.lab403.common.extensions.unaryMinus
import ru.inforion.lab403.kopycat.cores.base.abstracts.AInterrupt
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction.*
import ru.inforion.lab403.kopycat.cores.base.exceptions.HardwareException
import ru.inforion.lab403.kopycat.cores.mips.enums.ExcCode
import ru.inforion.lab403.kopycat.cores.mips.hardware.processors.MipsCPU

/**
 * @param excCode - MIPS exception code
 * vAddr - is common field and may be not used
 */
open class MipsHardwareException(
    excCode: ExcCode,
    where: ULong,
    val vAddr: ULong = -1uL,
    val cpuMode: MipsCPU.Mode = MipsCPU.Mode.R32
): HardwareException(excCode, where) {

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

    // if MipsCPU.Mode.R32 then VPN2 left border is always 31
    inline fun vpn2(segbits: Int = 31) = if (cpuMode == MipsCPU.Mode.R32) vAddr[31..13] else vAddr[segbits - 1..13]

    override fun toString(): String = "$prefix[${where.hex8}]: $excCode VA = ${vAddr.hex8}"

    class TLBMiss(LorS: AccessAction, where: ULong, vAddr: ULong) : MipsHardwareException(excCodeMiss(LorS), where, vAddr)
    class TLBInvalid(LorS: AccessAction, where: ULong, vAddr: ULong) : MipsHardwareException(excCodeInv(LorS), where, vAddr)

    class TLBModified(where: ULong, vAddr: ULong) : MipsHardwareException(ExcCode.MOD, where, vAddr)

    /**
     * EIC_Vector is common field for external interrupt controller support
     */
    class INT(where: ULong, val interrupt: AInterrupt?) : MipsHardwareException(ExcCode.INT, where)

    class BP(where: ULong) : MipsHardwareException(ExcCode.BP, where)
    class SYS(where: ULong) : MipsHardwareException(ExcCode.SYS, where)
    class OV(where: ULong) : MipsHardwareException(ExcCode.OV, where)
    class TR(where: ULong) : MipsHardwareException(ExcCode.TR, where)
    class RI(where: ULong) : MipsHardwareException(ExcCode.RI, where)
}