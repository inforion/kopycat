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
package ru.inforion.lab403.kopycat.modules.p2020

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.field
import ru.inforion.lab403.kopycat.modules.BUS32



class L2Cache(parent: Module, name: String) : Module(parent, name) {

    inner class Ports : ModulePorts(this) {
        val inp = Slave("in", BUS32)
        val ctrl = Slave("ctrl", BUS32)
    }

    override val ports = Ports()

    fun l2Address(n: Int): Long {
        val low = when(n) {
            0 -> L2_Cache_L2CEWAR0.ADDR
            1 -> L2_Cache_L2CEWAR1.ADDR
            2 -> L2_Cache_L2CEWAR2.ADDR
            3 -> L2_Cache_L2CEWAR3.ADDR
            else -> throw GeneralException("Wrong n: $n")
        }.toLong()

        val high = when(n) {
            0 -> L2_Cache_L2CEWAREA0.ADDR
            1 -> L2_Cache_L2CEWAREA1.ADDR
            2 -> L2_Cache_L2CEWAREA2.ADDR
            3 -> L2_Cache_L2CEWAREA3.ADDR
            else -> throw GeneralException("Wrong n: $n")
        }.toLong()

        return (high shl 24) or low
    }

    inner class L2_Cache_L2CEWARn(val n: Int) : Register(ports.ctrl, 0x2_0010 + 16 * n.toLong(), Datatype.DWORD, "L2_Cache_L2CEWAR$n") {

        var ADDR by field(31..8)

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            log.severe { "$name: L2C base address: ${l2Address(n).hex8}" }
        }

    }

    inner class L2_Cache_L2CEWAREAn(val n: Int) : Register(ports.ctrl, 0x2_0014 + 16 * n.toLong(), Datatype.DWORD, "L2_Cache_L2CEWAR$n") {

        var ADDR by field(3..0)

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            log.severe { "$name: L2C base address: ${l2Address(n).hex8}" }
        }

    }

    inner class L2_Cache_L2CEWCRn(val n: Int) : Register(ports.ctrl, 0x2_0018 + 16 * n.toLong(), Datatype.DWORD, "L2_Cache_L2CEWCR$n") {
        var E by bit(31)
        var LOCK by bit(30)
        var SIZMASK by field(27..0)

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            log.severe { "$name: L2C E=$E. LOCK=$LOCK, mask size: ${SIZMASK.hex8}" }
        }

    }



    //TODO: NOT FULLY IMPLEMENTEED
    val L2_Cache_L2CTL = object : Register(ports.ctrl, 0x2_0000, Datatype.DWORD, "L2_Cache_L2CTL", 0x2000_0000) {

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value and 0xBFFF_FFFF)
            log.warning { "Write to control"}
            log.severe { "$name: L2I=${value[30]}" }
        }
    }
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





}