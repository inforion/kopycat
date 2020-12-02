/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2020 INFORION, LLC
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Non-free licenses may also be purchased from INFORION, LLC, 
 * for users who do not want their programs protected by the GPL. 
 * Contact us for details kopycat@inforion.ru
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package ru.inforion.lab403.kopycat.cores.x86.hardware.processors

import net.sourceforge.argparse4j.inf.ArgumentParser
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACOP
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACore.Stage
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.enums.ExcCode
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.exceptions.x86HardwareException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.*
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.SSR.cs
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.SSR.ss
import ru.inforion.lab403.kopycat.cores.x86.x86utils
import ru.inforion.lab403.kopycat.interfaces.IInteractive
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import ru.inforion.lab403.kopycat.serializer.loadValue
import ru.inforion.lab403.kopycat.serializer.storeValues
import java.util.logging.Level.CONFIG


class x86COP(core: x86Core, name: String) : ACOP<x86COP, x86Core>(core, name) {

    companion object {
        @Transient val log = logger(CONFIG)
    }

    enum class GateType(val id: Int) {
        TASK_GATE_16(5),
        INT_GATE_16(6),
        TRAP_GATE_16(7),
        INT_GATE_32(0xE),
        TRAP_GATE_32(0xF)
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
     * Task Segment Selector register
     */
    var tssr = 0L

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
        val data = core.mmu.linearRead(offset, 8, true)
        return LDTEntry(data)
    }

    private fun saveContextGateSamePrivilegeLevel(
            prefs: Prefixes, error: HardwareError?, eflags: Long, cs: Long, ip: Long
    ) {
        val flagsSize = if (!prefs.is16BitOperandMode) DWORD else WORD
        x86utils.push(core, eflags, flagsSize, prefs)
        x86utils.push(core, cs, DWORD, prefs)
        x86utils.push(core, ip, DWORD, prefs)
        if (error != null && error.excCode.hasError)
            x86utils.push(core, error.value, DWORD, Prefixes(core))

        // cs and ip already changed to check requested priv. level (rpl)
    }

    private fun saveOldStackToTaskStack(prefs: Prefixes, ss: Long, sp: Long) {
        x86utils.push(core, ss, DWORD, prefs)
        x86utils.push(core, sp, DWORD, prefs)
    }

    private fun loadStackFromTaskDescriptor(index: Int, ss: SSR, sp: x86Register) {
//        https://wiki.osdev.org/Task_State_Segment
        val offset = 0x04 + index * 8
        val tss = core.mmu.readSegmentDescriptor(tssr).base
        val ss_sp = core.mmu.linearRead(tss + offset, 8, true)
        val tss_ss = ss_sp[47..32]
        val tss_sp = ss_sp[31..0]
        sp.value(core, tss_sp)
        ss.value(core, tss_ss)
    }

    override fun handleException(exception: GeneralException?): GeneralException? {
        if (exception is x86HardwareException) {
            error = HardwareError(exception.excCode as ExcCode, exception.errorCode)
            if (exception is x86HardwareException.PageFault) {
                log.config { "[${core.pc.hex8}] Page fault for address 0x${exception.address.hex} code=0x${exception.errorCode.hex}" }
                CTRLR.cr2.value(core, exception.address)
            }
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
        // Hardware error fault has the most high priority
            hwError != null -> {
                //
                // It decrement eip because interrupt must occur before the address
                // is incremented in reality, and we have after.
                //
                // If we decode instruction, execute it or already update clock
                // then we for sure increment EIP and should decrement it
                if (core.stage != Stage.INTERRUPTS_PROCESSED)
                    core.cpu.regs.eip -= core.cpu.insn.size
                hwError.excCode.code
            }

        // TODO: Who has higher priority software or hardware interrupt in x86?
        // Currently hardware interrupt has higher priority
            interrupt != null -> {
                log.finest { "Interrupt request: ${interrupt.stringify()}" }
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

        val prefs = Prefixes(core)
        val ip = x86Register.gpr(prefs.opsize, x86GPR.EIP)
        val sp = x86Register.gpr(prefs.opsize, x86GPR.ESP)

        val cpl = cs.cpl(core)
        val saved_eflags = eflags.value(core)
        val saved_cs = cs.value(core)
        val saved_ip = ip.value(core)
        val saved_ss = ss.value(core)
        val saved_sp = sp.value(core)

        val idtEntry = idt(vector)

        // change cs and ip because we need requested privilege level
        ip.value(core, idtEntry.base)
        cs.value(core, idtEntry.selector)

        val rpl = cs.cpl(core)

        log.finest {
            val context = "cpl=$cpl rpl=$rpl ip=${saved_cs.hex}:${saved_ip.hex} sp=${saved_ss.hex}:${saved_sp.hex} flags=${saved_eflags.hex}"
            "--> INT [0x${vector.hex}] IDTR=$idtr hdl=${idtEntry.selector.hex}:${idtEntry.base.hex} $context"
        }

        when (val gateTypeId = idtEntry.type.asInt) {
            GateType.TASK_GATE_16.id,
            GateType.INT_GATE_16.id,
            GateType.TRAP_GATE_16.id -> TODO("Gate ${idtEntry.type} not implemented for 16 bit mode!")

            GateType.INT_GATE_32.id,
            GateType.TRAP_GATE_32.id -> {
                if (cpl != rpl) {
                    loadStackFromTaskDescriptor(rpl, ss, sp)
                    saveOldStackToTaskStack(prefs, saved_ss, saved_sp)
                }

                saveContextGateSamePrivilegeLevel(prefs, hwError, saved_eflags, saved_cs, saved_ip)

                // see Vol. 1 6-11 (PROCEDURE CALLS, INTERRUPTS, AND EXCEPTIONS)
                // If the call is through an interrupt gate, clears the IF flag in the EFLAGS register.
                if (gateTypeId == GateType.INT_GATE_32.id)
                    eflags.ifq(core, false)
            }

            else -> throw GeneralException("Incorrect Gate type: ${idtEntry.type}")
        }
    }

    /* =============================== Overridden methods =============================== */

    override fun serialize(ctxt: GenericSerializer) = storeValues(
            "idtrBase" to idtr.base,
            "idtrLimit" to idtr.limit,
            "tssr" to tssr,
            "IRQ" to IRQ,
            "INT" to INT
    )

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        idtr.base = loadValue(snapshot, "idtrBase")
        idtr.limit = loadValue(snapshot, "idtrLimit")
        tssr = loadValue(snapshot, "tssr")
        IRQ = loadValue(snapshot, "IRQ")
        INT = loadValue(snapshot, "INT")
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