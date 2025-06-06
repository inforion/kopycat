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
package ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.systems

import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.APPCSystemDecoder
import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.decoders.FormX
import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.support.PatternTable
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.embedded.tlbmanage.tlbivax
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.embedded.tlbmanage.tlbre
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.embedded.tlbmanage.tlbwe
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



class PPCDecoder_EmbeddedMMUFSL(core: PPCCore) : APPCSystemDecoder(core) {

    override val name = "PowerPC Embedded MMU/FSL Decoder"

    private val tlbivaxDc = FormX(core, ::tlbivax)
    private val tlbreDc = FormX(core, ::tlbre)
    private val tlbweDc = FormX(core, ::tlbwe)

    override val group31 = PatternTable("Group of opcode 31",
            arrayOf(10..0),
            arrayOf("11000100100" to tlbivaxDc,
                    "11101100100" to tlbreDc,
                    "11110100100" to tlbweDc))
}