package ru.inforion.lab403.kopycat.cores.x86.hardware.processors

import net.sourceforge.argparse4j.inf.ArgumentParser
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACOP
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.enums.ExcCode
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.exceptions.x86HardwareException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.SSR.cs
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.eflags
import ru.inforion.lab403.kopycat.cores.x86.x86utils
import ru.inforion.lab403.kopycat.interfaces.IInteractive
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import java.util.logging.Level

/**
 * Created by davydov_vn on 14.12.16.
 */
class x86COP(core: x86Core, name: String) : ACOP<x86COP, x86Core>(core, name) {

    companion object {
        val log = logger(Level.INFO)
    }

    data class LDTEntry(val data: Long) {
        val dataHi by lazy { data[63..32] }
        val dataLo by lazy { data[31..0] }
        val baseLow: Long get() = dataLo[15..0]
        val baseHigh: Long get() = dataHi[31..16]
        val selector: Long get() = dataLo[31..16]
        val p: Long get() = dataHi[15]
        val dpl: Long get() = dataHi[14..13]
        val s: Long get() = dataHi[12]
        val type: Long get() = dataHi[11..8]
        val base: Long get() = baseLow.insert(baseHigh, 31..16)
    }

    data class HardwareError(val excCode: ExcCode, val value: Long)

    /**
     * Real register in x86Coprocessor. It contains information
     * about IDT - base and offset
     */
    val idtr = x86MMU.DescriptorRegister()

    /**
     * Software interrupt request flag
     */
    var INT: Boolean = false
    /**
     * Software interrupt request vector
     */
    var IRQ: Int = -1

    /**
     * Hardware exception dataclass.
     * if is null - no exception
     * else - it is max priority interrupt.
     */
    private var error: HardwareError? = null

    private fun idt(vector: Int): LDTEntry {
        val offset = idtr.base + vector * 8
        val data = core.mmu.load(offset)
        return LDTEntry(data)
    }

    private fun saveContextGateEF(error: HardwareError?, prefs: Prefixes) {
        x86utils.push(core, eflags.value(core), DWORD, prefs)                           // WARING! was "false". may be incorrect now, who know?
        x86utils.push(core, cs.value(core), DWORD, prefs)                               // WARING! was "false". may be incorrect now, who know?
        x86utils.push(core, x86Register.gpr(prefs.opsize, x86GPR.EIP).value(core), DWORD, prefs)    // WARING! was "false". may be incorrect now, who know?
        if (error != null && error.excCode.hasError)
            x86utils.push(core, error.value, DWORD, Prefixes(core))
    }

    override fun handleException(exception: GeneralException?): GeneralException? {
        if (exception is x86HardwareException) {
            error = HardwareError(exception.excCode as ExcCode, exception.errorCode)
            return null
        }
        return exception
    }

    override fun processInterrupts() {
        val hwError = error
        error = null

        val ie = eflags.ifq(core)
        val interrupt = pending(ie)
        val vector = when {
        // Hardware error fault has the most higher priority
            hwError != null -> {
                // it decrement eip because interrupt must occur before the address is incremented in
                // reality, and we have after.
                core.cpu.regs.eip -= core.cpu.insn.size
                hwError.excCode.code
            }

        // TODO: Who has higher priority software or hardware interrupt in x86?
        // Currently hardware interrupt has higher priority
            interrupt != null -> {
                log.config { "Interrupt request: $interrupt" }
                interrupt.pending = false
                interrupt.inService = true
                interrupt.vector
            }
            INT -> {
                val result = IRQ
                INT = false
                IRQ = -1  // to make sure that another software interrupt not be shot
                result
            }
            else -> return
        }

        if (vector > idtr.limit) throw GeneralException("IDT vector > limit [$vector > ${idtr.limit}]")

        val idtEntry = idt(vector)
        when (idtEntry.type.asInt) {
            0x05 -> TODO("Gate 0x05 not implemented")
            0x06 -> TODO("Gate 0x06 not implemented")
            0x07 -> TODO("Gate 0x07 not implemented")
            0x0E, 0x0F -> saveContextGateEF(hwError, Prefixes(core))
            else -> throw GeneralException("Incorrect Gate type: ${idtEntry.type}")
        }

        val eip = x86Register.gpr(DWORD, x86GPR.EIP)
        eip.value(core, idtEntry.base)
        cs.value(core, idtEntry.selector)
    }

    /* =============================== Overridden methods =============================== */

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return mapOf("idtrBase" to idtr.base.hex8,
                "idtrLimit" to idtr.limit.hex8)
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        idtr.base = (snapshot["idtrBase"] as String).hexAsULong
        idtr.limit = (snapshot["idtrLimit"] as String).hexAsULong
    }

    override fun command(): String = "cop"

    override fun configure(parent: ArgumentParser?, useParent: Boolean): ArgumentParser? =
            super.configure(parent, useParent)?.apply {
                variable<Int>("-r", "--irq", required = true, help = "Request interrupt by vector [in decimal]")
            }

    override fun process(context: IInteractive.Context): Boolean {
        if (super.process(context))
            return true

        val irq: Int = context["irq"]
        IRQ = irq
        INT = true

        context.result = "IRQ=$IRQ INT=$INT"

        return true
    }
}