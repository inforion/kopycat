package ru.inforion.lab403.kopycat.modules.cores

import ru.inforion.lab403.kopycat.cores.arm.ARMABI
import ru.inforion.lab403.kopycat.cores.arm.hardware.processors.AARMCOP
import ru.inforion.lab403.kopycat.cores.arm.hardware.processors.AARMCPU
import ru.inforion.lab403.kopycat.cores.arm.hardware.registers.GPRBank
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.abstracts.ABI
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACore
import ru.inforion.lab403.kopycat.cores.base.common.AddressTranslator
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts



abstract class AARMCore(parent: Module, name: String, frequency: Long, val version: Int, ipc: Double):
        ACore<AARMCore, AARMCPU, AARMCOP>(parent, name, frequency, ipc) {

    enum class InstructionSet(val code: Int) {
        CURRENT(-1),
        ARM(0b00),
        THUMB(0b01),
        JAZELLE(0b10),
        THUMB_EE(0b11);

        companion object {
            fun from(code: Int): InstructionSet = values().first { it.code == code }
        }
    }

    override val fpu = null
    override val mmu: AddressTranslator? = null

    override fun abi(heap: LongRange, stack: LongRange): ABI<AARMCore> = ARMABI(this, heap, stack, false)

//    inner class Ports : ModulePorts(this) {
//        val mem = Proxy("mem")
//    }
//
//    inner class Buses: ModuleBuses(this) {
//        val mem = Bus("mem")
//    }
//
//    override val ports = Ports()
//    override val buses = Buses()
}