package ru.inforion.lab403.kopycat.cores.arm.hardware.processors

import ru.inforion.lab403.common.extensions.clr
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.arm.enums.Mode
import ru.inforion.lab403.kopycat.cores.arm.enums.VectorTable
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unknown
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.modules.cores.ARMv6MCore
import java.util.logging.Level.FINE



class ARMv6MCOP(val cpu: ARMv6MCore, name: String) : AARMCOP(cpu, name) {
    companion object {
        val log = logger(FINE)
    }

    fun ExceptionEntry(core: ARMv6MCore, exceptionType: VectorTable) {
        PushStack(core, exceptionType)
        ExceptionTaken(core, exceptionType)
    }

    fun XPSR(core: ARMv6MCore, framePtrAlign: Long): Long {
        var result = core.cpu.sregs.apsr.value or core.cpu.sregs.ipsr.value or core.cpu.sregs.epsr.value
        result = result.insert(framePtrAlign, 9)
        return result
    }

    fun PushStack(core: ARMv6MCore, exceptionType: VectorTable) {
        val framePtrAlign: Long
        val framePtr: Long
        if (core.cpu.spr.control.spsel && core.cpu.CurrentMode == Mode.Thread) {
            framePtrAlign = core.cpu.regs.spProcess.value[2]
            core.cpu.regs.spProcess.value = (core.cpu.regs.spProcess.value - 0x20) clr 2
            framePtr = core.cpu.regs.spProcess.value
        } else {
            framePtrAlign = core.cpu.regs.spMain.value[2]
            core.cpu.regs.spMain.value = (core.cpu.regs.spMain.value - 0x20) clr 2
            framePtr = core.cpu.regs.spMain.value
        }
        core.outl(framePtr, core.cpu.regs.r0.value)
        core.outl(framePtr + 0x4, core.cpu.regs.r1.value)
        core.outl(framePtr + 0x8, core.cpu.regs.r2.value)
        core.outl(framePtr + 0xC, core.cpu.regs.r3.value)
        core.outl(framePtr + 0x10, core.cpu.regs.r12.value)
        core.outl(framePtr + 0x14, core.cpu.regs.lr.value)
        core.outl(framePtr + 0x18, ReturnAddress(core, exceptionType))
        core.outl(framePtr + 0x1C, XPSR(core, framePtrAlign))

        if (core.cpu.CurrentMode == Mode.Handler) {
            core.cpu.regs.lr.value = 0xFFFF_FFF1
        } else {
            core.cpu.regs.lr.value = if (!core.cpu.spr.control.spsel) 0xFFFF_FFF9 else 0xFFFF_FFFD
        }
    }

    fun ExceptionTaken(core: ARMv6MCore, exceptionType: VectorTable) {
        core.cpu.CurrentMode = Mode.Handler
        core.cpu.sregs.ipsr.value = exceptionType.exceptionNumber
        core.cpu.spr.control.spsel = false
        val start = core.inl(core.cpu.VTOR + 4 * exceptionType.exceptionNumber)
        core.cpu.BLXWritePC(start)
    }

    fun ReturnAddress(core: ARMv6MCore, exceptionType: VectorTable): Long =
            if(exceptionType.exceptionNumber in 2..47) core.cpu.pc clr 0
            else throw Unknown

    override fun handleException(exception: GeneralException?): GeneralException? = exception

    override fun processInterrupts() {
        if (core.cpu.CurrentMode() == Mode.Thread) {
            val interrupt = pending(true)
            if (interrupt != null) {
//                log.fine { "ARMv6M: ${interrupt.stringify()}" }
                ExceptionEntry(cpu, VectorTable.fromOffset(interrupt.vector))
                interrupt.onInterrupt()
            }
        }
    }
}
