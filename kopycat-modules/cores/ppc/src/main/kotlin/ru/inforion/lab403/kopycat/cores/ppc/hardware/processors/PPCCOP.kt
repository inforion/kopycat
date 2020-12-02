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
package ru.inforion.lab403.kopycat.cores.ppc.hardware.processors

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACOP
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.ppc.exceptions.PPCHardwareException
import ru.inforion.lab403.kopycat.modules.cores.PPCCore
import java.util.logging.Level


class PPCCOP(core: PPCCore, name: String) : ACOP<PPCCOP, PPCCore>(core, name) {
    companion object {
        @Transient val log = logger(Level.INFO)
    }

    override fun processInterrupts() {
        //TODO("not implemented")
        /*val interrupt = pending(core.cpu.flags.gie)
        if (interrupt != null) {
            if (core.cpu.flags.gie) {
                core.bus.outw(core.cpu.regs.r1StackPointer - 2, core.cpu.regs.r0ProgramCounter)
                core.bus.outw(core.cpu.regs.r1StackPointer - 4, core.cpu.regs.r2StatusRegister)
                core.cpu.regs.r1StackPointer = core.cpu.regs.r1StackPointer - 4

                core.cpu.regs.r2StatusRegister = 0
                core.cpu.regs.r0ProgramCounter = core.bus.inl(interrupt.vector.asULong)
                //TODO: Interrupt controller not implemented
            }
            interrupt.onInterrupt()
        }*/
    }

    override fun handleException(exception: GeneralException?): GeneralException? {
        if (exception !is PPCHardwareException)
            return exception

        log.severe { exception.toString() }

        exception.interrupt(core)

        return null
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return mapOf()
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}