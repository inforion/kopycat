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
package ru.inforion.lab403.kopycat.cores.arm

import ru.inforion.lab403.common.extensions.UNDEF
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.hardware.registers.GPRBank
import ru.inforion.lab403.kopycat.cores.arm.enums.GPR as eGPR
import ru.inforion.lab403.kopycat.cores.base.abstracts.ABI
import ru.inforion.lab403.kopycat.cores.base.abstracts.AContext
import ru.inforion.lab403.kopycat.cores.base.enums.ArgType
import ru.inforion.lab403.kopycat.cores.base.operands.ARegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore


class ARMABI(cpu: AARMCore, heap: LongRange, stack: LongRange, bigEndian: Boolean):
        ABI<AARMCore>(cpu, heap, stack, bigEndian) {

    override fun gpr(index: Int): ARegister<AARMCore> = GPRBank.Operand(index)
    override fun createCpuContext(): AContext<*> = ARMContext(core.cpu)
    override val ssr = UNDEF
    override val sp = GPRBank.Operand(eGPR.SPMain.id) // TODO: refactor
    override val ra = GPRBank.Operand(eGPR.LR.id)
    override val v0 = GPRBank.Operand(eGPR.R0.id)
    override val argl = listOf(
            GPRBank.Operand(eGPR.R0.id),
            GPRBank.Operand(eGPR.R1.id),
            GPRBank.Operand(eGPR.R2.id),
            GPRBank.Operand(eGPR.R3.id))

    override fun getArgs(n: Int, type: ArgType): Array<Long>{
        var res = argl.map { it.value(core) }
        val args = Array(n){ type }

        if (n > argl.size) {
            val ss = stackStream(where = argl.last().value(core))
            res.dropLast(1)
            res += args[argl.size until args.size].map {  // !!!!!!!!!!!!!!!!!!!!!  Не все аргументы !!!!!!!!!!!!!!1
                when (it) {
                    ArgType.Pointer -> ss.read(types.pointer)
                    ArgType.Word -> ss.read(types.word)
                    ArgType.Half -> ss.read(types.half)
                    ArgType.Byte -> ss.read(types.half)  // x86 can't push byte but others ...
                }
            }
        }

        return res.toTypedArray()
    }
}