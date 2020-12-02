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
package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.branch

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.cores.x86.enums.Flags
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.CTRLR.cr0
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.SSR.cs
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.SSR.ss
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.eflags
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.flags
import ru.inforion.lab403.kopycat.cores.x86.x86utils
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class IRet(core: x86Core, opcode: ByteArray, prefs: Prefixes):
        AX86Instruction(core, Type.IRET, opcode, prefs) {
    override val mnem = "iret"

    private fun realAddressingMode(): Unit = TODO("REAL-ADDRESS-MODE")

    private fun returnFromVirtual8086Mode(): Unit = TODO("RETURN-FROM-VIRTUAL-8086-MODE")

    private fun protectedMode() {
        val nt = eflags.nt(core)

        if (nt) taskReturn()

        val cpl = cs.cpl(core)
        val tmpip = x86utils.pop(core, prefs.opsize, prefs)
        val tmpcs = x86utils.pop(core, prefs.opsize, prefs)

        val flagsSize = if (!prefs.is16BitOperandMode) DWORD else WORD
        val tmpFlags = x86utils.pop(core, flagsSize, prefs)

        val ip = x86Register.gpr(prefs.opsize, x86GPR.EIP)
        ip.value(core, tmpip)
        cs.value(core, tmpcs)

        val rpl = cs.cpl(core)  // requested privileged level from cs register

//        log.fine { "CPL = $cpl RPL = $rpl" }

        if (tmpFlags[Flags.VM.bit] == 1L && cpl == 0)
            returnToVirtual8086Mode()
        else
            protectedModeReturn(cpl, rpl, tmpFlags)
    }

    private fun taskReturn(): Unit = TODO("TASK-RETURN; (* PE = 1, VM = 0, NT = 1 *)")

    private fun returnToVirtual8086Mode(): Unit = TODO("RETURN-TO-VIRTUAL-8086-MODE")

    private fun protectedModeReturn(cpl: Int, rpl: Int, tmpFlags: Long): Unit = if (rpl > cpl)
        returnToOuterPrivilegeLevel(cpl, tmpFlags)
    else
        returnToSamePrivilegeLevel(cpl, tmpFlags)

    private fun returnToOuterPrivilegeLevel(cpl: Int, tmpFlags: Long) {
//        log.fine { "returnToOuterPrivilegeLevel" }
        val sp = x86Register.gpr(prefs.opsize, x86GPR.ESP)
        val tmpsp = x86utils.pop(core, prefs.opsize, prefs)
        val tmpss = x86utils.pop(core, prefs.opsize, prefs)
        sp.value(core, tmpsp)
        ss.value(core, tmpss)

//        IF new mode ≠ 64-Bit Mode
//        THEN
//        IF EIP is not within CS limit
//        THEN #GP(0); FI;
//        ELSE (* new mode = 64-bit mode *)
//        IF RIP is non-canonical
//        THEN #GP(0); FI;
//        FI;

        val prevIF = eflags.ifq(core)
        val prevIOPL = eflags.iopl(core)

        if (!prefs.is16BitOperandMode) {
            // IF OperandSize = 32 THEN EFLAGS(VM, VIF, VIP) ← tempEFLAGS; FI;
            eflags.value(core, tmpFlags)
        } else {
            flags.value(core, tmpFlags)
        }

        // IF CPL ≤ IOPL THEN EFLAGS(IF) ← tempEFLAGS; FI;
        if (cpl > prevIOPL) eflags.ifq(core, prevIF)
        // IF CPL = 0 THEN EFLAGS(IOPL) ← tempEFLAGS;
        if (cpl != 0) eflags.iopl(core, prevIOPL)

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

    private fun returnToSamePrivilegeLevel(cpl: Int, tmpFlags: Long) {
//        log.fine { "returnToSamePrivilegeLevel" }
//        IF new mode ≠ 64-Bit Mode
//        THEN
//        IF EIP is not within CS limit
//        THEN #GP(0); FI;
//        ELSE (* new mode = 64-bit mode *)
//        IF RIP is non-canonical
//        THEN #GP(0); FI;
//        FI;

        val prevIF = eflags.ifq(core)
        val prevIOPL = eflags.iopl(core)
        val prevVIF = eflags.vif(core)
        val prevVIP = eflags.vip(core)

//        log.fine { "IF = $prevIF IOPL = $prevIOPL VIF = $prevVIF VIP = $prevVIP" }

        if (!prefs.is16BitOperandMode) {
            eflags.value(core, tmpFlags)
        } else {
            flags.value(core, tmpFlags)
        }

        // IF CPL ≤ IOPL THEN EFLAGS(IF) ← tempEFLAGS; FI;
        if (cpl > prevIOPL) eflags.ifq(core, prevIF)
        // IF CPL = 0 THEN (* VM = 0 in flags image *)
        if (cpl != 0) {
            // EFLAGS(IOPL) ← tempEFLAGS;
            eflags.iopl(core, prevIOPL)
            // IF OperandSize = 32 or OperandSize = 64  THEN EFLAGS(VIF, VIP) ← tempEFLAGS; FI;
            if (!prefs.is16BitOperandMode) {
                eflags.vif(core, prevVIF)
                eflags.vip(core, prevVIP)
            }
        }
    }

    override fun execute() {
        val pe = cr0.pe(core)
        val vm = eflags.vm(core)

        if (!pe) realAddressingMode()
        else {
            if (vm) returnFromVirtual8086Mode()
            else protectedMode()
        }

        log.finest {
            val prefs = Prefixes(core)
            val ip = x86Register.gpr(prefs.opsize, x86GPR.EIP)
            val sp = x86Register.gpr(prefs.opsize, x86GPR.ESP)

            val cpl = cs.cpl(core)
            val saved_eflags = eflags.value(core)
            val saved_cs = cs.value(core)
            val saved_ip = ip.value(core)
            val saved_ss = ss.value(core)
            val saved_sp = sp.value(core)

            "<-- RET cpl=$cpl ip=${saved_cs.hex}:${saved_ip.hex} sp=${saved_ss.hex}:${saved_sp.hex} flags=${saved_eflags.hex}"
        }
    }
}