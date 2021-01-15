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
package ru.inforion.lab403.kopycat.cores.x86.hardware.processors

import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.AFPU
import ru.inforion.lab403.kopycat.cores.x86.hardware.registers.CWRBank
import ru.inforion.lab403.kopycat.cores.x86.hardware.registers.FWRBank
import ru.inforion.lab403.kopycat.cores.x86.hardware.registers.SWRBank
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import ru.inforion.lab403.kopycat.serializer.deserialize
import ru.inforion.lab403.kopycat.serializer.loadValue



class x86FPU(core: x86Core, name: String): AFPU<x86Core>(core, name) {
    companion object {
        const val FPU_STACK_SIZE = 8
    }

    override fun describe(): String = "FPU for x86"

    private var pos = 0
    private val stack = Array(FPU_STACK_SIZE) { 0L }
    val fwr = FWRBank(core)
    val cwr = CWRBank(core)
    val swr = SWRBank(core)

    operator fun set(i: Int, e: Long) {
        stack[i] = e
    }

    operator fun get(i: Int): Long = stack[i]

    fun push(e: Long) {
        stack[pos] = e
        pos++
    }

    fun pop(): Long {
        pos--
        return stack[pos]
    }

    fun pop(count: Int) = repeat(count) { pop() }

    override fun reset() {
        stack.fill(0)
        fwr.reset()
        cwr.reset()
        swr.reset()
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return mapOf(
                "fwr" to fwr.serialize(ctxt),
                "cwr" to cwr.serialize(ctxt),
                "swr" to swr.serialize(ctxt),
                "pos" to pos,
                "stack" to stack
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        fwr.deserialize(ctxt, snapshot["fwr"] as Map<String, String>)
        cwr.deserialize(ctxt, snapshot["cwr"] as Map<String, String>)
        swr.deserialize(ctxt, snapshot["swr"] as Map<String, String>)
        pos = loadValue(snapshot, "pos") { 0 }
        stack.deserialize<Long, Int>(ctxt, snapshot["stack"]) { it.asULong }
    }
}