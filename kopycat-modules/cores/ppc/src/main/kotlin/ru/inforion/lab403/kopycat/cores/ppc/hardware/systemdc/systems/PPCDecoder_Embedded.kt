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
package ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.systems

import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.APPCSystemDecoder
import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.decoders.FormX
import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.decoders.FormXL
import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.support.PatternTable
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.embedded.procCtrl.mtmsr
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.embedded.sysLink.rfi
import ru.inforion.lab403.kopycat.modules.cores.PPCCore




class PPCDecoder_Embedded(core: PPCCore) : APPCSystemDecoder(core) {

    override val name = "PowerPC Embedded Decoder"

    private val mtmsrDc = FormX(core, ::mtmsr)

    private val rfiDc = FormXL(core, ::rfi)

    //Embeded
    /*
    private val dcbiDc = FormX(core, ::dcbi)
    private val mcrxrDc = FormX(core, ::mcrxr)
    */

    override val group13 = PatternTable("Group of opcode 13",
            arrayOf(10..0),
            arrayOf("00001100100" to rfiDc))

    override val group31 = PatternTable("Group of opcode 31",
            arrayOf(10..0),
            arrayOf("00100100100" to mtmsrDc))
    //"01110101100" to dcbiDc,        //E
    //"10000000000" to mcrxrDc,       //E

}