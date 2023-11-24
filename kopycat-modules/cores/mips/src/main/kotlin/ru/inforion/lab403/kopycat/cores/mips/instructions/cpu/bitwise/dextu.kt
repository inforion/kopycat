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
package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.bitwise

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException
import ru.inforion.lab403.kopycat.cores.mips.instructions.RsRtPosSizeInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * DEXTU rt, rs, pos, size
 *
 * Doubleword Extract Bit Field Upper
 *
 * To extract a bit field from GPR rs and store it right-justified into GPR rt
 */

class dextu(
    core: MipsCore,
    data: ULong,
    rt: MipsRegister,
    rs: MipsRegister,
    val lsbminus32: MipsImmediate,
    val msbd: MipsImmediate
) : RsRtPosSizeInsn(core, data, Type.VOID, rt, rs, lsbminus32, msbd) {

    override val mnem = "dextu"

    override fun execute() {
        if (core.ArchitectureRevision == 1) throw MipsHardwareException.RI(core.pc)

        val pos = lsbminus32.value.int + 32
        val size = msbd.value.int + 1

        vrt = if (pos + size > 64) {
            log.severe { "${this.mnem}: lsb + msb > 64 -> UNPREDICTABLE [pc=${core.pc.hex}]" }
            0u
        } else {
            val temp = (pos + msbd.value.int)..pos
            vrs[temp]     // TODO: test this
        }
    }

}