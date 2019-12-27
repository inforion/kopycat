package ru.inforion.lab403.kopycat.cores.arm.hardware.processors

import ru.inforion.lab403.common.extensions.clr
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.arm.enums.Mode
import ru.inforion.lab403.kopycat.cores.arm.enums.VectorTable
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unknown
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.modules.cores.ARMv6Core
import java.util.logging.Level

/**
 * Created by a.gladkikh on 13.01.18.
 */

class ARMv6COP(val cpu: ARMv6Core, name: String) : AARMCOP(cpu, name) {
    companion object {
        val log = logger(Level.INFO)
    }

    fun ExceptionEntry(core: ARMv6Core, exceptionType: VectorTable) {
        PushStack(core, exceptionType)
        ExceptionTaken(core, exceptionType)
    }

    fun XPSR(core: ARMv6Core, framePtrAlign: Long): Long {
        var result = core.cpu.sregs.apsr or core.cpu.sregs.ipsr or core.cpu.sregs.epsr
        result = result.insert(framePtrAlign, 9)
        return result
    }

    fun PushStack(core: ARMv6Core, exceptionType: VectorTable) {
        val framePtrAlign: Long
        val framePtr: Long
        if (core.cpu.spr.spsel && core.cpu.CurrentMode == Mode.Thread) {
            framePtrAlign = core.cpu.regs.spProcess[2]
            core.cpu.regs.spProcess = (core.cpu.regs.spProcess - 0x20) clr 2
            framePtr = core.cpu.regs.spProcess
        } else {
            framePtrAlign = core.cpu.regs.spMain[2]
            core.cpu.regs.spMain = (core.cpu.regs.spMain - 0x20) clr 2
            framePtr = core.cpu.regs.spMain
        }
        core.outl(framePtr, core.cpu.regs.r0)
        core.outl(framePtr + 0x4, core.cpu.regs.r1)
        core.outl(framePtr + 0x8, core.cpu.regs.r2)
        core.outl(framePtr + 0xC, core.cpu.regs.r3)
        core.outl(framePtr + 0x10, core.cpu.regs.r12)
        core.outl(framePtr + 0x14, core.cpu.regs.lr)
        core.outl(framePtr + 0x18, ReturnAddress(core, exceptionType))
        core.outl(framePtr + 0x1C, XPSR(core, framePtrAlign))

        if (core.cpu.CurrentMode == Mode.Handler) {
            core.cpu.regs.lr = 0xFFFF_FFF1
        } else {
            core.cpu.regs.lr = if (!core.cpu.spr.spsel) 0xFFFF_FFF9 else 0xFFFF_FFFD
        }
    }

    fun ExceptionTaken(core: ARMv6Core, exceptionType: VectorTable) {
        core.cpu.CurrentMode = Mode.Handler
        core.cpu.sregs.ipsr = exceptionType.exceptionNumber
        core.cpu.spr.spsel = false
        val start = core.inl(core.cpu.VTOR + 4 * exceptionType.exceptionNumber)
        core.cpu.BLXWritePC(start)
    }

    fun ReturnAddress(core: ARMv6Core, exceptionType: VectorTable): Long =
            if(exceptionType.exceptionNumber in 2..47) core.cpu.pc clr 0
            else throw Unknown

    override fun handleException(exception: GeneralException?): GeneralException? = exception

    override fun processInterrupts() {
        if (core.cpu.CurrentMode() == Mode.Thread) {
            val interrupt = pending(true)
            if (interrupt != null) {
                ExceptionEntry(cpu, VectorTable.fromOffset(interrupt.vector))
                interrupt.onInterrupt()
            }
        }
    }
}
