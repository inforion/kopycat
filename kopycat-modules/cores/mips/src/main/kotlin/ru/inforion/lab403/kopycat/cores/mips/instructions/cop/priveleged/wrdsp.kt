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
package ru.inforion.lab403.kopycat.cores.mips.instructions.cop.priveleged

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.untruth
import ru.inforion.lab403.kopycat.cores.base.abstracts.AInstruction.Type.VOID
import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException
import ru.inforion.lab403.kopycat.cores.mips.instructions.RdInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * RDDSP rd - for DSP extension
 */
class wrdsp(core: MipsCore,
            data: ULong,
            rd: MipsRegister,
            private val mask: MipsImmediate
) : RdInsn(core, data, VOID, rd) {

    override val mnem = "wrdsp"

    override fun execute() {
        if (!core.dspExtension) throw MipsHardwareException.DSPDis(core.pc)
        val rs = rd.value(core)
        val dspCtrl = core.dspModule?.regs?.DSPControl

        if (mask.value[0] == 1uL) dspCtrl?.pos = rs[5..0]
        if (mask.value[1] == 1uL) dspCtrl?.scount = rs[12..7]
        if (mask.value[2] == 1uL) dspCtrl?.c = rs[13].untruth
        if (mask.value[3] == 1uL) dspCtrl?.ouflag = rs[23..16]
        if (mask.value[4] == 1uL) dspCtrl?.ccond = rs[31..24]
        if (mask.value[5] == 1uL) dspCtrl?.EFI = rs[14].untruth

    }
}

