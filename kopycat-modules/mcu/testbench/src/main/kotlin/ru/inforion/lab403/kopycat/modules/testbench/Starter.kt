package ru.inforion.lab403.kopycat.modules.testbench

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.gdbstub.GDBServer

object Starter {
    @JvmStatic
    fun main(args: Array<String>) {
        // Create our Testbench device
        val top = Testbench(null, "testbench")

        // Initialize it as a top device (device that has no parent)
        top.initializeAndResetAsTopInstance()

        // Write some instructions into memory
        top.core.write(WORD, 0x0000_0000, 0x2003) // movs  r0, #3
        top.core.write(WORD, 0x0000_0002, 0x2107) // movs  r1, #7
        top.core.write(WORD, 0x0000_0004, 0x180A) // adds  r2, r1, r0

        // Setup program counter
        // Note, that we may use top.arm.cpu.pc but there is some caveat here
        // top.arm.cpu.pc just change PC but don't make flags changing (i.e. change core mode)
        // so be aware when change PC.
        top.arm.cpu.BXWritePC(0x0000_0000)

        // Make a step
        top.arm.step()
        assert(top.core.reg(0) == 3L)

        // Make another step
        top.arm.step()
        assert(top.core.reg(1) == 7L)

        // And one more step
        top.arm.step()
        assert(top.core.reg(2) == 10L)

        GDBServer(23946, true, binaryProtoEnabled = false).also { it.debuggerModule(top.debugger) }
    }
}