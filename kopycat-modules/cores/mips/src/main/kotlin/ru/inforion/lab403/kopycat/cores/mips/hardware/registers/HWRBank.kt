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
package ru.inforion.lab403.kopycat.cores.mips.hardware.registers

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBankNG
import ru.inforion.lab403.kopycat.cores.mips.Microarchitecture
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

class HWRBank(val core: MipsCore) : ARegistersBankNG<MipsCore>("Hardware MIPS Registers", 65536, 64) {

    inner class ReadOnly(
            name: String,
            id: Int,
            val output: (ReadOnly) -> ULong
    ) : Register(name, id) {
        override var value: ULong
            get() = output(this)
            set(value) = throw NotImplementedError("Write to $this not implemented!")
    }

    /**
     * Number of the CPU on which the program is currently running. This register
     * provides read access to the coprocessor 0 EBaseCPUNum field.
     */
    val hw0 = ReadOnly("hw0", 0) { core.cop.regs.EBase.value[9..0] }

    /**
     * Address step size to be used with the SYNCI instruction,
     * or zero if no caches need be synchronized.
     * See that instruction’s description for the use of this value.
     */
    val hw1 = ReadOnly("hw1", 1) { 0u }

    /**
     * High-resolution cycle counter. This register provides read access to the coprocessor 0 Count Register.
     */
    val hw2 = ReadOnly("hw2", 2) { core.cop.regs.Count.value }

    /**
     * 1 CC register increments every CPU cycle
     * 2 CC register increments every second CPU cycle
     * 3 CC register increments every third CPU cycle
     */
    val hw3 = ReadOnly("hw3", 3) { 1u }

    /**
     * User Local Register. This register provides read access to the coprocessor 0
     * UserLocal register, if it is implemented. In some operating environments, the
     * UserLocal register is a pointer to a thread-specific storage block.
     */
    val hw29 = ReadOnly("hw29", 29) { core.cop.regs.UserLocal.value }

    val cvmChOrd: Register? = if (core.microarchitecture == Microarchitecture.cnMips) {
        object : Register("ChOrd", 30) {
            override var value: ULong
                get() = 1uL
                set(_) { }
        }
    } else {
        null
    }

    val cvmCount = if (core.microarchitecture == Microarchitecture.cnMips) {
        ReadOnly("CvmCount", 31) { core.cop.regs.Count.value }
    } else {
        null
    }

    val cvmTripleDesKey = if (core.microarchitecture == Microarchitecture.cnMips) {
        Array(3) {
            Register("CVM_MF_3DES_KEY$it", 0x0080 + it)
        }
    } else {
        null
    }

    val cvmTripleDesIV = if (core.microarchitecture == Microarchitecture.cnMips) {
        Register("CVM_MT_3DES_IV", 0x0084)
    } else {
        null
    }

    val cvmTripleDesResult = if (core.microarchitecture == Microarchitecture.cnMips) {
        Register("CVM_MF_3DES_RESULT", 0x0088)
    } else {
        null
    }

    val cvmTripleDesEncCBC = if (core.microarchitecture == Microarchitecture.cnMips) {
        // Logic is not implemented
        Register("CVM_MT_3DES_ENC_CBC", 0x4088)
    } else {
        null
    }

    val cvmTripleDesDecCBC = if (core.microarchitecture == Microarchitecture.cnMips) {
        // Logic is not implemented
        Register("CVM_MT_3DES_DEC_CBC", 0x408C)
    } else {
        null
    }

    val cvmCrcPoly = if (core.microarchitecture == Microarchitecture.cnMips) {
        Register("CVM_MT_CRC_POLYNOMIAL", 0x4200)
    } else {
        null
    }

    val cvmCrcIV = if (core.microarchitecture == Microarchitecture.cnMips) {
        Register("CVM_MT_CRC_IV", 0x0201)
    } else {
        null
    }

    val cvmCrcDword = if (core.microarchitecture == Microarchitecture.cnMips) {
        Register("CVM_MT_CRC_DWORD", 0x1207)
    } else {
        null
    }
}
