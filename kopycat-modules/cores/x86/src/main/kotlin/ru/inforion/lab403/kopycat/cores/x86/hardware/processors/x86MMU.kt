/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBankNG
import ru.inforion.lab403.kopycat.cores.base.common.AddressTranslator
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction.*
import ru.inforion.lab403.kopycat.cores.base.exceptions.CrossPageAccessException
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.enums.SSR
import ru.inforion.lab403.kopycat.cores.x86.exceptions.x86HardwareException
import ru.inforion.lab403.kopycat.cores.x86.hardware.registers.SSRBank
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import ru.inforion.lab403.kopycat.interfaces.IConstructorSerializable
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 *
 * Stub for x86 address translation
 */
class x86MMU(core: x86Core, name: String) : AddressTranslator(core, name), IAutoSerializable {
    companion object {
        @Transient val log = logger(FINE)
        @DontAutoSerialize
        val INVALID_GDT_ENTRY = SegmentDescriptor32(-1uL)
        const val NXE = 11 // Long Mode Enable
        private val SSR_NULL_CHECK = setOf(SSR.CS.id, SSR.SS.id)
    }

    val x86 = core

    // Shadow registers/hidden part of segment registers
    // "3.4.3 Segment Registers" of Volume 3A, page 3-8
    var cs: SegmentDescriptor32 = INVALID_GDT_ENTRY
    var ss: SegmentDescriptor32 = INVALID_GDT_ENTRY

    data class DescriptorRegister(var limit: ULong = 0u, var base: ULong = 0u): IConstructorSerializable, IAutoSerializable {
        override fun toString() = "[base=0x${base.hex} limit=0x${limit.hex}]"
    }

    // Load and store from/to physical memory address
    private fun physicalRead(address: ULong, size: Int) = x86.mmu.ports.outp.read(address, 0, size)
    private fun physicalWrite(address: ULong, size: Int, value: ULong) = x86.mmu.ports.outp.write(address, 0, size, value)

    // Load and store from/to linear memory address
    fun linearRead(address: ULong, size: Int, privilege: Boolean = false) =
            physicalRead(PMLinear2Physical(address, size, LOAD, privilege), size)
    fun linearWrite(address: ULong, size: Int, value: ULong, privilege: Boolean = false) =
            physicalWrite(PMLinear2Physical(address, size, STORE, privilege), size, value)

    interface SegmentDescriptor: IConstructorSerializable, IAutoSerializable {
        val base: ULong
        val limit: ULong

        val isPresent: Boolean
        val isForAnAvailableTSS get() = true // why not? Internet nothing told how to check...
    }


    data class SegmentDescriptor32(val data: ULong): SegmentDescriptor {
        val data0 by lazy { data[31..0] }
        val data1 by lazy { data[63..32] }
        // -------------------------------------------------------------------------------------------------------------
        val limitLow get() = data0[15..0]
        val baseLow get() = data0[31..16]
        // -------------------------------------------------------------------------------------------------------------
        val baseMiddle get() = data1[7..0]
        val type get() = data[11..8]
        val a get() = data1[8].truth   // Accessed bit. Just set to 0. The CPU sets this to 1 when the segment is accessed.
        val r get() = data1[9].truth   // Readable bit/Writable bit.
        val c get() = data1[10].truth  // Direction bit/Conforming bit.
        val e get() = data1[11].truth  // Executable bit. If 1 code in this segment can be executed, ie. a code selector. If 0 it is a data selector.
        val s get() = data1[12].truth  // Descriptot type bit. Specifies whether the segment descriptor is for a system segment (S flag is clear) or a code or data segment (S flag is set).
        val dpl get() = data1[14..13]     // Privilege, 2 bits. Contains the ring level, 0 = highest (kernel), 3 = lowest (user applications).
        val p get() = data1[15].truth  // Present bit. This must be 1 for all valid selectors.
        val limitHigh get() = data1[19..16]
        val avl get() = data1[20].truth  // Always = 0
        val l get() = data1[21].truth    // IA-32 mode
        val d get() = data1[22].truth    // Size bit. If 0 the selector defines 16 bit protected mode. If 1 it defines 32 bit protected mode.
        val g get() = data1[23].truth    // Granularity bit. If 0 the limit is in 1 B blocks (byte granularity), if 1 the limit is in 4 KiB blocks (page granularity).
        val baseHigh get() = data1[31..24]

        // -------------------------------------------------------------------------------------------------------------

        override val base get() = baseLow.insert(baseMiddle, 23..16).insert(baseHigh, 31..24)
        override val limit get() = limitLow.insert(limitHigh, 19..16)
        val end get() = base + limit

        override val isPresent get() = p
//        val isForAnAvailableTSS get() = true // why not? Internet nothing told how to check...

        val isValid: Boolean = data != ULONG_MAX

        override fun toString() = "GDT[base=0x${base.hex8} limit=0x${limit.hex8} d=${d.int} r=${r.int} a=${a.int}]"

        companion object {
            fun createGdtEntry(
                    base: ULong, limit: ULong, type: ULong, s: Boolean, dpl: ULong, p: Boolean, l: Boolean, d_or_b: Boolean, g: Boolean
//                    g: Boolean = true, d: Boolean = true, l: Boolean = false, avl: Boolean = false, p: Boolean = true,
//                    dpl: ULong = 0u, e: Boolean = false, c: Boolean = false, r: Boolean = true, a: Boolean = true
            ): SegmentDescriptor32 {
                val baseLow = base[15..0]
                val baseMiddle = base[23..16]
                val baseHigh = base[31..24]

                val limitLow = limit[15..0]
                val limitHigh = limit[19..16]

                val dataLo = insert(limitLow, 15..0).insert(baseLow, 31..16)
                val dataHi = baseMiddle
                        .insert(type, 11..8)
//                        .insert(a.ulong, 8)
//                        .insert(r.ulong, 9)
//                        .insert(c.ulong, 10)
//                        .insert(e.ulong, 11)
                        .insert(s, 12)
                        .insert(dpl, 14..13)
                        .insert(p, 15)
                        .insert(limitHigh, 19..16)
//                        .insert(avl.ulong, 20) // always 0
                        .insert(l, 21)
                        .insert(d_or_b, 22)
                        .insert(g, 23)
                        .insert(baseHigh, 31..24)
                return SegmentDescriptor32(dataLo.insert(dataHi, 63..32))
            }
        }
    }

    data class SegmentDescriptor64(val dataLo: ULong, val dataHi: ULong): SegmentDescriptor {
        private val data0 by lazy { dataLo[31..0] }
        private val data1 by lazy { dataLo[63..32] }
        private val data2 by lazy { dataHi[31..0] }
        private val data3 by lazy { dataHi[63..32] }

        private val limit0: ULong get() = data0[15..0]
        private val base0: ULong get() = data0[31..16]

        private val base1: ULong get() = data1[7..0]
        private val type: ULong get() = data1[11..8]
        private val dpl: ULong get() = data1[14..13]
        private val p get() = data1[15].truth
        private val limit1: ULong get() = data1[19..16]
        private val avl get() = data1[20].truth
        private val g get() = data1[23].truth
        private val base2: ULong get() = data1[31..24]

        private val base3: ULong get() = data2

        override val base: ULong get() = base0.insert(base1, 23..16).insert(base2, 31..24).insert(base3, 63..32)
        override val limit: ULong get() = limit0.insert(limit1, 19..16)

        override val isPresent get() = p
    }

    // see Figure 4-4. Formats of CR3 and Paging-Structure Entries with 32-Bit Paging
    // TODO: merge
    interface PagingEntry {
        val data: ULong

        val frame get() = data[31..12] // Physical address of 4-KByte aligned page table referenced by this entry

        val a get() = data[5].truth // Accessed; indicates whether software has accessed
        val pcd get() = data[4].truth // Page-level cache disable
        val pwt get() = data[3].truth // Page-level write-through
        val us get() = data[2].truth // User/supervisor; if 0, user-mode accesses are not allowed to the 4-KByte page referenced by this entry
        val rw get() = data[1].truth // Read/write; if 0, writes may not be allowed to the 4-KByte page referenced by this entry
        val p get() = data[0].truth  // Present; must be 1 to map a 4-KByte page

        /*
US RW P - Description
0  0  0 - Supervisory process tried to read a non-present page entry
0  0  1 - Supervisory process tried to read a page and caused a protection fault
0  1  0 - Supervisory process tried to write to a non-present page entry
0  1  1 - Supervisory process tried to write a page and caused a protection fault
1  0  0 - User process tried to read a non-present page entry
1  0  1 - User process tried to read a page and caused a protection fault
1  1  0 - User process tried to write to a non-present page entry
1  1  1 - User process tried to write a page and caused a protection fault
         */
        // see also https://wiki.osdev.org/Paging and Interrupt 14—Page-Fault Exception (#PF) at 6-40 Vol. 3A
        fun checkAccessRights(core: x86Core, address: PageAddress4Kb, I: UInt, U: UInt, W: UInt) {
            // P = 1 if protection violation or not present!
            if (!p) throw x86HardwareException.PageFault(core.pc, address.data, I, 0u, U, W, 0u)
//            if (!us && U == 1) throw x86HardwareException.PageFault(core.pc, address.data, I, 0, U, W, 1)
            if (!rw && W == 1u) throw x86HardwareException.PageFault(core.pc, address.data, I, 0u, U, W, 1u)
        }
    }

    data class CR3(override val data: ULong) : PagingEntry {
        fun readPDE(core: x86Core, address: PageAddress4Kb): PageDirectoryEntry4Kb {
//            val pdeAddress = insert(frame, 31..12).insert(address.directory, 11..2)
            val pdeAddress = (frame shl 12) or (address.directory shl 2)
            return PageDirectoryEntry4Kb.read(core, pdeAddress)
        }

        override fun toString() = "CR3[0x${data.hex8}]: frame=0x${frame.hex} pcd=$pcd pwt=$pwt]"
    }

    class PageDirectoryEntry4Kb(override val data: ULong) : PagingEntry {
        val ps get() = data[7].truth  // Page size; must be 0 for 4KB

        fun readPTE(core: x86Core, address: PageAddress4Kb): PageTableEntry4Kb {
//            val pteAddress = insert(frame, 31..12).insert(address.table, 11..2)
            val pteAddress = (frame shl 12) or (address.table shl 2)
            return PageTableEntry4Kb.read(core, pteAddress)
        }

        fun checkPaging4KbAndReserved(core: x86Core, address: PageAddress4Kb, I: UInt, U: UInt, W: UInt) {
            // assume present checked before
            if (core.cpu.cregs.cr4.pse) {
                // PSE == 1 -> 4MB paging mode
                // think so right about RSVD bit...
                if (!ps) throw x86HardwareException.PageFault(core.pc, address.data, I, 1u, U, W, 1u)  // RSVD: PS must be 1
                TODO("Can't translate virtual address ${address.hex} for 4MB paging mode!")
            } else {
                // PSE == 0 -> 4KB paging mode
                // If CR4.PSE = 0, no bits are reserved with 32-bit paging.
                // so no exceptions for RSVD may be thrown...
                if (ps) throw x86HardwareException.PageFault(core.pc, address.data, I, 1u, U, W, 1u)  // RSVD: PS must be 1
            }
        }

        override fun toString() = "PDE[0x${data.hex8}]: frame=0x${frame.hex} pcd=$pcd pwt=$pwt us=$us rw=$rw p=$p]"

        companion object {
            fun read(core: x86Core, address: ULong): PageDirectoryEntry4Kb {
                val data = core.mmu.physicalRead(address, 4)
                return PageDirectoryEntry4Kb(data)
            }
        }
    }

    class PageTableEntry4Kb(override val data: ULong) : PagingEntry {
        val g get() = data[8].truth // Global; if CR4.PGE = 1, determines whether the translation is global
        val pat get() = data[7].truth // If the PAT is supported, indirectly determines the memory type used to access
        val d get() = data[6].truth // Dirty; indicates whether software has written

        fun makePhysical(address: PageAddress4Kb): ULong {
//            val physical = insert(frame, 31..12).insert(address.offset, 11..0)
            val physical = (frame shl 12) or address.offset
            log.finest { "Translate linear to physical ${address.hex} -> ${physical.hex}" }
            return physical
        }

        override fun toString() = "PTE[0x${data.hex8}]: frame=0x${frame.hex}  pcd=$pcd pwt=$pwt us=$us rw=$rw p=$p]"

        companion object {
            fun read(core: x86Core, address: ULong): PageTableEntry4Kb {
                val data = core.mmu.physicalRead(address, 4)
                return PageTableEntry4Kb(data)
            }
        }
    }

    private inner class TLBCache {
        /** Calculates starting address of PT page (4 KiB) */
        // fun pageBasePt(vAddr: ULong) = vAddr ushr 12

        /** Calculates starting address of PD page (2 MiB) */
        fun pageBasePd(vAddr: ULong) = vAddr ushr 21

        /**
         * @param tableAddr Corresponding table entry physical address
         * @param g Is global?
         */
        private inner class TLBCacheEntry(val tableAddr: ULong, val g: Boolean)

        // private val ptCache = HashMap<ULong, TLBCacheEntry>()
        private val pdCache = HashMap<ULong, TLBCacheEntry>()

        /** Invalidates cache */
        fun clear() {
            // ptCache.clear()
            pdCache.clear()
        }

        /** Invalidates non-global pages */
        fun clearNonGlobal() {
            // ptCache.removeIf { !it.value.g }
            pdCache.removeIf { !it.value.g }
        }

        /**
         * Removes cache entry for page containing [vAddr]
         * @param vAddr some virtual address
         */
        fun remove(vAddr: ULong) {
            // ptCache.remove(pageBasePt(vAddr))
            pdCache.remove(pageBasePd(vAddr))
        }

        /**
         * Adds cache entry for page containing [vAddr]
         * @param vAddr some virtual address
         * @param pt PT entry address
         * @param g Is global?
         */
        // fun addPt(vAddr: ULong, pt: ULong, g: Boolean) {
            // ptCache[pageBasePt(vAddr)] = TLBCacheEntry(pt, g)
        // }

        /**
         * Adds cache entry for page containing [vAddr]
         * @param vAddr some virtual address
         * @param pd PT entry address
         * @param g Is global?
         */
        fun addPd(vAddr: ULong, pd: ULong, g: Boolean) {
            pdCache[pageBasePd(vAddr)] = TLBCacheEntry(pd, g)
        }

        /**
         * Tries to find cached table address for page containing [vAddr]
         * @param vAddr some virtual address
         */
        fun find(vAddr: ULong) = /*ptCache[pageBasePt(vAddr)]?.let {
            PTEntry(physicalRead(it.tableAddr, 8))
        } ?: */ pdCache[pageBasePd(vAddr)]?.let {
            PDEntry(physicalRead(it.tableAddr, 8))
        }
    }

    @DontAutoSerialize
    private val tlb = TLBCache()

    fun invalidatePagingCache() = if (x86.cpu.cregs.cr4.pge) {
        // G, or 'Global' tells the processor not to invalidate the TLB entry corresponding to the page upon a
        // MOV to CR3 instruction. Bit 7 (PGE) in CR4 must be set to enable global pages.
        tlb.clearNonGlobal()
    } else {
        tlb.clear()
    }

    fun invalidatePageTranslation(addr: ULong) = tlb.remove(addr)

    inner class PageAddress4Kb(val data: ULong) {
        val directory get() = data[31..22]
        val table get() = data[21..12]
        val offset get() = data[11..0]

        val hex get() = data.hex

        val page get() = data and 0xFFFF_F000u

        override fun toString() = "Address[0x${data.hex8}]: dir=0x${directory.hex} table=0x${table.hex} off=0x${offset.hex}"

        // 4.3 32-BIT PAGING of Vol. 3A 4-7
        fun loadTranslationTable(LorS: AccessAction, privilege: Boolean): PageTableEntry4Kb {
//            val lPage = page

            val I = (LorS == FETCH).uint // just for page-fault exceptions
            val W = (LorS == STORE).uint
            val U = (x86.isRing3 && !privilege).uint // current access rights (3 - user-mode)

//            val cached = pagingCache[lPage]
//            if (cached != null) {
//                val (pde, pte) = cached
//                // reserved not checked because it can't be cached with ill reserved flags
//                pde.checkAccessRights(x86, this, I, U, W)
//                pte.checkAccessRights(x86, this, I, U, W)
//                return pte
//            }

            val cr3 = CR3(x86.cpu.cregs.cr3.value)

            val pde = cr3.readPDE(x86, this).also {
                it.checkAccessRights(x86, this, I, U, W)
                it.checkPaging4KbAndReserved(x86, this, I, U, W)
                // here we sure 4KB paging mode
            }

            val pte = pde.readPTE(x86, this).also {
                it.checkAccessRights(x86, this, I, U, W)
            }

            // Cache PTE for specified page
//            pagingCache[lPage] = pde to pte
//            log.warning { "Cached 0x${lPage.hex} -> $pte" }

            return pte
        }

        /**
         * Actual address translation using specified PTE
         */
        fun translate(LorS: AccessAction, privilege: Boolean) =
                loadTranslationTable(LorS, privilege).makePhysical(this)
    }

    val gdtr = DescriptorRegister()
    var ldtr = 0uL

    private val cache = Array(6) { INVALID_GDT_ENTRY }
    private val protectedModeEnabled = Array(6) { false }

    private fun isLDT(ss: ULong) = ss[2] != 0uL

    private fun descriptorByOffset(base: ULong, offset: ULong, makeDirty: Boolean): SegmentDescriptor32 {
        val linear = base + offset
        var data = linearRead(linear, 8, true)
        if (makeDirty && data[40].untruth) {
            data = data set 40
            linearWrite(linear, 8, data, true)
        }
        val result = SegmentDescriptor32(data)
        // log.fine { "Reload by [${ss.hex}] GDT -> $result" }
        return result
    }

    private fun descriptorByOffset64(base: ULong, offset: ULong, makeDirty: Boolean): SegmentDescriptor64 {
        val linear = base + offset
        var dataLo = linearRead(linear, 8, true)
        var dataHi = linearRead(linear + 8u, 8, true)
        if (makeDirty) {
//            data = data or 0x10000000000uL
//            linearWrite(linear, 8, data, true)
        }
        val result = SegmentDescriptor64(dataLo, dataHi)
        // log.fine { "Reload by [${ss.hex}] GDT -> $result" }
        return result
    }

    fun readTaskStateSegment(ss: ULong): SegmentDescriptor {
        if (x86.cpu.mode != x86CPU.Mode.R64)
            return readSegmentDescriptor32(ss)

        if (isLDT(ss))
            TODO("LDT in 64-bit mode isn't implemented yet")

        return descriptorByOffset64(gdtr.base, ss and 0xFFFF_FFF8u, true)
    }

    fun readSegmentDescriptor32(ss: ULong, ssrId: Int = SSR.ES.id): SegmentDescriptor32 {
        val isR64 = x86.cpu.mode == x86CPU.Mode.R64
        if (!isR64 && ss == 0uL && ssrId in SSR_NULL_CHECK) throw x86HardwareException.GeneralProtectionFault(core.pc, ss)

        // The General Protection Fault sets an error code, which is the segment selector index
        // when the exception is segment related. Otherwise, 0.
        val base = if (isLDT(ss)) {
            if (ldtr == 0uL || (!isR64 && ldtr > gdtr.limit))
                throw GeneralException("Incorrect LDTR=${ldtr.hex} GDTR=$gdtr")
            descriptorByOffset(gdtr.base, ldtr, false).base
        } else gdtr.base

        return descriptorByOffset(base, ss and 0xFFFF_FFF8u, true)
    }
    private fun reloadDescriptor(ssr: ARegistersBankNG<x86Core>.Register): SegmentDescriptor32 {
        val desc = readSegmentDescriptor32(ssr.value, ssr.id)
        cache[ssr.id] = desc
        when (ssr.id) {
            SSR.FS.id ->
                x86.config.fs_base = desc.base
            SSR.GS.id ->
                x86.config.gs_base = desc.base
        }
        return desc
    }

    private fun PMVirtual2Linear(vAddr: ULong, ssr: ARegistersBankNG<x86Core>.Register): ULong {
        var desc = cache[ssr.id]
        if (!desc.isValid) {
            if (ssr.value > gdtr.limit) return when {
                x86.is64bit -> vAddr
                else -> RMSegment2Linear(vAddr, ssr)
            }
            desc = reloadDescriptor(ssr)
//            log.warning { "Invalid GDT entry in cache -> reload for 0x${vAddr.hex} ssr=$ssr[${ss.hex}] $gdtEntry" }
        }

//        // Don't load to hidden parts until met direct access in instruction definition
//        // TODO: maybe merge it with cache somehow?
//        // TODO: val cs get() = cache[SS.id]
//        when (ssr) {
//            x86.cpu.sregs.cs -> cs = desc
//            x86.cpu.sregs.ss -> ss = desc
//        }
        if (ssr == x86.cpu.sregs.cs) {
            x86.cpu.csd = desc.d
            x86.cpu.csl = desc.l
        }

        return when {
            x86.is64bit -> when (ssr.id) {
                SSR.FS.id -> x86.config.fs_base + vAddr
                SSR.GS.id -> x86.config.gs_base + vAddr
                else -> vAddr
            }
            else -> vAddr + desc.base
        }
    }

    abstract inner class ATable(val data: ULong) {
        val p by lazy { data[0].truth }
        val rw by lazy { data[1].truth } // 0 - write isn't allowed
        val us by lazy { data[2].truth } // 0 - user access isn't allowed
        val pwt by lazy { data[3].truth } // Page-level write-through
        val pcd by lazy { data[4].truth } // Page-level cache disable
        val a by lazy { data[5].truth } // Accessed
        val d by lazy { data[6].truth } // Dirty
        val ps by lazy { data[7].truth } // Page size
        val g by lazy { data[8].truth } // Global - if CR4.PGE = 1, determines whether the translation is global (Only if PS=1)

        abstract val pat: Boolean
        abstract val lsb: Int

        val address by lazy { data[50..lsb] }

        val protectionKey by lazy { data[62..59] } // Protection key; if CR4.PKE = 1 or CR4.PKS = 1, this may control the page’s access rights
        val xd by lazy { data[63].truth } // If IA32_EFER.NXE = 1, execute-disable: if 1, instruction fetches are not allowed

        fun withOffset(offset: ULong) = (address shl lsb) or offset

        abstract fun translate(vAddr: ULong, size: Int, I: UInt, W: UInt, U: UInt): ULong

        protected fun checkPermissions(address: ULong, I: UInt, W: UInt, U: UInt) {
            if (!p)
                throw x86HardwareException.PageFault(core.pc, address, I, 0u, U, W, 0u)
            // TODO: U
            if (!rw && W == 1u)
                throw x86HardwareException.PageFault(core.pc, address, I, 0u, U, W, 1u)
            if (x86.config.efer[NXE].truth && xd && I == 1u)
                throw x86HardwareException.PageFault(core.pc, address, I, 0u, U, W, 1u)
        }
    }


    inner class PTEntry(data: ULong) : ATable(data) {
        override val pat by lazy { data[7].truth } // Indirectly determines the memory type used
        override val lsb = 12

        override fun translate(vAddr: ULong, size: Int, I: UInt, W: UInt, U: UInt): ULong {
            checkPermissions(vAddr, I, W, U)
            return withOffset(vAddr[11..0])
        }
    }

    inner class PDEntry(data: ULong) : ATable(data) {
        override val pat by lazy { data[12].truth } // Indirectly determines the memory type used (Only if PS=1)
        override val lsb by lazy { if (ps) 21 else 12 }

        private inline fun toPTAddress(vAddr: ULong) = withOffset(vAddr[20..12] shl 3)

        override fun translate(vAddr: ULong, size: Int, I: UInt, W: UInt, U: UInt): ULong {
            checkPermissions(vAddr, I, W, U)

            return if (ps) {
                withOffset(vAddr[20..0])
            } else {
                val ptAddress = toPTAddress(vAddr)

                val vAddrEnd = vAddr + size - 1u
                val ptAddressEnd = toPTAddress(vAddrEnd)
                if (ptAddress != ptAddressEnd) {
                    throw CrossPageAccessException(core.pc, vAddr, ubitMask64(12).inv())
//                    log.severe { "Cross-page access: ${vAddr.hex16}..${vAddrEnd.hex16}" }
//                    PTEntry(physicalRead(ptAddressEnd, 8)).translate(vAddrEnd, 1, I, W, U)
                }

                val pte = PTEntry(physicalRead(ptAddress, 8))
                val addr = pte.translate(vAddr, size, I, W, U)
                // if (!pte.pcd) {
                    // tlb.addPt(vAddr, ptAddress, pte.g)
                // }
                addr
            }
        }
    }

    inner class PDPTEntry(data: ULong) : ATable(data) {
        override val pat by lazy { data[12].truth }  // Indirectly determines the memory type used (Only if PS=1)
        override val lsb by lazy { if (ps) 30 else 12 }

        private inline fun toPDEAddress(vAddr: ULong) = withOffset(vAddr[29..21] shl 3)

        override fun translate(vAddr: ULong, size: Int, I: UInt, W: UInt, U: UInt): ULong {
            checkPermissions(vAddr, I, W, U)

            if (ps)
                TODO("Not implemented")

            val pdeAddress = toPDEAddress(vAddr)

            val vAddrEnd = vAddr + size - 1u
            val pdeAddressEnd = toPDEAddress(vAddrEnd)
            if (pdeAddress != pdeAddressEnd) {
                throw CrossPageAccessException(core.pc, vAddr, ubitMask64(21).inv())
//                log.severe { "Cross-page access: ${vAddr.hex16}..${vAddrEnd.hex16}" }
//                PDEntry(physicalRead(pdeAddressEnd, 8)).translate(vAddrEnd, 1, I, W, U)
            }

            val pde = PDEntry(physicalRead(pdeAddress, 8))
            val addr = pde.translate(vAddr, size, I, W, U)
            if (!pde.pcd) {
                tlb.addPd(vAddr, pdeAddress, pde.g)
            }
            return addr
        }
    }

    inner class PML4Entry(data: ULong) : ATable(data) {
        override val pat: Boolean get() = throw GeneralException("PAT is undefined for PML4 Entry")
        override val lsb = 12

        private inline fun toPDPTAddress(vAddr: ULong) = withOffset(vAddr[38..30] shl 3)

        override fun translate(vAddr: ULong, size: Int, I: UInt, W: UInt, U: UInt): ULong {
            checkPermissions(vAddr, I, W, U)

            val pdptAddress = toPDPTAddress(vAddr)

            val vAddrEnd = vAddr + size - 1u
            val pdptAddressEnd = toPDPTAddress(vAddrEnd)
            if (pdptAddress != pdptAddressEnd) {
                throw CrossPageAccessException(core.pc, vAddr, ubitMask64(30).inv())
//                log.severe { "Cross-page access: ${vAddr.hex16}..${vAddrEnd.hex16}" }
//                PDPTEntry(physicalRead(pdptAddressEnd, 8)).translate(vAddrEnd, 1, I, W, U)
            }

            return PDPTEntry(physicalRead(pdptAddress, 8)).translate(vAddr, size, I, W, U)
        }
    }

    private inline fun toPML4Address(vAddr: ULong) = x86.cpu.cregs.cr3.PML4Address or (vAddr[47..39] shl 3)

    fun x64Translate(vAddr: ULong, size: Int, LorS: AccessAction, privilege: Boolean): ULong {
        val I = (LorS == FETCH).uint // just for page-fault exceptions
        val W = (LorS == STORE).uint
        val U = (x86.isRing3 && !privilege).uint // current access rights (3 - user-mode)

        val entry = tlb.find(vAddr)
        if (entry != null) {
            return entry.translate(vAddr, size, I, W, U)
        }

        val pml4Address = toPML4Address(vAddr)

        val vAddrEnd = vAddr + size - 1u
        val pml4AddressEnd = toPML4Address(vAddrEnd)
        if (pml4Address != pml4AddressEnd) {  // if there are 2 different pages
            throw CrossPageAccessException(core.pc, vAddr, ubitMask64(39).inv())
//            log.severe { "Cross-page access: ${vAddr.hex16}..${vAddrEnd.hex16}" }
//            PML4Entry(physicalRead(pml4AddressEnd, 8)).translate(vAddrEnd, 1, I, W, U)
        }

        return PML4Entry(physicalRead(pml4Address, 8)).translate(vAddr, size, I, W, U)
    }

    fun x86Translate(vAddr: ULong, size: Int, LorS: AccessAction, privilege: Boolean): ULong {
        val start = PageAddress4Kb(vAddr)

        val result = start.translate(LorS, privilege)

        if (size == 1)
            return result

        val end = PageAddress4Kb(vAddr + size.uint - 1u)
        if (start.page == end.page)
            return result

        // Access to first address of next page if page not load then PageFault generates
        PageAddress4Kb(end.page).loadTranslationTable(LorS, privilege)

        return result
    }

    /**
     * Translate linear address to physical address in Paging Mode see 4.3 32-BIT PAGING of Vol. 3A 4-7
     * It also checks if access range extents current page limit and if so try to translate nearest next page address
     * by loading it [PageDirectoryEntry4Kb] and [PageTableEntry4Kb] and checks entries flags: U/S, R/W, P.
     *
     * If page can't be accessed e.g. not present (P-flag) then [x86HardwareException.PageFault] will be generated.
     *
     * WARNING: The second translation is crucial when access among
     *          two pages in one request - a really weird thing may happen!
     */
    private fun PMLinear2Physical(vAddr: ULong, size: Int, LorS: AccessAction, privilege: Boolean): ULong {
        if (!x86.cpu.cregs.cr0.pg)
            return vAddr

        return if (x86.cpu.cregs.cr4.pae) x64Translate(vAddr, size, LorS, privilege) else x86Translate(vAddr, size, LorS, privilege)
    }

    private fun RMSegment2Linear(vAddr: ULong, ssr: ARegistersBankNG<x86Core>.Register): ULong = (ssr.value shl 4) + vAddr

    override fun translate(ea: ULong, ss: Int, size: Int,  LorS: AccessAction): ULong {
        return if (ss != UNDEF) {
            val ssr = x86.cpu.sregs[ss]
            if (protectedModeEnabled[ss] || x86.cpu.csl) {
                val lAddr = PMVirtual2Linear(ea, ssr)
                PMLinear2Physical(lAddr, size, LorS, false)
            } else RMSegment2Linear(ea, ssr)
            // log.fine { "%08X -> %08X".format(vAddr, pAddr) }
        } else ea
    }

    fun updateCache(ssr: SSRBank.SSR) {
        val ss = ssr.value
        // log.finer { "$ssr = ${ss.hex}" }
        if (x86.cpu.cregs.cr0.pe) {
            protectedModeEnabled[ssr.id] = true
            if (ss <= gdtr.limit) {
                reloadDescriptor(ssr)
            }
//                cache[ssr.id] = INVALID_GDT_ENTRY
//                cache[ssr.reg] = gdt(ss)
        } else protectedModeEnabled[ssr.id] = false
    }

    fun invalidateGdtCache() = cache.fill(INVALID_GDT_ENTRY)

    fun invalidateProtectedMode() = protectedModeEnabled.fill(false)

    override fun reset(){
        invalidateGdtCache()
        invalidateProtectedMode()
        invalidatePagingCache()
    }

//    override fun serialize(ctxt: GenericSerializer) = storeValues(
//            "MMUgdtrBase" to gdtr.base.hex16,
//            "MMUgdtrLimit" to gdtr.limit.hex16,
//            "Ldtr" to ldtr.hex16,
//            "cache" to cache.map { it.data.hex16 },
//            "protectedModeEnabled" to protectedModeEnabled)
//
//    // TODO: maybe broken
//    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
//        gdtr.base = loadHex(snapshot, "MMUgdtrBase", 0u)
//        gdtr.limit = loadHex(snapshot, "MMUgdtrLimit", 0u)
//        ldtr = loadHex(snapshot, "Ldtr", 0u)
//        cache.deserialize<SegmentDescriptor, String>(ctxt, snapshot["cache"]) { SegmentDescriptor(it.ulongByHex) }
//        protectedModeEnabled.deserialize<Boolean, Any>(ctxt, snapshot["protectedModeEnabled"]) { it as Boolean }
//    }

    override fun stringify() = buildString {
        appendLine("$name:")
        appendLine("LDTR = 0x${ldtr.hex16}")
        appendLine("GDTR  : base = 0x${gdtr.base.hex16}   limit = 0x${gdtr.limit.hex4}")
        appendLine("Hidden: CS   = 0x${cs.data.hex16}   SS    = 0x${ss.data.hex16}")
    }

    fun stringifyTranslateAll(pc: ULong) = buildString {
        appendLine("PC[0x${pc.hex16}] Translation")
        SSR.values()
            .mapNotNull { ssr ->
                runCatching { translate(pc, ssr.id, 1, AccessAction.FETCH) }.getOrNull()?.let { ssr to it }
            }
            .forEach {
                appendLine("    ${it.first.name}: 0x${it.second.hex16}")
            }
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return super<IAutoSerializable>.serialize(ctxt)
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super<IAutoSerializable>.deserialize(ctxt, snapshot)
    }
}
