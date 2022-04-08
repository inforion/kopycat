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
        val INVALID_GDT_ENTRY = SegmentDescriptor(-1uL)
    }

    val x86 = core

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

    data class SegmentDescriptor(val data: ULong): IConstructorSerializable, IAutoSerializable {
        val dataHi by lazy { data[63..32] }
        val dataLo by lazy { data[31..0] }
        // -------------------------------------------------------------------------------------------------------------
        val baseHigh: ULong get() = dataHi[31..24]

        val g: Boolean get() = dataHi[23].truth    // Granularity bit. If 0 the limit is in 1 B blocks (byte granularity), if 1 the limit is in 4 KiB blocks (page granularity).
        val d: Boolean get() = dataHi[22].truth    // Size bit. If 0 the selector defines 16 bit protected mode. If 1 it defines 32 bit protected mode.
        val l: Boolean get() = dataHi[21].truth    // Long-mode code flag. If set (1), the descriptor defines a 64-bit code segment. When set, Sz should always be clear. For any other type of segment (other code types or any data segment), it should be clear (0).
        val avl: Boolean get() = dataHi[20].truth  // Always = 0

        val limitHigh: ULong get() = dataHi[19..16]

        val p: Boolean get() = dataHi[15].truth  // Present bit. This must be 1 for all valid selectors.
        val dpl: ULong get() = dataHi[14..13]     // Privilege, 2 bits. Contains the ring level, 0 = highest (kernel), 3 = lowest (user applications).
        // bit 12 always equals to 1
        val e: Boolean get() = dataHi[11].truth  // Executable bit. If 1 code in this segment can be executed, ie. a code selector. If 0 it is a data selector.
        val c: Boolean get() = dataHi[10].truth  // Direction bit/Conforming bit.
        val r: Boolean get() = dataHi[9].truth   // Readable bit/Writable bit.
        val a: Boolean get() = dataHi[8].truth   // Accessed bit. Just set to 0. The CPU sets this to 1 when the segment is accessed.

        val baseMiddle: ULong get() = dataHi[7..0]
        val baseLow: ULong get() = dataLo[31..16]
        val limitLow: ULong get() = dataLo[15..0]
        // -------------------------------------------------------------------------------------------------------------

        val base: ULong get() = baseLow.insert(baseMiddle, 23..16).insert(baseHigh, 31..24)
        val limit: ULong get() = limitLow.insert(limitHigh, 19..16)
        val end: ULong get() = base + limit

        val isPresent get() = p
        val isForAnAvailableTSS get() = true // why not? Internet nothing told how to check...

        val isValid: Boolean = data != ULONG_MAX

        override fun toString() = "GDT[base=0x${base.hex8} limit=0x${limit.hex8} d=${d.int} r=${r.int} a=${a.int}]"

        companion object {
            fun createGdtEntry(
                    base: ULong, limit: ULong,
                    g: Boolean = true, d: Boolean = true, l: Boolean = false, avl: Boolean = false, p: Boolean = true,
                    dpl: ULong = 0u, e: Boolean = false, c: Boolean = false, r: Boolean = true, a: Boolean = true
            ): ULong {
                val baseLow = base[15..0]
                val baseMiddle = base[23..16]
                val baseHigh = base[31..24]

                val limitLow = limit[15..0]
                val limitHigh = limit[19..16]

                val dataHi = insert(baseMiddle, 7..0)
                        .insert(a.ulong, 8)
                        .insert(r.ulong, 9)
                        .insert(c.ulong, 10)
                        .insert(e.ulong, 11)
                        .insert(1u, 12)
                        .insert(dpl, 14..13)
                        .insert(p.ulong, 15)
                        .insert(limitHigh, 19..16)
                        .insert(avl.ulong, 20)
                        .insert(l.ulong, 21)
                        .insert(d.ulong, 22)
                        .insert(g.ulong, 23)
                        .insert(baseHigh, 31..24)
                val dataLo = insert(limitLow, 15..0).insert(baseLow, 31..16)
                val entry = insert(dataLo, 31..0).insert(dataHi, 63..32)
                return entry
            }
        }
    }

    // see Figure 4-4. Formats of CR3 and Paging-Structure Entries with 32-Bit Paging
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

//    private val pagingCache = dictionary<ULong, Pair<PageDirectoryEntry4Kb, PageTableEntry4Kb>>(0x1000)

//    fun invalidatePagingCache() = pagingCache.clear()
//    fun invalidatePageTranslation(page: ULong) = pagingCache.remove(page)

    fun invalidatePagingCache() = Unit
    fun invalidatePageTranslation(page: ULong) = Unit

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
            val U = (x86.cpu.sregs.cs.cpl == 3uL && !privilege).uint // current access rights (3 - user-mode)

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

    internal fun descriptor(index: Int) = cache[index]

    internal fun isProtectedModeEnabled(index: Int) = protectedModeEnabled[index]

    private fun isLDT(ss: ULong) = ss[2] != 0uL

    private fun descriptorByOffset(base: ULong, offset: ULong, makeDirty: Boolean): SegmentDescriptor {
        val linear = base + offset
        var data = linearRead(linear, 8, true)
        if (makeDirty) {
            data = data or 0x10000000000uL
            linearWrite(linear, 8, data, true)
        }
        val result = SegmentDescriptor(data)
        // log.fine { "Reload by [${ss.hex}] GDT -> $result" }
        return result
    }

    fun readSegmentDescriptor(ss: ULong): SegmentDescriptor {
        if (ss == 0uL) throw x86HardwareException.GeneralProtectionFault(core.pc, ss)

        // The General Protection Fault sets an error code, which is the segment selector index
        // when the exception is segment related. Otherwise, 0.
        val base = if (isLDT(ss)) {
            if (ldtr == 0uL || ldtr > gdtr.limit)
                throw GeneralException("Incorrect LDTR=${ldtr.hex} GDTR=$gdtr")
            descriptorByOffset(gdtr.base, ldtr, false).base
        } else gdtr.base

        return descriptorByOffset(base, ss and 0xFFFF_FFF8u, true)
    }

    private fun PMVirtual2Linear(vAddr: ULong, ssr: ARegistersBankNG<x86Core>.Register): ULong {
        var desc = cache[ssr.id]
        if (!desc.isValid) {
            val ss = ssr.value
            if (ss > gdtr.limit) return when {
                x86.is64bit -> vAddr
                else -> RMSegment2Linear(vAddr, ssr)
            }

            desc = readSegmentDescriptor(ss)
            cache[ssr.id] = desc
//            log.warning { "Invalid GDT entry in cache -> reload for 0x${vAddr.hex} ssr=$ssr[${ss.hex}] $gdtEntry" }
        }

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

    private fun x64Translate(vAddr: ULong): ULong {
        val pml4 = vAddr[47..39]
        val dirPtr = vAddr[38..30]
        val dir = vAddr[29..21]
        val table = vAddr[20..12]
        val offset = vAddr[11..0]


        val pml4Address = x86.cpu.cregs.cr3.PML4Address or (pml4 shl 3)
        val pml4e = physicalRead(pml4Address, 8)

        val pdptAddress = (pml4e[51..12] shl 12) or (dirPtr shl 3)
        val pdpte = physicalRead(pdptAddress, 8)

        // PS
        if (pdpte[7].truth)
            TODO("Not implemented")

        val pageDirAddress = (pdpte[51..12] shl 12) or (dir shl 3)
        val pde = physicalRead(pageDirAddress, 8)

        // PS
        val physicalAddress = if (pde[7].truth) {
            (pde[51..21] shl 21) or vAddr[20..0]
        } else {
            val pageTableAddress = (pde[51..12] shl 12) or (table shl 3)
            val pte = physicalRead(pageTableAddress, 8)

            (pte[51..12] shl 12) or offset
        }
        return physicalAddress
    }

    private fun x86Translate(vAddr: ULong, size: Int, LorS: AccessAction, privilege: Boolean): ULong {
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

        return if (x86.cpu.cregs.cr4.pae) x64Translate(vAddr) else x86Translate(vAddr, size, LorS, privilege)
    }

    private fun RMSegment2Linear(vAddr: ULong, ssr: ARegistersBankNG<x86Core>.Register): ULong = (ssr.value shl 4) + vAddr

    override fun translate(ea: ULong, ss: Int, size: Int,  LorS: AccessAction): ULong {
        return if (ss != UNDEF) {
            val ssr = x86.cpu.sregs[ss]
            if (isProtectedModeEnabled(ss) || x86.cpu.csl) {
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
            if (ss <= gdtr.limit)
                cache[ssr.id] = INVALID_GDT_ENTRY
//                cache[ssr.reg] = gdt(ss)
        } else protectedModeEnabled[ssr.id] = false
    }

    internal fun invalidateGdtCache() = cache.fill(INVALID_GDT_ENTRY)

    internal fun invalidateProtectedMode() = protectedModeEnabled.fill(false)

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

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return super<IAutoSerializable>.serialize(ctxt)
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super<IAutoSerializable>.deserialize(ctxt, snapshot)
    }
}