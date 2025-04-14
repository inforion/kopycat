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
package ru.inforion.lab403.kopycat.modules.elanSC520

import ru.inforion.lab403.common.logging.FINER
import ru.inforion.lab403.common.logging.FINEST
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.BYTE
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD


class DMAC(parent: Module, name: String) : Module(parent, name) {
    companion object {
        @Transient val log = logger(FINER)
    }

    inner class Ports : ModulePorts(this) {
        val mmcr = Port("mmcr")
        val io = Port("io")
    }

    override val ports = Ports()
    
    val GPDMA0MAR = Register(ports.io,0x0000u, BYTE, name = "GPDMA0MAR")
    val GPDMA0TC = Register(ports.io,0x0001u, BYTE, name = "GPDMA0TC")
    val GPDMA1MAR = Register(ports.io,0x0002u, BYTE, name = "GPDMA1MAR")
    val GPDMA1TC = Register(ports.io,0x0003u, BYTE, name = "GPDMA1TC")
    val GPDMA2MAR = Register(ports.io,0x0004u, BYTE, name = "GPDMA2MAR")
    val GPDMA2TC = Register(ports.io,0x0005u, BYTE, name = "GPDMA2TC")
    val GPDMA3MAR = Register(ports.io,0x0006u, BYTE, name = "GPDMA3MAR")
    val GPDMA3TC = Register(ports.io,0x0007u, BYTE, name = "GPDMA3TC")
    val SLDMASTA = Register(ports.io,0x0008u, BYTE, name = "SLDMASTA")
    val SLDMACTL = Register(ports.io,0x0008u, BYTE, name = "SLDMACTL")
    val SLDMASWREQ = Register(ports.io,0x0009u, BYTE, name = "SLDMASWREQ")
    val SLDMAMSK = Register(ports.io,0x000Au, BYTE, name = "SLDMAMSK")
    val SLDMAMODE = Register(ports.io,0x000Bu, BYTE, name = "SLDMAMODE")
    val SLDMACBP = Register(ports.io,0x000Cu, BYTE, name = "SLDMACBP")
    val SLDMARST = Register(ports.io,0x000Du, BYTE, name = "SLDMARST")
    val SLDMATMP = Register(ports.io,0x000Du, BYTE, name = "SLDMATMP")
    val SLDMAMSKRST = Register(ports.io,0x000Eu, BYTE, name = "SLDMAMSKRST", level = FINEST)
    val SLDMAGENMSK = Register(ports.io,0x000Fu, BYTE, name = "SLDMAGENMSK")

    // DMA Page and General Registers
    val GPDMAGR0 = Register(ports.io,0x0080u, BYTE, name = "GPDMAGR0", level = FINEST)
    val GPDMA2PG = Register(ports.io,0x0081u, BYTE, name = "GPDMA2PG")
    val GPDMA3PG = Register(ports.io,0x0082u, BYTE, name = "GPDMA3PG")
    val GPDMA1PG = Register(ports.io,0x0083u, BYTE, name = "GPDMA1PG")
    val GPDMAGR1 = Register(ports.io,0x0084u, BYTE, name = "GPDMAGR1", level = FINEST)
    val GPDMAGR2 = Register(ports.io,0x0085u, BYTE, name = "GPDMAGR2")
    val GPDMAGR3 = Register(ports.io,0x0086u, BYTE, name = "GPDMAGR3")
    val GPDMA0PG = Register(ports.io,0x0087u, BYTE, name = "GPDMA0PG")
    val GPDMAGR4 = Register(ports.io,0x0088u, BYTE, name = "GPDMAGR4")
    val GPDMA6PG = Register(ports.io,0x0089u, BYTE, name = "GPDMA6PG")
    val GPDMA7PG = Register(ports.io,0x008Au, BYTE, name = "GPDMA7PG")
    val GPDMA5PG = Register(ports.io,0x008Bu, BYTE, name = "GPDMA5PG")
    val GPDMAGR5 = Register(ports.io,0x008Cu, BYTE, name = "GPDMAGR5")
    val GPDMAGR6 = Register(ports.io,0x008Du, BYTE, name = "GPDMAGR6")
    val GPDMAGR7 = Register(ports.io,0x008Eu, BYTE, name = "GPDMAGR7")
    val GPDMAGR8 = Register(ports.io,0x008Fu, BYTE, name = "GPDMAGR8")

    // Master DMA
    val GPDMA4MAR = Register(ports.io,0x00C0u, BYTE, name = "GPDMA4MAR")
    val GPDMA4TC = Register(ports.io,0x00C2u, BYTE, name = "GPDMA4TC")
    val GPDMA5MAR = Register(ports.io,0x00C4u, BYTE, name = "GPDMA5MAR")
    val GPDMA5TC = Register(ports.io,0x00C6u, BYTE, name = "GPDMA5TC")
    val GPDMA6MAR = Register(ports.io,0x00C8u, BYTE, name = "GPDMA6MAR")
    val GPDMA6TC = Register(ports.io,0x00CAu, BYTE, name = "GPDMA6TC")
    val GPDMA7MAR = Register(ports.io,0x00CCu, BYTE, name = "GPDMA7MAR")
    val GPDMA7TC = Register(ports.io,0x00CEu, BYTE, name = "GPDMA7TC")

    val MSTDMASTA_CTL = Register(ports.io,0x00D0u, BYTE, name = "MSTDMASTA_CTL")
    val MSTDMASWREQ = Register(ports.io,0x00D2u, BYTE, name = "MSTDMASWREQ")
    val MSTDMAMSK = Register(ports.io,0x00D4u, BYTE, name = "MSTDMAMSK")
    val MSTDMAMODE = Register(ports.io,0x00D6u, BYTE, name = "MSTDMAMODE")
    val MSTDMACBP = Register(ports.io,0x00D8u, BYTE, name = "MSTDMACBP")
    val MSTDMARST_TMP = Register(ports.io,0x00DAu, BYTE, name = "MSTDMARST")
    val MSTDMAMSKRST = Register(ports.io,0x00DCu, BYTE, name = "MSTDMAMSKRST")
    val MSTDMAGENMSK = Register(ports.io,0x00DEu, BYTE, name = "MSTDMAGENMSK")

    val GPDMACTL = Register(ports.mmcr, 0xD80u, BYTE, "GPDMACTL")
    val GPDMGPDMAMMIOACTL = Register(ports.mmcr, 0xD81u, BYTE, "GPDMAMMIO")
    val GPDMAEXTCHMAPA = Register(ports.mmcr, 0xD82u, WORD, "GPDMAEXTCHMAPA")
    val GPDMAEXTCHMAPB = Register(ports.mmcr, 0xD84u, WORD, "GPDMAEXTCHMAPB")
}