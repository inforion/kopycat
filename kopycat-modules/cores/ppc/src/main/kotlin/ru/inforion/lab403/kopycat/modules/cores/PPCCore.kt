package ru.inforion.lab403.kopycat.modules.cores

import ru.inforion.lab403.common.extensions.MHz
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.ABI
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACore
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.ppc.enums.eIrq
import ru.inforion.lab403.kopycat.cores.ppc.enums.eSystem
import ru.inforion.lab403.kopycat.cores.ppc.exceptions.IPPCExceptionHolder
import ru.inforion.lab403.kopycat.cores.ppc.hardware.peripheral.TimeBase
import ru.inforion.lab403.kopycat.cores.ppc.hardware.processors.APPCMMU
import ru.inforion.lab403.kopycat.cores.ppc.hardware.processors.PPCCOP
import ru.inforion.lab403.kopycat.cores.ppc.hardware.processors.PPCCPU
import ru.inforion.lab403.kopycat.modules.BUS32



abstract class PPCCore(
        parent: Module,
        name: String,
        frequency: Long,
        val exceptionHolder: IPPCExceptionHolder,
        optionalCpu: ((PPCCore, String) -> PPCCPU)? = null,
        optionalCop: ((PPCCore, String) -> PPCCOP)? = null
) : ACore<PPCCore, PPCCPU, PPCCOP>(parent, name, frequency, 1.0) {

    inner class Buses : ModuleBuses(this) {
        val physical = Bus("physical", BUS32)
        val virtual = Bus("virtual", BUS32)
    }

    inner class Ports : ModulePorts(this) {
        val mem = Proxy("mem")
    }
    //override val internalHardwareExceptions = eIrq.values().toSet()

    override val buses = Buses()
    override val ports = Ports()

    @Suppress("LeakingThis")
    final override val cpu = optionalCpu?.invoke(this, "cpu") ?: PPCCPU(this, "cpu", eSystem.Base)

    @Suppress("LeakingThis")
    final override val cop = optionalCop?.invoke(this, "cop") ?: PPCCOP(this, "cop")

    override val fpu = null //TODO("FPU")
    abstract override val mmu: APPCMMU //TODO: change this

    override fun abi(heap: LongRange, stack: LongRange): ABI<PPCCore> =
            throw TODO("ABI")

    //Because buses, cpu and ports aren't final
    open fun initRoutine() {
        //cpu.ports.mem.connect(buses.physical)
        cpu.ports.mem.connect(buses.virtual)
        mmu.ports.inp.connect(buses.virtual)

        mmu.ports.outp.connect(buses.physical)
        ports.mem.connect(buses.physical)
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return super.serialize(ctxt) + mapOf(
                "cpu" to cpu.serialize(ctxt),
                "cop" to cop.serialize(ctxt)
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        cpu.deserialize(ctxt, snapshot["cpu"] as Map<String, String>)
        cop.deserialize(ctxt, snapshot["cop"] as Map<String, String>)
    }

}