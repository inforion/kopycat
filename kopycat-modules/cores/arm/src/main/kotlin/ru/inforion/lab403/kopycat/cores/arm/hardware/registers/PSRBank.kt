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

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.arm.enums.ProcessorMode
import ru.inforion.lab403.kopycat.cores.arm.hardware.processors.AARMCPU
import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBankNG
import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import java.io.Serializable
import kotlin.reflect.KProperty


class PSRBank(val cpu: AARMCPU) : ARegistersBankNG<AARMCore>(
        "ARM Program Status Registers Bank", 5, 32) {

    inner class APSR : Register("apsr", 0) {
        inner class bitOf(val bit: Int): Serializable {
            operator fun getValue(thisRef: APSR, property: KProperty<*>) = cpsr.value[bit].toBool()
            operator fun setValue(thisRef: APSR, property: KProperty<*>, newValue: Boolean) {
                cpsr.value = cpsr.value.insert(newValue.toInt(), bit)
            }
        }

        override var value: Long
            get() = cpsr.value and 0xF00F_0000L
            set(value) {
                cpsr.bits31_27 = value[31..27]
                cpsr.bits19_16 = value[19..16]
            }

        var n by bitOf(31)
        var z by bitOf(30)
        var c by bitOf(29)
        var v by bitOf(28)
    }

    inner class IPSR : Register("ipsr", 1) {
        var exceptionNumber by fieldOf(5..0)
    }

    inner class EPSR : Register("epsr", 2) {
        var t by bitOf(24)
    }

    inner class CPSR : Register("cpsr", 3, default = ProcessorMode.svc.id.toULong()) {

        override var value: Long
            get() = super.value
            set(value) {
                val prevModeId = super.value and 0b11111
                val newModeId = value and 0b11111
                if (prevModeId != newModeId) {
                    if (prevModeId != 0L) {
                        val prevMode = first<ProcessorMode> { it.id == prevModeId.toInt() }
                        val newMode = first<ProcessorMode> { it.id == newModeId.toInt() }
                        cpu.switchBankedRegisters(prevMode, newMode)
                    }
                }
                super.value = value
            }

        var n by bitOf(31)
        var z by bitOf(30)
        var c by bitOf(29)
        var v by bitOf(28)

        var q by bitOf(27)
        var ITSTATE by fieldOf(
                15..10 to 7..2,
                26..25 to 1..0)
        var j by bitOf(24)
        var ge by fieldOf(19..16)
        var ENDIANSTATE by bitOf(9)
        var a by bitOf(8)
        var i by bitOf(7)
        var f by bitOf(6)
        var t by bitOf(5)
        var ISETSTATE by fieldOf(
                24..24 to 1..1,
                5..5 to 0..0)
        var m by fieldOf(4..0)

        // See CPSRWriteByInstr
        var bits31_27 by fieldOf(31..27)
        var bits26_24 by fieldOf(26..24)
        var bits19_16 by fieldOf(19..16)
        var bits15_10 by fieldOf(15..10)
    }

    inner class SPSR : Register("spsr", 4) {
        // See SPSRWriteByInstr
        var bits31_24 by fieldOf(31..24)
        var bits19_16 by fieldOf(19..16)
        var bits15_8 by fieldOf(15..8)
        var bits7_5 by fieldOf(7..5)
        var bits4_0 by fieldOf(4..0)
    }

    val apsr = APSR()
    val ipsr = IPSR()
    val epsr = EPSR()
    val cpsr = CPSR()
    val spsr = SPSR()
}