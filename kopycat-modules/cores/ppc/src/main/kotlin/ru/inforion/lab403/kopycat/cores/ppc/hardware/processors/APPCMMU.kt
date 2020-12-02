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
package ru.inforion.lab403.kopycat.cores.ppc.hardware.processors

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.AddressTranslator
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.ppc.enums.systems.embedded.mmufsl.eMAS1
import ru.inforion.lab403.kopycat.cores.ppc.enums.systems.embedded.mmufsl.eMAS2
import ru.inforion.lab403.kopycat.cores.ppc.enums.systems.embedded.mmufsl.eMAS3
import ru.inforion.lab403.kopycat.cores.ppc.enums.systems.embedded.mmufsl.eMMUCSR0
import ru.inforion.lab403.kopycat.interfaces.ISerializable
import ru.inforion.lab403.kopycat.modules.cores.PPCCore
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty



abstract class APPCMMU(parent: Module, name: String, tlbs: Int = 4, tlbsize: Int = 64): AddressTranslator(parent, name) {

    enum class Access(val ind: Int) {
        X(0),
        W(2),
        R(4);
    }


    class TLBEntry(var mas1: Long,
                   var mas2: Long,
                   var mas3: Long,
                   var RPN: Long) : ISerializable { // Real page number (up to 54 bits)


        // Sorry, but I really need in it all
        enum class MasIndex {
            MAS1,
            MAS2,
            MAS3
        }

        class bitHolder(val mas: MasIndex, val bit: Int) : ReadWriteProperty<TLBEntry, Boolean> {
            override fun getValue(thisRef: TLBEntry, property: KProperty<*>): Boolean {
                return when(mas) {
                    MasIndex.MAS1 -> thisRef.mas1[bit].toBool()
                    MasIndex.MAS2 -> thisRef.mas2[bit].toBool()
                    MasIndex.MAS3 -> thisRef.mas3[bit].toBool()
                }
            }

            override fun setValue(thisRef: TLBEntry, property: KProperty<*>, value: Boolean) {
                when(mas) {
                    MasIndex.MAS1 -> thisRef.mas1 = thisRef.mas1.replace(bit, value)
                    MasIndex.MAS2 -> thisRef.mas2 = thisRef.mas2.replace(bit, value)
                    MasIndex.MAS3 -> thisRef.mas3 = thisRef.mas3.replace(bit, value)
                }
            }
        }

        class fieldHolder(val mas: MasIndex, val field: IntRange) : ReadWriteProperty<TLBEntry, Long> {
            override fun getValue(thisRef: TLBEntry, property: KProperty<*>): Long {
                return when(mas) {
                    MasIndex.MAS1 -> thisRef.mas1[field]
                    MasIndex.MAS2 -> thisRef.mas2[field]
                    MasIndex.MAS3 -> thisRef.mas3[field]
                }
            }

            override fun setValue(thisRef: TLBEntry, property: KProperty<*>, value: Long) {
                when(mas) {
                    MasIndex.MAS1 -> thisRef.mas1 = thisRef.mas1.replace(field, value)
                    MasIndex.MAS2 -> thisRef.mas2 = thisRef.mas2.replace(field, value)
                    MasIndex.MAS3 -> thisRef.mas3 = thisRef.mas3.replace(field, value)
                }
            }
        }


        var V: Boolean by bitHolder(MasIndex.MAS1, eMAS1.V.bit)           // Valid
        var IPROT: Boolean by bitHolder(MasIndex.MAS1, eMAS1.IPROT.bit)   // Invalidation protection
        var TID: Long by fieldHolder(MasIndex.MAS1, eMAS1.TID)            // Translation ID (imp dep)
        var TS: Boolean by bitHolder(MasIndex.MAS1, eMAS1.TS.bit)         // Translation Address Space
        var SIZE: Long by fieldHolder(MasIndex.MAS1, eMAS1.TSIZE)         // Page Size 0..15


        var EPN: Long by fieldHolder(MasIndex.MAS2, eMAS2.EPN)            // Effective Page Number (up to 54 bits)
        var VLE: Boolean by bitHolder(MasIndex.MAS2, eMAS2.VLE.bit)       // Variable length encoding
        var W: Boolean by bitHolder(MasIndex.MAS2, eMAS2.W.bit)           // Write-through required
        var I: Boolean by bitHolder(MasIndex.MAS2, eMAS2.I.bit)           // Caching inhibited
        var M: Boolean by bitHolder(MasIndex.MAS2, eMAS2.M.bit)           // Memory coherence required
        var G: Boolean by bitHolder(MasIndex.MAS2, eMAS2.G.bit)           // Guarded
        var E: Boolean by bitHolder(MasIndex.MAS2, eMAS2.E.bit)           // Endian mode
        var ACM: Long by fieldHolder(MasIndex.MAS2, eMAS2.ACM)            // Alternate coherency mode

        var U0: Boolean by bitHolder(MasIndex.MAS3, eMAS3.U0.bit)         // User-definable storage control
        var U1: Boolean by bitHolder(MasIndex.MAS3, eMAS3.U1.bit)
        var U2: Boolean by bitHolder(MasIndex.MAS3, eMAS3.U2.bit)
        var U3: Boolean by bitHolder(MasIndex.MAS3, eMAS3.U3.bit)
        var UX: Boolean by bitHolder(MasIndex.MAS3, eMAS3.UX.bit)         // User state execute enable
        var SX: Boolean by bitHolder(MasIndex.MAS3, eMAS3.SX.bit)         // Supervisor state execute enable
        var UW: Boolean by bitHolder(MasIndex.MAS3, eMAS3.UW.bit)         // User state write enable
        var SW: Boolean by bitHolder(MasIndex.MAS3, eMAS3.SW.bit)         // Supervisor state write enable
        var UR: Boolean by bitHolder(MasIndex.MAS3, eMAS3.UR.bit)         // User state read enable
        var SR: Boolean by bitHolder(MasIndex.MAS3, eMAS3.SR.bit)         // Supervisor state read enable

        val accesses
            get() = arrayOf(SX, UX, SW, UW, SR, UR)

        val epn: Long
            get() = EPN shl eMAS2.EPNLow.bit
        val rpn: Long
            get() = RPN shl eMAS2.EPNLow.bit

        val pageRange: IntRange
            get() = if (SIZE.toInt() >= 0b1011)
                throw GeneralException("Page size too high")
            else
                31..(10 + SIZE.toInt()*2)

        val offsetRange: IntRange
            get() = if (SIZE.toInt() >= 0b1011)
                throw GeneralException("Page size too high")
            else
                (9 + SIZE.toInt()*2)..0

        fun match(ea: Long, AS: Boolean, procID: Long): Boolean =
                V &&
                (AS == TS) &&
                ((TID == 0L) || (TID == procID)) &&
                (epn[pageRange] == ea[pageRange])

        fun granted(access: Access, PR: Boolean): Boolean = accesses[access.ind + PR.toInt()]

        fun translate(ea: Long): Long = rpn or ea[offsetRange]

        fun pageString(): String {
            val value = 1 shl (2 * (SIZE.toInt() % 5))
            val postfix = when(SIZE / 5) {
                0L -> "KB"
                1L -> "MB"
                2L -> "TB"
                else -> throw GeneralException("Wrong size")
            }
            return "$value$postfix"
        }

        override fun toString(): String {
            return "${epn.hex8} -> ${rpn.hex8} (Page = ${pageString()}, Valid = $V, TS = $TS, TID = ${TID.hex8}, Access = ${accesses.map {it.toInt()}})"
        }

        override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
            return mapOf(
                    "MAS1" to mas1.hex8,
                    "MAS2" to mas2.hex8,
                    "MAS3" to mas3.hex8,
                    "RPN" to RPN.hex8)
        }

        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            mas1 = (snapshot["MAS1"] as String).hexAsULong
            mas2 = (snapshot["MAS2"] as String).hexAsULong
            mas3 = (snapshot["MAS3"] as String).hexAsULong
            RPN = (snapshot["RPN"] as String).hexAsULong
        }

        constructor() : this(0, 0, 0, 0) { this.SIZE = 1 }

        companion object {

            @Suppress("UNUSED_PARAMETER")
            fun createFromSnapshot(ctxt: GenericSerializer, snapshot: Map<String, String>): TLBEntry {
                return TLBEntry(
                        (snapshot["MAS1"] as String).hexAsULong,
                        (snapshot["MAS2"] as String).hexAsULong,
                        (snapshot["MAS3"] as String).hexAsULong,
                        (snapshot["RPN"] as String).hexAsULong)
            }
        }
    }

    val TLB = Array(tlbs) { Array(tlbsize) { TLBEntry() } }
    val cache = mutableListOf<TLBEntry>()


    fun createEntry(tlb: Int, ent: Int, params: Map<String, Long>) {

        val entry = TLB[tlb][ent]

        val oldEntry = cache.find { it == entry }
        if (oldEntry != null)
            cache.remove(oldEntry)
        cache.add(entry)

        entry.RPN = params["RPN"]?.get(entry.pageRange) ?: 0

        entry.V = params["V"]?.toBool() ?: false
        entry.IPROT = params["IPROT"]?.toBool() ?: false
        entry.TID = params["TID"] ?: 0
        entry.TS = params["TS"]?.toBool() ?: false
        entry.SIZE = params["SIZE"] ?: 1

        entry.EPN = params["EPN"]?.get(entry.pageRange) ?: 0
        entry.VLE = params["VLE"]?.toBool() ?: false
        entry.W = params["W"]?.toBool() ?: false
        entry.I = params["I"]?.toBool() ?: false
        entry.M = params["M"]?.toBool() ?: false
        entry.G = params["G"]?.toBool() ?: false
        entry.E = params["E"]?.toBool() ?: false
        entry.ACM = params["ACM"] ?: 0

        entry.U0 = params["U0"]?.toBool() ?: false
        entry.U1 = params["U1"]?.toBool() ?: false
        entry.U2 = params["U2"]?.toBool() ?: false
        entry.U3 = params["U3"]?.toBool() ?: false
        entry.UX = params["UX"]?.toBool() ?: false
        entry.SX = params["SX"]?.toBool() ?: false
        entry.UW = params["UW"]?.toBool() ?: false
        entry.SW = params["SW"]?.toBool() ?: false
        entry.UR = params["UR"]?.toBool() ?: false
        entry.SR = params["SR"]?.toBool() ?: false
    }

    fun tlbWrite(tlb: Int, ent: Int, mas1: Long, mas2: Long, mas3: Long, rpn: Long) {
        val entry = TLBEntry(mas1, mas2, mas3, rpn)
        val oldEntry = cache.find { it == TLB[tlb][ent] }
        if (oldEntry != null)
            cache.remove(oldEntry)
        TLB[tlb][ent] = entry
        log.severe { entry.toString() }
        cache.add(entry)
    }

    fun tlbInvalidate(n: Int, force: Boolean=false) {
        for (entry in TLB[n])
            if (!entry.IPROT || force)
                entry.V = false
    }


    fun MMUCSR0Update(value: Long) {
        for (i: Int in 0..3)
           if (value[eMMUCSR0.TLBn_PS(i)] != 0L)
               TODO("Change TLB size isn't implemented")

        for (i: Int in 0..5)
            if (value[eMMUCSR0.TLBn_FI(i)] != 0L)
                tlbInvalidate(if (i < 4) i else i - 2) //Kostil
    }

    abstract fun processID(): Array<Long>

    fun cacheRead(ea: Long, AS: Boolean, PID: Array<Long>): TLBEntry? {
        val entry = cache.find {
            entry -> PID.find{ p -> entry.match(ea, AS, p) } != null
        } ?: return null

        cache.remove(entry)
        cache.add(0, entry)

        return entry
    }

    override fun reset() {
        cache.clear()
        for (tlb in 0 until TLB.size)
            tlbInvalidate(tlb, true)
    }

    override fun translate(ea: Long, ss: Int, size: Int, LorS: AccessAction): Long {
        val ppccore = core as PPCCore

        val instFetch = ppccore.cpu.pc == ea
        val AS = if (instFetch) ppccore.cpu.msrBits.IS else ppccore.cpu.msrBits.DS
        val PID = processID()

        if (cache.isEmpty()) return ea

        val entry = cacheRead(ea, AS, PID) ?: throw if (instFetch)
            ppccore.exceptionHolder.tlbInstructionException(ppccore.pc, ea, AS.asLong)
        else
            ppccore.exceptionHolder.tlbDataException(ppccore.pc - 4, LorS == AccessAction.STORE, ea, AS.asLong)


        val access = when {
            instFetch -> Access.X
            LorS == AccessAction.LOAD -> Access.R
            LorS == AccessAction.STORE -> Access.W
            else -> throw GeneralException("Unreachable error")
        }
        val PR = ppccore.cpu.msrBits.PR
        if (!entry.granted(access, PR))
            throw GeneralException("Access translation denied")

        return entry.translate(ea)
    }


    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return mapOf(
                "TLB" to TLB.map {
                    tlb -> tlb.map { entry -> entry.serialize(ctxt) }
                })
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        cache.clear()
        (snapshot["TLB"] as ArrayList<ArrayList<Map<String, String>>>).forEachIndexed {
            i, tlb -> tlb.forEachIndexed { j, map ->
            TLB[i][j].deserialize(ctxt, map)
            if (TLB[i][j].V)
                cache.add(TLB[i][j]) }
        }
    }

}