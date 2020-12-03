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
@file:Suppress("NOTHING_TO_INLINE", "FunctionName", "MemberVisibilityCanBePrivate", "LocalVariableName")

package ru.inforion.lab403.kopycat.cores.arm.hardware.processors

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.arm.enums.*
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition.*
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.hardware.registers.*
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.coprocessor.MCR
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.coprocessor.MRC
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACPU
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import ru.inforion.lab403.kopycat.modules.cores.AARMCore.InstructionSet
import ru.inforion.lab403.kopycat.serializer.loadValue


abstract class AARMCPU(
        val arm: AARMCore,
        name: String,
        //Returns TRUE if the implementation
        //includes the Security Extensions
        val haveSecurityExt: Boolean = false,
        //Returns TRUE if the implementation
        //includes the Virtualization Extensions
        val haveVirtExt: Boolean = false
) : ACPU<AARMCPU, AARMCore, AARMInstruction, GPR>(arm, name) {
    // TODO: index <-> reg is ambiguous
    override fun reg(index: Int): Long = regs.read(index)
    override fun reg(index: Int, value: Long) = regs.write(index, value)
    override fun count() = regs.count
    override fun flags() = sregs.cpsr.value

    override var pc: Long
        get() = regs.pc.value
        set(value) { regs.pc.value = value }

    inline fun ConditionPassed(cond: Condition): Boolean = when (cond) {
        EQ -> flags.z
        NE -> !flags.z
        CS -> flags.c
        CC -> !flags.c
        MI -> flags.n
        PL -> !flags.n
        VS -> flags.v
        VC -> !flags.v
        HI -> flags.c && !flags.z
        LS -> !flags.c || flags.z
        GE -> flags.n == flags.v
        LT -> flags.n != flags.v
        GT -> !flags.z && (flags.n == flags.v)
        LE -> flags.z || (flags.n != flags.v)
        AL,
        UN -> true
    }

    protected var pipelineRefillRequired = false

    fun BranchTo(address: Long, refill: Boolean) {
        if (refill) pipelineRefillRequired = true
        pc = address
    }

    fun BadMode(mode: Long) = when(mode) {
        0b10000L -> false               // User mode
        0b10001L -> false               // FIQ mode
        0b10010L -> false               // IRQ mode
        0b10011L -> false               // Supervisor mode
        0b10110L -> !haveSecurityExt    // Monitor mode
        0b10111L -> false               // Abort mode
        0b11010L -> !haveVirtExt        // Hyp mode
        0b11011L -> false               // Undefined mode
        0b11111L -> false               // System mode
        else -> true
    }

    open fun BranchWritePC(address: Long, refill: Boolean = true) {
        when (CurrentInstrSet()) {
            InstructionSet.ARM -> {
                if (ArchVersion() < 6 && address[1..0] != 0L)
                    throw ARMHardwareException.Unpredictable
                BranchTo(address bzero 1..0, refill)
            }
            InstructionSet.JAZELLE -> TODO("WILL NEVER BE IMPLEMENTED!")
            else -> BranchTo(address clr 0, refill)
        }
    }

    open fun BXWritePC(address: Long, refill: Boolean = true) {
        if (CurrentInstrSet() == InstructionSet.THUMB_EE) {
            TODO("Not implemented")
        } else when {
            address[0] == 1L -> {
                SelectInstrSet(InstructionSet.THUMB)
                BranchTo(address clr 0, refill)
            }
            address[1] == 0L -> {
                SelectInstrSet(InstructionSet.ARM)
                BranchTo(address, refill)
            }
            address[1..0] == 0b10L -> throw ARMHardwareException.Unpredictable
        }
    }

    open fun LoadWritePC(address: Long) {
        if (ArchVersion() >= 5) {
            BXWritePC(address)
        } else {
            BranchWritePC(address)
        }
    }

    open fun ALUWritePC(address: Long) {
        if (ArchVersion() >= 7 && CurrentInstrSet() == InstructionSet.ARM)
            BXWritePC(address)
        else
            BranchWritePC(address)
    }

    open fun CurrentInstrSet() = InstructionSet.from(status.ISETSTATE.asInt)

    open fun CurrentModeIsPrivileged() = false

    open fun CurrentMode() = Mode.Thread

    open fun SelectInstrSet(target: InstructionSet) {
        if (target != InstructionSet.CURRENT)
            status.ISETSTATE = target.code.asLong
    }

    open fun StackPointerSelect() = StackPointer.Main

    open fun InITBlock(): Boolean = false

    open fun LastInITBlock(): Boolean = false

    fun ArchVersion(): Int = arm.version

    fun CurrentModeIsHyp(): Boolean {
        if (BadMode(status.m)) throw ARMHardwareException.Unpredictable
        return status.m == 0b11010L // Hyp mode
    }

    fun CurrentModeIsNotUser(): Boolean {
        if (BadMode(status.m)) throw ARMHardwareException.Unpredictable
        return status.m != 0b10000L // Not user mode
    }

    fun CurrentModeIsUserOrSystem(): Boolean {
        if (BadMode(sregs.cpsr.m))
            throw ARMHardwareException.Unpredictable
        return when (sregs.cpsr.m) {
            0b10000L, 0b11111L -> true // User mode, System mode
            else -> false // Other modes
        }
    }

    fun UnalignedSupport(): Boolean = true

    fun PCStoreValue(): Long = pc

    fun IsSecure() = !haveSecurityExt || !ser.scr.ns || status.m == 0b10110L // Monitor mode

    fun CurrentCond() = insn.cond

    // ec: 6 bits
    // hsrString: 25 bits
    fun WriteHSR(ec: Long, hsrString: Long) {
        var hsrValue = 0L
        hsrValue = hsrValue.insert(ec, 31..26)

        // HSR.IL not valid for unknown reasons (0x00), Prefetch Aborts (0x20, 0x21), and Data
        // Aborts (0x24, 0x25) for which the ISS information is not valid.
        val cond = when(ec) {
            0x00L, 0x20L, 0x21L -> true
            0x24L, 0x25L -> hsrString[24] == 1L
            else -> false
        }
        if (cond && insn.size == 4)
            hsrValue = hsrValue.set(25)

        // Condition code valid for EC[5:4] nonzero
        if (ec[5..4] == 0L && ec[3..0] != 0L) {
            if (CurrentInstrSet() == InstructionSet.ARM) {
                hsrValue = hsrValue.set(24)
                hsrValue = hsrValue.insert(CurrentCond().opcode.toLong(), 23..20)
            } else
                throw NotImplementedError("IMPLEMENTATION_DEFINED")
            hsrValue = hsrValue.insert(hsrString[19..0], 19..0)
        }
        else
            hsrValue = hsrValue.insert(hsrString, 24..0)
        ver.hsr.value = hsrValue
    }

    fun ITAdvance() {
        if (status.ITSTATE[2..0] == 0b000L)
            status.ITSTATE = 0L
        else
            status.ITSTATE.insert(status.ITSTATE[4..0] shl 1, 4..0)
    }

    fun EnterHypMode(new_spsr_value: Long, prefered_exceptn_return: Long, vect_offset: Int) {
        TODO("Not implemented")
//        status.m = 0b11010
//        sregs.spsr = new_spsr_value
//        ELR_hyp = prefered_exceptn_return
//        status.j = false
//        status.t = HSCTLR.TE
//        status.t = HSCTLR.TE
        //...
    }

    fun ExcVectorBase(): Long {
        return when {
            vmsa.sctlr.v -> 0xFFFF_0000 // Hivecs selected, base = 0xFFFF0000
            haveSecurityExt -> ser.vbar.value
            else -> 0L
        }
    }

    fun TakeSVCException() {
        // Determine return information. SPSR is to be the current CPSR, after changing the IT[]
        // bits to give them the correct values for the following instruction, and LR is to be
        // the current PC minus 2 for Thumb or 4 for ARM, to change the PC offsets of 4 or 8
        // respectively from the address of the current instruction into the required address of
        // the next instruction, the SVC instruction having size 2bytes for Thumb or 4 bytes for ARM.
        ITAdvance()
        val new_lr_value = pc + 4//if (status.t) pc - 2 else pc - 4
        val new_spsr_value = sregs.cpsr.value
        val vect_offset = 8

        // Check whether to take exception to Hyp mode
        // if in Hyp mode then stay in Hyp mode
        val take_to_hyp = (haveVirtExt && haveSecurityExt && ser.scr.ns && status.m == 0b11010L)
        // if HCR.TGE is set to 1, take to Hyp mode through Hyp Trap vector
        val route_to_hyp = (haveVirtExt && haveSecurityExt && !IsSecure() && ver.hcr.tge &&
                status.m == 0b10000L) // User mode
        // if HCR.TGE == '1' and in a Non-secure PL1 mode, the effect is UNPREDICTABLE

        val preferred_exceptn_return = new_lr_value
        when {
            take_to_hyp -> EnterHypMode(new_spsr_value, preferred_exceptn_return, vect_offset)
            route_to_hyp -> EnterHypMode(new_spsr_value, preferred_exceptn_return, 20)
            else -> {
                // Enter Supervisor ('10011') mode, and ensure Secure state if initially in Monitor
                // ('10110') mode. This affects the Banked versions of various registers accessed later
                // in the code.
                if (status.m == 0b10110L)
                    ser.scr.ns = false
                status.m = 0b10011

                // Write return information to registers, and make further CPSR changes: IRQs disabled,
                // IT state reset, instruction set and endianness set to SCTLR-configured values.
                sregs.spsr.value = new_spsr_value
                regs.lr.value = new_lr_value
                status.i = true
                status.ITSTATE = 0L
                status.j = false; status.t = vmsa.sctlr.te
                status.ENDIANSTATE = vmsa.sctlr.ee

                BranchTo(ExcVectorBase() + vect_offset, true)
            }
        }

    }


    // immediate: 16 bits
    fun CallSupervisor(immediate: Long) {
        if (CurrentModeIsHyp() ||
                (haveVirtExt && !IsSecure() && !CurrentModeIsNotUser() && ver.hcr.tge)) {
            val hsrString = if (CurrentCond() == AL) immediate else throw ARMHardwareException.Unknown
            WriteHSR(0b010001L, hsrString)
        }
        throw ARMHardwareException.CVCException
    }


    fun Coproc_Accepted(cp_num: Int, instr: AARMInstruction): Boolean {
        // Not called for CP10 and CP11 coprocessors
        if (cp_num == 10 || cp_num == 11)
            throw ARMHardwareException.Undefined // assert !(cp_num IN {10,11});

        if (cp_num != 14 && cp_num != 15)
            TODO("Not implemented")
        else if (cp_num == 14)
            TODO("Not implemented")
        else if (cp_num == 15) {
            // Only MCR/MCRR/MRRC/MRC are supported in CP15
            // TODO: maybe, not optimal
            val two_reg = false
            val CrNnum = if (instr is MCR) {
                if (instr.cond == Condition.UN)
                    throw ARMHardwareException.Unpredictable
                instr.crm
            }
            else if (instr is MRC) {
                if (instr.cond == Condition.UN)
                    throw ARMHardwareException.Unpredictable
                instr.crm
            }
//            else if (instr is MCRR || instr is MRRC) {}
            else
                throw ARMHardwareException.Undefined

            if (CrNnum == 4)
                throw ARMHardwareException.Unpredictable

            // Check for coarse-grained Hyp traps

            // Check against HSTR for PL1 accesses
            if (haveSecurityExt && haveVirtExt && !IsSecure() && !CurrentModeIsHyp()
                    && CrNnum != 14 && ver.hstr.value[CrNnum].toBool())
                TODO("Not implemented")

            // Check for TIDCP as a coarse-grain check for PL1 accesses
            if (haveSecurityExt && haveVirtExt && !IsSecure() && !CurrentModeIsHyp()
                    && ver.hcr.tidcp && !two_reg)
                TODO("Not implemented")

            return true
        }

        TODO("Not implemented")
    }


    // See B1.3.3
    fun CPSRWriteByInstr(value: Long, bytemask: Int, is_excpt_return: Boolean) {
        val privileged = CurrentModeIsNotUser()
        val nmfi = vmsa.sctlr.nmfi

        if (bytemask[3] == 1) {
            sregs.cpsr.bits31_27 = value[31..27] // N,Z,C,V,Q flags
            if (is_excpt_return)
                sregs.cpsr.bits26_24 = value[26..24] // IT<1:0>,J execution state bits
        }

        if (bytemask[2] == 1) {
            // bits <23:20> are reserved SBZP bits
            sregs.cpsr.bits19_16 = value[19..16] // GE<3:0> flags
        }

        if (bytemask[1] == 1) {
            if (is_excpt_return)
                sregs.cpsr.bits15_10 = value[15..10] // IT<7:2> execution state bits
            sregs.cpsr.ENDIANSTATE = value[9].toBool() // E bit is user-writable
            if (privileged && (IsSecure() || ser.scr.aw || haveVirtExt))
                sregs.cpsr.a = value[8].toBool() // A interrupt mask
        }

        if (bytemask[0] == 1) {
            if (privileged)
                sregs.cpsr.i = value[7].toBool() // I interrupt mask
            if (privileged && (!nmfi || value[6] == 0L) && (IsSecure() || ser.scr.fw || haveVirtExt))
                sregs.cpsr.f = value[6].toBool() // F interrupt mask
            if (is_excpt_return)
                sregs.cpsr.t = value[5].toBool() // T execution state bit
            if (privileged)
                if (BadMode(value[4..0]))
                    throw ARMHardwareException.Unpredictable
                else {
                    // Check for attempts to enter modes only permitted in Secure state from
                    // Non-secure state. These are Monitor mode ('10110'), and FIQ mode ('10001')
                    // if the Security Extensions have reserved it. The definition of UNPREDICTABLE
                    // does not permit the resulting behavior to be a security hole.
                    if (!IsSecure() && value[4..0] == 0b10110L) throw ARMHardwareException.Unpredictable
                    if (!IsSecure() && value[4..0] == 0b10001L && ser.nsacr.rfr)
                        throw ARMHardwareException.Unpredictable
                    // There is no Hyp mode ('11010') in Secure state, so that is UNPREDICTABLE
                    if (!ser.scr.ns && value[4..0] == 0b11010L) throw ARMHardwareException.Unpredictable
                    // Cannot move into Hyp mode directly from a Non-secure PL1 mode
                    if (!IsSecure() && sregs.cpsr.m != 0b11010L && value[4..0] == 0b11010L)
                        throw ARMHardwareException.Unpredictable
                    // Cannot move out of Hyp mode with this function except on an exception return
                    if (sregs.cpsr.m == 0b11010L && value[4..0] != 0b11010L && !is_excpt_return)
                        throw ARMHardwareException.Unpredictable
                    sregs.cpsr.m = value[4..0] // CPSR<4:0>, mode bits
                }
        }
    }

    // See B1.3.3
    fun SPSRWriteByInstr(value: Long, bytemask: Int) {
        if (CurrentModeIsUserOrSystem()) throw ARMHardwareException.Unpredictable

        if (bytemask[3] == 1)
            sregs.spsr.bits31_24 = value[31..24] // N,Z,C,V,Q flags, IT<1:0>,J execution state bits

        if (bytemask[2] == 1) {
            // bits <23:20> are reserved SBZP bits
            sregs.spsr.bits19_16 = value[19..16] // GE<3:0> flags
        }

        if (bytemask[1] == 1)
            sregs.spsr.bits15_8 = value[15..8] // IT<7:2> execution state bits, E bit, A interrupt mask

        if (bytemask[0] == 1) {
            sregs.spsr.bits7_5 = value[7..5] // I,F interrupt masks, T execution state bit
            if (BadMode(value[4..0])) // Mode bits
                throw ARMHardwareException.Unpredictable
            else
                sregs.spsr.bits4_0 = value[4..0]
        }

    }

    fun TakeDataAbortException() {
        // Determine return information. SPSR is to be the current CPSR, and LR is to be the
        // current PC plus 4 for Thumb or 0 for ARM, to change the PC offsets of 4 or 8
        // respectively from the address of the current instruction into the required address
        // of the current instruction plus 8. For an asynchronous abort, the PC and CPSR are
        // considered to have already moved on to their values for the instruction following
        // the instruction boundary at which the exception occurred.
        val new_lr_value = (if (sregs.cpsr.t) pc + 4 else pc) + 8
        val new_spsr_value = sregs.cpsr.value
        val vect_offset = 16
        val preferred_exceptn_return = new_lr_value - 8

        // Determine whether this is an external abort to be routed to Monitor mode.
        val route_to_monitor = haveSecurityExt && ser.scr.ea /*&& IsExternalAbort()*/

        // Check whether to take exception to Hyp mode
        // if in Hyp mode then stay in Hyp mode
        val take_to_hyp = haveVirtExt && haveSecurityExt && ser.scr.ns && sregs.cpsr.m == 0b11010L
        // otherwise, check whether to take to Hyp mode through Hyp Trap vector
        // TODO: It's too much
        val route_to_hyp = (haveVirtExt && haveSecurityExt && !IsSecure() /*&&
                (SecondStageAbort() || (CPSR.M != '11010' &&
                        (IsExternalAbort() && IsAsyncAbort() && HCR.AMO == '1') ||
                        (DebugException() && HDCR.TDE == '1')) ||
                        (CPSR.M == '10000' && HCR.TGE == '1' &&
                                (IsAlignmentFault() || (IsExternalAbort() && !IsAsyncAbort()))))*/)
        // if HCR.TGE == '1' and in a Non-secure PL1 mode, the effect is UNPREDICTABLE

        when {
            route_to_monitor ->  {
                // Ensure Secure state if initially in Monitor mode. This affects the Banked
                // versions of various registers accessed later in the code
                if (sregs.cpsr.m == 0b10110L) ser.scr.ns = false
//            EnterMonitorMode(new_spsr_value, new_lr_value, vect_offset)
            }
            take_to_hyp -> EnterHypMode(new_spsr_value, preferred_exceptn_return, vect_offset)
            route_to_hyp -> EnterHypMode(new_spsr_value, preferred_exceptn_return, 20)
            else -> {
                // Handle in Abort mode. Ensure Secure state if initially in Monitor mode. This
                // affects the Banked versions of various registers accessed later in the code
                if (haveSecurityExt && sregs.cpsr.m == 0b10110L)
                    ser.scr.ns = false

                sregs.cpsr.m = 0b10111 // Abort mode

                // Write return information to registers, and make further CPSR changes:
                // IRQs disabled, other interrupts disabled if appropriate,
                // IT state reset, instruction set and endianness set to SCTLR-configured values.
                sregs.spsr.value = new_spsr_value
                regs.lr.value = new_lr_value
                sregs.cpsr.i = true
                if (!haveSecurityExt || haveVirtExt || !ser.scr.ns || ser.scr.aw)
                    sregs.cpsr.a = true
                sregs.cpsr.ITSTATE = 0b00000000
                sregs.cpsr.j = false
                sregs.cpsr.t = vmsa.sctlr.te // TE=0: ARM, TE=1: Thumb
                sregs.cpsr.ENDIANSTATE = vmsa.sctlr.ee // EE=0: little-endian, EE=1: big-endian
                BranchTo(ExcVectorBase() + vect_offset, true)
            }
        }
    }

    fun switchBankedRegisters(from: ProcessorMode, to: ProcessorMode) {
        saveRegs(from)
        loadRegs(to)
    }

    inline fun saveRegs(mode: ProcessorMode) {
        when(mode) {
            ProcessorMode.fiq -> {
                banking[mode.ordinal].r8.value = regs.r8.value
                banking[mode.ordinal].r9.value = regs.r9.value
                banking[mode.ordinal].r10.value = regs.r10.value
                banking[mode.ordinal].r11.value = regs.r11.value
                banking[mode.ordinal].r12.value = regs.r12.value
            }
            else -> {
                banking[ProcessorMode.usr.ordinal].r8.value = regs.r8.value
                banking[ProcessorMode.usr.ordinal].r9.value = regs.r9.value
                banking[ProcessorMode.usr.ordinal].r10.value = regs.r10.value
                banking[ProcessorMode.usr.ordinal].r11.value = regs.r11.value
                banking[ProcessorMode.usr.ordinal].r12.value = regs.r12.value
            }
        }

        when(mode) {
            ProcessorMode.sys -> banking[ProcessorMode.usr.ordinal].sp.value = regs.sp.value
            else -> banking[mode.ordinal].sp.value = regs.sp.value
        }

        when(mode) {
            ProcessorMode.sys,
            ProcessorMode.hyp -> banking[ProcessorMode.usr.ordinal].lr.value = regs.lr.value
            else -> banking[mode.ordinal].lr.value = regs.lr.value
        }
        banking[mode.ordinal].spsr.value = sregs.spsr.value
    }

    inline fun loadRegs(mode: ProcessorMode) {
        when(mode) {
            ProcessorMode.fiq -> {
                regs.r8.value = banking[mode.ordinal].r8.value
                regs.r9.value = banking[mode.ordinal].r9.value
                regs.r10.value = banking[mode.ordinal].r10.value
                regs.r11.value = banking[mode.ordinal].r11.value
                regs.r12.value = banking[mode.ordinal].r12.value
            }
            else -> {
                regs.r8.value = banking[ProcessorMode.usr.ordinal].r8.value
                regs.r9.value = banking[ProcessorMode.usr.ordinal].r9.value
                regs.r10.value = banking[ProcessorMode.usr.ordinal].r10.value
                regs.r11.value = banking[ProcessorMode.usr.ordinal].r11.value
                regs.r12.value = banking[ProcessorMode.usr.ordinal].r12.value
            }
        }

        when(mode) {
            ProcessorMode.sys -> regs.sp.value = banking[ProcessorMode.usr.ordinal].sp.value
            else -> regs.sp.value = banking[mode.ordinal].sp.value
        }

        when(mode) {
            ProcessorMode.sys,
            ProcessorMode.hyp -> regs.lr.value = banking[ProcessorMode.usr.ordinal].lr.value
            else -> regs.lr.value = banking[mode.ordinal].lr.value
        }
        sregs.spsr.value = banking[mode.ordinal].spsr.value
    }

    override fun reset() {
        super.reset()
        regs.reset()
        sregs.reset()
        flags.reset()
        status.reset()
        spr.reset()
        vmsa.reset()
        ver.reset()
        ser.reset()
    }

    // See B1.9.10
    fun TakePhysicalIRQException() {
        // Determine return information. SPSR is to be the current CPSR, and LR is to be the
        // current PC minus 0 for Thumb or 4 for ARM, to change the PC offsets of 4 or 8
        // respectively from the address of the current instruction into the required address
        // of the instruction boundary at which the interrupt occurred plus 4. For this
        // purpose, the PC and CPSR are considered to have already moved on to their values
        // for the instruction following that boundary.
        val new_lr_value = (if (sregs.cpsr.t) pc else pc - 4) + 4 + 4
        val new_spsr_value = sregs.cpsr.value
        val vect_offset = 24

        // Determine whether IRQs are routed to Monitor mode.
        val route_to_monitor = haveSecurityExt && ser.scr.irq

        // Determine whether IRQs are routed to Hyp mode.
        val route_to_hyp = (haveVirtExt && haveSecurityExt && !ser.scr.irq && ver.hcr.imo && !IsSecure())
                || sregs.cpsr.m == 0b11010L

        if (route_to_monitor) {
            // Ensure Secure state if initially in Monitor ('10110') mode. This affects
            // the Banked versions of various registers accessed later in the code.
//            if CPSR.M == '10110' then SCR.NS = '0';
//            EnterMonitorMode(new_spsr_value, new_lr_value, vect_offset);
            TODO("Not implemented: route_to_monitor")
        } else if (route_to_hyp) {
//            HSR = bits(32) UNKNOWN;
//            preferred_exceptn_return = new_lr_value - 4;
//            EnterHypMode(new_spsr_value, preferred_exceptn_return, vect_offset);
            TODO("Not implemented: route_to_hyp")
        } else {
            // Handle in IRQ mode. Ensure Secure state if initially in Monitor mode. This
            // affects the Banked versions of various registers accessed later in the code.
            if (sregs.cpsr.m == 0b10110L) ser.scr.ns = false
            sregs.cpsr.m = 0b10010 // IRQ mode

            // Write return information to registers, and make further CPSR changes:
            // IRQs disabled, IT state reset, instruction set and endianness set to
            // SCTLR-configured values.
            sregs.spsr.value = new_spsr_value
            regs.lr.value = new_lr_value

            sregs.cpsr.i = true

            if (!haveSecurityExt || haveVirtExt || !ser.scr.ns || ser.scr.aw)
                sregs.cpsr.a = true
            sregs.cpsr.ITSTATE = 0b00000000
            sregs.cpsr.j = false
            sregs.cpsr.t = vmsa.sctlr.te // TE=0: ARM, TE=1: Thumb
            sregs.cpsr.ENDIANSTATE = vmsa.sctlr.ee // EE=0: little-endian, EE=1: big-endian

            // Branch to correct IRQ vector.
            if (vmsa.sctlr.ve) {
                // IMPLEMENTATION_DEFINED branch to an IRQ vector;
                TODO("IMPLEMENTATION_DEFINED")
            } else
                BranchTo(ExcVectorBase() + vect_offset, true)
        }
    }

    // TakePrefetchAbortException()
    // ============================
    fun TakePrefetchAbortException() {
        // Determine return information. SPSR is to be the current CPSR, and LR is to be the
        // current PC minus 0 for Thumb or 4 for ARM, to change the PC offsets of 4 or 8
        // respectively from the address of the current instruction into the required address
        // of the current instruction plus 4.
        val new_lr_value = (if (sregs.cpsr.t) pc else pc - 4) + 8
        val new_spsr_value = sregs.cpsr.value
        val vect_offset = 12
        val preferred_exceptn_return = new_lr_value - 4

        // Determine whether this is an external abort to be routed to Monitor mode.
        val route_to_monitor = haveSecurityExt && ser.scr.ea /*&& IsExternalAbort()*/

        // Check whether to take exception to Hyp mode
        // if in Hyp mode then stay in Hyp mode
        val take_to_hyp = haveVirtExt && haveSecurityExt && ser.scr.ns && sregs.cpsr.m == 0b11010L
        // otherwise, check whether to take to Hyp mode through Hyp Trap vector
        // TODO: It's too much
        val route_to_hyp = (haveVirtExt && haveSecurityExt && !IsSecure() /*&&
                (SecondStageAbort() || (CPSR.M != '11010' &&
                        (IsExternalAbort() && IsAsyncAbort() && HCR.AMO == '1') ||
                        (DebugException() && HDCR.TDE == '1')) ||
                        (CPSR.M == '10000' && HCR.TGE == '1' &&
                                (IsAlignmentFault() || (IsExternalAbort() && !IsAsyncAbort()))))*/)
        // if HCR.TGE == '1' and in a Non-secure PL1 mode, the effect is UNPREDICTABLE

        when {
            route_to_monitor ->  {
                // Ensure Secure state if initially in Monitor mode. This affects the Banked
                // versions of various registers accessed later in the code
                if (sregs.cpsr.m == 0b10110L) ser.scr.ns = false
//            EnterMonitorMode(new_spsr_value, new_lr_value, vect_offset)
            }
            take_to_hyp -> EnterHypMode(new_spsr_value, preferred_exceptn_return, vect_offset)
            route_to_hyp -> EnterHypMode(new_spsr_value, preferred_exceptn_return, 20)
            else -> {
                // Handle in Abort mode. Ensure Secure state if initially in Monitor mode. This
                // affects the Banked versions of various registers accessed later in the code
                if (haveSecurityExt && sregs.cpsr.m == 0b10110L)
                    ser.scr.ns = false

                sregs.cpsr.m = 0b10111 // Abort mode

                // Write return information to registers, and make further CPSR changes:
                // IRQs disabled, other interrupts disabled if appropriate,
                // IT state reset, instruction set and endianness set to SCTLR-configured values.
                sregs.spsr.value = new_spsr_value
                regs.lr.value = new_lr_value
                sregs.cpsr.i = true
                if (!haveSecurityExt || haveVirtExt || !ser.scr.ns || ser.scr.aw)
                    sregs.cpsr.a = true
                sregs.cpsr.ITSTATE = 0b00000000
                sregs.cpsr.j = false
                sregs.cpsr.t = vmsa.sctlr.te // TE=0: ARM, TE=1: Thumb
                sregs.cpsr.ENDIANSTATE = vmsa.sctlr.ee // EE=0: little-endian, EE=1: big-endian
                BranchTo(ExcVectorBase() + vect_offset, true)
            }
        }
    }

    inline fun BigEndian() = status.ENDIANSTATE

    val regs = GPRBank(this)

    val banking = ProcessorMode.values().map { RegistersBanking(it) }.toTypedArray()

    val sregs = PSRBank(this)

    inline val flags get() = sregs.apsr
    inline val status get() = sregs.cpsr

    val spr = SPRBank()

    val vmsa = VMSABank()
    val ver = VERBank() // Virtualization Extensions Registers
    val ser = SERBank() // Security Extensions Registers

    override fun stringify() = buildString {
        appendLine("ARM CPU:")
        appendLine(regs.stringify())
        appendLine(sregs.stringify())
        append(spr.stringify())
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        super.serialize(ctxt)

        val savedBanking = banking.map { it.serialize(ctxt) }

        return super.serialize(ctxt) + mapOf(
                "regs" to regs.serialize(ctxt),
                "banking" to savedBanking,
                "sregs" to sregs.serialize(ctxt),
                "spr" to spr.serialize(ctxt),
                "vmsa" to vmsa.serialize(ctxt),
                "ver" to ver.serialize(ctxt),
                "ser" to ser.serialize(ctxt)
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super.deserialize(ctxt, snapshot)

        loadValue(snapshot, "banking") { emptyList<Map<String, Any>>() }
                .forEachIndexed { i, data -> banking[i].deserialize(ctxt, data) }

        regs.deserialize(ctxt, loadValue(snapshot, "regs") { emptyMap<String, String>() })
        sregs.deserialize(ctxt, loadValue(snapshot, "sregs") { emptyMap<String, String>() })
        spr.deserialize(ctxt, loadValue(snapshot, "spr") { emptyMap<String, String>() })
        vmsa.deserialize(ctxt, loadValue(snapshot, "vmsa") { emptyMap<String, String>() })
        ver.deserialize(ctxt, loadValue(snapshot, "ver") { emptyMap<String, String>() })
        ser.deserialize(ctxt, loadValue(snapshot, "ser") { emptyMap<String, String>() })
    }
}