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
package ru.inforion.lab403.kopycat.cores.arm.hardware.registers

import ru.inforion.lab403.kopycat.cores.arm.enums.StackPointer
import ru.inforion.lab403.kopycat.cores.arm.hardware.processors.AARMCPU
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBankNG
import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import ru.inforion.lab403.kopycat.serializer.loadValue
import ru.inforion.lab403.kopycat.serializer.storeValues


class GPRBank(val cpu: AARMCPU) : ARegistersBankNG<AARMCore>(
        "General purpose registers", 16, 32) {

    val r0 = Register("r0", 0)
    val r1 = Register("r1", 1)
    val r2 = Register("r2", 2)
    val r3 = Register("r3", 3)
    val r4 = Register("r4", 4)
    val r5 = Register("r5", 5)
    val r6 = Register("r6", 6)
    val r7 = Register("r7", 7)
    val r8 = Register("r8", 8)
    val r9 = Register("r9", 9)
    val r10 = Register("r10", 10)
    val r11 = Register("r11", 11)
    val r12 = Register("r12", 12)

    inner class SP : Register("sp", 13) {
        internal var main = 0L
        internal var process = 0L

        override var value: Long
            get() = when (cpu.StackPointerSelect()) {
                StackPointer.Process -> process and mask
                StackPointer.Main -> main and mask
            }

            set(value) {
                when (cpu.StackPointerSelect()) {
                    StackPointer.Process -> process = value and mask
                    StackPointer.Main -> main = value and mask
                }
            }

        override fun reset() {
            main = 0
            process = 0
        }

        override fun serialize(ctxt: GenericSerializer) = storeValues("main" to main, "process" to process)

        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            main = loadValue(snapshot, "main")
            process = loadValue(snapshot, "process")
        }
    }

    val sp = SP()

    val lr = Register("lr", 14)
    val pc = Register("pc", 15)
}