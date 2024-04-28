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
package ru.inforion.lab403.kopycat.cores.mips.config

import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.field
import ru.inforion.lab403.kopycat.interfaces.IValuable

data class Config3(override var data: ULong = 0uL) : IValuable {
    /** This bit is reserved to indicate that a Config4 register is present */
    var M by bit(31)

    /** Big Pages feature is implemented */
    var BPG by bit(30)

    /** Coherency Manager memory-mapped Global Configuration Register Space is implemented */
    var CMGCR by bit(29)

    /** MIPS SIMD Architecture (MSA) is implemented */
    var MSAP by bit(28)

    /**
     * BadInstrP register implemented. This bit indicates whether
     * the faulting prior branch instruction word register is present.
     * Release 6: BadInstrP is always implemented.
     */
    var BP by bit(27)

    /**
     * BadInstr register implemented. This bit indicates whether
     * the faulting instruction word register is present.
     * Release 6: BadInstr is always implemented.
     */
    var BI by bit(26)

    /**
     * Segment Control implemented. This bit indicates
     * whether the Segment Control registers SegCtl0,
     * SegCtl1 and SegCtl2 are present.
     */
    var SC by bit(25)

    /** HardWare Page Table Walk implemented */
    var PW by bit(24)

    /** Virtualization Module implemented */
    var VZ by bit(23)

    /** Width of Status_IPL and Cause_RIPL fields */
    var IPLW by field(22..21)

    /** microMIPS64 Architecture revision level */
    var MMAR by field(20..18)

    /** MIPS MCU ASE is implemented */
    var MCU by bit(17)

    /** Reflects the Instruction Set Architecture used after vectoring to an exception */
    var ISAOnExc by bit(16)

    /** Indicates Instruction Set Availability */
    var ISA by field(15..14)

    /**
     * Pre-Release 6: UserLocal register implemented. This
     * bit indicates whether the UserLocal Coprocessor 0 register is implemented.
     * Release 6: UserLocal is always implemented.
     */
    var ULRI by bit(13)

    /**
     * Pre-Release 6: Indicates whether the RIE and XIE bits
     * exist within the PageGrain register.
     * Release 6: The RIE and XIE bits are always implemented.
     */
    var RXI by bit(12)

    /** MIPS DSP Module Revision 2 implemented */
    var DSP2P by bit(11)

    /** MIPS DSP Module implemented */
    var DSPP by bit(10)

    /**
     * ContextConfig and XContextConfig registers are
     * implemented and the width of the BadVPN2 field within
     * the Config register and the XConfig register depends on
     * the contents of the ContextConfig register and
     * XContextConfig register respectively.
     */
    var CTXTC by bit(9)

    /** MIPS® IFlowtrace mechanism implemented */
    var ITL by bit(8)

    /** Large Physical Address support is implemented, and the PageGrain register exists */
    var LPA by bit(7)

    /** Support for an external interrupt controller is implemented */
    var VEIC by bit(6)

    /** Vectored interrupts implemented */
    var VInt by bit(5)

    /** Small (1 kB) page support is implemented, and the PageGrain register exists */
    var SP by bit(4)

    /** Common Device Memory Map implemented */
    var CDMM by bit(3)

    /** MIPS MT Module implemented */
    var MT by bit(2)

    /** SmartMIPS ASE implemented */
    var SM by bit(1)

    /** Trace Logic implemented */
    var TL by bit(0)
}
