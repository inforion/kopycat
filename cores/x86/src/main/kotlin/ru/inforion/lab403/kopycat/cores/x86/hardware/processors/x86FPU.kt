package ru.inforion.lab403.kopycat.cores.x86.hardware.processors

import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.AFPU
import ru.inforion.lab403.kopycat.cores.x86.hardware.registers.CWRBank
import ru.inforion.lab403.kopycat.cores.x86.hardware.registers.FWRBank
import ru.inforion.lab403.kopycat.cores.x86.hardware.registers.SWRBank
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import ru.inforion.lab403.kopycat.serializer.deserialize
import ru.inforion.lab403.kopycat.serializer.loadValue

/**
 * Created by davydov_vn on 08.09.16.
 */

class x86FPU(core: x86Core, name: String): AFPU<x86Core>(core, name) {
    companion object {
        const val FPU_STACK_SIZE = 8
    }

    override fun describe(): String = "FPU for x86"

    private var pos = 0
    private val stack = Array(FPU_STACK_SIZE) { 0L }
    val fwr = FWRBank(core)
    val cwr = CWRBank(core)
    val swr = SWRBank(core)

    operator fun set(i: Int, e: Long) {
        stack[i] = e
    }

    operator fun get(i: Int): Long = stack[i]

    fun push(e: Long) {
        stack[pos] = e
        pos++
    }

    fun pop(): Long {
        pos--
        return stack[pos]
    }

    fun pop(count: Int) = repeat(count) { pop() }

    override fun reset() {
        stack.fill(0)
        fwr.reset()
        cwr.reset()
        swr.reset()
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return mapOf(
                "fwr" to fwr.serialize(ctxt),
                "cwr" to cwr.serialize(ctxt),
                "swr" to swr.serialize(ctxt),
                "pos" to pos,
                "stack" to stack
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        fwr.deserialize(ctxt, snapshot["fwr"] as Map<String, String>)
        cwr.deserialize(ctxt, snapshot["cwr"] as Map<String, String>)
        swr.deserialize(ctxt, snapshot["swr"] as Map<String, String>)
        pos = loadValue(snapshot, "pos", 0)
        stack.deserialize<Long, Int>(ctxt, snapshot["stack"]) { it.asULong }
    }
}