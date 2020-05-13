package ru.inforion.lab403.kopycat.cores.msp430.hardware.processors

import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACPU
import ru.inforion.lab403.kopycat.cores.msp430.enums.MSP430GPR
import ru.inforion.lab403.kopycat.cores.msp430.hardware.registers.FLBank
import ru.inforion.lab403.kopycat.cores.msp430.hardware.registers.GPRBank
import ru.inforion.lab403.kopycat.cores.msp430.hardware.systemdc.MSP430SystemDecoder
import ru.inforion.lab403.kopycat.cores.msp430.instructions.AMSP430Instruction
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core



class MSP430CPU(val msp430: MSP430Core, name: String):
        ACPU<MSP430CPU, MSP430Core, AMSP430Instruction, MSP430GPR>(msp430, name) {

    override fun reg(index: Int): Long = regs[index].value(msp430)
    override fun reg(index: Int, value: Long) = regs[index].value(msp430, value)
    override fun count() = regs.count()

    override var pc : Long
        get() = regs.r0ProgramCounter
        set(value) { regs.r0ProgramCounter = value }

    val regs = GPRBank(msp430)
    val flags = FLBank(msp430)

    private val decoder = MSP430SystemDecoder(msp430)

    override fun reset() {
        super.reset()
        decoder.reset()
        regs.reset()
        regs.r0ProgramCounter = core.fetch(0xFFFE, 0, 2)
    }

    override fun decode() {
        insn = decoder.decode(regs.r0ProgramCounter)
    }

    override fun execute(): Int {
        regs.r0ProgramCounter += insn.size
        insn.execute()
//        println("${pc.hex8}   [${data.hex16}]   ${insn.size}   $insn")
        return 1  // TODO: get from insn.execute()
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return mapOf(
                "regs" to regs.serialize(ctxt),
                "flags" to flags.serialize(ctxt),
                "pc" to pc.hex8
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super.deserialize(ctxt, snapshot)
        regs.deserialize(ctxt, snapshot["regs"] as Map<String, String>)
        flags.deserialize(ctxt, snapshot["flags"] as Map<String, String>)
    }

}