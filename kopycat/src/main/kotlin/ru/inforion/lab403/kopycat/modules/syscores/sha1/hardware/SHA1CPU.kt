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
package ru.inforion.lab403.kopycat.modules.syscores.sha1.hardware

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.interfaces.*
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACPU
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.exceptions.DecoderException
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import ru.inforion.lab403.kopycat.modules.syscores.sha1.SHA1Engine
import ru.inforion.lab403.kopycat.modules.syscores.sha1.enums.GPR
import ru.inforion.lab403.kopycat.modules.syscores.sha1.enums.INSN
import ru.inforion.lab403.kopycat.modules.syscores.sha1.enums.Stub
import ru.inforion.lab403.kopycat.modules.syscores.sha1.instructions.*

class SHA1CPU constructor(core: SHA1Core, name: String):
    ACPU<SHA1CPU, SHA1Core, Instruction, Stub>(core, name), IAutoSerializable {
    override fun reg(index: Int): ULong = regs[index]

    override fun reg(index: Int, value: ULong) {
        regs[index] = value
    }

    override fun count() = regs.size

    override var pc: ULong
        get() = regs[GPR.PC]
        set(value) { regs[GPR.PC] = value }

    internal val engine = SHA1Engine()

    internal val regs = Array<ULong>(32) { 0u }

    // cache core with right type
    private val self = core

    override fun decode() = when (regs[GPR.MD].int) {
        0 -> {  // normal mode
            val opcode = core.read(DWORD, regs[GPR.PC], 0)
            insn = when (opcode) {
                INSN.SHA1_INIT -> Init(self)
                INSN.SHA1_UPDATE -> Update(self)
                INSN.SHA1_ROUND -> Round(self)
                INSN.SHA1_FINAL -> Final(self)
                INSN.SHA1_READ -> Read(self)
                INSN.SHA1_WRITE -> Write(self)
                INSN.NOP -> Nop(self)
                else -> throw DecoderException(opcode, regs[GPR.PC])
            }
        }  // counter mode
        1 -> insn = Nop(self)
        else -> throw GeneralException("Unknown processor mode: ${regs[GPR.MD]}")
    }

    override fun execute(): Int {
        if (regs[GPR.FK] != 0uL)
            throw GeneralException("Register FK must be zero but equals to ${regs[GPR.FK]}!")
        insn.execute()
        regs[GPR.PC] = regs[GPR.PC] + insn.size.uint
        regs[GPR.CT] = regs[GPR.CT] + 1u
        return 1
    }

    override fun reset() {
        super.reset()
        regs.fill(0u)
        engine.reset()
    }

    override fun serialize(ctxt: GenericSerializer) = super<IAutoSerializable>.serialize(ctxt)

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) =
        super<IAutoSerializable>.deserialize(ctxt, snapshot)

/*    override fun serialize(ctxt: GenericSerializer) = super.serialize(ctxt) + storeValues(
            "regs" to regs,
            "engine" to engine.serialize(ctxt)
    )

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super.deserialize(ctxt, snapshot)
        val values = loadValue<List<Long>>(snapshot, "regs")
        repeat(values.size) { regs[it] = values[it].ulong }
        engine.deserialize(ctxt, snapshot["engine"] as Map<String, Any>)
    }*/
}