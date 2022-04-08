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


    class TLBEntry(var mas1: ULong,
                   var mas2: ULong,
                   var mas3: ULong,
                   var RPN: ULong) : ISerializable { // Real page number (up to 54 bits)


        // Sorry, but I really need in it all
        enum class MasIndex {
            MAS1,
            MAS2,
            MAS3
        }

        class bitHolder(val mas: MasIndex, val bit: Int) : ReadWriteProperty<TLBEntry, Boolean> {
            override fun getValue(thisRef: TLBEntry, property: KProperty<*>): Boolean {
                return when(mas) {
                    MasIndex.MAS1 -> thisRef.mas1[bit].truth
                    MasIndex.MAS2 -> thisRef.mas2[bit].truth
                    MasIndex.MAS3 -> thisRef.mas3[bit].truth
                }
            }

            override fun setValue(thisRef: TLBEntry, property: KProperty<*>, value: Boolean) {
                when(mas) {
                    MasIndex.MAS1 -> thisRef.mas1 = thisRef.mas1.insert(value.ulong, bit)
                    MasIndex.MAS2 -> thisRef.mas2 = thisRef.mas2.insert(value.ulong, bit)
                    MasIndex.MAS3 -> thisRef.mas3 = thisRef.mas3.insert(value.ulong, bit)
                }
            }
        }

        class fieldHolder(val mas: MasIndex, val field: IntRange) : ReadWriteProperty<TLBEntry, ULong> {
            override fun getValue(thisRef: TLBEntry, property: KProperty<*>): ULong = when(mas) {
                MasIndex.MAS1 -> thisRef.mas1[field]
                MasIndex.MAS2 -> thisRef.mas2[field]
                MasIndex.MAS3 -> thisRef.mas3[field]
            }

            override fun setValue(thisRef: TLBEntry, property: KProperty<*>, value: ULong) = when(mas) {
                MasIndex.MAS1 -> thisRef.mas1 = thisRef.mas1.insert(value, field)
                MasIndex.MAS2 -> thisRef.mas2 = thisRef.mas2.insert(value, field)
                MasIndex.MAS3 -> thisRef.mas3 = thisRef.mas3.insert(value, field)
            }
        }


        var V: Boolean by bitHolder(MasIndex.MAS1, eMAS1.V.bit)           // Valid
        var IPROT: Boolean by bitHolder(MasIndex.MAS1, eMAS1.IPROT.bit)   // Invalidation protection
        var TID: ULong by fieldHolder(MasIndex.MAS1, eMAS1.TID)            // Translation ID (imp dep)
        var TS: Boolean by bitHolder(MasIndex.MAS1, eMAS1.TS.bit)         // Translation Address Space
        var SIZE: ULong by fieldHolder(MasIndex.MAS1, eMAS1.TSIZE)         // Page Size 0..15


        var EPN: ULong by fieldHolder(MasIndex.MAS2, eMAS2.EPN)            // Effective Page Number (up to 54 bits)
        var VLE: Boolean by bitHolder(MasIndex.MAS2, eMAS2.VLE.bit)       // Variable length encoding
        var W: Boolean by bitHolder(MasIndex.MAS2, eMAS2.W.bit)           // Write-through required
        var I: Boolean by bitHolder(MasIndex.MAS2, eMAS2.I.bit)           // Caching inhibited
        var M: Boolean by bitHolder(MasIndex.MAS2, eMAS2.M.bit)           // Memory coherence required
        var G: Boolean by bitHolder(MasIndex.MAS2, eMAS2.G.bit)           // Guarded
        var E: Boolean by bitHolder(MasIndex.MAS2, eMAS2.E.bit)           // Endian mode
        var ACM: ULong by fieldHolder(MasIndex.MAS2, eMAS2.ACM)            // Alternate coherency mode

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

        val accesses get() = arrayOf(SX, UX, SW, UW, SR, UR)

        val epn get() = EPN shl eMAS2.EPNLow.bit
        val rpn get() = RPN shl eMAS2.EPNLow.bit

        val pageRange: IntRange
            get() = if (SIZE.int >= 0b1011)
                throw GeneralException("Page size too high")
            else
                31..(10 + SIZE.int * 2)

        val offsetRange: IntRange
            get() = if (SIZE.int >= 0b1011)
                throw GeneralException("Page size too high")
            else
                (9 + SIZE.int * 2)..0

        fun match(ea: ULong, AS: Boolean, procID: ULong): Boolean =
                V &&
                (AS == TS) &&
                ((TID == 0uL) || (TID == procID)) &&
                (epn[pageRange] == ea[pageRange])

        fun granted(access: Access, PR: Boolean): Boolean = accesses[access.ind + PR.int]

        fun translate(ea: ULong) = rpn or ea[offsetRange]

        fun pageString(): String {
            val value = 1 shl (2 * (SIZE.int % 5))
            val postfix = when (SIZE / 5u) {
                0uL -> "KB"
                1uL -> "MB"
                2uL -> "GB"
                else -> throw GeneralException("Wrong size")
            }
            return "$value$postfix"
        }

        override fun toString() =
            "${epn.hex8} -> ${rpn.hex8} (Page = ${pageString()}, Valid = $V, TS = $TS, TID = ${TID.hex8}, Access = ${accesses.map { it.int }})"

        override fun serialize(ctxt: GenericSerializer): Map<String, Any> = mapOf(
                "MAS1" to mas1.hex8,
                "MAS2" to mas2.hex8,
                "MAS3" to mas3.hex8,
                "RPN" to RPN.hex8)

        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            mas1 = (snapshot["MAS1"] as String).ulongByHex
            mas2 = (snapshot["MAS2"] as String).ulongByHex
            mas3 = (snapshot["MAS3"] as String).ulongByHex
            RPN = (snapshot["RPN"] as String).ulongByHex
        }

        constructor() : this(0u, 0u, 0u, 0u) { SIZE = 1u }

        companion object {

            @Suppress("UNUSED_PARAMETER")
            fun createFromSnapshot(ctxt: GenericSerializer, snapshot: Map<String, String>): TLBEntry = TLBEntry(
                    (snapshot["MAS1"] as String).ulongByHex,
                    (snapshot["MAS2"] as String).ulongByHex,
                    (snapshot["MAS3"] as String).ulongByHex,
                    (snapshot["RPN"] as String).ulongByHex)
        }
    }

    val TLB = Array(tlbs) { Array(tlbsize) { TLBEntry() } }
    val cache = mutableListOf<TLBEntry>()


    fun createEntry(tlb: Int, ent: Int, params: Map<String, ULong>) {

        val entry = TLB[tlb][ent]

        val oldEntry = cache.find { it == entry }
        if (oldEntry != null)
            cache.remove(oldEntry)
        cache.add(entry)

        entry.RPN = params["RPN"]?.get(entry.pageRange) ?: 0u

        entry.V = params["V"]?.truth ?: false
        entry.IPROT = params["IPROT"]?.truth ?: false
        entry.TID = params["TID"] ?: 0u
        entry.TS = params["TS"]?.truth ?: false
        entry.SIZE = params["SIZE"] ?: 1u

        entry.EPN = params["EPN"]?.get(entry.pageRange) ?: 0u
        entry.VLE = params["VLE"]?.truth ?: false
        entry.W = params["W"]?.truth ?: false
        entry.I = params["I"]?.truth ?: false
        entry.M = params["M"]?.truth ?: false
        entry.G = params["G"]?.truth ?: false
        entry.E = params["E"]?.truth ?: false
        entry.ACM = params["ACM"] ?: 0u

        entry.U0 = params["U0"]?.truth ?: false
        entry.U1 = params["U1"]?.truth ?: false
        entry.U2 = params["U2"]?.truth ?: false
        entry.U3 = params["U3"]?.truth ?: false
        entry.UX = params["UX"]?.truth ?: false
        entry.SX = params["SX"]?.truth ?: false
        entry.UW = params["UW"]?.truth ?: false
        entry.SW = params["SW"]?.truth ?: false
        entry.UR = params["UR"]?.truth ?: false
        entry.SR = params["SR"]?.truth ?: false
    }

    fun tlbRead(tlb: Int, ent: Int) = TLB[tlb][ent]

    fun tlbWrite(tlb: Int, ent: Int, mas1: ULong, mas2: ULong, mas3: ULong, rpn: ULong) {
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


    fun MMUCSR0Update(value: ULong) {
        for (i: Int in 0..3)
           if (value[eMMUCSR0.TLBn_PS(i)] != 0uL)
               TODO("Change TLB size isn't implemented")

        for (i: Int in 0..5)
            if (value[eMMUCSR0.TLBn_FI(i)] != 0uL)
                tlbInvalidate(if (i < 4) i else i - 2) //Kostil
    }

    abstract fun processID(): Array<ULong>

    fun cacheRead(ea: ULong, AS: Boolean, PID: Array<ULong>): TLBEntry? {
        val entry = cache.find {
            entry -> PID.find { p -> entry.match(ea, AS, p) } != null
        } ?: return null

        cache.remove(entry)
        cache.add(0, entry)

        return entry
    }

    override fun reset() {
        cache.clear()
        for (tlb in 0 until TLB.size)
            tlbInvalidate(tlb, true)

        val entry0 = TLBEntry(0u, 0u, 0u, 0u).apply {
            V = true
            TS = false
            TID = 0x0u
            EPN = 0xFFFFFu
            RPN = 0xFFFFFu
            SIZE = 1u // 4 KB
            SX = true
            SR = true
            SW = true
            UX = false
            UR = false
            UW = false
            W = false
            I = true
            M = false
            G = false
            E = false
            //X0-X1 = 00
            U0 = false
            U1 = false
            U2 = false
            U3 = false
            IPROT = true
        }
        TLB[1][0] = entry0
        cache.add(entry0)
    }

    override fun translate(ea: ULong, ss: Int, size: Int, LorS: AccessAction): ULong {
        val ppccore = core as PPCCore

        val instFetch = ppccore.cpu.pc == ea // TODO: LorS?
        val AS = if (instFetch) ppccore.cpu.msrBits.IS else ppccore.cpu.msrBits.DS
        val PID = processID()

        val entry = cacheRead(ea, AS, PID) ?: return -1uL /* throw if (instFetch)
            ppccore.exceptionHolder.tlbInstructionException(ppccore.pc, ea, AS.asLong)
        else
            ppccore.exceptionHolder.tlbDataException(ppccore.pc - 4, LorS == AccessAction.STORE, ea, AS.asLong)*/


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