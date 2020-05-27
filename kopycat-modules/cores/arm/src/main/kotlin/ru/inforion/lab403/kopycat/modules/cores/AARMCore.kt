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
package ru.inforion.lab403.kopycat.modules.cores

import ru.inforion.lab403.kopycat.cores.arm.ARMABI
import ru.inforion.lab403.kopycat.cores.arm.hardware.processors.AARMCOP
import ru.inforion.lab403.kopycat.cores.arm.hardware.processors.AARMCPU
import ru.inforion.lab403.kopycat.cores.arm.hardware.registers.GPRBank
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.abstracts.ABI
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACore
import ru.inforion.lab403.kopycat.cores.base.common.AddressTranslator
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts



abstract class AARMCore(parent: Module, name: String, frequency: Long, val version: Int, ipc: Double):
        ACore<AARMCore, AARMCPU, AARMCOP>(parent, name, frequency, ipc) {

    enum class InstructionSet(val code: Int) {
        CURRENT(-1),
        ARM(0b00),
        THUMB(0b01),
        JAZELLE(0b10),
        THUMB_EE(0b11);

        companion object {
            fun from(code: Int): InstructionSet = values().first { it.code == code }
        }
    }

    override val fpu = null
    override val mmu: AddressTranslator? = null

    override fun abi(heap: LongRange, stack: LongRange): ABI<AARMCore> = ARMABI(this, heap, stack, false)

//    inner class Ports : ModulePorts(this) {
//        val mem = Proxy("mem")
//    }
//
//    inner class Buses: ModuleBuses(this) {
//        val mem = Bus("mem")
//    }
//
//    override val ports = Ports()
//    override val buses = Buses()
}