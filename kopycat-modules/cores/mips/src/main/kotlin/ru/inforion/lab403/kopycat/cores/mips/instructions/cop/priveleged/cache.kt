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
package ru.inforion.lab403.kopycat.cores.mips.instructions.cop.priveleged

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.kopycat.cores.base.abstracts.AInstruction.Type.VOID
import ru.inforion.lab403.kopycat.cores.mips.instructions.OpOffsetBaseInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsDisplacement
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * CACHE op, offset(base)
 */
class cache(core: MipsCore,
            data: Long,
            imm: MipsImmediate,
            off: MipsDisplacement) : OpOffsetBaseInsn(core, data, VOID, imm, off) {

    companion object {
        const val Index_Invalidate_I = 0x00
        const val Index_Writeback_Inv_D = 0x01
        const val Index_Load_Tag_I = 0x04
        const val Index_Load_Tag_D = 0x05
        const val Index_Store_Tag_I = 0x08
        const val Index_Store_Tag_D = 0x09
    }

    override val mnem = "cache"

    override fun execute() {
        val spramConfigEnabled = core.cop.regs.ErrCtl.value[28] == 1L

        if (spramConfigEnabled) {
            val opId = imm.value(core)
            val off = off.value(core)

            when (opId.asInt) {
                Index_Invalidate_I -> {
                    log.severe { "[${core.pc.hex8}] Index_Invalidate_I not implemented!" }
                    core.cop.regs.TagLo0.value = 0
                }
                Index_Writeback_Inv_D -> {
                    log.severe { "[${core.pc.hex8}] Index_Writeback_Inv_D not implemented!" }
                    core.cop.regs.TagLo2.value = 0
                }

                Index_Load_Tag_I -> {
                    log.severe { "[${core.pc.hex8}] Index_Load_Tag_I not implemented TagLo0=${core.cop.regs.TagLo0.value.hex8}!" }
                    core.cop.regs.TagLo0.value = 0
                }

                Index_Load_Tag_D -> {
                    log.severe { "[${core.pc.hex8}] Index_Load_Tag_D not implemented TagLo2=${core.cop.regs.TagLo2.value.hex8}!" }
                    core.cop.regs.TagLo2.value = 0
                }

                Index_Store_Tag_I -> {
                    log.severe { "[${core.pc.hex8}] Index_Store_Tag_I not implemented TagLo0=${core.cop.regs.TagLo0.value.hex8}!" }
                    core.cop.regs.TagLo0.value = 0
                }

                Index_Store_Tag_D -> {
                    log.severe { "[${core.pc.hex8}] Index_Store_Tag_D not implemented TagLo2=${core.cop.regs.TagLo2.value.hex8}!" }
                    core.cop.regs.TagLo2.value = 0
                }
            }
        }

//        log.warning { "Be careful, cache instruction isn't emulate..." }
    }
}
