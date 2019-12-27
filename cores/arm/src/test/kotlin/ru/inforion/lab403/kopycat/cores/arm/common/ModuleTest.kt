package ru.inforion.lab403.kopycat.cores.arm.common

import org.junit.Assert
import org.junit.Test
import ru.inforion.lab403.common.extensions.MHz
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS.R_W
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.exceptions.AreaDefinitionError
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import ru.inforion.lab403.kopycat.cores.base.exceptions.RegisterDefinitionError
import ru.inforion.lab403.kopycat.library.types.Resource
import ru.inforion.lab403.kopycat.loader.KopycatHelper
import ru.inforion.lab403.kopycat.modules.BUS16
import ru.inforion.lab403.kopycat.modules.BUS32
import ru.inforion.lab403.kopycat.modules.cores.ARMv7Core
import ru.inforion.lab403.kopycat.modules.debuggers.ARMDebugger
import ru.inforion.lab403.kopycat.modules.memory.RAM

class ModuleTest: Module(null, "Module Test") {
    inner class Buses: ModuleBuses(this) {
        val mem = Bus("mem")
    }
    override val buses = Buses()
    private val arm = ARMv7Core(this, "arm", 48.MHz, 1.0)
    private val ram1 = RAM(this, "ram1", 0x1000, Resource("binaries/strcpy.bin"))
    private val dbg = ARMDebugger(this, "dbg")

    class PortsModule(parent: Module): Module(parent, "Ports module") {
        inner class Ports : ModulePorts(this) {
            val slave32 = Slave("slave 32", BUS32)
            val slave16 = Slave("slave 16", BUS16)
        }
        override val ports = Ports()
    }
    private val module = PortsModule(this)

    init {
        arm.ports.mem.connect(buses.mem)
        ram1.ports.mem.connect(buses.mem)
        arm.cpu.pc = 0x0
        arm.cpu.status.ISETSTATE = 1
        dbg.ports.breakpoint.connect(buses.mem)
        KopycatHelper.initializeToken(System.getenv("KC_LICENCE"))
        initializeAndResetAsTopInstance()
    }

    private fun assert(expected: Boolean, actual: Boolean, type: String = "Module") =
            Assert.assertEquals("$type error: $expected != $actual", expected, actual)

    private fun assert(expected: Long, actual: Long, type: String = "Module") =
            Assert.assertEquals("$type error: $expected != $actual", expected, actual)

    private fun assert(expected: String, actual: String, type: String = "Module") =
            Assert.assertEquals("$type error: $expected != $actual", expected, actual)

    @Test fun isCorePresentTest() {
        assert(true, isCorePresent)
    }
    @Test fun isDebuggerPresentTest() {
        assert(true, isDebuggerPresent)
    }
    @Test fun isTracerPresentTest() {
        assert(false, isTracerPresent)
    }
    @Test(expected = AreaDefinitionError::class) fun voidTest1() {
        Void(module.ports.slave16, 0x1_FFFF, 0x2_0000, "a", R_W)
    }
    @Test(expected = AreaDefinitionError::class) fun voidTest2() {
        Void(module.ports.slave16, 0xEFFF, 0x1_0000, "a", R_W)
    }
    @Test fun voidTest3() {
        val mem = Void(module.ports.slave16, 0x10, 0x20, "a", R_W)
        this.assert(0, mem.read(DWORD, 0x18))
    }
    @Test fun voidTest4() {
        val mem = Void(module.ports.slave16, 0x10, 0x20, "a", R_W)
        mem.write(DWORD, 0x18, 0xFFFF_FFFF)
        this.assert(0, mem.read(DWORD, 0x18))
    }
    @Test(expected = AreaDefinitionError::class) fun memoryTest1() {
        Memory(module.ports.slave16, 0x1_FFFF, 0x2_0000, "a", R_W)
    }
    @Test(expected = AreaDefinitionError::class) fun memoryTest2() {
        Memory(module.ports.slave16, 0xEFFF, 0x1_0000, "a", R_W)
    }
    @Test fun memoryTest3() {
        val mem = Memory(module.ports.slave32, 0x10, 0x20, "a", R_W)
        mem.write(BYTE, 0x18, 0xFA)
        this.assert(0xFA, mem.read(BYTE, 0x18))
    }
    @Test fun memoryTest4() {
        val mem = Memory(module.ports.slave32, 0x10, 0x20, "a", R_W)
        mem.write(WORD, 0x18, 0xFEED)
        this.assert(0xFEED, mem.read(WORD, 0x18))
    }
    @Test fun memoryTest5() {
        val mem = Memory(module.ports.slave32, 0x10, 0x20, "a", R_W)
        mem.write(DWORD, 0x18, 0xFEED_BEEF)
        this.assert(0xFEED_BEEF, mem.read(DWORD, 0x18))
    }
    @Test fun memoryTest6() {
        val mem = Memory(module.ports.slave32, 0x10, 0x20, "a", R_W)
        mem.write(QWORD, 0x18, 0xEE_1234_FFFF_FFFF)
        this.assert(0xEE_1234_FFFF_FFFF, mem.read(QWORD, 0x18))
    }
    @Test fun memoryTest7() {
        val mem = Memory(module.ports.slave32, 0x10, 0x20, "a", R_W)
        mem.write(FWORD, 0x18, 0xFAFA_DADA_BABA)
        this.assert(0xFAFA_DADA_BABA, mem.read(FWORD, 0x18))
    }
    @Test fun memoryTest8() {
        val mem = Memory(module.ports.slave32, 0x10, 0x20, "a", R_W)
        mem.write(FWORD, 0x18, 0xFAFA_DADA_BABA)
        this.assert(0xFAFA_DADA_BABA, mem.read(FWORD, 0x18))
    }
    @Test(expected = MemoryAccessError::class) fun memoryTest9() {
        val mem = Memory(module.ports.slave32, 0x10, 0x20, "a", R_W)
        mem.write(0x18L, 0, 17, 0xFAFA_DADA_BABA)
    }
    @Test(expected = MemoryAccessError::class) fun memoryTest10() {
        val mem = Memory(module.ports.slave32, 0x10, 0x20, "a", R_W)
        mem.read(0x18, 0, 17)
    }
    @Test fun memoryTest11() {
        val mem = Memory(module.ports.slave32, 0x10, 0x20, "a", R_W)
        mem.store( 0x18, "Hello".toByteArray(Charsets.UTF_8))
        val str = mem.load(0x18, 5).toString(Charsets.UTF_8)
        assert("Hello", str)
    }
    @Test fun memoryTest12() {
        val mem = Memory(module.ports.slave32, 0x10, 0x20, "a", R_W)
        mem.write( 0x18, "Hello".toByteArray(Charsets.UTF_8).inputStream())
        val str = mem.load(0x18, 5).toString(Charsets.UTF_8)
        assert("Hello", str)
    }
    @Test(expected = RegisterDefinitionError::class) fun registerTest1() {
        Register(module.ports.slave16, 0x1_FFFF, DWORD, "a")
    }
    @Test fun registerTest2() {
        val mem = Register(module.ports.slave16, 0xFAFA, DWORD, "a")
        mem.write(BYTE, 0x18, 0xFA)
        assert(0xFA, mem.read(BYTE, 0x18))
    }
    @Test fun registerTest3() {
        val mem = Register(module.ports.slave16, 0xFAFA, DWORD, "a")
        assert("Module Test.Ports module:slave 16[Sx====]->a@FAFA[DWORD]", mem.toString())
    }
}