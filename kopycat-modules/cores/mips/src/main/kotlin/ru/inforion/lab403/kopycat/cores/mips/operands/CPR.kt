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
package ru.inforion.lab403.kopycat.cores.mips.operands

import ru.inforion.lab403.common.extensions.first
import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.common.extensions.random
import ru.inforion.lab403.kopycat.cores.mips.enums.Designation
import ru.inforion.lab403.kopycat.cores.mips.enums.eCPR
import ru.inforion.lab403.kopycat.cores.mips.hardware.processors.ProcType
import ru.inforion.lab403.kopycat.modules.cores.MipsCore
import ru.inforion.lab403.kopycat.cores.mips.enums.Cause as CauseBits
import ru.inforion.lab403.kopycat.cores.mips.enums.IntCtl as IntCtlBits

// COP0 GPR
open class CPR(desc: eCPR) : MipsRegister<eCPR>(ProcType.SystemControlCop, Designation.General, desc) {
    constructor(reg: Int, sel: Int) : this(first { it.id == reg && it.sel == sel })

    // NOTE: for performance sake it must be final!
    final override fun value(core: MipsCore, data: Long) {
        fun insertFields(vararg ranges: IntRange) {
            val oldVal = core.cop.regs.readIntern(reg)
            val modVal = ranges.fold(oldVal) { old, range -> old.insert(data, range) }
            if (oldVal != modVal)
                log.warning { "[${core.cpu.pc.hex8}] $this register modified ${oldVal.hex8} -> ${modVal.hex8}" }
            core.cop.regs.writeIntern(reg, modVal)
        }

        return when (reg) {
            Random.reg,
            Config1.reg,
            Config2.reg,
            Config3.reg -> log.warning { "[${core.cpu.pc.hex8}] Store data to $$reg = ${data.hex8} -> ignored" }

            Compare.reg -> {
                core.cop.clearCountCompareTimerBits()
                core.cop.regs.writeIntern(reg, data)
            }

            Config0.reg -> {
                val K23 = 30..28
                val KU = 27..25
                val K0 = 2..0
                insertFields(K23, KU, K0)
            }

            IntCtl.reg -> {
                insertFields(IntCtlBits.VS.range)
            }

            else -> core.cop.regs.writeIntern(reg, data)
        }
    }

    final override fun value(core: MipsCore): Long = when (reg) {
        Random.reg -> {
            val lowerRandomBound = Wired.value(core)
            val upperRandomBound = (core.mmu.tlbEntries - 1).toLong()
            random.randuint(lowerRandomBound..upperRandomBound)
        }
        else -> core.cop.regs.readIntern(reg)
    }

    object Index : CPR(eCPR.INDEX)
    object Config3 : CPR(eCPR.CONFIG3)
    object Cause : CPR(eCPR.CAUSE)
    object Context : CPR(eCPR.CONTEXT)
    object UserLocal : CPR(eCPR.USER_LOCAL)
    object Compare : CPR(eCPR.COMPARE)
    object PageMask : CPR(eCPR.PAGEMASK)
    object EntryHi : CPR(eCPR.ENTRYHI)
    object EntryLo0 : CPR(eCPR.ENTRYLO0)
    object EntryLo1 : CPR(eCPR.ENTRYLO1)

    object Count : CPR(eCPR.COUNTR)

    object EPC : CPR(eCPR.EPC)
    object ErrorEPC : CPR(eCPR.ERROREPC)
    object PRId : CPR(eCPR.PRID)

    object Random : CPR(eCPR.RANDOM)

    object Wired : CPR(eCPR.WIRED)
    object HWREna : CPR(eCPR.HWRENA)
    object Status : CPR(eCPR.STATUS)
    object BadVAddr : CPR(eCPR.BADVADDR)
    object IntCtl : CPR(eCPR.IMPLSPEC0)
    object SRSCtl : CPR(eCPR.IMPLPIC32)
    object SRSMap : CPR(eCPR.IMPLSPEC1)
    object EBase : CPR(eCPR.IMPLSPEC2)
    object ImplSpec3 : CPR(eCPR.IMPLSPEC3)

    object Config0 : CPR(eCPR.CONFIG0)
    object Config1 : CPR(eCPR.CONFIG1)
    object Config2 : CPR(eCPR.CONFIG2)

    object WatchLo0 : CPR(eCPR.WATCHLO0)
    object WatchHi0 : CPR(eCPR.WATCHHI0)
    object WatchLo1 : CPR(eCPR.WATCHLO1)
    object WatchHi1 : CPR(eCPR.WATCHHI1)
    object WatchLo2 : CPR(eCPR.WATCHLO2)
    object WatchHi2 : CPR(eCPR.WATCHHI2)
    object WatchLo3 : CPR(eCPR.WATCHLO3)
    object WatchHi3 : CPR(eCPR.WATCHHI3)

    object LlAddr : CPR(eCPR.LLADR)

    object XContext : CPR(eCPR.XCONTEXT)
    object Debug : CPR(eCPR.DEBUG)
    object DEPC0 : CPR(eCPR.DEPC0)
    object DEPC6 : CPR(eCPR.DEPC6)
    object PerfCnt : CPR(eCPR.PERFCNT)
    object ErrCnt : CPR(eCPR.ERRCNT)
    object CacheErr0 : CPR(eCPR.CACHEERR0)
    object CacheErr1 : CPR(eCPR.CACHEERR1)
    object CacheErr2 : CPR(eCPR.CACHEERR2)
    object CacheErr3 : CPR(eCPR.CACHEERR3)
    object TagLo0 : CPR(eCPR.TAGLO0)
    object TagLo2 : CPR(eCPR.TAGLO2)
    object TagLo4 : CPR(eCPR.TAGLO4)
    object DataLo1 : CPR(eCPR.DATALO1)
    object DataLo3 : CPR(eCPR.DATALO3)
    object TagHi0 : CPR(eCPR.TAGHI0)
    object TagHi2 : CPR(eCPR.TAGHI2)
    object TagHi4 : CPR(eCPR.TAGHI4)
    object DataHi1 : CPR(eCPR.DATAHI1)
    object DataHi3 : CPR(eCPR.DATAHI3)
    object DESAVE : CPR(eCPR.DESAVE)
}