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
package ru.inforion.lab403.kopycat.modules.elanSC520

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.BYTE
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.modules.BUS12
import ru.inforion.lab403.kopycat.modules.BUS16
import java.util.logging.Level
import java.util.logging.Level.FINER


class DMAC(parent: Module, name: String) : Module(parent, name) {
    companion object {
        @Transient val log = logger(FINER)
    }

    inner class Ports : ModulePorts(this) {
        val mmcr = Slave("mmcr", BUS12)
        val io = Slave("io", BUS16)
    }

    override val ports = Ports()
    
    val GPDMA0MAR = Register(ports.io,0x0000, BYTE, name = "GPDMA0MAR")
    val GPDMA0TC = Register(ports.io,0x0001, BYTE, name = "GPDMA0TC")
    val GPDMA1MAR = Register(ports.io,0x0002, BYTE, name = "GPDMA1MAR")
    val GPDMA1TC = Register(ports.io,0x0003, BYTE, name = "GPDMA1TC")
    val GPDMA2MAR = Register(ports.io,0x0004, BYTE, name = "GPDMA2MAR")
    val GPDMA2TC = Register(ports.io,0x0005, BYTE, name = "GPDMA2TC")
    val GPDMA3MAR = Register(ports.io,0x0006, BYTE, name = "GPDMA3MAR")
    val GPDMA3TC = Register(ports.io,0x0007, BYTE, name = "GPDMA3TC")
    val SLDMASTA = Register(ports.io,0x0008, BYTE, name = "SLDMASTA")
    val SLDMACTL = Register(ports.io,0x0008, BYTE, name = "SLDMACTL")
    val SLDMASWREQ = Register(ports.io,0x0009, BYTE, name = "SLDMASWREQ")
    val SLDMAMSK = Register(ports.io,0x000A, BYTE, name = "SLDMAMSK")
    val SLDMAMODE = Register(ports.io,0x000B, BYTE, name = "SLDMAMODE")
    val SLDMACBP = Register(ports.io,0x000C, BYTE, name = "SLDMACBP")
    val SLDMARST = Register(ports.io,0x000D, BYTE, name = "SLDMARST")
    val SLDMATMP = Register(ports.io,0x000D, BYTE, name = "SLDMATMP")
    val SLDMAMSKRST = Register(ports.io,0x000E, BYTE, name = "SLDMAMSKRST", level = Level.FINEST)
    val SLDMAGENMSK = Register(ports.io,0x000F, BYTE, name = "SLDMAGENMSK")

    // DMA Page and General Registers
    val GPDMAGR0 = Register(ports.io,0x0080, BYTE, name = "GPDMAGR0", level = Level.FINEST)
    val GPDMA2PG = Register(ports.io,0x0081, BYTE, name = "GPDMA2PG")
    val GPDMA3PG = Register(ports.io,0x0082, BYTE, name = "GPDMA3PG")
    val GPDMA1PG = Register(ports.io,0x0083, BYTE, name = "GPDMA1PG")
    val GPDMAGR1 = Register(ports.io,0x0084, BYTE, name = "GPDMAGR1", level = Level.FINEST)
    val GPDMAGR2 = Register(ports.io,0x0085, BYTE, name = "GPDMAGR2")
    val GPDMAGR3 = Register(ports.io,0x0086, BYTE, name = "GPDMAGR3")
    val GPDMA0PG = Register(ports.io,0x0087, BYTE, name = "GPDMA0PG")
    val GPDMAGR4 = Register(ports.io,0x0088, BYTE, name = "GPDMAGR4")
    val GPDMA6PG = Register(ports.io,0x0089, BYTE, name = "GPDMA6PG")
    val GPDMA7PG = Register(ports.io,0x008A, BYTE, name = "GPDMA7PG")
    val GPDMA5PG = Register(ports.io,0x008B, BYTE, name = "GPDMA5PG")
    val GPDMAGR5 = Register(ports.io,0x008C, BYTE, name = "GPDMAGR5")
    val GPDMAGR6 = Register(ports.io,0x008D, BYTE, name = "GPDMAGR6")
    val GPDMAGR7 = Register(ports.io,0x008E, BYTE, name = "GPDMAGR7")
    val GPDMAGR8 = Register(ports.io,0x008F, BYTE, name = "GPDMAGR8")

    // Master DMA
    val GPDMA4MAR = Register(ports.io,0x00C0, BYTE, name = "GPDMA4MAR")
    val GPDMA4TC = Register(ports.io,0x00C2, BYTE, name = "GPDMA4TC")
    val GPDMA5MAR = Register(ports.io,0x00C4, BYTE, name = "GPDMA5MAR")
    val GPDMA5TC = Register(ports.io,0x00C6, BYTE, name = "GPDMA5TC")
    val GPDMA6MAR = Register(ports.io,0x00C8, BYTE, name = "GPDMA6MAR")
    val GPDMA6TC = Register(ports.io,0x00CA, BYTE, name = "GPDMA6TC")
    val GPDMA7MAR = Register(ports.io,0x00CC, BYTE, name = "GPDMA7MAR")
    val GPDMA7TC = Register(ports.io,0x00CE, BYTE, name = "GPDMA7TC")

    val MSTDMASTA_CTL = Register(ports.io,0x00D0, BYTE, name = "MSTDMASTA_CTL")
    val MSTDMASWREQ = Register(ports.io,0x00D2, BYTE, name = "MSTDMASWREQ")
    val MSTDMAMSK = Register(ports.io,0x00D4, BYTE, name = "MSTDMAMSK")
    val MSTDMAMODE = Register(ports.io,0x00D6, BYTE, name = "MSTDMAMODE")
    val MSTDMACBP = Register(ports.io,0x00D8, BYTE, name = "MSTDMACBP")
    val MSTDMARST_TMP = Register(ports.io,0x00DA, BYTE, name = "MSTDMARST")
    val MSTDMAMSKRST = Register(ports.io,0x00DC, BYTE, name = "MSTDMAMSKRST")
    val MSTDMAGENMSK = Register(ports.io,0x00DE, BYTE, name = "MSTDMAGENMSK")

    val GPDMACTL = Register(ports.mmcr, 0xD80, BYTE, "GPDMACTL")
    val GPDMGPDMAMMIOACTL = Register(ports.mmcr, 0xD81, BYTE, "GPDMAMMIO")
    val GPDMAEXTCHMAPA = Register(ports.mmcr, 0xD82, WORD, "GPDMAEXTCHMAPA")
    val GPDMAEXTCHMAPB = Register(ports.mmcr, 0xD84, WORD, "GPDMAEXTCHMAPB")
}