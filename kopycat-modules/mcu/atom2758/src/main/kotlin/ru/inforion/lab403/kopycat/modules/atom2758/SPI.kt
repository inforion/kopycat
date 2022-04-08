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
@file:Suppress("unused")

package ru.inforion.lab403.kopycat.modules.atom2758

import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import java.util.logging.Level.CONFIG

class SPI(parent: Module, name: String) : Module(parent, name) {

    companion object {
        const val BUS_SIZE = 512
        const val BUS_INDEX = 5
    }

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", BUS_SIZE)
    }

    override val ports = Ports()

    private val BFPREG = ByteAccessRegister(ports.mem, 0x00u, DWORD, "BFPREG", level = CONFIG)
    private val HSFSTS_HSFCTL = ByteAccessRegister(ports.mem, 0x04u, DWORD, "HSFSTS_HSFCTL", level = CONFIG)
    private val FADDR = ByteAccessRegister(ports.mem, 0x08u, DWORD, "FADDR", level = CONFIG)

    private val FDATA0 = ByteAccessRegister(ports.mem, 0x10u, DWORD, "FDATA0", level = CONFIG)
    private val FDATA1 = ByteAccessRegister(ports.mem, 0x14u, DWORD, "FDATA1", level = CONFIG)
    private val FDATA2 = ByteAccessRegister(ports.mem, 0x18u, DWORD, "FDATA2", level = CONFIG)
    private val FDATA3 = ByteAccessRegister(ports.mem, 0x1Cu, DWORD, "FDATA3", level = CONFIG)
    private val FDATA4 = ByteAccessRegister(ports.mem, 0x20u, DWORD, "FDATA4", level = CONFIG)
    private val FDATA5 = ByteAccessRegister(ports.mem, 0x24u, DWORD, "FDATA5", level = CONFIG)
    private val FDATA6 = ByteAccessRegister(ports.mem, 0x28u, DWORD, "FDATA6", level = CONFIG)
    private val FDATA7 = ByteAccessRegister(ports.mem, 0x2Cu, DWORD, "FDATA7", level = CONFIG)
    private val FDATA8 = ByteAccessRegister(ports.mem, 0x30u, DWORD, "FDATA8", level = CONFIG)
    private val FDATA9 = ByteAccessRegister(ports.mem, 0x34u, DWORD, "FDATA9", level = CONFIG)
    private val FDATA10 = ByteAccessRegister(ports.mem, 0x38u, DWORD, "FDATA10", level = CONFIG)
    private val FDATA11 = ByteAccessRegister(ports.mem, 0x3Cu, DWORD, "FDATA11", level = CONFIG)
    private val FDATA12 = ByteAccessRegister(ports.mem, 0x40u, DWORD, "FDATA12", level = CONFIG)
    private val FDATA13 = ByteAccessRegister(ports.mem, 0x44u, DWORD, "FDATA13", level = CONFIG)
    private val FDATA14 = ByteAccessRegister(ports.mem, 0x48u, DWORD, "FDATA14", level = CONFIG)
    private val FDATA15 = ByteAccessRegister(ports.mem, 0x4Cu, DWORD, "FDATA15", level = CONFIG)

    private val FRACC = ByteAccessRegister(ports.mem, 0x50u, DWORD, "FRACC", level = CONFIG)
    private val FREG0 = ByteAccessRegister(ports.mem, 0x54u, DWORD, "FREG0", level = CONFIG)
    private val FREG1 = ByteAccessRegister(ports.mem, 0x58u, DWORD, "FREG1", level = CONFIG)
    private val FREG2 = ByteAccessRegister(ports.mem, 0x5Cu, DWORD, "FREG2", level = CONFIG)
    private val FREG3 = ByteAccessRegister(ports.mem, 0x60u, DWORD, "FREG3", level = CONFIG)
    private val FREG4 = ByteAccessRegister(ports.mem, 0x64u, DWORD, "FREG4", level = CONFIG)

    private val PR0 = ByteAccessRegister(ports.mem, 0x74u, DWORD, "PR0", level = CONFIG)
    private val PR1 = ByteAccessRegister(ports.mem, 0x78u, DWORD, "PR1", level = CONFIG)
    private val PR2 = ByteAccessRegister(ports.mem, 0x7Cu, DWORD, "PR2", level = CONFIG)
    private val PR3 = ByteAccessRegister(ports.mem, 0x80u, DWORD, "PR3", level = CONFIG)
    private val PR4 = ByteAccessRegister(ports.mem, 0x84u, DWORD, "PR4", level = CONFIG)

    private val SSFSTS_SSFCTL = ByteAccessRegister(ports.mem, 0x90u, DWORD, "SSFCTLSTS", level = CONFIG)
    private val PREOP_OPTYPE = ByteAccessRegister(ports.mem, 0x94u, DWORD, "PREOP_OPTYPE", level = CONFIG)

    private val OPMENU0 = ByteAccessRegister(ports.mem, 0x98u, DWORD, "OPMENU0", level = CONFIG)
    private val OPMENU1 = ByteAccessRegister(ports.mem, 0x9Cu, DWORD, "OPMENU1", level = CONFIG)

    private val FDOC = ByteAccessRegister(ports.mem, 0xB0u, DWORD, "FDOC", level = CONFIG)
    private val FDOD = ByteAccessRegister(ports.mem, 0xB4u, DWORD, "FDOD", level = CONFIG)

    private val AFC = ByteAccessRegister(ports.mem, 0xC0u, DWORD, "AFC", level = CONFIG)
    private val LVSCC = ByteAccessRegister(ports.mem, 0xC4u, DWORD, "LVSCC", level = CONFIG)
    private val UVSCC = ByteAccessRegister(ports.mem, 0xC8u, DWORD, "UVSCC", level = CONFIG)
    private val FPB = ByteAccessRegister(ports.mem, 0xD0u, DWORD, "FPB", level = CONFIG)
    private val SCS = ByteAccessRegister(ports.mem, 0xF8u, DWORD, "SCS", level = CONFIG)
    private val BCR = ByteAccessRegister(ports.mem, 0xFCu, DWORD, "BCR", level = CONFIG)
    private val TCGC = ByteAccessRegister(ports.mem, 0x100u, DWORD, "TCGC", level = CONFIG)
}