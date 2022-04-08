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
package ru.inforion.lab403.kopycat.cores.x86.hardware.processors

import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.AFPU
import ru.inforion.lab403.kopycat.cores.x86.hardware.registers.FWRBank
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import ru.inforion.lab403.kopycat.serializer.deserialize
import ru.inforion.lab403.kopycat.serializer.loadValue



class x86FPU(core: x86Core, name: String): AFPU<x86Core>(core, name), IAutoSerializable {
    companion object {
        const val FPU_STACK_SIZE = 8
    }

    private var pos = 0
    private val stack = Array(FPU_STACK_SIZE) { 0uL }
    val fwr = FWRBank(core)

    operator fun set(i: Int, e: ULong) {
        stack[i] = e
    }

    operator fun get(i: Int): ULong = stack[i]

    fun push(e: ULong) {
        stack[pos] = e
        pos++
    }

    fun pop(): ULong {
        pos--
        return stack[pos]
    }

    fun pop(count: Int) = repeat(count) { pop() }

    override fun reset() {
        stack.fill(0u)
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
        super<IAutoSerializable>.deserialize(ctxt, snapshot)
    }
}