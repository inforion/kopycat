package ru.inforion.lab403.kopycat.modules.cores

import ru.inforion.lab403.common.extensions.MHz
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.ppc.exceptions.IPPCExceptionHolder
import ru.inforion.lab403.kopycat.cores.ppc.exceptions.PPCExceptionHolder_Embedded
import ru.inforion.lab403.kopycat.cores.ppc.hardware.peripheral.TimeBase
import ru.inforion.lab403.kopycat.cores.ppc.hardware.processors.PPCCOP
import ru.inforion.lab403.kopycat.cores.ppc.hardware.processors.PPCCPU
import ru.inforion.lab403.kopycat.cores.ppc.hardware.processors.systems.PPCCPU_Embedded



abstract class PPCCoreEmbedded(parent: Module,
                               name: String,
                               frequency: Long,
                               exceptionHolder: IPPCExceptionHolder = PPCExceptionHolder_Embedded,
                               optionalCpu: ((PPCCore, String) -> PPCCPU)? = ::PPCCPU_Embedded,
                               optionalCop: ((PPCCore, String) -> PPCCOP)? = null):
        PPCCore(parent, name, frequency, exceptionHolder, optionalCpu, optionalCop) {

    @Suppress("LeakingThis")
    val timebase = TimeBase(this, "timebase", 1.MHz)

    override fun initRoutine() {
        super.initRoutine()
        timebase.connect()
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return super.serialize(ctxt) + mapOf(
                "ppccore" to super.serialize(ctxt),
                "timebase" to timebase.serialize(ctxt)
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super.deserialize(ctxt, snapshot["ppccore"] as Map<String, String>)
        timebase.deserialize(ctxt, snapshot["timebase"] as Map<String, String>)
    }

}