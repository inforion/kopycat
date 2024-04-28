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
package ru.inforion.lab403.kopycat.cores.x86

import ru.inforion.lab403.common.extensions.uint
import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.exceptions.x86HardwareException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Предоставляет набор функций для раннего отлова page fault.
 * Не меняет данные, восстанавливает RSP после выполнения.
 * Выкидывает page fault после завершения если он произошел внутри блока.
 * @throws x86HardwareException.PageFault
 */
fun pageFault(core: x86Core, block: PageFaultHelper.() -> Unit) {
    val sp = core.cpu.regs.gpr(x86GPR.RSP, Datatype.QWORD).value
    val result = runCatching {
        PageFaultHelper(core).block()
    }
    core.cpu.regs.gpr(x86GPR.RSP, Datatype.QWORD).value = sp
    result.getOrThrow()
}

/** Предоставляет набор функций для раннего отлова page fault */
class PageFaultHelper(private val core: x86Core) {
    fun push(dtyp: Datatype, prefs: Prefixes) {
        val sp = core.cpu.regs.gpr(x86GPR.RSP, prefs.addrsize)
        core.raisePageFault(sp.value - dtyp.bytes.uint, core.cpu.sregs.ss.id, dtyp.bytes, AccessAction.STORE)
        sp.value -= dtyp.bytes.uint
    }

    fun pop(dtyp: Datatype, prefs: Prefixes, offset: ULong = 0u) {
        val sp = core.cpu.regs.gpr(x86GPR.RSP, prefs.addrsize)
        core.raisePageFault(sp.value, core.cpu.sregs.ss.id, dtyp.bytes, AccessAction.LOAD)
        sp.value += dtyp.bytes.uint + offset
    }

    fun read(ea: ULong, ss: Int = 0, size: Int) = core.raisePageFault(ea, ss, size, AccessAction.LOAD)
    fun read(ea: ULong, ss: Int = 0, dtyp: Datatype) = read(ea, ss, dtyp.bytes)

    fun write(ea: ULong, ss: Int = 0, size: Int) = core.raisePageFault(ea, ss, size, AccessAction.STORE)
    fun write(ea: ULong, ss: Int = 0, dtyp: Datatype) = write(ea, ss, dtyp.bytes)

    fun AOperand<x86Core>.read() {
        if (hasEffectiveAddress) read(effectiveAddress(core), ssr.reg, dtyp)
    }

    fun AOperand<x86Core>.write() {
        if (hasEffectiveAddress) write(effectiveAddress(core), ssr.reg, dtyp)
    }

    fun outb(ea: ULong, ss: Int = 0) = write(ea, ss, Datatype.BYTE)
    // fun outw(ea: ULong, ss: Int = 0) = write(ea, ss, Datatype.WORD)
    fun outl(ea: ULong, ss: Int = 0) = write(ea, ss, Datatype.DWORD)
    fun outq(ea: ULong, ss: Int = 0) = write(ea, ss, Datatype.QWORD)
    fun oute(ea: ULong, size: Int, ss: Int = 0) = (0uL until size.ulong_z).forEach { outb(ea + it, ss) }

    // fun inb(ea: ULong, ss: Int = 0) = read(ea, ss, Datatype.BYTE)
    // fun inw(ea: ULong, ss: Int = 0) = read(ea, ss, Datatype.WORD)
    // fun inl(ea: ULong, ss: Int = 0) = read(ea, ss, Datatype.DWORD)
    fun inq(ea: ULong, ss: Int = 0) = read(ea, ss, Datatype.QWORD)
    // fun ine(ea: ULong, size: Int, ss: Int = 0) = (0uL until size.ulong_z).forEach { inb(ea + it, ss) }
}
