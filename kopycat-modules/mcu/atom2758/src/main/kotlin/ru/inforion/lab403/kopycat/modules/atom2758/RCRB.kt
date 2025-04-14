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
@file:Suppress("unused")

package ru.inforion.lab403.kopycat.modules.atom2758

import ru.inforion.lab403.common.logging.CONFIG
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS.*
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*

class RCRB(parent: Module, name: String) : Module(parent, name) {
    companion object {
        const val BUS_SIZE = 0x4000
        const val BUS_INDEX = 8
    }

    inner class Ports : ModulePorts(this) {
        val mem = Port("mem")
    }

    override val ports = Ports()

    private fun REG(address: ULong, datatype: Datatype, name: String, default: ULong = 0u) =
        ByteAccessRegister(ports.mem, address, datatype, name, default, level = CONFIG)

    private fun TABLE(address: ULong, end: ULong, name: String) =
        Memory(ports.mem, address, end, name, R_W)

    val VCH = REG(0x0000u, DWORD, "VCH", 0x10010002u)
    val VCAP1 = REG(0x0004u, DWORD, "VCAP1", 0x00000801u)
    val VCAP2 = REG(0x0008u, DWORD, "VCAP2", 0x00000001u)
    val PVC = REG(0x000Cu, WORD, "PVC")
    val PVS = REG(0x000Eu, WORD, "PVS")
    val V0CAP = REG(0x0010u, DWORD, "V0CAP", 0x00000001u)
    val V0CTL = REG(0x0014u, DWORD, "V0CTL", 0x800000FFu)
    val V0STS = REG(0x001Au, WORD, "V0STS")
    val V1CAP = REG(0x001Cu, DWORD, "V1CAP", 0x03008011u)
    val V1CTL = REG(0x0020u, DWORD, "V1CTL")
    val V1STS = REG(0x0026u, WORD, "V1STS")
    val PAT = TABLE(0x0030u, 0x006Fu, "PAT")
    val CIR1 = REG(0x0088u, DWORD, "CIR1")
    val REC = REG(0x00ACu, WORD, "REC")
    val RCTCL = REG(0x0100u, DWORD, "RCTCL", 0x1A010005u)
    val ESD = REG(0x0104u, DWORD, "ESD", 0x00000802u)
    val ULD = REG(0x0108u, DWORD, "ULD", 0x00000001u)
    val ULBA = REG(0x0118u, QWORD, "ULBA")
    val RP1D = REG(0x0120u, DWORD, "RP1D", 0x01cc0002u)
    val RP1BA = REG(0x0128u, QWORD, "RP1BA", 0x00000000000E0000u)
    val RP2D = REG(0x0130u, DWORD, "RP2D", 0x02cc0002u)
    val RP2BA = REG(0x0138u, QWORD, "RP2BA", 0x00000000000E1000u)
    val RP3D = REG(0x00140u, DWORD, "RP3D", 0x03cc0002u)
    val RP3BA = REG(0x0148u, QWORD, "RP3BA", 0x00000000000E2000u)
    val RP4D = REG(0x0150u, DWORD, "RP4D", 0x04cc0002u)
    val RP4BA = REG(0x0158u, QWORD, "RP4BA", 0x00000000000E3000u)
    val HDD = REG(0x0160u, DWORD, "HDD", 0x15cc0002u)
    val HDBA = REG(0x0168u, QWORD, "HDBA", 0x00000000000D8000u)
    val RP5D = REG(0x0170u, DWORD, "RP5D", 0x05cc0002u)
    val RP5A = REG(0x0178u, QWORD, "RP5A", 0x00000000000E4000u)
    val RP6D = REG(0x0180u, DWORD, "RP6D", 0x06cc0002u)
    val RP6BA = REG(0x0188u, QWORD, "RP6BA", 0x00000000000E5000u)
    val ILCL = REG(0x01A0u, DWORD, "ILCL", 0x00010006u)
    val LCAP = REG(0x01A4u, DWORD, "LCAP", 0x00012841u)
    val LCTL = REG(0x01A8u, WORD, "LCTL", 0x0000u)
    val LSTS = REG(0x01AAu, WORD, "LSTS", 0x0041u)
    val CIR2 = REG(0x01F4u, DWORD, "CIR2", 0x00000000u)
    val CIR3 = REG(0x01FCu, WORD, "CIR3", 0x0000u)

    val REG_200 = REG(0x0200u, DWORD, "REG_200", 0x00000000u)
    val REG_204 = REG(0x0204u, DWORD, "REG_204", 0x00000000u)
    val REG_208 = REG(0x0208u, DWORD, "REG_208", 0x00000000u)

    val RPC = REG(0x0224u, DWORD, "RPC", 0x0000000du)
    val DMIC = REG(0x0234u, DWORD, "DMIC", 0x00000000u)
    val RPFN = REG(0x0238u, DWORD, "RPFN", 0x00543210u)

    val REG_284 = REG(0x0284u, DWORD, "REG_284", 0x00000000u)

    val FPSS = REG(0x0290u, DWORD, "FPSS", 0x00000000u)

    val REG_318 = REG(0x0318u, DWORD, "REG_318", 0x00000000u)
    val REG_31C = REG(0x031Cu, DWORD, "REG_31C", 0x00000000u)

    val CIR13 = REG(0x0F20u, DWORD, "CIR13", 0xB2B477CCu)
    val CIR5 = REG(0x1D40u, QWORD, "CIR5", 0x0000000000000000u)
    val TRSR = REG(0x1E00u, DWORD, "TRSR", 0x00000000u)
    val TRCR = REG(0x1E10u, QWORD, "TRCR", 0x0000000000000000u)
    val TWDR = REG(0x1E18u, QWORD, "TWDR", 0x0000000000000000u)
    val IOTR0 = REG(0x1E80u, QWORD, "IOTR0", 0x0000000000000000u)
    val IOTR1 = REG(0x1E88u, QWORD, "IOTR1", 0x0000000000000000u)
    val IOTR2 = REG(0x1E90u, QWORD, "IOTR2", 0x0000000000000000u)
    val IOTR3 = REG(0x1E98u, QWORD, "IOTR3", 0x0000000000000000u)
    val DMC = REG(0x2010u, DWORD, "DMC", 0x00000002u)
    val CIR6 = REG(0x2024u, DWORD, "CIR6", 0x0B4030C0u)
    val CIR7 = REG(0x2034u, DWORD, "CIR7", 0xB2B477CCu)
    val CIR11 = REG(0x20C4u, WORD, "CIR11", 0x0000u)
    val CIR12 = REG(0x20E4u, WORD, "CIR12", 0x0000u)
    val TCTL = REG(0x3000u, BYTE, "TCTL", 0x00u)
    val D31IP = REG(0x3100u, DWORD, "D31IP", 0x03243200u)
    val D30IP = REG(0x3104u, DWORD, "D30IP", 0x00000000u)
    val D29IP = REG(0x3108u, DWORD, "D29IP", 0x10004321u)
    val D28IP = REG(0x310Cu, DWORD, "D28IP", 0x00214321u)
    val D27IP = REG(0x3110u, DWORD, "D27IP", 0x00000001u)
    val D26IP = REG(0x3114u, DWORD, "D26IP", 0x30000321u)
    val D25IP = REG(0x3118u, DWORD, "D25IP", 0x00000001u)
    val D31IR = REG(0x3140u, WORD, "D31IR", 0x3210u)
    val D30IR = REG(0x3142u, WORD, "D30IR", 0x0000u)
    val D29IR = REG(0x3144u, WORD, "D29IR", 0x3210u)
    val D28IR = REG(0x3146u, WORD, "D28IR", 0x3210u)
    val D27IR = REG(0x3148u, WORD, "D27IR", 0x3210u)
    val D26IR = REG(0x314Cu, WORD, "D26IR", 0x3210u)
    val D25IR = REG(0x3150u, WORD, "D25IR", 0x3210u)
    val OIC = REG(0x31FFu, BYTE, "OIC", 0x00u)
    val SBEMC3 = REG(0x3300u, DWORD, "SBEMC3", 0x00000000u)
    val SBEMC4 = REG(0x3304u, DWORD, "SBEMC4", 0x00000000u)
    val RC = REG(0x3400u, DWORD, "RC", 0x00000000u)
    val HPTC = REG(0x3404u, DWORD, "HPTC", 0x00000000u)
    val GCS = REG(0x3410u, DWORD, "GCS", 0x000000dd0u)
    val BUC = REG(0x3414u, BYTE, "BUC", 0x00u)
    val FD = REG(0x3418u, DWORD, "FD", 0x00000000u)
    val CG = REG(0x341Cu, DWORD, "CG", 0x00000000u)
    val PDSW = REG(0x3420u, BYTE, "PDSW", 0x00u)
    val CIR8 = REG(0x3430u, DWORD, "CIR8", 0x00000000u)
    val CIR9 = REG(0x350Cu, DWORD, "CIR9", 0x00000000u)
    val PPO = REG(0x3524u, WORD, "PPO", 0x0000u)
    val CIR10 = REG(0x352Cu, DWORD, "CIR10", 0x0008C008u)
    val MAP = REG(0x35F0u, DWORD, "MAP", 0x00000000u)

    val SPI = TABLE(0x3800u, 0x3FFFu, "SPI")
}