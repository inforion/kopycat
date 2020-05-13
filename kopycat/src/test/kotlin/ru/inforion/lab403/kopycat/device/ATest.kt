package ru.inforion.lab403.kopycat.device

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
import ru.inforion.lab403.kopycat.modules.memory.RAM
import ru.inforion.lab403.kopycat.cores.base.operands.Memory as Mem

abstract class ATest: Module(null, "Test") {
    protected fun displacement(
            reg: TestRegister,
            off: Immediate<TestCore>,
            dtyp: Datatype = DWORD,
            access: AOperand.Access = ANY) = Displacement(dtyp, reg, off, access)
    protected fun immediate(value: Long, dtyp: Datatype = DWORD): Immediate<TestCore> = Immediate(value, dtyp = dtyp)
    protected fun register(id: Int) = TestRegister.gpr(id)
    protected fun memory(address: Long, dtyp: Datatype = DWORD, atyp: Datatype = DWORD) =
            Mem<TestCore>(dtyp, atyp, address, ANY)
    protected fun error(value: Long, expected: Long, actual: Long, operand: String, test: String): String =
            "Operands $operand $test test expected ${expected.hex}, but got ${actual.hex} for test ${value.hex}"
    protected fun assert(error: String, expected: Long, actual: Long) = Assert.assertEquals(error, expected, actual)
    protected fun load(address: Long, size: Int): String = testCore.load(address, size).hexlify()
    protected fun load(address: Long, dtyp: Datatype): Long = testCore.read(dtyp, address, 0)
    protected fun store(address: Long, data: String)= testCore.store(address, data.unhexlify())
    protected fun store(address: Long, data: Long, dtyp: Datatype = DWORD) = testCore.write(dtyp, address, data, 0)
    protected fun regs(r0: Long = 0,  r1: Long = 0) { testCore.cpu.regs.r0 = r0; testCore.cpu.regs.r1 = r1 }

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