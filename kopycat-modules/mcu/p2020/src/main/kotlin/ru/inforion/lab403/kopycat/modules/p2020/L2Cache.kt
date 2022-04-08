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
@file:Suppress("MemberVisibilityCanBePrivate", "PropertyName", "unused")

package ru.inforion.lab403.kopycat.modules.p2020

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.AddressTranslator
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.field
import ru.inforion.lab403.kopycat.modules.BUS32



class L2Cache(parent: Module, name: String) : Module(parent, name) {

    inner class Ports : ModulePorts(this) {
        val inp get() = filter.ports.inp
        val outp  get() = filter.ports.outp
        val ctrl = Slave("ctrl", BUS32)
    }

    override val ports = Ports()

    fun l2ExternalWriteAddress(n: Int): ULong {
        val low = when(n) {
            0 -> L2_Cache_L2CEWAR0.ADDR
            1 -> L2_Cache_L2CEWAR1.ADDR
            2 -> L2_Cache_L2CEWAR2.ADDR
            3 -> L2_Cache_L2CEWAR3.ADDR
            else -> throw GeneralException("Wrong n: $n")
        }

        val high = when(n) {
            0 -> L2_Cache_L2CEWAREA0.ADDR
            1 -> L2_Cache_L2CEWAREA1.ADDR
            2 -> L2_Cache_L2CEWAREA2.ADDR
            3 -> L2_Cache_L2CEWAREA3.ADDR
            else -> throw GeneralException("Wrong n: $n")
        }

        return (high shl 24) or low
    }


    fun l2MemoryMappedSRAMAddress(n: Int): ULong {
        val low = when(n) {
            0 -> L2_Cache_L2SRBAR0.ADDR
            1 -> L2_Cache_L2SRBAR1.ADDR
            else -> throw GeneralException("Wrong n: $n")
        }

        val high = when(n) {
            0 -> L2_Cache_L2SRBAREA0.ADDR
            1 -> L2_Cache_L2SRBAREA1.ADDR
            else -> throw GeneralException("Wrong n: $n")
        }

        return ((high shl 18) or low) shl 14
    }

    val l2SRAMSize get() = when (L2_Cache_L2CTL.L2SRAM.int) {
        0b000 -> 0
        0b001 -> 1024
        0b010 -> 512
        0b011 -> 512
        0b100 -> 256
        0b101 -> 256
        0b110 -> 128
        0b111 -> 128
        else -> throw GeneralException("Wrong L2SRAM: ${L2_Cache_L2CTL.L2SRAM}")
    } * 1024 // KB

    val l2SRAMCount get() = when(L2_Cache_L2CTL.L2SRAM.int) {
        0b000 -> 0
        0b001 -> 1
        0b010 -> 1
        0b011 -> 2
        0b100 -> 1
        0b101 -> 2
        0b110 -> 1
        0b111 -> 2
        else -> throw GeneralException("Wrong L2SRAM: ${L2_Cache_L2CTL.L2SRAM}")
    }
    val l2SRAMRange get() = 0 until l2SRAMCount

    fun l2MemoryMappedSRAMRange(n: Int): ULongRange {
        val address = l2MemoryMappedSRAMAddress(n)
        return address until (address + l2SRAMSize.uint)
    }


    open inner class L2CacheRegister(
            offset: ULong,
            name: String,
            default: ULong = 0u,
            writable: Boolean = true,
            readable: Boolean = true
    ) : Register(ports.ctrl, 0x2_0000u + offset, Datatype.DWORD, name, default, writable, readable)

    inner class L2_Cache_L2CEWARn(val n: Int) : L2CacheRegister(0x10uL + 16 * n,"L2_Cache_L2CEWAR$n") {

        var ADDR by field(31..8)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            log.severe { "$name: L2C base address: ${l2ExternalWriteAddress(n).hex8}" }
        }

    }

    inner class L2_Cache_L2CEWAREAn(val n: Int) : L2CacheRegister(0x14uL + 16 * n, "L2_Cache_L2CEWAR$n") {

        var ADDR by field(3..0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            log.severe { "$name: L2C base address: ${l2ExternalWriteAddress(n).hex8}" }
        }

    }

    inner class L2_Cache_L2CEWCRn(val n: Int) : L2CacheRegister(0x18uL + 16 * n, "L2_Cache_L2CEWCR$n") {
        var E by bit(31)
        var LOCK by bit(30)
        var SIZMASK by field(27..0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            log.severe { "$name: L2C E=$E. LOCK=$LOCK, mask size: ${SIZMASK.hex8}" }
        }

    }

    inner class L2_Cache_L2SRBARn(val n: Int) : L2CacheRegister(0x100uL + 8 * n, "L2_Cache_L2SRBAR$n") {
        val ADDR by field(31..14)
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            log.severe { "$name: L2C SDRAM ADDRESS = ${l2MemoryMappedSRAMAddress(n).hex8}" }
        }
    }

    inner class L2_Cache_L2SRBAREAn(val n: Int) : L2CacheRegister(0x104uL + 8 * n, "L2_Cache_L2SRBAREA$n") {
        val ADDR by field(3..0)
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            log.severe { "$name: L2C SDRAM ADDRESS = ${l2MemoryMappedSRAMAddress(n).hex8}" }
        }
    }


    inner class L2CTL : L2CacheRegister(0x0u, "L2_Cache_L2CTL", 0x3000_0000u) {

        var L2E by bit(31)
        var L2I  by bit(30)
        var L2SRAM by field(18..16)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, (value and 0xBFFF_FFFFu) or 0x3000_0000u)
            log.warning { "Write to control"}
            log.severe { "$name: L2E=$L2E" }
            log.severe { "$name: L2I=$L2I" }
            log.severe { "$name: L2SRAM=$L2SRAM" }
        }
    }


    //TODO: NOT FULLY IMPLEMENTED
    val L2_Cache_L2CTL = L2CTL()
    val L2_Cache_L2CWAP = L2CacheRegister(0x04u, "L2_Cache_L2CWAP")
    val L2_Cache_L2CEWAR0 = L2_Cache_L2CEWARn(0)
    val L2_Cache_L2CEWAREA0 = L2_Cache_L2CEWAREAn(0)
    val L2_Cache_L2CEWCR0 = L2_Cache_L2CEWCRn(0)
    val L2_Cache_L2CEWAR1 = L2_Cache_L2CEWARn(1)
    val L2_Cache_L2CEWAREA1 = L2_Cache_L2CEWAREAn(1)
    val L2_Cache_L2CEWCR1 = L2_Cache_L2CEWCRn(1)
    val L2_Cache_L2CEWAR2 = L2_Cache_L2CEWARn(2)
    val L2_Cache_L2CEWAREA2 = L2_Cache_L2CEWAREAn(2)
    val L2_Cache_L2CEWCR2 = L2_Cache_L2CEWCRn(2)
    val L2_Cache_L2CEWAR3 = L2_Cache_L2CEWARn(3)
    val L2_Cache_L2CEWAREA3 = L2_Cache_L2CEWAREAn(3)
    val L2_Cache_L2CEWCR3 = L2_Cache_L2CEWCRn(3)


    val L2_Cache_L2SRBAR0 = L2_Cache_L2SRBARn(0)
    val L2_Cache_L2SRBAREA0 = L2_Cache_L2SRBAREAn(0)
    val L2_Cache_L2SRBAR1 = L2_Cache_L2SRBARn(1)
    val L2_Cache_L2SRBAREA1 = L2_Cache_L2SRBAREAn(1)
    val L2_Cache_L2ERRDIS = L2CacheRegister(0xE44u, "L2_Cache_L2ERRDIS")
    val L2_Cache_L2ERRINTEN = L2CacheRegister(0xE48u, "L2_Cache_L2ERRINTEN")
    val L2_Cache_L2ERRCTL = L2CacheRegister(0xE58u, "L2_Cache_L2ERRCTL")




    inner class Filter(parent: Module) : AddressTranslator(parent, "filter", BUS32) {
        override fun translate(ea: ULong, ss: Int, size: Int, LorS: AccessAction): ULong {
            if (!L2_Cache_L2CTL.L2E.truth || l2SRAMCount == 0)
                return 0xFFFF_FFFFu // Stub - to avoid wrong access

            val (index, range) = l2SRAMRange.mapIndexed { i, it -> i to l2MemoryMappedSRAMRange(it) }.find {
                ea in it.second
            } ?: return 0xFFFF_FFFFu

            return ea - range.first + index * 512 * 1024 // 512 Kb offset

        }
    }

    val filter = Filter(this)

}