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
package ru.inforion.lab403.kopycat.modules.cores.device

import org.junit.jupiter.api.BeforeEach
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Access.ANY
import ru.inforion.lab403.kopycat.cores.base.operands.Displacement
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.device.operands.TestRegister
import ru.inforion.lab403.kopycat.modules.memory.RAM
import ru.inforion.lab403.kopycat.cores.base.operands.Memory as Mem
import ru.inforion.lab403.kopycat.interfaces.*
import kotlin.test.assertEquals

abstract class ATest: Module(null, "Test") {
    protected fun displacement(
            reg: TestRegister,
            off: Immediate<TestCore>,
            dtyp: Datatype = DWORD,
            access: AOperand.Access = ANY) = Displacement(dtyp, reg, off, access)
    protected fun immediate(value: ULong, dtyp: Datatype = DWORD): Immediate<TestCore> = Immediate(value, dtyp = dtyp)
    protected fun register(id: Int) = testCore.cpu.regs[id].toOperand()
    protected fun memory(address: ULong, dtyp: Datatype = DWORD, atyp: Datatype = DWORD) =
            Mem<TestCore>(dtyp, atyp, address, ANY)
    protected fun error(value: ULong, expected: ULong, actual: ULong, operand: String, test: String): String =
            "Operands $operand $test test expected ${expected.hex}, but got ${actual.hex} for test ${value.hex}"
    protected fun <T> assert(error: String, expected: T, actual: T) = assertEquals(expected, actual, error)
    protected fun load(address: ULong, size: Int): String = testCore.load(address, size).hexlify()
    protected fun load(address: ULong, dtyp: Datatype): ULong = testCore.read(dtyp, address, 0)
    protected fun store(address: ULong, data: String)= testCore.store(address, data.unhexlify())
    protected fun store(address: ULong, data: ULong, dtyp: Datatype = DWORD) = testCore.write(dtyp, address, data, 0)
    protected fun regs(r0: ULong = 0u,  r1: ULong = 0u) {
        testCore.cpu.regs.r0.value = r0;
        testCore.cpu.regs.r1.value = r1
    }

    inner class Buses : ModuleBuses(this) { val mem = Bus("mem") }
    final override val buses = Buses()
    val testCore = TestCore(this, "Test Core", 66.MHz)
    private val ram = RAM(this, "rom", 0x10_0000)

    init {
        testCore.ports.mem.connect(buses.mem)
        ram.ports.mem.connect(buses.mem)
        initializeAndResetAsTopInstance()
    }

    @BeforeEach fun resetTest() {
        testCore.reset()
    }

    var address = 0uL
    var value = 0uL
    var actual = 0uL
    var expected = 0uL
}