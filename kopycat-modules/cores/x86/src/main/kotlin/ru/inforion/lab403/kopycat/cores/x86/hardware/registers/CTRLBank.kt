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
package ru.inforion.lab403.kopycat.cores.x86.hardware.registers

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.cores.x86.enums.CR0
import ru.inforion.lab403.kopycat.cores.x86.enums.CR4
import ru.inforion.lab403.kopycat.cores.x86.enums.CTRLR
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class CTRLBank(core: x86Core) : ARegistersBank<x86Core, CTRLR>(core, CTRLR.values(), bits = 32) {
    override val name: String = "Control Registers"

    var cr0 by valueOf(x86Register.CTRLR.cr0)
    var cr1 by valueOf(x86Register.CTRLR.cr1)
    var cr2 by valueOf(x86Register.CTRLR.cr2)
    var cr3 by valueOf(x86Register.CTRLR.cr3)
    var cr4 by valueOf(x86Register.CTRLR.cr4)

    var vpe by bitOf(x86Register.CTRLR.cr0, CR0.PE.bit)
    var vmp by bitOf(x86Register.CTRLR.cr0, CR0.MP.bit)
    var vem by bitOf(x86Register.CTRLR.cr0, CR0.EM.bit)
    var vts by bitOf(x86Register.CTRLR.cr0, CR0.TS.bit)
    var vet by bitOf(x86Register.CTRLR.cr0, CR0.ET.bit)
    var vne by bitOf(x86Register.CTRLR.cr0, CR0.NE.bit)
    var vwp by bitOf(x86Register.CTRLR.cr0, CR0.WP.bit)
    var vam by bitOf(x86Register.CTRLR.cr0, CR0.AM.bit)
    var vnw by bitOf(x86Register.CTRLR.cr0, CR0.NW.bit)
    var vcd by bitOf(x86Register.CTRLR.cr0, CR0.CD.bit)
    var vpg by bitOf(x86Register.CTRLR.cr0, CR0.PG.bit)

    var vpme by bitOf(x86Register.CTRLR.cr4, CR4.VME.bit)
    var vpvi by bitOf(x86Register.CTRLR.cr4, CR4.PVI.bit)
    var vtsd by bitOf(x86Register.CTRLR.cr4, CR4.TSD.bit)
    var vde by bitOf(x86Register.CTRLR.cr4, CR4.DE.bit)
    var vpse by bitOf(x86Register.CTRLR.cr4, CR4.PSE.bit)
    var vpae by bitOf(x86Register.CTRLR.cr4, CR4.PAE.bit)
    var vmce by bitOf(x86Register.CTRLR.cr4, CR4.MCE.bit)
    var vpge by bitOf(x86Register.CTRLR.cr4, CR4.PGE.bit)
    var vpce by bitOf(x86Register.CTRLR.cr4, CR4.PCE.bit)
    var vosfxsr by bitOf(x86Register.CTRLR.cr4, CR4.OSFXSR.bit)
    var vosxmmexcpt by bitOf(x86Register.CTRLR.cr4, CR4.OSXMMEXCPT.bit)
    var vvmxe by bitOf(x86Register.CTRLR.cr4, CR4.VMXE.bit)
    var vsmxe by bitOf(x86Register.CTRLR.cr4, CR4.SMXE.bit)
    var vfsgsbase by bitOf(x86Register.CTRLR.cr4, CR4.FSGSBASE.bit)
    var vpcide by bitOf(x86Register.CTRLR.cr4, CR4.PCIDE.bit)
    var vosxsave by bitOf(x86Register.CTRLR.cr4, CR4.OSXSAVE.bit)
    var vsmep by bitOf(x86Register.CTRLR.cr4, CR4.SMEP.bit)
    var vsmap by bitOf(x86Register.CTRLR.cr4, CR4.SMAP.bit)
    var vpke by bitOf(x86Register.CTRLR.cr4, CR4.PKE.bit)
}