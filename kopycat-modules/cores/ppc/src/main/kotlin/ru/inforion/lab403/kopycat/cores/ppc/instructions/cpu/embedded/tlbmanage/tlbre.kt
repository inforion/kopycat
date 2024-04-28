/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2023 INFORION, LLC
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
package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.embedded.tlbmanage

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.kopycat.cores.ppc.enums.systems.embedded.mmufsl.eMAS0
import ru.inforion.lab403.kopycat.cores.ppc.enums.systems.embedded.mmufsl.eMAS3
import ru.inforion.lab403.kopycat.cores.ppc.enums.systems.embedded.mmufsl.eSPR_EmbeddedMMUFSL
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.modules.cores.PPCCore

/**
 * Created by shiftdj on 15.03.2021.
 */

//TLB Read Entry
class tlbre(core: PPCCore, val fieldA: Int, val fieldB: Int, val fieldC: Int, val flag: Boolean):
        APPCInstruction(core, Type.VOID) {
    override val mnem = "tlbre"

    override fun execute() {
        val mas0 = core.cpu.spr(eSPR_EmbeddedMMUFSL.MAS0.id).value(core)
        val tlb = mas0[eMAS0.TLBSEL].int
        val ent = mas0[eMAS0.ESEL].int
        val entry = core.mmu.tlbRead(tlb, ent)

        val mas3 = entry.mas3.insert(entry.rpn[31..12], eMAS3.RPNL)
        val mas7 = entry.rpn[63..32]


        core.cpu.spr(eSPR_EmbeddedMMUFSL.MAS1.id).value(core, entry.mas1)
        core.cpu.spr(eSPR_EmbeddedMMUFSL.MAS2.id).value(core, entry.mas2)
        core.cpu.spr(eSPR_EmbeddedMMUFSL.MAS3.id).value(core, mas3)
        core.cpu.spr(eSPR_EmbeddedMMUFSL.MAS7.id).value(core, mas7)
    }
}