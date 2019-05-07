package ru.inforion.lab403.kopycat.cores.msp430.hardware.processors

import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACOP
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core
import java.util.logging.Level

/**
 * Created by shiftdj on 16/02/18.
 */

class MSP430COP(core: MSP430Core, name: String) : ACOP<MSP430COP, MSP430Core>(core, name) {
    companion object { val log = logger(Level.INFO) }

    override fun handleException(exception: GeneralException?): GeneralException? {
        // TODO("not implemented")
        return exception
    }

    override fun processInterrupts() {
        val interrupt = pending(core.cpu.flags.gie)
        if (interrupt != null) {
            if (core.cpu.flags.gie) {
                core.outw(core.cpu.regs.r1StackPointer - 2, core.cpu.regs.r0ProgramCounter)
                core.outw(core.cpu.regs.r1StackPointer - 4, core.cpu.regs.r2StatusRegister)
                core.cpu.regs.r1StackPointer = core.cpu.regs.r1StackPointer - 4

                core.cpu.regs.r2StatusRegister = 0
                core.cpu.regs.r0ProgramCounter = core.inl(interrupt.vector.asULong)
                //TODO: Interrupt controller not implemented
            }
            interrupt.onInterrupt()
        }
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}