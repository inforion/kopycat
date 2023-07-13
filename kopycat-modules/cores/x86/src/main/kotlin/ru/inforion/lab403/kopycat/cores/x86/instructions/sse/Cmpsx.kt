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
package ru.inforion.lab403.kopycat.cores.x86.instructions.sse

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class Cmpsx(
    core: x86Core,
    opcode: ByteArray,
    prefs: Prefixes,
    private val typ: Datatype,
    vararg operands: AOperand<x86Core>
) : ASSEInstruction(core, opcode, prefs, *operands) {
    override val mnem = when (typ) {
        Datatype.DWORD -> "cmpss"
        Datatype.QWORD -> "cmpsd"
        else -> TODO("cmps* variant")
    }

    // This is horrible
    private class FloatingPoint private constructor(private val smth: Any) {
        constructor(f: Float) : this(f as Any)
        constructor(d: Double) : this(d as Any)

        fun isNaN(): Boolean = when (smth) {
            is Float -> smth.isNaN()
            else -> (smth as Double).isNaN()
        }

        operator fun compareTo(other: FloatingPoint): Int {
            if (smth::class.java != other.smth::class.java) {
                throw RuntimeException("Can't compare")
            }

            return when (smth) {
                is Float -> smth.compareTo(other.smth as Float)
                is Double -> smth.compareTo(other.smth as Double)
                else -> throw RuntimeException("Can't compare")
            }
        }
    }

    private fun ULong.floatingPoint() = when (typ) {
        Datatype.DWORD -> FloatingPoint(this.uint.ieee754())
        Datatype.QWORD -> FloatingPoint(this.ieee754())
        else -> TODO("cmps* variant")
    }

    // Ordered comparisons returns false for NaN operands
    private fun ordered(a1: FloatingPoint, a2: FloatingPoint, cmp: (FloatingPoint, FloatingPoint) -> Boolean) =
        (!a1.isNaN() && !a2.isNaN()) && cmp(a1, a2)

    // Unordered comparison returns true for NaN operands
    private fun unordered(a1: FloatingPoint, a2: FloatingPoint, cmp: (FloatingPoint, FloatingPoint) -> Boolean) =
        a1.isNaN() || a2.isNaN() || cmp(a1, a2)

    override fun executeSSEInstruction() {
        // TODO: MXCSR

        val dest = op1.extValue(core)

        val a1 = dest[typ.msb..typ.lsb].ulong.floatingPoint()
        val a2 = when (op2.dtyp) {
            Datatype.XMMWORD -> op2.extValue(core).ulong.floatingPoint()
            else -> op2.value(core).floatingPoint()
        }

        val result = when (val predicate = op3.value(core)) {
            // 0: OP3 := EQ_OQ
            0uL -> ordered(a1, a2) { a, b -> a == b }

            // 1: OP3 := LT_OS
            1uL -> ordered(a1, a2) { a, b -> a < b }

            // 2: OP3 := LE_OS
            2uL -> ordered(a1, a2) { a, b -> a <= b }

            // 3: OP3 := UNORD_Q
            3uL -> TODO("CMPUNORDSD")

            // 4: OP3 := NEQ_UQ
            4uL -> unordered(a1, a2) { a, b -> a != b }

            // 5: OP3 := NLT_US
            5uL -> unordered(a1, a2) { a, b -> a >= b }

            // 6: OP3 := NLE_US
            6uL -> unordered(a1, a2) { a, b -> a > b }

            // 7: OP3 := ORD_Q
            7uL -> TODO("CMPORDSD")

            else -> TODO("Predicate ${predicate.hex8}")
        }

        op1.extValue(
            core,
            if (result) {
                dest set typ.msb..typ.lsb
            } else {
                dest clr typ.msb..typ.lsb
            }
        )
    }
}
