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
package ru.inforion.lab403.kopycat.modules.cores.device

import org.junit.Assert
import org.junit.Before
import ru.inforion.lab403.common.extensions.MHz
import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.extensions.hexlify
import ru.inforion.lab403.common.extensions.unhexlify
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

abstract class ATest: Module(null, "Test") {
    protected fun displacement(
            reg: TestRegister,
            off: Immediate<TestCore>,
            dtyp: Datatype = DWORD,
            access: AOperand.Access = ANY) = Displacement(dtyp, reg, off, access)
    protected fun immediate(value: Long, dtyp: Datatype = DWORD): Immediate<TestCore> = Immediate(value, dtyp = dtyp)
    protected fun register(id: Int) = testCore.cpu.regs[id].toOperand()
    protected fun memory(address: Long, dtyp: Datatype = DWORD, atyp: Datatype = DWORD) =
            Mem<TestCore>(dtyp, atyp, address, ANY)
    protected fun error(value: Long, expected: Long, actual: Long, operand: String, test: String): String =
            "Operands $operand $test test expected ${expected.hex}, but got ${actual.hex} for test ${value.hex}"
    protected fun <T> assert(error: String, expected: T, actual: T) = Assert.assertEquals(error, expected, actual)
    protected fun load(address: Long, size: Int): String = testCore.load(address, size).hexlify()
    protected fun load(address: Long, dtyp: Datatype): Long = testCore.read(dtyp, address, 0)
    protected fun store(address: Long, data: String)= testCore.store(address, data.unhexlify())
    protected fun store(address: Long, data: Long, dtyp: Datatype = DWORD) = testCore.write(dtyp, address, data, 0)
    protected fun regs(r0: Long = 0,  r1: Long = 0) {
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

    @Before fun resetTest() {
        testCore.reset()
    }

    var address = 0L
    var value = 0L
    var actual = 0L
    var expected = 0L
}