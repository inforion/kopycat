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
package ru.inforion.lab403.kopycat.cores.x86.hardware.processors

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.AFPU
import ru.inforion.lab403.kopycat.cores.x86.hardware.registers.FWRBank
import ru.inforion.lab403.kopycat.cores.x86.instructions.fpu.LongDouble
import ru.inforion.lab403.kopycat.cores.x86.instructions.fpu.longDouble
import ru.inforion.lab403.kopycat.cores.x86.instructions.fpu.softfloat.RoundingMode
import ru.inforion.lab403.kopycat.cores.x86.instructions.fpu.softfloat.SoftFloat
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import java.math.BigInteger

class x86FPU(core: x86Core, name: String): AFPU<x86Core>(core, name), IAutoSerializable {
    companion object {
        const val FPU_STACK_SIZE = 8
    }

    private val stack = Array(FPU_STACK_SIZE) { BigInteger.ZERO }

    val fwr = FWRBank(core)
//    val cwr = CWRBank(core)
//    val swr = SWRBank(core)

    @DontAutoSerialize
    val softfloat = object : SoftFloat() {
        override val roundingMode get() = when (fwr.FPUControlWord.rc) {
            FWRBank.RoundControl.RoundToNearestEven -> RoundingMode.RoundNearEven
            FWRBank.RoundControl.RoundTowardsNegative -> RoundingMode.RoundMin
            FWRBank.RoundControl.RoundTowardsPositive -> RoundingMode.RoundMax
            FWRBank.RoundControl.RoundTowardZero -> RoundingMode.RoundMinMag
        }
        override val f80RoundingPrecision get() = fwr.FPUControlWord.pc
        override val exceptionFlags = null
    }

    /** Returns st([i]) */
    fun st(i: Int): BigInteger = stack[(fwr.FPUStatusWord.top.int + i) % 8]

    /** Returns st([i]) */
    fun stld(i: Int) = st(i).longDouble(softfloat)

    /** Sets st([i]) = [v] */
    fun st(i: Int, v: BigInteger) {
        stack[(fwr.FPUStatusWord.top.int + i) % 8] = v
    }

    /** Sets st([i]) = [v] */
    fun stld(i: Int, v: LongDouble) {
        stack[(fwr.FPUStatusWord.top.int + i) % 8] = v.ieee754AsUnsigned()
    }

    /**
     * Returns mmx register value
     * @param i register number
     * @return mmx register value
     */
    fun mmx(i: Int): ULong = stack[i].ulong

    /**
     * Sets mmx register value
     * @param n register number
     * @param v new value
     */
    fun mmx(n: Int, v: ULong) {
        stack[n] = v.bigint.insert(0xFFFFuL, 79..64)

        fwr.FPUStatusWord.value = 0uL
        for (i in 0 until FPU_STACK_SIZE) {
            if (stack[i][79..64] == 0xFFFFuL.bigint) {
                fwr.FPUTagWord[i] = FWRBank.TagValue.Special
            } else if (stack[i][63..0] == BigInteger.ZERO) {
                fwr.FPUTagWord[i] = FWRBank.TagValue.Zero
            }
        }
    }

    /** Pushes [v] to x87 stack */
    fun push(v: BigInteger) {
        if (fwr.FPUStatusWord.top == 0uL) {
            fwr.FPUStatusWord.top = 7uL
        } else {
            fwr.FPUStatusWord.top--
        }

        if (fwr.FPUTagWord[fwr.FPUStatusWord.top.int] != FWRBank.TagValue.Empty) {
            // Overflow
            fwr.FPUStatusWord.ie = true
            fwr.FPUStatusWord.sf = true
            fwr.FPUStatusWord.c1 = true

            // Real CPU pushes NaN on stack overflow
            // Unicorn pushes the value
//            stack[fwr.FPUStatusWord.top.int] = "ffffc000000000000000".bigintByHex
//            fwr.FPUTagWord[fwr.FPUStatusWord.top.int] = FWRBank.TagValue.Special
        } //else {
            stack[fwr.FPUStatusWord.top.int] = v
            fwr.FPUTagWord[fwr.FPUStatusWord.top.int] = if (v.longDouble(softfloat).isZero) {
                FWRBank.TagValue.Zero
            } else {
                FWRBank.TagValue.Valid
            }
     //   }
    }

    /** Pops from x87 stack */
    fun pop(): BigInteger {
        val value = stack[fwr.FPUStatusWord.top.int]
        fwr.FPUTagWord[fwr.FPUStatusWord.top.int] = FWRBank.TagValue.Empty

        if (fwr.FPUStatusWord.top == 7uL) {
            fwr.FPUStatusWord.top = 0uL

            /*
            // Underflow
            fwr.FPUStatusWord.ie = true
            fwr.FPUStatusWord.sf = true
            fwr.FPUStatusWord.c1 = false
            */
        } else {
            fwr.FPUStatusWord.top++
        }
        return value
    }

    /** Pops from x87 stack [count] times */
    fun pop(count: Int) = repeat(count) { pop() }

    override fun reset() {
        stack.fill(BigInteger.ZERO)
        fwr.reset()
    }

//    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
//        return mapOf(
//                "fwr" to fwr.serialize(ctxt),
//                "pos" to pos,
//                "stack" to stack
//        )
//    }
//
//    @Suppress("UNCHECKED_CAST")
//    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
//        fwr.deserialize(ctxt, snapshot["fwr"] as Map<String, String>)
//        pos = loadValue(snapshot, "pos") { 0 }
//        stack.deserialize<ULong, Int>(ctxt, snapshot["stack"]) { it.ulong_z }
//    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return super<IAutoSerializable>.serialize(ctxt)
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super<IAutoSerializable>.deserialize(
            ctxt,
            if ("stack" in snapshot && (snapshot["stack"] as List<HashMap<String, String>>)[0]["class"] == "kotlin.ULong") {
                // Old snapshot; discard stack
                snapshot.filterKeys { it != "stack" }
            } else {
                snapshot
            }
        )
    }
}
