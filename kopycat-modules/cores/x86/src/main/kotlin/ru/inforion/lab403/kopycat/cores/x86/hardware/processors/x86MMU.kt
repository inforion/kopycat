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
import ru.inforion.lab403.kopycat.cores.base.common.AddressTranslator
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction.*
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.exceptions.x86HardwareException
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.SSR.cs
import ru.inforion.lab403.kopycat.interfaces.IInteractive
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import ru.inforion.lab403.kopycat.serializer.deserialize
import ru.inforion.lab403.kopycat.serializer.loadHex
import ru.inforion.lab403.kopycat.serializer.storeValues
import java.nio.ByteOrder
import java.util.logging.Level

/**
 *
 * Stub for x86 address translation
 */
class x86MMU(core: x86Core, name: String) : AddressTranslator(core, name) {
    companion object {
        @Transient val log = logger(Level.FINE)
        val INVALID_GDT_ENTRY = SegmentDescriptor(-1)
    }

    val x86 = core

    data class DescriptorRegister(var limit: Long = 0, var base: Long = 0) {
        override fun toString() = "[base=0x${base.hex} limit=0x${limit.hex}]"
    }

    // Load and store from/to physical memory address
    private fun physicalRead(address: Long, size: Int) = x86.mmu.ports.outp.read(address, 0, size)
    private fun physicalWrite(address: Long, size: Int, value: Long) = x86.mmu.ports.outp.write(address, 0, size, value)

    // Load and store from/to linear memory address
    fun linearRead(address: Long, size: Int, privilege: Boolean = false) =
            physicalRead(PMLinear2Physical(address, size, LOAD, privilege), size)
    fun linearWrite(address: Long, size: Int, value: Long, privilege: Boolean = false) =
            physicalWrite(PMLinear2Physical(address, size, STORE, privilege), size, value)

    data class SegmentDescriptor(val data: Long) {
        val dataHi by lazy { data[63..32] }
        val dataLo by lazy { data[31..0] }
        // -------------------------------------------------------------------------------------------------------------
        val baseHigh: Long get() = dataHi[31..24]

        val g: Boolean get() = dataHi[23] == 1L    // Granularity bit. If 0 the limit is in 1 B blocks (byte granularity), if 1 the limit is in 4 KiB blocks (page granularity).
        val d: Boolean get() = dataHi[22] == 1L    // Size bit. If 0 the selector defines 16 bit protected mode. If 1 it defines 32 bit protected mode.
        val l: Boolean get() = dataHi[21] == 1L    // Always = 0
        val avl: Boolean get() = dataHi[20] == 1L  // Always = 0

        val limitHigh: Long get() = dataHi[19..16]

        val p: Boolean get() = dataHi[15] == 1L  // Present bit. This must be 1 for all valid selectors.
        val dpl: Long get() = dataHi[14..13]     // Privilege, 2 bits. Contains the ring level, 0 = highest (kernel), 3 = lowest (user applications).
        // bit 12 always equals to 1
        val e: Boolean get() = dataHi[11] == 1L  // Executable bit. If 1 code in this segment can be executed, ie. a code selector. If 0 it is a data selector.
        val c: Boolean get() = dataHi[10] == 1L  // Direction bit/Conforming bit.
        val r: Boolean get() = dataHi[9] == 1L   // Readable bit/Writable bit.
        val a: Boolean get() = dataHi[8] == 1L   // Accessed bit. Just set to 0. The CPU sets this to 1 when the segment is accessed.

        val baseMiddle: Long get() = dataHi[7..0]
        val baseLow: Long get() = dataLo[31..16]
        val limitLow: Long get() = dataLo[15..0]
        // -------------------------------------------------------------------------------------------------------------

        val base: Long get() = baseLow.insert(baseMiddle, 23..16).insert(baseHigh, 31..24)
        val limit: Long get() = limitLow.insert(limitHigh, 19..16)
        val end: Long get() = base + limit

        val isPresent get() = p
        val isForAnAvailableTSS get() = true // why not? Internet nothing told how to check...

        val isValid: Boolean = data != -1L

        override fun toString() = "GDT[base=0x${base.hex8} limit=0x${limit.hex8} d=${d.toInt()} r=${r.toInt()} a=${a.toInt()}]"

        companion object {
            fun createGdtEntry(
                    base: Long, limit: Long,
                    g: Boolean = true, d: Boolean = true, l: Boolean = false, avl: Boolean = false, p: Boolean = true,
                    dpl: Long = 0, e: Boolean = false, c: Boolean = false, r: Boolean = true, a: Boolean = true
            ): Long {
                val baseLow = base[15..0]
                val baseMiddle = base[23..16]
                val baseHigh = base[31..24]

                val limitLow = limit[15..0]
                val limitHigh = limit[19..16]

                val dataHi = insert(baseMiddle, 7..0)
                        .insert(if (a) 1 else 0, 8)
                        .insert(if (r) 1 else 0, 9)
                        .insert(if (c) 1 else 0, 10)
                        .insert(if (e) 1 else 0, 11)
                        .insert(1, 12)
                        .insert(dpl, 14..13)
                        .insert(if (p) 1 else 0, 15)
                        .insert(limitHigh, 19..16)
                        .insert(if (avl) 1 else 0, 20)
                        .insert(if (l) 1 else 0, 21)
                        .insert(if (d) 1 else 0, 22)
                        .insert(if (g) 1 else 0, 23)
                        .insert(baseHigh, 31..24)
                val dataLo = insert(limitLow, 15..0).insert(baseLow, 31..16)
                val entry = insert(dataLo, 31..0).insert(dataHi, 63..32)
                return entry
            }
        }
    }

    // see Figure 4-4. Formats of CR3 and Paging-Structure Entries with 32-Bit Paging
    interface PagingEntry {
        val data: Long

        val frame get() = data[31..12] // Physical address of 4-KByte aligned page table referenced by this entry

        val a get() = data[5] == 1L // Accessed; indicates whether software has accessed
        val pcd get() = data[4] == 1L // Page-level cache disable
        val pwt get() = data[3] == 1L // Page-level write-through
        val us get() = data[2] == 1L // User/supervisor; if 0, user-mode accesses are not allowed to the 4-KByte page referenced by this entry
        val rw get() = data[1] == 1L // Read/write; if 0, writes may not be allowed to the 4-KByte page referenced by this entry
        val p get() = data[0] == 1L  // Present; must be 1 to map a 4-KByte page

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
        fun checkAccessRights(core: x86Core, address: PageAddress4Kb, I: Int, U: Int, W: Int) {
            // P = 1 if protection violation or not present!
            if (!p) throw x86HardwareException.PageFault(core.pc, address.data, I, 0, U, W, 0)
//            if (!us && U == 1) throw x86HardwareException.PageFault(core.pc, address.data, I, 0, U, W, 1)
            if (!rw && W == 1) throw x86HardwareException.PageFault(core.pc, address.data, I, 0, U, W, 1)
        }
    }

    data class CR3(override val data: Long) : PagingEntry {
        fun readPDE(core: x86Core, address: PageAddress4Kb): PageDirectoryEntry4Kb {
//            val pdeAddress = insert(frame, 31..12).insert(address.directory, 11..2)
            val pdeAddress = (frame shl 12) or (address.directory shl 2)
            return PageDirectoryEntry4Kb.read(core, pdeAddress)
        }

        override fun toString() = "CR3[0x${data.hex8}]: frame=0x${frame.hex} pcd=$pcd pwt=$pwt]"
    }

    class PageDirectoryEntry4Kb(override val data: Long) : PagingEntry {
        val ps get() = data[7] == 1L  // Page size; must be 0 for 4KB

        fun readPTE(core: x86Core, address: PageAddress4Kb): PageTableEntry4Kb {
//            val pteAddress = insert(frame, 31..12).insert(address.table, 11..2)
            val pteAddress = (frame shl 12) or (address.table shl 2)
            return PageTableEntry4Kb.read(core, pteAddress)
        }

        fun checkPaging4KbAndReserved(core: x86Core, address: PageAddress4Kb, I: Int, U: Int, W: Int) {
            // assume present checked before
            if (core.cpu.cregs.vpse) {
                // PSE == 1 -> 4MB paging mode
                // think so right about RSVD bit...
                if (!ps) throw x86HardwareException.PageFault(core.pc, address.data, I, 1, U, W, 1)  // RSVD: PS must be 1
                TODO("Can't translate virtual address ${address.hex} for 4MB paging mode!")
            } else {
                // PSE == 0 -> 4KB paging mode
                // If CR4.PSE = 0, no bits are reserved with 32-bit paging.
                // so no exceptions for RSVD may be thrown...
                if (ps) throw x86HardwareException.PageFault(core.pc, address.data, I, 1, U, W, 1)  // RSVD: PS must be 1
            }
        }

        override fun toString() = "PDE[0x${data.hex8}]: frame=0x${frame.hex} pcd=$pcd pwt=$pwt us=$us rw=$rw p=$p]"

        companion object {
            fun read(core: x86Core, address: Long): PageDirectoryEntry4Kb {
                val data = core.mmu.physicalRead(address, 4)
                return PageDirectoryEntry4Kb(data)
            }
        }
    }

    class PageTableEntry4Kb(override val data: Long) : PagingEntry {
        val g get() = data[8] == 1L // Global; if CR4.PGE = 1, determines whether the translation is global
        val pat get() = data[7] == 1L // If the PAT is supported, indirectly determines the memory type used to access
        val d get() = data[6] == 1L // Dirty; indicates whether software has written

        fun makePhysical(address: PageAddress4Kb): Long {
//            val physical = insert(frame, 31..12).insert(address.offset, 11..0)
            val physical = (frame shl 12) or address.offset
            log.finest { "Translate linear to physical ${address.hex} -> ${physical.hex}" }
            return physical
        }

        override fun toString() = "PTE[0x${data.hex8}]: frame=0x${frame.hex}  pcd=$pcd pwt=$pwt us=$us rw=$rw p=$p]"

        companion object {
            fun read(core: x86Core, address: Long): PageTableEntry4Kb {
                val data = core.mmu.physicalRead(address, 4)
                return PageTableEntry4Kb(data)
            }
        }
    }

//    private val pagingCache = THashMap<Long, Pair<PageDirectoryEntry4Kb, PageTableEntry4Kb>>(0x1000)

//    fun invalidatePagingCache() = pagingCache.clear()
//    fun invalidatePageTranslation(page: Long) = pagingCache.remove(page)

    fun invalidatePagingCache() = Unit
    fun invalidatePageTranslation(page: Long) = Unit

    inner class PageAddress4Kb(val data: Long) {
        val directory get() = data[31..22]
        val table get() = data[21..12]
        val offset get() = data[11..0]

        val hex get() = data.hex

        val page get() = data and 0xFFFF_F000

        override fun toString() = "Address[0x${data.hex8}]: dir=0x${directory.hex} table=0x${table.hex} off=0x${offset.hex}"

        // 4.3 32-BIT PAGING of Vol. 3A 4-7
        fun loadTranslationTable(LorS: AccessAction, privilege: Boolean): PageTableEntry4Kb {
//            val lPage = page

            val I = if (LorS == FETCH) 1 else 0  // just for page-fault exceptions
            val W = if (LorS == STORE) 1 else 0
            val U = if (cs.cpl(x86) == 3 && !privilege) 1 else 0 // current access rights (3 - user-mode)

//            val cached = pagingCache[lPage]
//            if (cached != null) {
//                val (pde, pte) = cached
//                // reserved not checked because it can't be cached with ill reserved flags
//                pde.checkAccessRights(x86, this, I, U, W)
//                pte.checkAccessRights(x86, this, I, U, W)
//                return pte
//            }

            val cr3 = CR3(x86.cpu.cregs.cr3)

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
    var ldtr = 0L

    private val cache = Array(6) { INVALID_GDT_ENTRY }
    private val protectedModeEnabled = Array(6) { false }

    private fun isLDT(ss: Long) = ss[2] != 0L

    private fun descriptorByOffset(base: Long, offset: Long, makeDirty: Boolean): SegmentDescriptor {
        val linear = base + offset
        var data = linearRead(linear, 8, true)
        if (makeDirty) {
            data = data or 0x10000000000L
            linearWrite(linear, 8, data, true)
        }
        val result = SegmentDescriptor(data)
        // log.fine { "Reload by [${ss.hex}] GDT -> $result" }
        return result
    }

    fun readSegmentDescriptor(ss: Long): SegmentDescriptor {
        if (ss == 0L) throw x86HardwareException.GeneralProtectionFault(core.pc, ss)

        // The General Protection Fault sets an error code, which is the segment selector index
        // when the exception is segment related. Otherwise, 0.
        val base = if (isLDT(ss)) {
            if (ldtr == 0L || ldtr > gdtr.limit)
                throw GeneralException("Incorrect LDTR=${ldtr.hex} GDTR=$gdtr")
            descriptorByOffset(gdtr.base, ldtr, false).base
        } else gdtr.base

        return descriptorByOffset(base, ss and 0xFFFF_FFF8, true)
    }

    private fun PMVirtual2Linear(vAddr: Long, ssr: x86Register): Long {
        val ss = ssr.value(x86)
        if (ss > gdtr.limit)
            return RMSegment2Linear(vAddr, ssr)

        var desc = cache[ssr.reg]
        if (!desc.isValid) {
            desc = readSegmentDescriptor(ss)
            cache[ssr.reg] = desc
//            log.warning { "Invalid GDT entry in cache -> reload for 0x${vAddr.hex} ssr=$ssr[${ss.hex}] $gdtEntry" }
        }

        if (ssr is cs)
            x86.cpu.defaultSize = desc.d

        return vAddr + desc.base
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
    private fun PMLinear2Physical(vAddr: Long, size: Int, LorS: AccessAction, privilege: Boolean): Long {
        if (!x86.cpu.cregs.vpg)
            return vAddr

        val start = PageAddress4Kb(vAddr)

        if (x86.cpu.cregs.vpae) TODO("Can't translate linear address ${start.hex} in PAE mode!")

        val result = start.translate(LorS, privilege)

        if (size == 1)
            return result

        val end = PageAddress4Kb(vAddr + size - 1)
        if (start.page == end.page)
            return result

        // Access to first address of next page if page not load then PageFault generates
        PageAddress4Kb(end.page).loadTranslationTable(LorS, privilege)

        return result
    }

    private fun RMSegment2Linear(vAddr: Long, ssr: x86Register): Long = (ssr.value(x86) shl 4) + vAddr

    override fun translate(ea: Long, ss: Int, size: Int,  LorS: AccessAction): Long {
        return if (ss != UNDEF) {
            val ssr = x86Register.sreg(ss)
            if (protectedModeEnabled[ss]) {
                val lAddr = PMVirtual2Linear(ea, ssr)
                PMLinear2Physical(lAddr, size, LorS, false)
            } else RMSegment2Linear(ea, ssr)
            // log.fine { "%08X -> %08X".format(vAddr, pAddr) }
        } else ea
    }

    fun updateCache(ssr: x86Register) {
        val ss = ssr.value(x86)
        // log.finer { "$ssr = ${ss.hex}" }
        if (x86.cpu.cregs.vpe) {
            protectedModeEnabled[ssr.reg] = true
            if (ss <= gdtr.limit)
                cache[ssr.reg] = INVALID_GDT_ENTRY
//                cache[ssr.reg] = gdt(ss)
        } else
            protectedModeEnabled[ssr.reg] = false
    }

    fun invalidateGdtCache() = cache.fill(INVALID_GDT_ENTRY)

    fun invalidateProtectedMode() = protectedModeEnabled.fill(false)

    override fun reset(){
        invalidateGdtCache()
        invalidateProtectedMode()
        invalidatePagingCache()
    }

    override fun serialize(ctxt: GenericSerializer) = storeValues(
            "MMUgdtrBase" to gdtr.base.hex16,
            "MMUgdtrLimit" to gdtr.limit.hex16,
            "Ldtr" to ldtr.hex16,
            "cache" to cache.map { it.data.hex16 },
            "protectedModeEnabled" to protectedModeEnabled)

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        gdtr.base = loadHex(snapshot, "MMUgdtrBase", 0)
        gdtr.limit = loadHex(snapshot, "MMUgdtrLimit", 0)
        ldtr = loadHex(snapshot, "Ldtr", 0)
        cache.deserialize<SegmentDescriptor, String>(ctxt, snapshot["cache"]) { SegmentDescriptor(it.hexAsULong) }
        protectedModeEnabled.deserialize<Boolean, Any>(ctxt, snapshot["protectedModeEnabled"]) { it as Boolean }
    }

    override fun command(): String = "mmu"

    override fun configure(parent: ArgumentParser?, useParent: Boolean): ArgumentParser? =
            super.configure(parent, useParent)?.apply {
                subparser("get_info").apply {
                    variable<String>("-s", "--ss", required = false, help = "Request segment selector value")
                    variable<String>("-p", "--phy", required = false, help = "Found appropriate GDT for spec. physical address")
                }
                subparser("clear_mmu").apply { }
                subparser("add_memory_translator", help = "Translate memory").apply {
                    variable<String>("-v", "--startVaddress", required = true, help = "Segment start address")
                    variable<String>("-p", "--startPaddress", required = true, help = "Segment end address")
                    variable<String>("-s", "--size", required = true, help = "Segment end address")
                }
            }

    override fun process(context: IInteractive.Context): Boolean {
        if (super.process(context))
            return true

        context.result = when (context.command()) {
            "get_info" -> {
                val ss: String? = context["ss"]
                val phy: String? = context["phy"]
                when {
                    ss != null -> {
                        val desc = readSegmentDescriptor(ss.hexAsULong)
                        "base=%08X[hex] limit=%05X[hex] d=%s".format(desc.base, desc.limit, desc.d)
                    }
                    phy != null -> {
                        val offset = (0 until 0x10000 step 8).find {
                            val desc = readSegmentDescriptor(it.asULong)
                            phy.hexAsULong in desc.base..desc.base + desc.limit
                        }

                        if (offset != null) {
                            val desc = readSegmentDescriptor(offset.asULong)
                            "[${offset.hex8}] -> $desc"
                        } else "GDT RECORD NOT FOUND FOR $phy"
                    }
                    else -> {
                        val offset = (0 until 0x10000 step 8).find {
                            val desc = readSegmentDescriptor(it.asULong)
                            desc.limit != 0L && desc.isValid
                        }

                        if (offset != null) {
                            val desc = readSegmentDescriptor(offset.asULong)
                            "[${offset.hex8}] -> $desc"
                        } else "GDT RECORD NOT FOUND"
                    }
                }
            }

            "clear_mmu" -> {
                reset()
                "MMU cleared"
            }

            "add_memory_translator" -> {
                val startVaddressString: String = context["startVaddress"]
                val startPaddressString: String = context["startPaddress"]
                val sizeString: String = context["size"]
                val startVaddress = startVaddressString.toULong(16)
                val startPaddress = startPaddressString.toULong(16)
                val size = sizeString.toULong(16)
                if (startVaddress == 0L && startPaddress == 0L && size == 0x1_0000_0000) {
                    val data = SegmentDescriptor.createGdtEntry(startVaddress, 0xFFFFF)
                    x86.store(0xFFFF_FFB0, data.pack(8, ByteOrder.LITTLE_ENDIAN))
                    gdtr.base = 0xFFFF_FFB0
                    gdtr.limit = 0x10
                    ldtr = 0
                    "Memory translation added..."
                } else "Physical and virtual addresses must be 0x0000_0000 and size must be 0x1_0000_0000"
            }

            else -> return false
        }

        context.pop()

        return true
    }
}