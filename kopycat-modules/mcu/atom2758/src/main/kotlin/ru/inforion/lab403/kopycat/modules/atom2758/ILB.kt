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
package ru.inforion.lab403.kopycat.modules.atom2758

import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import java.util.logging.Level.CONFIG

class ILB(parent: Module, name: String) : Module(parent, name) {
    companion object {
        const val BUS_SIZE = 512
        const val BUS_INDEX = 4
    }

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", BUS_SIZE)
    }

    override val ports = Ports()

    private val ACTL = ByteAccessRegister(ports.mem, 0x00u, DWORD, "ACTL", 0u, level = CONFIG)
    private val MC = ByteAccessRegister(ports.mem, 0x04u, DWORD, "MC", 0u, level = CONFIG)

    private val PIRQA = Register(ports.mem, 0x08u, BYTE, "PIRQA", 0u, level = CONFIG)
    private val PIRQB = Register(ports.mem, 0x09u, BYTE, "PIRQB", 0u, level = CONFIG)
    private val PIRQC = Register(ports.mem, 0x0Au, BYTE, "PIRQC", 0u, level = CONFIG)
    private val PIRQD = Register(ports.mem, 0x0Bu, BYTE, "PIRQD", 0u, level = CONFIG)
    private val PIRQE = Register(ports.mem, 0x0Cu, BYTE, "PIRQE", 0u, level = CONFIG)
    private val PIRQF = Register(ports.mem, 0x0Du, BYTE, "PIRQF", 0u, level = CONFIG)
    private val PIRQG = Register(ports.mem, 0x0Eu, BYTE, "PIRQG", 0u, level = CONFIG)
    private val PIRQH = Register(ports.mem, 0x0Fu, BYTE, "PIRQH", 0u, level = CONFIG)

    private val SCNT = ByteAccessRegister(ports.mem, 0x10u, DWORD, "SCNT", 0u, level = CONFIG)
    private val KMC = ByteAccessRegister(ports.mem, 0x14u, DWORD, "KMC", 0u, level = CONFIG)
    private val FS = ByteAccessRegister(ports.mem, 0x18u, DWORD, "FS", 0u, level = CONFIG)
    private val BC = ByteAccessRegister(ports.mem, 0x1Cu, DWORD, "BC", 0u, level = CONFIG)


    private val IR = Array(32) {
        ByteAccessRegister(ports.mem, 0x20u + 2u * it.ulong_z, WORD, "IR${it}", 0u, level = CONFIG)
    }

    private val OIC = ByteAccessRegister(ports.mem, 0x60u, WORD, "OIC", 0u, level = CONFIG)
    private val RC = ByteAccessRegister(ports.mem, 0x64u, DWORD, "RC", 0u, level = CONFIG)
    private val RTM = ByteAccessRegister(ports.mem, 0x68u, DWORD, "RTM", 0u, level = CONFIG)
    private val BCS = ByteAccessRegister(ports.mem, 0x6Cu, DWORD, "BCS", 0u, level = CONFIG)
    private val LE = ByteAccessRegister(ports.mem, 0x70u, DWORD, "LE", 0u, level = CONFIG)

    private val GNMI = ByteAccessRegister(ports.mem, 0x80u, DWORD, "GNMI", 0u, level = CONFIG)
    private val LPCC = ByteAccessRegister(ports.mem, 0x84u, DWORD, "LPCC", 0u, level = CONFIG)
    private val IRQE = ByteAccessRegister(ports.mem, 0x88u, DWORD, "IRQE", 0u, level = CONFIG)
}