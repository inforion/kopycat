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
package ru.inforion.lab403.kopycat.cores.msp430.hardware.processors

import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.kopycat.interfaces.*
import ru.inforion.lab403.common.logging.INFO
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACOP
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core


class MSP430COP(core: MSP430Core, name: String) : ACOP<MSP430COP, MSP430Core>(core, name) {
    companion object {
        @Transient val log = logger(INFO)
    }

    override fun handleException(exception: GeneralException?): GeneralException? {
        // TODO("not implemented")
        return exception
    }

    override fun processInterrupts() {
        val interrupt = pending(core.cpu.flags.gie)
        if (interrupt != null) {
            if (core.cpu.flags.gie) {
                core.outw(core.cpu.regs.r1StackPointer - 2u, core.cpu.regs.r0ProgramCounter)
                core.outw(core.cpu.regs.r1StackPointer - 4u, core.cpu.regs.r2StatusRegister)
                core.cpu.regs.r1StackPointer = core.cpu.regs.r1StackPointer - 4u

                core.cpu.regs.r2StatusRegister = 0u
                core.cpu.regs.r0ProgramCounter = core.inl(interrupt.vector.ulong_z)
                //TODO: Interrupt controller not implemented
            }
            interrupt.onInterrupt()
        }
    }

    override fun serialize(ctxt: GenericSerializer) = TODO("not implemented")

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) = TODO("not implemented")

}