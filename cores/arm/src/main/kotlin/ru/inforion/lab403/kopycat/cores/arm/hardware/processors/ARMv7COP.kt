package ru.inforion.lab403.kopycat.cores.arm.hardware.processors

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import java.util.logging.Level

/**
 * Created by a.gladkikh on 13.01.18.
 */

class ARMv7COP(cpu: AARMCore, name: String) : AARMCOP(cpu, name) {
    companion object {
        val log = logger(Level.INFO)
    }

    override fun handleException(exception: GeneralException?): GeneralException? {
        // TODO("not implemented")
        return exception
    }

    override fun processInterrupts() {
        val interrupt = pending(true)
        interrupt?.onInterrupt()
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
