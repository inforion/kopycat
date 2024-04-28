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
package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.branch

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.extensions.uint
import ru.inforion.lab403.common.extensions.untruth
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.x86.enums.Flags
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.exceptions.x86HardwareException
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86CPU
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.x86utils
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class IRet(core: x86Core, opcode: ByteArray, prefs: Prefixes):
        AX86Instruction(core, Type.IRET, opcode, prefs) {
    override val mnem = "iret"

    private fun realAddressingMode() {
        val ip = core.cpu.regs.gpr(x86GPR.RIP, prefs.opsize)
        val cs = core.cpu.sregs.cs

        ip.value = x86utils.pop(core, prefs.opsize, prefs)
        cs.value = x86utils.pop(core, prefs.opsize, prefs) and 0xFFFFu

        if (!prefs.is16BitOperandMode) {
            val tmpFlags = x86utils.pop(core, DWORD, prefs)
            val flags = core.cpu.flags.eflags

            flags.value = (tmpFlags and 0x257FD5u) or (flags.value and 0x1A0000u)
        } else {
            val tmpFlags = x86utils.pop(core, WORD, prefs)
            val flags = core.cpu.flags.flags

            flags.value = tmpFlags
        }
    }

    private fun returnFromVirtual8086Mode(): Unit = TODO("RETURN-FROM-VIRTUAL-8086-MODE")

    private fun protectedMode() {
        val nt = core.cpu.flags.nt

        if (nt) taskReturn()

        val cpl = core.cpu.sregs.cs.cpl.uint
        val tmpip = x86utils.pop(core, prefs.opsize, prefs)
        val tmpcs = x86utils.pop(core, prefs.opsize, prefs)

        val flagsSize = prefs.opsize // if (!prefs.is16BitOperandMode) DWORD else WORD
        val tmpFlags = x86utils.pop(core, flagsSize, prefs)

        val ip = core.cpu.regs.gpr(x86GPR.RIP, prefs.opsize)
        ip.value = tmpip
        core.cpu.sregs.cs.value = tmpcs

        val rpl = core.cpu.sregs.cs.cpl.uint  // requested privileged level from cs register

//        log.fine { "CPL = $cpl RPL = $rpl" }

        if (tmpFlags[Flags.VM.bit] == 1uL && core.isRing0)
            returnToVirtual8086Mode()
        else
            protectedModeReturn(cpl, rpl, tmpFlags)
    }

    private fun taskReturn(): Unit = TODO("TASK-RETURN; (* PE = 1, VM = 0, NT = 1 *)")

    private fun returnToVirtual8086Mode(): Unit = TODO("RETURN-TO-VIRTUAL-8086-MODE")

    private fun protectedModeReturn(cpl: UInt, rpl: UInt, tmpFlags: ULong): Unit = if (rpl > cpl)
        returnToOuterPrivilegeLevel(cpl, tmpFlags)
    else
        returnToSamePrivilegeLevel(cpl, tmpFlags)

    private fun returnToOuterPrivilegeLevel(cpl: UInt, tmpFlags: ULong) {
//        log.fine { "returnToOuterPrivilegeLevel" }
        val sp = core.cpu.regs.gpr(x86GPR.RSP, prefs.opsize)
        val tmpsp = x86utils.pop(core, prefs.opsize, prefs)
        val tmpss = x86utils.pop(core, prefs.opsize, prefs)
        sp.value = tmpsp
        core.cpu.sregs.ss.value = tmpss

//        IF new mode ≠ 64-Bit Mode
//        THEN
//        IF EIP is not within CS limit
//        THEN #GP(0); FI;
//        ELSE (* new mode = 64-bit mode *)
//        IF RIP is non-canonical
//        THEN #GP(0); FI;
//        FI;

        val prevIF = core.cpu.flags.ifq
        val prevIOPL = core.cpu.flags.iopl

        if (!prefs.is16BitOperandMode) {
            // IF OperandSize = 32 THEN EFLAGS(VM, VIF, VIP) ← tempEFLAGS; FI;
            core.cpu.flags.eflags.value = tmpFlags
        } else {
            core.cpu.flags.flags.value = tmpFlags
        }

        // IF CPL ≤ IOPL THEN EFLAGS(IF) ← tempEFLAGS; FI;
        if (cpl > prevIOPL) core.cpu.flags.ifq = prevIF
        // IF CPL = 0 THEN EFLAGS(IOPL) ← tempEFLAGS;
        if (cpl != 0u) core.cpu.flags.iopl = prevIOPL

        // CPL ← CS(RPL);

        /*
        IF OperandSize = 64 THEN EFLAGS(VIF, VIP) ← tempEFLAGS; FI;

        FOR each SegReg in (ES, FS, GS, and DS)
        DO
        tempDesc ← descriptor cache for SegReg (* hidden part of segment register *)
        IF tempDesc(DPL) < CPL AND tempDesc(Type) is data or non-conforming code
        THEN (* Segment register invalid *)
        SegReg ← NULL;
        FI;
        OD;
         */
    }

    private fun returnToSamePrivilegeLevel(cpl: UInt, tmpFlags: ULong) {
//        log.fine { "returnToSamePrivilegeLevel" }
//        IF new mode ≠ 64-Bit Mode
//        THEN
//        IF EIP is not within CS limit
//        THEN #GP(0); FI;
//        ELSE (* new mode = 64-bit mode *)
//        IF RIP is non-canonical
//        THEN #GP(0); FI;
//        FI;

        val prevIF = core.cpu.flags.ifq
        val prevIOPL = core.cpu.flags.iopl
        val prevVIF = core.cpu.flags.vif
        val prevVIP = core.cpu.flags.vip

//        log.fine { "IF = $prevIF IOPL = $prevIOPL VIF = $prevVIF VIP = $prevVIP" }

        if (!prefs.is16BitOperandMode) {
            core.cpu.flags.eflags.value = tmpFlags
        } else {
            core.cpu.flags.flags.value = tmpFlags
        }

        // IF CPL ≤ IOPL THEN EFLAGS(IF) ← tempEFLAGS; FI;
        if (cpl > prevIOPL) core.cpu.flags.ifq = prevIF
        // IF CPL = 0 THEN (* VM = 0 in flags image *)
        if (cpl != 0u) {
            // EFLAGS(IOPL) ← tempEFLAGS;
            core.cpu.flags.iopl = prevIOPL
            // IF OperandSize = 32 or OperandSize = 64  THEN EFLAGS(VIF, VIP) ← tempEFLAGS; FI;
            if (!prefs.is16BitOperandMode) {
                core.cpu.flags.vif = prevVIF
                core.cpu.flags.vip = prevVIP
            }
        }
    }

    private fun ia32Mode() {
        // IF NT = 1
        if (core.cpu.flags.nt) {
            throw x86HardwareException.GeneralProtectionFault(core.pc, 0uL)
        }

        val cpl = core.cpu.sregs.cs.cpl.uint
        val tmpip = x86utils.pop(core, prefs.opsize, prefs)
        val tmpcs = x86utils.pop(core, prefs.opsize, prefs)
        val tmpFlags = x86utils.pop(core, prefs.opsize, prefs)

        val ip = core.cpu.regs.gpr(x86GPR.RIP, prefs.opsize)
        ip.value = tmpip
        core.cpu.sregs.cs.value = tmpcs

        val rpl = core.cpu.sregs.cs.cpl.uint  // requested privileged level from cs register

        // IF CS.RPL > CPL
        if (rpl > cpl) {
            returnToOuterPrivilegeLevel(cpl, tmpFlags)
        } else {
            if (core.cpu.mode == x86CPU.Mode.R64) {
                val rsp = x86utils.pop(core, prefs.opsize, prefs)
                val ss = x86utils.pop(core, prefs.opsize, prefs)[15..0]

                core.cpu.regs.gpr(x86GPR.RSP, QWORD).value = rsp
                core.cpu.sregs.ss.value = ss
            }
            returnToSamePrivilegeLevel(cpl, tmpFlags)
        }
    }

    override fun execute() {
        val pe = core.cpu.cregs.cr0.pe
        val vm = core.cpu.flags.eflags.vm

        if (!pe) realAddressingMode()
        else if (core.cpu.x86.config.efer[x86CPU.LME].untruth /* docs: IA32_EFER.LMA = 0 */) {
            if (vm) returnFromVirtual8086Mode()
            else protectedMode()
        } else {
            ia32Mode()
        }

        log.finest {
            val prefs = Prefixes(core)
            val ip = core.cpu.regs.gpr(x86GPR.RIP, prefs.opsize)
            val sp = core.cpu.regs.gpr(x86GPR.RSP, prefs.opsize)

            val cpl = core.cpu.sregs.cs.cpl
            val saved_eflags = core.cpu.flags.eflags.value
            val saved_cs = core.cpu.sregs.cs.value
            val saved_ip = ip.value
            val saved_ss = core.cpu.sregs.ss.value
            val saved_sp = sp.value

            "<-- RET cpl=$cpl ip=${saved_cs.hex}:${saved_ip.hex} sp=${saved_ss.hex}:${saved_sp.hex} flags=${saved_eflags.hex}"
        }
    }
}