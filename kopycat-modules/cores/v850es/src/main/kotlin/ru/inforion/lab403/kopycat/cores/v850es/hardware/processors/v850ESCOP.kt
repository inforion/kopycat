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

import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACOP
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore
import java.util.logging.Level



class v850ESCOP(core: v850ESCore, name: String) : ACOP<v850ESCOP, v850ESCore>(core, name) {
    companion object {
        @Transient val log = logger(Level.INFO)
    }

    override fun handleException(exception: GeneralException?): GeneralException? {
        // TODO("not implemented")
        return exception
    }

    override fun processInterrupts() {
        val interrupt = pending(!core.cpu.flags.id)
        if (interrupt != null) {
            if (!core.cpu.flags.id) {
                core.cpu.cregs.eipc = core.cpu.regs.pc
                core.cpu.cregs.eipsw = core.cpu.cregs.psw

                // Exception code of non-maskable interrupt (NMI)
                val fecc = core.cpu.cregs.ecr[31..16]
                // Exception code of exception or maskable interrupt
                val eicc = interrupt.vector
                core.cpu.cregs.ecr = (fecc shl 16) + eicc

                core.cpu.flags.ep = false
                core.cpu.flags.id = true
                core.cpu.pc = interrupt.vector.asULong
            }
            interrupt.onInterrupt()
        }
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return emptyMap()
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
    }
}
