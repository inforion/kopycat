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
package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.trap

import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException
import ru.inforion.lab403.kopycat.cores.mips.instructions.Code20bitInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * SYSCALL
 *
 * To cause a System Call exception
 *
 * A system call exception occurs, immediately and unconditionally transferring control to the exception handler.
 * The code field is available for use as software parameters, but is retrieved by the exception handler only by
 * loading the contents of the memory word containing the instruction.
 */
class syscall(core: MipsCore,
              data: ULong,
              code: MipsImmediate) : Code20bitInsn(core, data, Type.VOID, code) {

    override val mnem = "syscall"

    override fun execute() = throw MipsHardwareException.SYS(core.pc)
}