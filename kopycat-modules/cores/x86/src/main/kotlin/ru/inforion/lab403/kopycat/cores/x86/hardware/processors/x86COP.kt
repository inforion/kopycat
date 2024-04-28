/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2023 INFORION, LLC
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

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACOP
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACore.Stage
import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBankNG
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.enums.ExcCode
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.exceptions.x86HardwareException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.pageFault
import ru.inforion.lab403.kopycat.cores.x86.x86utils
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import ru.inforion.lab403.kopycat.interfaces.IConstructorSerializable
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import java.util.logging.Level.CONFIG


class x86COP(core: x86Core, name: String) : ACOP<x86COP, x86Core>(core, name), IAutoSerializable {

    companion object {
        @Transient val log = logger(CONFIG)
    }

    enum class GateType(val id: Int) {
        // 0 - reserved
        TSS_16_Avail(1),
        LDT(2),
        TSS_16_Busy(3),
        CALL_GATE_16(4),
        TASK_GATE_16(5),
        INT_GATE_16(6),
        TRAP_GATE_16(7),
        // 8 - reserved
        TSS_32_64_Avail(9),
        // 10 - reserved
        TSS_32_64_Busy(11),
        CALL_GATE_32_64(12),
        // 13 - reserved
        INT_GATE_32_64(14),
        TRAP_GATE_32_64(15)
    }

    interface CallGateDescriptor {
        val selector: ULong
        val type: ULong
        val s: ULong // always 0?
        val dpl: ULong
        val p: ULong
        val base: ULong
    }

    data class IVTEntryDescriptor(val data: ULong) : CallGateDescriptor {
        override val base: ULong = data[15..0]
        override val selector: ULong = data[31..16]
        override val type: ULong = GateType.INT_GATE_16.id.ulong_z
        override val s: ULong = 0uL // ?
        override val dpl: ULong = 0uL // ?
        override val p: ULong = 0uL // ?
    }

    data class CallGateDescriptor32(val data: ULong) : CallGateDescriptor {
        private val data0 by lazy { data[31..0] }
        private val data1 by lazy { data[63..32] }

        private val baseLow: ULong get() = data0[15..0]
        override val selector: ULong get() = data0[31..16]

        override val type: ULong get() = data1[11..8]
        override val s: ULong get() = data1[12] // always 0?
        override val dpl: ULong get() = data1[14..13]
        override val p: ULong get() = data1[15]
        private val baseHigh: ULong get() = data1[31..16]

        override val base: ULong get() = baseLow.insert(baseHigh, 31..16)
    }

    data class CallGateDescriptor64(val dataLo: ULong, val dataHi: ULong) : CallGateDescriptor {
        private val data0 by lazy { dataLo[31..0] }
        private val data1 by lazy { dataLo[63..32] }
        private val data2 by lazy { dataHi[31..0] }
        private val data3 by lazy { dataHi[63..32] }

        private val baseLow: ULong get() = data0[15..0]
        override val selector: ULong get() = data0[31..16]

        private val typeLow: ULong get() = data1[11..8]
        override val s: ULong get() = data1[12] // always 0?
        override val dpl: ULong get() = data1[14..13]
        override val p: ULong get() = data1[15]
        private val baseMid: ULong get() = data1[31..16]

        private val baseHigh: ULong get() = data2

        private val typeHigh: ULong get() = data3[12..8]

        override val base: ULong get() = baseLow.insert(baseMid, 31..16).insert(baseHigh, 63..32)
        override val type: ULong get() = typeLow.insert(typeHigh, 8..4)
    }

    data class HardwareError(val excCode: ExcCode, val value: ULong): IConstructorSerializable, IAutoSerializable

    /**
     * Task Segment Selector register - ?? Task Register? (ss for TSS) почему 0uL и нигде не меняется
     */
    var tssr = 0uL

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

    /**
     *
     * Single-instruction interrupt shadow
     *
     * STI (or any other interrupt flag change command) delays toggling interrupts on one command.
     * This field is being used to delay interrupts on IRQ flag set to TRUE.
     *
     * The value means the number of commands to delay (include the interrupt change command itself).
     * For Example:
     * ```asm
     * sti ; here the intShadow := 2
     * ; intShadow = 1 (decreased by 1)
     * sysexit
     * ; intShadow = 0 (decreated by 1) and InterruptsEnabled == 1 now
     * ```
     *
     * See https://www.felixcloutier.com/x86/sti
     */
    var intShadow: Int = 0

    private fun idt(vector: UInt): CallGateDescriptor = if (core.cpu.cregs.cr0.pe) {
        if (core.cpu.is64BitCompatibilityMode || core.cpu.mode == x86CPU.Mode.R64) {
            val offset = idtr.base + vector * 16u
            val dataLo = core.mmu.linearRead(offset, 8, true)
            val dataHi = core.mmu.linearRead(offset + 8u, 8, true)
            CallGateDescriptor64(dataLo, dataHi)
        } else {
            val offset = idtr.base + vector * 8u
            val data = core.mmu.linearRead(offset, 8, true)
            CallGateDescriptor32(data)
        }
    } else {
        // IVT
        val data = core.mmu.linearRead(vector.ulong_z * 4u, 4, true)
        IVTEntryDescriptor(data)
    }

    private fun saveContextGateSamePrivilegeLevel(
        prefs: Prefixes, error: HardwareError?, dtyp: Datatype, eflags: ULong, cs: ULong, ip: ULong
    ) {
//        val flagsSize = prefs.opsize //if (!prefs.is16BitOperandMode) DWORD else WORD
        pageFault(core) {
            (0 until 3).forEach { _ -> push(dtyp, prefs) }
            if (error != null && error.excCode.hasError) push(dtyp, Prefixes(core))
        }

        x86utils.push(core, eflags, dtyp, prefs)
        x86utils.push(core, cs, dtyp, prefs)
        x86utils.push(core, ip, dtyp, prefs)
        if (error != null && error.excCode.hasError)
            x86utils.push(core, error.value, dtyp, Prefixes(core))

        // cs and ip already changed to check requested priv. level (rpl)
    }

    private fun saveOldStackToTaskStack(prefs: Prefixes, ss: ULong, sp: ULong) {
        pageFault(core) {
            (0 until 2).forEach { _ -> push(prefs.opsize, prefs) }
        }

        x86utils.push(core, ss, prefs.opsize, prefs)
        x86utils.push(core, sp, prefs.opsize, prefs)
    }

    private fun loadStackFromTaskDescriptor(index: UInt, sp: ARegistersBankNG<x86Core>.Register) {
//        https://wiki.osdev.org/Task_State_Segment
        val offset = 0x04u + index * 8u
        val tss = core.mmu.readTaskStateSegment(tssr).base
        val cell = core.mmu.linearRead(tss + offset, 8, true)
        sp.value = if (core.cpu.mode == x86CPU.Mode.R64)
            cell
        else {
            core.cpu.sregs.ss.value = cell[47..32]
            cell[31..0]
        }
    }

    override fun handleException(exception: GeneralException?): GeneralException? {
        if (exception is x86HardwareException) {
            error = HardwareError(exception.excCode as ExcCode, exception.errorCode)
            if (exception is x86HardwareException.PageFault) {
                log.config { "[0x${core.pc.hex8}] Page fault for address 0x${exception.address.hex} code=0x${exception.errorCode.hex}" }
                core.cpu.cregs.cr2.value = exception.address
            } else if (exception is x86HardwareException.DeviceNotAvailable) {
                log.config { "[0x${core.pc.hex8}] FPU device not available exception" }
            }
            return null
        }
        return exception
    }

    override fun processInterrupts() {
        val hwError = error
        error = null

        val ie = core.cpu.flags.eflags.ifq && intShadow == 0
        val interrupt = pending(ie)
        val vector = when {
        // Hardware error fault has the most high priority
            hwError != null -> {
                //
                // It decrements eip because interrupt must occur before the address
                // is incremented in reality, and we have after.
                //
                // If we decode instruction, execute it or already update clock
                // then we for sure increment EIP and should decrement it
                if (core.stage != Stage.INTERRUPTS_PROCESSED)
                    core.cpu.regs.rip.value -= core.cpu.insn.size.uint
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

        check (vector.uint <= idtr.limit) { "IDT vector > limit [$vector > ${idtr.limit}]" }

        val prefs = Prefixes(core).apply { rexW = true }
        val opsize = if (core.cpu.is64BitCompatibilityMode) QWORD else prefs.opsize
        val ip = core.cpu.regs.gpr(x86GPR.RIP, opsize)
        val sp = core.cpu.regs.gpr(x86GPR.RSP, opsize)

        val ss = core.cpu.sregs.ss
        val cs = core.cpu.sregs.cs
        val eflags = core.cpu.flags.eflags

        val cpl = cs.cpl
        val saved_eflags = eflags.value
        val saved_cs = cs.value
        val saved_ip = ip.value
        val saved_ss = ss.value
        val saved_sp = sp.value

        val idtEntry = idt(vector.uint)

        // change cs and ip because we need requested privilege level
        ip.value = idtEntry.base
        cs.value = idtEntry.selector

        val rpl = cs.cpl

        log.finest {
            val context = "cpl=$cpl rpl=$rpl ip=${saved_cs.hex}:${saved_ip.hex} sp=${saved_ss.hex}:${saved_sp.hex} flags=${saved_eflags.hex}"
            "--> INT [0x${vector.hex}] IDTR=$idtr hdl=${idtEntry.selector.hex}:${idtEntry.base.hex} $context"
        }

        val gateType = GateType.values().find { it.id == idtEntry.type.int } ?: error("Incorrect Gate Type id: ${idtEntry.type}")
        when (gateType) {
            GateType.TASK_GATE_16,
            GateType.INT_GATE_16,
            GateType.TRAP_GATE_16 -> {
                with (core.cpu.flags.eflags) {
                    ifq = false
                    af = false
                    tf = false
                }
                check (prefs.opsize == WORD) { "Something gone wrong" }
                saveContextGateSamePrivilegeLevel(prefs, null, prefs.opsize, saved_eflags, saved_cs, saved_ip)
            }

            GateType.TSS_16_Avail,
            GateType.LDT,
            GateType.TSS_16_Busy,
            GateType.CALL_GATE_16 -> TODO("Gate ${idtEntry.type} is not implemented for 16 bit mode!")

            GateType.TSS_32_64_Avail,
            GateType.TSS_32_64_Busy,
            GateType.CALL_GATE_32_64 -> TODO("Gate ${idtEntry.type} is not implemented yet")


            GateType.INT_GATE_32_64,
            GateType.TRAP_GATE_32_64 -> {
                // HACK: Trigger csl update
                // This makes cpu.mode = R64 when jumping 32-bit ring 3 -> 64-bit ring 0
                // Don't do csl = true: the kernel might be 32 bit as well; in that case csl must not be true
                core.mmu.translate(ip.value, cs.id, 1, AccessAction.FETCH)

                if (cpl != rpl) {
                    loadStackFromTaskDescriptor(rpl.uint, sp)
                }

                // NOTE: prefs is probably DWORD here if jumping 32-bit ring 3 -> 64-bit ring 0
                // NOTE: dig here in case something odd happens
                saveOldStackToTaskStack(prefs, saved_ss, saved_sp)

                saveContextGateSamePrivilegeLevel(prefs, hwError, prefs.opsize, saved_eflags, saved_cs, saved_ip)

                // see Vol. 1 6-11 (PROCEDURE CALLS, INTERRUPTS, AND EXCEPTIONS)
                // If the call is through an interrupt gate, clears the IF flag in the EFLAGS register.
                if (gateType == GateType.INT_GATE_32_64)
                    core.cpu.flags.eflags.ifq = false
            }
        }
    }


    /* =============================== Overridden methods =============================== */

//    override fun serialize(ctxt: GenericSerializer) = storeValues(
//            "idtrBase" to idtr.base,
//            "idtrLimit" to idtr.limit,
//            "tssr" to tssr,
//            "IRQ" to IRQ,
//            "INT" to INT
//    )
//
//    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
//        idtr.base = loadValue(snapshot, "idtrBase")
//        idtr.limit = loadValue(snapshot, "idtrLimit")
//        tssr = loadValue(snapshot, "tssr")
//        IRQ = loadValue(snapshot, "IRQ")
//        INT = loadValue(snapshot, "INT")
//    }

    override fun stringify() = buildString {
        appendLine("$name:")
        appendLine("IDTR  : base = 0x${idtr.base.hex16}   limit = 0x${idtr.limit.hex16}")
        appendLine("Task Register, TSS Segment Selector = 0x${tssr.hex16}")
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return super<IAutoSerializable>.serialize(ctxt)
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super<IAutoSerializable>.deserialize(ctxt, snapshot)
    }
}