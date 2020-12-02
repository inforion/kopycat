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
package ru.inforion.lab403.kopycat.cores.v850es.hardware.processors

import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACPU
import ru.inforion.lab403.kopycat.cores.v850es.enums.GPR
import ru.inforion.lab403.kopycat.cores.v850es.hardware.memory.CTRLBank
import ru.inforion.lab403.kopycat.cores.v850es.hardware.memory.FLBank
import ru.inforion.lab403.kopycat.cores.v850es.hardware.memory.GPRBank
import ru.inforion.lab403.kopycat.cores.v850es.hardware.systemdc.v850ESSystemDecoder
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

/**
 * {RU}
 * Процессор ядра v850ES
 *
 *
 * @property core ядро, в которое помещается процессор
 * @property name произвольное имя объекта
 * {RU}
 */
class v850ESCPU(val v850es: v850ESCore, name: String): ACPU<v850ESCPU, v850ESCore, AV850ESInstruction, GPR>(v850es, name) {

    override fun reg(index: Int): Long = regs[index].value(v850es)
    override fun reg(index: Int, value: Long) = regs[index].value(v850es, value)
    override fun count() = regs.count()
    override fun flags() = flags.value

    override var pc: Long
        get() = regs.pc
        set(value) { regs.pc = value }

    val regs = GPRBank(v850es)
    val cregs = CTRLBank(v850es)
    val flags = FLBank(v850es)

    private val decoder = v850ESSystemDecoder(v850es)

//    override lateinit var insn: AV850ESInstruction

    override fun reset() {
        super.reset()
        decoder.reset()
        regs.reset()
    }

    override fun decode() {
        insn = decoder.decode(regs.pc).also { it.ea = pc }
    }

    override fun execute(): Int {
        insn.execute()
        regs.pc += insn.size
        return 1  // TODO: get from insn.execute()
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return mapOf(
                "regs" to regs.serialize(ctxt),
                "flags" to flags.serialize(ctxt),
                "cregs" to cregs.serialize(ctxt),
                "pc" to pc.hex8
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super.deserialize(ctxt, snapshot)
        regs.deserialize(ctxt, snapshot["regs"] as Map<String, String>)
        flags.deserialize(ctxt, snapshot["flags"] as Map<String, String>)
        cregs.deserialize(ctxt, snapshot["cregs"] as Map<String, String>)
    }
}