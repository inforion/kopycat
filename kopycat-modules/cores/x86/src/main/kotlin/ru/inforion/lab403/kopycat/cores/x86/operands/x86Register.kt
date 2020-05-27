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
package ru.inforion.lab403.kopycat.cores.x86.operands

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.common.extensions.clr
import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Access.ANY
import ru.inforion.lab403.kopycat.cores.base.operands.ARegister
import ru.inforion.lab403.kopycat.cores.x86.enums.CR0
import ru.inforion.lab403.kopycat.cores.x86.enums.Flags.*
import ru.inforion.lab403.kopycat.cores.x86.enums.Regtype
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import ru.inforion.lab403.kopycat.cores.x86.enums.CTRLR as eCTRLR
import ru.inforion.lab403.kopycat.cores.x86.enums.DBGR as eDBGR
import ru.inforion.lab403.kopycat.cores.x86.enums.FR as eFR
import ru.inforion.lab403.kopycat.cores.x86.enums.FWR as eFWR
import ru.inforion.lab403.kopycat.cores.x86.enums.SSR as eSSR


abstract class x86Register(
        dtyp: Datatype,
        reg: Int,
        val rtyp: Regtype = Regtype.GPR,
        access: AOperand.Access = ANY) :
        ARegister<x86Core>(reg, access, dtyp) {

    override fun toString(): String {
        val rtype = when (rtyp) {
            Regtype.GPR -> {
                val e = x86GPR.from(reg)
                when (dtyp) {
                    DWORD -> e.n32
                    WORD -> e.n16
                    BYTE -> e.n8
                    else -> throw GeneralException("Incorrect datatype")
                }
            }
            Regtype.DBGR -> eDBGR.from(reg).name
            Regtype.SSR -> eSSR.from(reg).name
            Regtype.FR -> eFR.from(reg).name
            Regtype.CTRLR -> eCTRLR.from(reg).name
            Regtype.FWR -> eFWR.from(reg).name
        }
        return rtype.toLowerCase()
    }

    companion object {
        fun gpr(dtyp: Datatype, id: Int): x86Register = when (dtyp) {
            BYTE -> when (id) {

                x86GPR.EAX.id -> GPRBL.al
                x86GPR.ECX.id -> GPRBL.cl
                x86GPR.EDX.id -> GPRBL.dl
                x86GPR.EBX.id -> GPRBL.bl

                x86GPR.ESP.id -> GPRBH.ah
                x86GPR.EBP.id -> GPRBH.ch
                x86GPR.ESI.id -> GPRBH.dh
                x86GPR.EDI.id -> GPRBH.bh

                else -> throw GeneralException("Unknown byte register id")
            }
            WORD -> when (id) {
                x86GPR.EAX.id -> GPRW.ax
                x86GPR.ECX.id -> GPRW.cx
                x86GPR.EDX.id -> GPRW.dx
                x86GPR.EBX.id -> GPRW.bx
                x86GPR.ESP.id -> GPRW.sp
                x86GPR.EBP.id -> GPRW.bp
                x86GPR.ESI.id -> GPRW.si
                x86GPR.EDI.id -> GPRW.di
                x86GPR.EIP.id -> GPRW.ip
                else -> throw GeneralException("Unknown word register id")
            }
            DWORD -> when (id) {
                x86GPR.EAX.id -> GPRDW.eax
                x86GPR.ECX.id -> GPRDW.ecx
                x86GPR.EDX.id -> GPRDW.edx
                x86GPR.EBX.id -> GPRDW.ebx
                x86GPR.ESP.id -> GPRDW.esp
                x86GPR.EBP.id -> GPRDW.ebp
                x86GPR.ESI.id -> GPRDW.esi
                x86GPR.EDI.id -> GPRDW.edi
                x86GPR.EIP.id -> GPRDW.eip
                x86GPR.NONE.id -> none
                else -> throw GeneralException("Unknown dword register id")
            }

            else -> throw GeneralException("Unknown datatype register id")
        }

        fun gpr(dtyp: Datatype, enum: x86GPR): x86Register = gpr(dtyp, enum.id)

        fun gpr8(id: Int): x86Register = gpr(BYTE, id)
        fun gpr16(id: Int): x86Register = gpr(WORD, id)
        fun gpr32(id: Int): x86Register = gpr(DWORD, id)

        fun dbg(id: Int): x86Register = when (id) {
            eDBGR.DR0.id -> x86Register.DBGR.dr0
            eDBGR.DR1.id -> x86Register.DBGR.dr1
            eDBGR.DR2.id -> x86Register.DBGR.dr2
            eDBGR.DR3.id -> x86Register.DBGR.dr3
            eDBGR.DR4.id -> x86Register.DBGR.dr4
            eDBGR.DR5.id -> x86Register.DBGR.dr5
            eDBGR.DR6.id -> x86Register.DBGR.dr6
            eDBGR.DR7.id -> x86Register.DBGR.dr7
            else -> throw GeneralException("Unknown dbg register id")
        }

        fun dbg(enum: x86GPR): x86Register = dbg(enum.id)

        fun creg(id: Int): x86Register = when (id) {
            eCTRLR.CR0.id -> x86Register.CTRLR.cr0
            eCTRLR.CR1.id -> x86Register.CTRLR.cr1
            eCTRLR.CR2.id -> x86Register.CTRLR.cr2
            eCTRLR.CR3.id -> x86Register.CTRLR.cr3
            eCTRLR.CR4.id -> x86Register.CTRLR.cr4
            else -> throw GeneralException("Unknown dbg register id")
        }

        fun creg(enum: x86GPR): x86Register = creg(enum.id)

        fun sreg(id: Int): x86Register = when (id) {
            eSSR.CS.id -> x86Register.SSR.cs
            eSSR.DS.id -> x86Register.SSR.ds
            eSSR.ES.id -> x86Register.SSR.es
            eSSR.SS.id -> x86Register.SSR.ss
            eSSR.FS.id -> x86Register.SSR.fs
            eSSR.GS.id -> x86Register.SSR.gs
            else -> throw GeneralException("Unknown ssr register id")
        }

        fun sreg(enum: x86GPR): x86Register = sreg(enum.id)
    }

    object none : x86Register(DWORD, x86GPR.NONE.id) {
        override fun value(core: x86Core, data: Long) = core.cpu.regs.writeIntern(reg, data)
        override fun value(core: x86Core): Long = core.cpu.regs.readIntern(reg)
    }

    sealed class GPRBL(dtyp: Datatype, id: Int) : x86Register(dtyp, id) {
        override fun value(core: x86Core, data: Long) = core.cpu.regs.writeIntern(reg, data, 7..0)
        override fun value(core: x86Core): Long = core.cpu.regs.readIntern(reg, 7..0)

        object al : GPRBL(BYTE, x86GPR.EAX.id)
        object cl : GPRBL(BYTE, x86GPR.ECX.id)
        object dl : GPRBL(BYTE, x86GPR.EDX.id)
        object bl : GPRBL(BYTE, x86GPR.EBX.id)
    }

    sealed class GPRBH(dtyp: Datatype, id: Int) : x86Register(dtyp, id) {
        override fun value(core: x86Core, data: Long) = core.cpu.regs.writeIntern(reg - 4, data, 15..8)
        override fun value(core: x86Core): Long = core.cpu.regs.readIntern(reg - 4, 15..8)

        object ah : GPRBH(BYTE, x86GPR.ESP.id)
        object ch : GPRBH(BYTE, x86GPR.EBP.id)
        object dh : GPRBH(BYTE, x86GPR.ESI.id)
        object bh : GPRBH(BYTE, x86GPR.EDI.id)

    }

    sealed class GPRW(id: Int) : x86Register(WORD, id) {
        override fun value(core: x86Core, data: Long) = core.cpu.regs.writeIntern(reg, data, 15..0)
        override fun value(core: x86Core): Long = core.cpu.regs.readIntern(reg, 15..0)

        object ax : GPRW(x86GPR.EAX.id)
        object cx : GPRW(x86GPR.ECX.id)
        object dx : GPRW(x86GPR.EDX.id)
        object bx : GPRW(x86GPR.EBX.id)
        object sp : GPRW(x86GPR.ESP.id)
        object bp : GPRW(x86GPR.EBP.id)
        object si : GPRW(x86GPR.ESI.id)
        object di : GPRW(x86GPR.EDI.id)

        object ip : GPRW(x86GPR.EIP.id)
    }

    sealed class GPRDW(id: Int) : x86Register(DWORD, id) {
        override fun value(core: x86Core, data: Long) = core.cpu.regs.writeIntern(reg, data)
        override fun value(core: x86Core): Long = core.cpu.regs.readIntern(reg)

        object eax : GPRDW(x86GPR.EAX.id)
        object ecx : GPRDW(x86GPR.ECX.id)
        object edx : GPRDW(x86GPR.EDX.id)
        object ebx : GPRDW(x86GPR.EBX.id)
        object esp : GPRDW(x86GPR.ESP.id)
        object ebp : GPRDW(x86GPR.EBP.id)
        object esi : GPRDW(x86GPR.ESI.id)
        object edi : GPRDW(x86GPR.EDI.id)

        object eip : GPRDW(x86GPR.EIP.id)
    }

    sealed class SSR(id: Int) : x86Register(WORD, id, Regtype.SSR) {
        override fun value(core: x86Core, data: Long) {
            core.cpu.sregs.writeIntern(reg, data)
            core.mmu.updateCache(this)
        }
        override fun value(core: x86Core): Long = core.cpu.sregs.readIntern(reg)

        object cs: SSR(eSSR.CS.id) {
            fun cpl(core: x86Core): Int = bits(core, 1..0).asInt
        }
        object ds: SSR(eSSR.DS.id)
        object ss: SSR(eSSR.SS.id)
        object es: SSR(eSSR.ES.id)
        object fs: SSR(eSSR.FS.id)
        object gs: SSR(eSSR.GS.id)
    }

    sealed class FWR(id: Int) : x86Register(WORD, id, Regtype.FWR) {
        override fun value(core: x86Core, data: Long) = core.fpu.fwr.writeIntern(reg, data)
        override fun value(core: x86Core): Long = core.fpu.fwr.readIntern(reg)

        object SWR : FWR(eFWR.SWR.id)
        object CWR : FWR(eFWR.CWR.id)
        object TWR : FWR(eFWR.TWR.id)
        object FDP : FWR(eFWR.DPR.id)
        object FIP : FWR(eFWR.IPR.id)
        object LIO : FWR(eFWR.LIO.id)
    }

    /**
     * Note: Don't inherit from this class use singleton flags or eflags
     */
    abstract class AFlags(dtyp: Datatype) : x86Register(dtyp, eFR.EFLAGS.id, Regtype.FR) {
        private fun flagProcess(core: x86Core, data: Long): Long {
            var tmp = data
            if (core.generation < x86Core.Generation.i286)
                tmp = tmp clr IOPLH.bit..IOPLL.bit
            if (core.generation < x86Core.Generation.i386) {
                tmp = tmp clr RF.bit
                tmp = tmp clr VM.bit
            }
            if (core.generation < x86Core.Generation.i486)
                tmp = tmp clr AC.bit
            if (core.generation < x86Core.Generation.Pentium) {
//                TODO: Solve this mess flags!
//                tmp = tmp clr VIF.bit
//                tmp = tmp clr VIP.bit
                tmp = tmp clr ID.bit
            }
            return tmp
        }

        override fun value(core: x86Core, data: Long) {
            val tmp = flagProcess(core, data)
            core.cpu.flags.writeIntern(0, tmp, dtyp.bits-1..0)
        }

        override fun value(core: x86Core): Long {
            val tmp = core.cpu.flags.readIntern(0, dtyp.bits-1..0)
            return flagProcess(core, tmp)
        }
    }

    object flags: AFlags(WORD)

    object eflags: AFlags(DWORD) {
        fun cf(core: x86Core): Boolean = bit(core, CF.bit) == 1
        fun cf(core: x86Core, value: Boolean) = bit(core, CF.bit, value.asInt)

        fun pf(core: x86Core): Boolean = bit(core, PF.bit) == 1
        fun pf(core: x86Core, value: Boolean) = bit(core, PF.bit, value.asInt)

        fun af(core: x86Core): Boolean = bit(core, AF.bit) == 1
        fun af(core: x86Core, value: Boolean) = bit(core, AF.bit, value.asInt)

        fun zf(core: x86Core): Boolean = bit(core, ZF.bit) == 1
        fun zf(core: x86Core, value: Boolean) = bit(core, ZF.bit, value.asInt)

        fun sf(core: x86Core): Boolean = bit(core, SF.bit) == 1
        fun sf(core: x86Core, value: Boolean) = bit(core, SF.bit, value.asInt)

        fun tf(core: x86Core): Boolean = bit(core, TF.bit) == 1
        fun tf(core: x86Core, value: Boolean) = bit(core, TF.bit, value.asInt)

        fun df(core: x86Core): Boolean = bit(core, DF.bit) == 1
        fun df(core: x86Core, value: Boolean) = bit(core, DF.bit, value.asInt)

        fun of(core: x86Core): Boolean = bit(core, OF.bit) == 1
        fun of(core: x86Core, value: Boolean) = bit(core, OF.bit, value.asInt)

        fun nt(core: x86Core): Boolean = bit(core, NT.bit) == 1
        fun nt(core: x86Core, value: Boolean) = bit(core, NT.bit, value.asInt)

        fun ifq(core: x86Core): Boolean = bit(core, IF.bit) == 1
        fun ifq(core: x86Core, value: Boolean) = bit(core, IF.bit, value.asInt)

        fun iopl(core: x86Core): Int = bits(core, IOPLH.bit..IOPLL.bit).toInt()
        fun iopl(core: x86Core, value: Int) = bits(core, IOPLH.bit..IOPLL.bit, value.asULong)

        fun vif(core: x86Core): Boolean = bit(core, VIF.bit) == 1
        fun vif(core: x86Core, value: Boolean) = bit(core, VIF.bit, value.asInt)

        fun vm(core: x86Core): Boolean = bit(core, VM.bit) == 1
        fun vm(core: x86Core, value: Boolean) = bit(core, VM.bit, value.asInt)

        fun vip(core: x86Core): Boolean = bit(core, VIP.bit) == 1
        fun vip(core: x86Core, value: Boolean) = bit(core, VIP.bit, value.asInt)

        fun ac(core: x86Core): Boolean = bit(core, AC.bit) == 1
        fun ac(core: x86Core, value: Boolean) = bit(core, AC.bit, value.asInt)

        fun id(core: x86Core): Boolean = bit(core, ID.bit) == 1
        fun id(core: x86Core, value: Boolean) = bit(core, ID.bit, value.asInt)

        fun rf(core: x86Core): Boolean = bit(core, RF.bit) == 1
        fun rf(core: x86Core, value: Boolean) = bit(core, RF.bit, value.asInt)
    }

    sealed class CTRLR(id: Int) : x86Register(DWORD, id, Regtype.CTRLR) {
        override fun value(core: x86Core, data: Long) = core.cpu.cregs.writeIntern(reg, data)
        override fun value(core: x86Core): Long = core.cpu.cregs.readIntern(reg)

        object cr0 : CTRLR(eCTRLR.CR0.id) {
            fun pe(core: x86Core): Boolean = cr0.bit(core, CR0.PE.bit) == 1
            fun pe(core: x86Core, value: Boolean) = cr0.bit(core, CR0.PE.bit, value.asInt)
        }

        object cr1 : CTRLR(eCTRLR.CR1.id)
        object cr2 : CTRLR(eCTRLR.CR2.id)
        object cr3 : CTRLR(eCTRLR.CR3.id) {
            override fun value(core: x86Core, data: Long) {
                log.fine { "[${core.pc.hex}] CR3 register changed to ${data.hex} -> paging cache invalidated!" }
                core.cpu.cregs.writeIntern(reg, data)
                core.mmu.invalidatePagingCache()
            }
        }
        object cr4 : CTRLR(eCTRLR.CR4.id)
    }

    sealed class DBGR(id: Int) : x86Register(DWORD, id, Regtype.DBGR) {
        override fun value(core: x86Core, data: Long) = core.cpu.dregs.writeIntern(reg, data)
        override fun value(core: x86Core): Long = core.cpu.dregs.readIntern(reg)

        object dr0 : DBGR(eDBGR.DR0.id)
        object dr1 : DBGR(eDBGR.DR1.id)
        object dr2 : DBGR(eDBGR.DR2.id)
        object dr3 : DBGR(eDBGR.DR3.id)
        object dr4 : DBGR(eDBGR.DR4.id)
        object dr5 : DBGR(eDBGR.DR5.id)
        object dr6 : DBGR(eDBGR.DR6.id)
        object dr7 : DBGR(eDBGR.DR7.id)
    }
}