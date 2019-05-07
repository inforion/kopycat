package ru.inforion.lab403.kopycat.cores.x86.hardware.processors

import net.sourceforge.argparse4j.inf.ArgumentParser
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.AddressTranslator
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.exceptions.x86HardwareException
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.interfaces.IInteractive
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import ru.inforion.lab403.kopycat.serializer.deserialize
import java.nio.ByteOrder
import java.util.logging.Level

/**
 * Created by batman on 09/06/16.
 *
 * Stub for x86 address translation
 */
class x86MMU(core: x86Core, name: String) : AddressTranslator(core, name) {
    companion object {
        val log = logger(Level.FINE)
        val INVALID_GDT_ENTRY = GDTEntry(-1)
    }

    val x86 = core as x86Core

    data class DescriptorRegister(var limit: Long = 0, var base: Long = 0) {
        override fun toString(): String = "[base=${base.hex} limit=${limit.hex}]"
    }

    // Load and store from/to physical memory
    fun load(base: Long) = x86.mmu.ports.outp.inq(base)
    fun store(base: Long, value: Long) = x86.mmu.ports.outp.outq(base, value)

    data class GDTEntry(val data: Long) {
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

        override fun toString(): String = "GDT[base=0x${base.hex8} limit=0x${limit.hex8} d=${d.toInt()} r=${r.toInt()} a=${a.toInt()}]"

        companion object {
            fun createGdtEntry(base: Long, limit: Long, g: Boolean = true, d: Boolean = true,
                               l: Boolean = false, avl: Boolean = false, p: Boolean = true,
                               dpl: Long = 0, e: Boolean = false, c: Boolean = false,
                               r: Boolean = true, a: Boolean = true): Long {
                val baseLow = base[15..0]
                val baseMiddle = base[23..16]
                val baseHigh = base[31..24]

                val limitLow = limit[15..0]
                val limitHigh = limit[19..16]

                val dataHi = insert(baseMiddle, 7..0).insert(if (a) 1 else 0, 8).insert(if (r) 1 else 0, 9).insert(if (c) 1 else 0, 10).insert(if (e) 1 else 0, 11).insert(1, 12).insert(dpl, 14..13).insert(if (p) 1 else 0, 15).insert(limitHigh, 19..16).insert(if (avl) 1 else 0, 20).insert(if (l) 1 else 0, 21).insert(if (d) 1 else 0, 22).insert(if (g) 1 else 0, 23).insert(baseHigh, 31..24)
                val dataLo = insert(limitLow, 15..0).insert(baseLow, 31..16)
                val entry = insert(dataLo, 31..0).insert(dataHi, 63..32)
                return entry
            }
        }
    }

    val gdtr = DescriptorRegister()
    var ldtr = 0L
    var tssr = 0L

    private val cache = Array(6) { INVALID_GDT_ENTRY }
    private val protectedModeEnabled = Array(6) { false }

    fun gdt(ss: Long): GDTEntry {
        val base: Long
        val offset: Long
        if (ss == 0L)
            throw x86HardwareException.GeneralProtectionFault(core.pc, ss)
        // The General Protection Fault sets an error code, which is the segment selector index when the exception is segment related. Otherwise, 0.
        if (ss[2] == 0L) {
            base = gdtr.base
            offset = ss
        } else {
            if (ldtr == 0L || ldtr > gdtr.limit)
                throw GeneralException("Incorrect LDTR=${ldtr.hex} GDTR=$gdtr")
            val LDTData = load(gdtr.base + ldtr)
            base = GDTEntry(LDTData).base
            offset = ss and 0xFFFF_FFF8
        }
        val address = base + offset
        val data = load(address) or 0x10000000000L
        store(address, data)
        val result = GDTEntry(data)
        // log.fine { "Reload by [${ss.hex}] GDT -> $result" }
        return result
    }

    private fun PMVirtual2Linear(vAddr: Long, ssr: x86Register): Long {
        val ss = ssr.value(x86)
        if (ss > gdtr.limit)
            return RMSegment2Linear(vAddr, ssr)

        var gdtEntry = cache[ssr.reg]
        if (!gdtEntry.isValid) {
            gdtEntry = gdt(ss)
            cache[ssr.reg] = gdtEntry
//            log.warning { "Invalid GDT entry in cache -> reload for 0x${vAddr.hex} ssr=$ssr[${ss.hex}] $gdtEntry" }
        }

        if (ssr is x86Register.SSR.cs)
            x86.cpu.defaultSize = gdtEntry.d

        return vAddr + gdtEntry.base
    }

    private fun PMLinear2Physical(vAddr: Long): Long {
        if (x86.cpu.cregs.vpae)
            TODO("Can't translate virtual address ${vAddr.hex} in PAE mode!")

        val addressOfPageDirectory = x86.cpu.cregs.cr3[31..12]
        val directory = vAddr[31..22]

        val pdeAddress = insert(addressOfPageDirectory, 31..12).insert(directory, 11..2)
        val pde = load(pdeAddress)

        if (x86.cpu.cregs.vpse && pde[7] == 1L)  // PSE == 1 and PDE[PS] == 1
            TODO("Can't translate virtual address ${vAddr.hex} for 4MB paging mode!")

        // 4KB paging mode
        if (!x86.cpu.cregs.vpse && pde[7] == 0L) {
            val addressOfPageTable = pde[31..12]
            val table = vAddr[21..12]
            val pteAddress = insert(addressOfPageTable, 31..12).insert(table, 11..2)
            val pte = load(pteAddress)

            val addressOf4KBPageFrame = pte[31..12]
            val offset = vAddr[11..0]
            val address = insert(addressOf4KBPageFrame, 31..12).insert(offset, 11..0)

//            log.warning { "Translate linear to physical ${vAddr.hex} -> ${address.hex}" }

            return address
        }

        throw GeneralException("Incorrect PSE and PDE[PS] mode for ${vAddr.hex}!")
    }

    private fun RMSegment2Linear(vAddr: Long, ssr: x86Register): Long = (ssr.value(x86) shl 4) + vAddr

    override fun translate(ea: Long, ss: Int, size: Int,  LorS: AccessAction): Long {
        return if (ss != UNDEF) {
            val ssr = x86Register.sreg(ss)
            if (protectedModeEnabled[ss]) {
                val lAddr = PMVirtual2Linear(ea, ssr)
                if (x86.cpu.cregs.vpg) PMLinear2Physical(lAddr) else lAddr
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

    fun invalidateCache() = cache.fill(INVALID_GDT_ENTRY)

    fun invalidateProtectedMode() = protectedModeEnabled.fill(false)

    override fun reset(){
        invalidateCache()
        invalidateProtectedMode()
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return mapOf(
                "MMUgdtrBase" to gdtr.base.hex16,
                "MMUgdtrLimit" to gdtr.limit.hex16,
                "Ldtr" to ldtr.hex16,
                "cache" to cache.map { it.data.hex16 },
                "protectedModeEnabled" to protectedModeEnabled)
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        gdtr.base = ctxt.loadHex(snapshot, "MMUgdtrBase", 0)
        gdtr.limit = ctxt.loadHex(snapshot, "MMUgdtrLimit", 0)
        ldtr = ctxt.loadHex(snapshot, "Ldtr", 0)
        cache.deserialize<GDTEntry, String>(ctxt, snapshot["cache"]) { GDTEntry(it.hexAsULong) }
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
                        val desc = gdt(ss.hexAsULong)
                        "base=%08X[hex] limit=%05X[hex] d=%s".format(desc.base, desc.limit, desc.d)
                    }
                    phy != null -> {
                        val offset = (0 until 0x10000 step 8).find {
                            val desc = gdt(it.asULong)
                            phy.hexAsULong in desc.base..desc.base + desc.limit
                        }

                        if (offset != null) {
                            val desc = gdt(offset.asULong)
                            "[${offset.hex8}] -> $desc"
                        } else "GDT RECORD NOT FOUND FOR $phy"
                    }
                    else -> {
                        val offset = (0 until 0x10000 step 8).find {
                            val desc = gdt(it.asULong)
                            desc.limit != 0L && desc.isValid
                        }

                        if (offset != null) {
                            val desc = gdt(offset.asULong)
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
                    val data = GDTEntry.createGdtEntry(startVaddress, 0xFFFFF)
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