package ru.inforion.lab403.kopycat.cores.ppc.hardware.processors

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACOP
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.exceptions.HardwareException
import ru.inforion.lab403.kopycat.cores.ppc.enums.eIrq
import ru.inforion.lab403.kopycat.cores.ppc.exceptions.IPPCExceptionHolder
import ru.inforion.lab403.kopycat.cores.ppc.exceptions.PPCHardwareException
import ru.inforion.lab403.kopycat.modules.cores.PPCCore
import java.util.logging.Level



class PPCCOP(core: PPCCore, name: String) : ACOP<PPCCOP, PPCCore>(core, name) {
    companion object { val log = logger(Level.INFO) }

    override fun processInterrupts() {
        //TODO("not implemented")
        /*val interrupt = pending(core.cpu.flags.gie)
        if (interrupt != null) {
            if (core.cpu.flags.gie) {
                core.bus.outw(core.cpu.regs.r1StackPointer - 2, core.cpu.regs.r0ProgramCounter)
                core.bus.outw(core.cpu.regs.r1StackPointer - 4, core.cpu.regs.r2StatusRegister)
                core.cpu.regs.r1StackPointer = core.cpu.regs.r1StackPointer - 4

                core.cpu.regs.r2StatusRegister = 0
                core.cpu.regs.r0ProgramCounter = core.bus.inl(interrupt.vector.asULong)
                //TODO: Interrupt controller not implemented
            }
            interrupt.onInterrupt()
        }*/
    }

    override fun handleException(exception: GeneralException?): GeneralException? {
        if (exception !is PPCHardwareException)
            return exception

        log.severe { exception.toString() }

        exception.interrupt(core)

        return null
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return mapOf()
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}