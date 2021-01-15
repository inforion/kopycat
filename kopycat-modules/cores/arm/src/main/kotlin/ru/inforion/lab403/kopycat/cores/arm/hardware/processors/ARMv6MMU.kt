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
@file:Suppress("NOTHING_TO_INLINE")

package ru.inforion.lab403.kopycat.cores.arm.hardware.processors

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.AddressTranslator
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.interfaces.ISerializable
import ru.inforion.lab403.kopycat.modules.cores.AARMv6Core
import ru.inforion.lab403.kopycat.serializer.loadEnum
import ru.inforion.lab403.kopycat.serializer.loadValue
import ru.inforion.lab403.kopycat.serializer.storeValues


class ARMv6MMU(parent: Module, name: String) : AddressTranslator(parent, name) {
    private var privEnabled: Boolean = false

    var enabled: Boolean
        get() = privEnabled
        set(value) {
            if (!value)
                tlbInvalidate()
            privEnabled = value
        }

    val acore = parent as AARMv6Core

    // See B3.19.2
    inline fun FCSETranslate(va: Long): Long {
        if (va[31..25] == 0L)
            //mva = FCSEIDR.PID:va<24:0>;
            // If FCSE is not implemented, FCSEIDR.PID is '0000000'
            return va and bitMask(24..0)
        return va
    }

    // See B3.19.6
    inline fun ConvertAttrsHints(rgn: Int): Int {
        // Converts the Short-descriptor attribute fields for Normal memory as used
        // in the TTBR and TEX fields to the orthogonal concepts of Attributes and Hints
        return when {
            rgn == 0b00 -> 0b0000 // Non-cacheable
            rgn and 1 == 1 -> { // Write-Back
                val invbit = ((rgn shr 1) xor 1)
                (invbit shl 2) or 0b1011
            }
            else -> 0b1010 // Write-Through
        }
    }

    enum class DAbort {
        AccessFlag,
        Alignment,
        Background,
        Domain,
        Permission,
        Translation,
        SyncExternal,
        SyncExternalonWalk,
        SyncParity,
        SyncParityonWalk,
        AsyncParity,
        AsyncExternal,
        SyncWatchpoint,
        AsyncWatchpoint,
        TLBConflict,
        Lockdown,
        Coproc,
        ICacheMaint
    }

    fun TLBLookupCameFromCacheMaintenance(): Boolean {
        TODO("TLBLookupCameFromCacheMaintenance")
    }

    fun EncodeLDFSR(x: DAbort, y: Int): Long {
        TODO("EncodeLDFSR")
    }

    // See B2.4.10
    inline fun EncodeSDFSR(type: DAbort, level: Int): Long {
        val levelbit = level and 0b10
        val result: Int = when (type) {
            DAbort.AccessFlag -> if (level == 1) 0b00011 else 0b00110
            DAbort.Alignment -> 0b0001
            DAbort.Permission -> 0b01101 or levelbit
            DAbort.Domain -> (0b01001 or (level and 0b10))
            DAbort.Translation -> 0b00101 or levelbit
            DAbort.SyncExternal -> 0b01000
            DAbort.SyncExternalonWalk -> 0b01100 or levelbit
            DAbort.SyncParity -> 0b11001
            DAbort.SyncParityonWalk -> 0b11100 or levelbit
            DAbort.AsyncParity -> 0b11000
            DAbort.AsyncExternal -> 0b10110
            DAbort.SyncWatchpoint,
            DAbort.AsyncWatchpoint -> 0b00010
            DAbort.TLBConflict -> 0b10000
            DAbort.Lockdown -> 0b10100
            DAbort.Coproc -> 0b11010
            DAbort.ICacheMaint -> 0b00100
            else -> TODO("UNKNOWN")
        }
        return result.toLong()
    }

    // See B2.4.10
    fun DataAbort(vaddress: Long,
                  ipaddress: Long,
                  domain: Int,
                  level: Int,
                  iswrite: Boolean,
                  type: DAbort,
                  taketohypmode: Boolean,
                  secondstageabort: Boolean,
                  ipavalid: Boolean,
                  LDFSRformat: Boolean,
                  s2fs1walk: Boolean) {
        // Data Abort handling for Memory Management generated aborts

        // TODO: Only VMSA implementation
        if (!taketohypmode) {
            var DFSRString = 0L

            if (type in setOf(DAbort.AsyncParity, DAbort.AsyncExternal, DAbort.AsyncWatchpoint) ||
                    (type == DAbort.SyncWatchpoint /*&& DBGDIDR.Version <= 4*/)) {
//                acore.cpu.vmsa.dfar.value = UNKNOWN
            } else
                acore.cpu.vmsa.dfar.value = vaddress

            if (LDFSRformat) {
                // new format
                DFSRString = if (TLBLookupCameFromCacheMaintenance()) (DFSRString set 13) else (DFSRString clr 13)
                if (type in setOf(DAbort.AsyncExternal, DAbort.SyncExternal)) {
//                        DFSRString < 12 > = IMPLEMENTATION_DEFINED
                    TODO("IMPLEMENTATION_DEFINED")
                } else
                    DFSRString = DFSRString clr 12

                if (type in setOf(DAbort.SyncWatchpoint, DAbort.AsyncWatchpoint)) {
//                        DFSRString<11> = bit UNKNOWN;
                } else
                    DFSRString = if (iswrite) (DFSRString set 11) else (DFSRString clr 11)
//
//                DFSRString<10> = bit UNKNOWN;
                DFSRString = DFSRString set 9
//                DFSRString<8:6> = bits(3) UNKNOWN;
                DFSRString = DFSRString.insert(EncodeLDFSR(type, level), 5..0)
            } else {
                if (false /*HaveLPAE()*/)
                    DFSRString = if (TLBLookupCameFromCacheMaintenance()) (DFSRString set 13) else (DFSRString clr 13)

                if (type in setOf(DAbort.AsyncExternal, DAbort.SyncExternal)) {
//                    DFSRString < 12 > = IMPLEMENTATION_DEFINED;
                } else
                    DFSRString = DFSRString clr 12

                if (type in setOf(DAbort.SyncWatchpoint, DAbort.AsyncWatchpoint)) {
//                    DFSRString<11> = bit UNKNOWN;
                } else
                    DFSRString = if (iswrite) (DFSRString set 11) else (DFSRString clr 11)

                DFSRString = DFSRString clr 9
//                DFSRString<8> = bit UNKNOWN;
                val domain_valid = (type == DAbort.Domain ||
                        (level == 2 &&
                                type in setOf(DAbort.Translation, DAbort.AccessFlag,
                                DAbort.SyncExternalonWalk, DAbort.SyncParityonWalk)) ||
                        (/*!HaveLPAE() && */false && type == DAbort.Permission))
                if (domain_valid) {
                    DFSRString = DFSRString.insert(domain.toLong(), 7..4)
                } else {
//                    DFSRString < 7:4> = bits(4) UNKNOWN;
                }
                val sdfsr = EncodeSDFSR(type, level)
                DFSRString = if (sdfsr[4] == 1L) (DFSRString set 10) else (DFSRString clr 10)
                DFSRString = DFSRString.insert(sdfsr[3..0], 3..0)
            }
            acore.cpu.vmsa.dfsr.bits13_0 = DFSRString
        } else {
            TODO("taketohypmode")
        }

        throw ARMHardwareException.DataAbortException
    }


    // See B2.4.1
    enum class MemType {
        Normal,
        Device,
        StronglyOrdered
    }

    class MemoryAttributes(
            var type: MemType = MemType.Normal,
            // The possible encodings for each attributes field are as follows:
            // '00' = Non-cacheable; '10' = Write-Through
            // '11' = Write-Back; '01' = RESERVED
            var innerattrs: Int = 0, // 2 bits
            var outerattrs: Int = 0, // 2 bits

            // the possible encodings for the hints are as follows
            // '00' = No-Allocate; '01' = Write-Allocate
            // '10' = Read-Allocate; ;'11' = Read-Allocate and Write-Allocate
            var innerhints: Int = 0, // 2 bits
            var outerhints: Int = 0, // 2 bits

            var innertransient: Boolean = false,
            var outertransient: Boolean = false,

            var shareable: Boolean = false,
            var outersharable: Boolean = false
    ) : ISerializable {
        override fun serialize(ctxt: GenericSerializer) = storeValues(
                "type" to type,
                "innerattrs" to innerattrs,
                "outerattrs" to outerattrs,
                "innerhints" to innerhints,
                "outerhints" to outerhints,
                "innertransient" to innertransient,
                "outertransient" to outertransient,
                "shareable" to shareable,
                "outersharable" to outersharable
        )

        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            type = loadEnum(snapshot, "type")
            innerattrs = loadValue(snapshot, "innerattrs")
            outerattrs = loadValue(snapshot, "outerattrs")
            innerhints = loadValue(snapshot, "innerhints")
            outerhints = loadValue(snapshot, "outerhints")
            innertransient = loadValue(snapshot, "innertransient")
            outertransient = loadValue(snapshot, "outertransient")
            shareable = loadValue(snapshot, "shareable")
            outersharable = loadValue(snapshot, "outersharable")
        }
    }

    class FullAddress(
            var physicalAddress: Long = 0L, // 40 bits
            var ns: Boolean = false, // false = Secure, true = Non-secure


            // Non-standard option:
            var mask: Long = 0L
    ): ISerializable {

        fun updateAddress(mva: Long) {
            physicalAddress = (physicalAddress and mask) or (mva and mask.inv())
        }

        override fun serialize(ctxt: GenericSerializer) = storeValues(
                "physicalAddress" to physicalAddress,
                "ns" to ns,
                "mask" to mask
        )

        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            physicalAddress = loadValue(snapshot, "physicalAddress")
            ns = loadValue(snapshot, "ns")
            mask = loadValue(snapshot, "mask")
        }
    }

    class AddressDescriptor(
            val memattrs: MemoryAttributes = MemoryAttributes(),
            val paddress: FullAddress = FullAddress()
    ): ISerializable {
        override fun serialize(ctxt: GenericSerializer) = storeValues(
                "memattrs" to memattrs.serialize(ctxt),
                "paddress" to paddress.serialize(ctxt)
        )

        @Suppress("UNCHECKED_CAST")
        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            memattrs.deserialize(ctxt, snapshot["memattrs"] as Map<String, Any>)
            paddress.deserialize(ctxt, snapshot["paddress"] as Map<String, Any>)
        }
    }

    class Permissions(
            var ap: Int = 0, // Access permission bits
            var xn: Boolean = false, // Execute-never bit
            var pxn: Boolean = false // Privileged execute-never bit
    ) : ISerializable {
        override fun serialize(ctxt: GenericSerializer) = storeValues(
                "ap" to ap,
                "xn" to xn,
                "pxn" to pxn
        )

        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            ap = loadValue(snapshot, "ap")
            xn = loadValue(snapshot, "xn")
            pxn = loadValue(snapshot, "pxn")
        }
    }

    // See B3.19.5
    class TLBRecord(
            var perms: Permissions = Permissions(),
            var nG: Boolean = false, // '0' = Global, '1' = not Global
            var domain: Int = 0,
            var contiguousbit: Boolean = false,
            var level: Int = 0, // generalises Section/Page to Table level
            var blocksize: Int = 0, // describes size of memory translated in KBytes
            var addrdesc: AddressDescriptor = AddressDescriptor()
    ) : ISerializable {
        override fun serialize(ctxt: GenericSerializer) = storeValues(
                "perms" to perms.serialize(ctxt),
                "nG" to nG,
                "domain" to domain,
                "contiguousbit" to contiguousbit,
                "level" to level,
                "blocksize" to blocksize,
                "addrdesc" to addrdesc.serialize(ctxt)
        )

        @Suppress("UNCHECKED_CAST")
        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            perms.deserialize(ctxt, snapshot["perms"] as Map<String, Any>)
            nG = loadValue(snapshot, "nG")
            domain = loadValue(snapshot, "domain")
            contiguousbit = loadValue(snapshot, "contiguousbit")
            level = loadValue(snapshot, "level")
            blocksize = loadValue(snapshot, "blocksize")
            addrdesc.deserialize(ctxt, snapshot["addrdesc"] as Map<String, Any>)
        }
    }

    fun TranslationTableWalkLD(
            ia: Long,
            va: Long,
            is_write: Boolean,
            stage1: Boolean,
            s2fs1walk: Boolean,
            size: Int): TLBRecord {
        TODO("TranslationTableWalkLD")
    }

    fun CheckPermissionS2(
            perms: Permissions,
            mva: Long,
            ipa: Long,
            level: Int,
            is_write: Boolean,
            s2fs1walk: Boolean) {
        TODO("CheckPermissionS2")
    }

    fun CombineS1S2Desc(s1desc: AddressDescriptor, s2desc: AddressDescriptor): AddressDescriptor {
        TODO("CombineS1S2Desc")
    }

    // See B3.19.6
    // This function is called from a stage 1 translation table walk when
    // the accesses generated from that requires a second stage of translation
    fun SecondStageTranslate(
            s1outaddrdesc: AddressDescriptor,
            mva: Long,
            size: Int,
            is_write: Boolean): AddressDescriptor {

        val result: AddressDescriptor
        val tlbrecordS2: TLBRecord

        if (acore.cpu.haveVirtExt && !acore.cpu.IsSecure() && !acore.cpu.CurrentModeIsHyp()) {
            if (acore.cpu.ver.hcr.vm) { // second stage enabled
                val s2ia = s1outaddrdesc.paddress.physicalAddress
                val stage1 = false
                val s2fs1walk = true
                tlbrecordS2 = TranslationTableWalkLD(s2ia, mva, is_write, stage1, s2fs1walk, size)
                CheckPermissionS2(tlbrecordS2.perms, mva, s2ia, tlbrecordS2.level, false, s2fs1walk)
                if (acore.cpu.ver.hcr.ptw) {
                    // protected table walk
                    if (tlbrecordS2.addrdesc.memattrs.type != MemType.Normal) {
//                        domain = bits(4) UNKNOWN;
//                        taketohypmode = TRUE;
//                        secondstageabort = TRUE;
//                        ipavalid = TRUE;
//                        LDFSRformat = TRUE;
//                        s2fs1walk = TRUE;
//                        DataAbort(mva, s2ia, domain, tlbrecordS2.level,
//                                is_write, DAbort_Permission, taketohypmode,
//                                secondstageabort, ipavalid, LDFSRformat, s2fs1walk);
                        TODO("Not implemented for non-normal MemType")
                    }

                }
                result = CombineS1S2Desc(s1outaddrdesc, tlbrecordS2.addrdesc)
            } else {
                result = s1outaddrdesc
            }

            return result
        }
        throw GeneralException("We shouldn't be here")
    }


    abstract class FirstLevelTLBEntry(val l1desc: Long) : ISerializable{
        abstract fun getRecord(mva: Long, is_write: Boolean): TLBRecord
    }

    inner class Fault(l1desc: Long): FirstLevelTLBEntry(l1desc) {
        override fun getRecord(mva: Long, is_write: Boolean): TLBRecord {
            val taketohypmode = false
            val IA = 0L // 40 bits
            val ipavalid = false
            val stage2 = false
            val LDFSRformat = false
            val s2fs1walk = false
            val domain = 0
            val level = 1

            DataAbort(mva, IA, domain, level, is_write, DAbort.Translation,
                    taketohypmode, stage2, ipavalid, LDFSRformat, s2fs1walk)
            throw IllegalStateException("Unreachable code")
        }

        override fun serialize(ctxt: GenericSerializer): Map<String, Any> = mapOf()

        @Suppress("UNCHECKED_CAST")
        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) = Unit
    }

    inner class Page(l1desc: Long): FirstLevelTLBEntry(l1desc) {
        val l2descs = Array<TLBRecord?>(256) { null }

        val domain = l1desc[8..5].toInt()
        val level = 2
        val pxn = l1desc[2].toBool()
        val NS = l1desc[3].toBool()

        override fun getRecord(mva: Long, is_write: Boolean): TLBRecord {
            val l2ind = mva[19..12]
            val tl2desc = l2descs[l2ind.toInt()]

            val desc = if (tl2desc != null) tl2desc else {
                val l2descaddr = (l1desc and bitMask(31..10)) or (mva[19..12] shl 2)

                if (!(!acore.cpu.haveVirtExt || acore.cpu.IsSecure()))
                    throw NotImplementedError("SecondStageTranslate")

                val l2desc = ports.outp.inl(l2descaddr)
                if (acore.cpu.vmsa.sctlr.ee)
                    throw NotImplementedError("Big endian")

                val taketohypmode = false
                val IA = 0L // 40 bits
                val ipavalid = false
                val stage2 = false
                val LDFSRformat = false
                val s2fs1walk = false
                if (l2desc[1..0] == 0b00L)
                    DataAbort(mva, IA, domain, level, is_write, DAbort.Translation,
                            taketohypmode, stage2, ipavalid, LDFSRformat, s2fs1walk)

                val S = l2desc[10].toBool()
                val ap = ((l2desc[9] shl 2) or l2desc[5..4]).toInt()
                val nG = l2desc[11].toBool()
                if (acore.cpu.vmsa.sctlr.afe && l2desc[4] == 0L) {
                    when {
                        acore.cpu.vmsa.sctlr.ha -> DataAbort(mva, IA, domain, level, is_write, DAbort.AccessFlag,
                                taketohypmode, stage2, ipavalid, LDFSRformat, s2fs1walk)
                        // Hardware-managed Access flag must be set in memory
                        acore.cpu.vmsa.sctlr.ee -> ports.outp.outl(l2descaddr, l2desc.set(28))
                        else -> ports.outp.outl(l2descaddr, l2desc.set(4))
                    }
                }

                val xn: Boolean
                val blocksize: Int
                val physicaladdress: Long
                val mask: Long
                if (l2desc[1]== 0L) {// Large page
                    xn = l2desc[15].toBool()
                    blocksize = 64
                    mask = bitMask(31..16)
                    physicaladdress = (l2desc and mask) or (mva and mask.inv())
                }
                else { // Small page
                    xn = l2desc[0].toBool()
                    blocksize = 4
                    mask = bitMask(31..12)
                    physicaladdress = (l2desc and mask) or (mva and mask.inv())
                }

                val result = TLBRecord(
                        addrdesc = AddressDescriptor(
                                memattrs = MemoryAttributes(
                                        // transient bits are not supported in this format
                                        innertransient = false,
                                        outertransient = false
                                ),
                                paddress = FullAddress(
                                        physicalAddress = physicaladdress,
                                        ns = /* if IsSecure() then NS else '1' */ true,
                                        mask = mask
                                )
                        ),
                        perms = Permissions(
                                ap = ap,
                                xn = xn,
                                pxn = pxn
                        ),
                        nG = nG,
                        domain = domain,
                        level = level,
                        blocksize = blocksize
                )
                l2descs[l2ind.toInt()] = result
                result
            }
            desc.addrdesc.paddress.updateAddress(mva)
            return desc
        }

        override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
            val tlb = l2descs.map { it?.serialize(ctxt) }
            return storeValues("l2descs" to tlb)
        }

        @Suppress("UNCHECKED_CAST")
        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            (snapshot["l2descs"] as ArrayList<Map<String, Any>?>).forEachIndexed { i, recordSnapshot ->
                l2descs[i] = if (recordSnapshot == null) null else TLBRecord().apply { deserialize(ctxt, recordSnapshot) }
            }
        }

    }

    inner class Section(l1desc: Long, mva: Long): FirstLevelTLBEntry(l1desc) {
        val texcb = ((l1desc[14..12] shl 2) or (l1desc[3] shl 1) or l1desc[2]).toInt()
        val S = l1desc[16].toBool()
        val ap = ((l1desc[15] shl 2) or l1desc[11..10]).toInt()
        val xn = l1desc[4].toBool()
        val pxn = l1desc[0].toBool()
        val nG = l1desc[17].toBool()
        val level = 1
        val NS = l1desc[19].toBool()
        val entry: TLBRecord

        init {
            val domain: Int
            val blocksize: Int
            val physicaladdressext: Long
            val physicaladdress: Long
            val mask: Long
            if (l1desc[18] == 0L) { // Section
                domain = l1desc[8..5].toInt()
                blocksize = 1024
                physicaladdressext = 0b00000000L
                mask = bitMask(31..20)
                physicaladdress = (l1desc and mask) or (mva and mask.inv())
            }
            else // Supersection
                throw NotImplementedError("Not implemented supersection logic")

            entry = TLBRecord(
                    addrdesc = AddressDescriptor(
                            memattrs = MemoryAttributes(
                                    // transient bits are not supported in this format
                                    innertransient = false,
                                    outertransient = false
                            ),
                            paddress = FullAddress(
                                    physicalAddress = (physicaladdressext shl 32) or physicaladdress,
                                    ns = /* if IsSecure() then NS else '1' */ true,
                                    mask = mask
                            )
                    ),
                    perms = Permissions(
                            ap = ap,
                            xn = xn,
                            pxn = pxn
                    ),
                    nG = nG,
                    domain = domain,
                    level = level,
                    blocksize = blocksize
            )
        }

        override fun getRecord(mva: Long, is_write: Boolean): TLBRecord {
            entry.addrdesc.paddress.updateAddress(mva)
            return entry
        }


        override fun serialize(ctxt: GenericSerializer) = storeValues("entry" to entry.serialize(ctxt))

        @Suppress("UNCHECKED_CAST")
        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            entry.deserialize(ctxt, snapshot["entry"] as Map<String, Any>)
        }
    }

    private val tlb_fast = Array<FirstLevelTLBEntry?>(4096) { null } // 2^12

    //    private val tlb = Array<TLBRecord?>(4096) { null }

    fun tlbInvalidate() {
//        tlb.fill(null)
        tlb_fast.fill(null)
    }

    fun getTLBRecord(mva: Long, is_write: Boolean): TLBRecord {
        val ttbr: Long // 64 bits
        val ttbcrN = acore.cpu.vmsa.ttbcr.n.toInt()

        val n = if (ttbcrN == 0 || mva[31..(32-ttbcrN)] == 0L) {
            ttbr = acore.cpu.vmsa.ttbr0.value
            ttbcrN
        }
        else {
            ttbr = acore.cpu.vmsa.ttbr1.value
            0 // TTBR1 translation always works like N=0 TTBR0 translation
        }

        val ind = mva[(31-n)..20]
        val flte = tlb_fast[ind.toInt()]
        val entry = if (flte != null) flte else {
            if (!(!acore.cpu.haveVirtExt || acore.cpu.IsSecure()))
                throw NotImplementedError("Second stage was not tested")

            val l1descaddr = ttbr and bitMask(31..(14 - n)) or (mva[(31-n)..20] shl 2)
            val l1desc = ports.outp.inl(l1descaddr)

            when (l1desc[1..0].toInt()) {
                0b00 -> { // Fault, Reserved
                    Fault(l1desc)
                }
                0b01 -> { // Large page or Small page
                    Page(l1desc)
                }
                else -> { // Section or Supersection
                    Section(l1desc, mva)
                }
            }
        }
        tlb_fast[ind.toInt()] = entry
        return entry.getRecord(mva, is_write)
    }


    inline fun TranslateAddressVFast(ea: Long, ss: Int, size: Int, LorS: AccessAction): Long {
        if (!enabled) // TODO: refactor
            return ea

        if (acore.cpu.CurrentModeIsHyp())
            throw NotImplementedError("Hyper mode isn't implemented")

        if (acore.cpu.haveSecurityExt)
            throw NotImplementedError("Security ext isn't implemented")

        val tlbrecordS1: TLBRecord
        try {
            tlbrecordS1 = getTLBRecord(ea, LorS == AccessAction.STORE)
        }
        catch (e: ARMHardwareException.DataAbortException) {
            if (LorS == AccessAction.FETCH)
                throw ARMHardwareException.PrefetchAbortException
            throw e
        }

        val checkPermission = CheckDomain(tlbrecordS1.domain, ea, tlbrecordS1.level, LorS == AccessAction.STORE)

        if (checkPermission)
            CheckPermission(tlbrecordS1.perms, ea, tlbrecordS1.level, tlbrecordS1.domain, LorS == AccessAction.STORE,
                    /*ispriv*/acore.cpu.CurrentModeIsNotUser(), /*ishyp*/false, false)

        if (acore.cpu.haveVirtExt && !acore.cpu.IsSecure() && true /*!ishyp*/)
            throw NotImplementedError("Some code")

        return tlbrecordS1.addrdesc.paddress.physicalAddress
    }


    // See B3.19.6
    fun TranslationTableWalkSD(mva: Long, is_write: Boolean, size: Int): TLBRecord {

        // this is only called when the MMU is enabled
        val result: TLBRecord
        val l1descaddr: AddressDescriptor
        val l2descaddr: AddressDescriptor

        // variables for DAbort function
        val taketohypmode = false
        val IA = 0L // 40 bits
        val ipavalid = false
        val stage2 = false
        val LDFSRformat = false
        val s2fs1walk = false

        // default setting of the domain
        var domain = 0 // 4 bits

        // Determine correct Translation Table Base Register to use.
        val ttbr: Long // 64 bits
        var n = acore.cpu.vmsa.ttbcr.n.toInt()

        val disabled = if (n == 0 || mva[31..(32-n)] == 0L){
            ttbr = acore.cpu.vmsa.ttbr0.value
//            acore.cpu.vmsa.ttbcr.pd0
            false
        } else {
            ttbr = acore.cpu.vmsa.ttbr1.value
            n = 0 // TTBR1 translation always works like N=0 TTBR0 translation
//            acore.cpu.vmsa.ttbcr.pd1
            false
        }

        // Check this Translation Table Base Register is not disabled.
        if (acore.cpu.haveSecurityExt && disabled) {
            TODO("Not implemented")
//            level = 1;
//            DataAbort(mva, IA, domain, level, is_write, DAbort_Translation,
//                    taketohypmode, stage2, ipavalid, LDFSRformat, s2fs1walk);
        }

//        val tlbEntry = tlb[mva[(31-n)..20].toInt()]
//        if (tlbEntry != null) {
//            val physicaladdress = (tlbEntry.addrdesc.paddress.physicalAddress and bitMask(31..20)) or
//                    (mva and bitMask(19..0))
//            tlbEntry.addrdesc.paddress.physicalAddress = physicaladdress
//            return tlbEntry
//        }

        // Obtain First level descriptor.
        val paddress = FullAddress(
                physicalAddress = ttbr and bitMask(31..(14 - n)) or (mva[(31-n)..20] shl 2),
                ns = !acore.cpu.IsSecure()
        )

        /*
        if HaveMPExt() {
            hintsattrs = ConvertAttrsHints(ttbr<0>:ttbr<6>);
            l1descaddr.memattrs.innerattrs = hintsattrs<1:0>;
            l1descaddr.memattrs.innerhints = hintsattrs<3:2>;
        } else
        */
        val innerattrs: Int
        val innerhints: Int
        if (ttbr[0] == 0L) {
            val hintsattrs = ConvertAttrsHints(0b00)
            innerattrs = hintsattrs[1..0].toInt()
            innerhints = hintsattrs[3..2].toInt()
        } else {
            // l1descaddr.memattrs.innerattrs = IMPLEMENTATION_DEFINED 10 or 11;
            // l1descaddr.memattrs.innerhints = IMPLEMENTATION_DEFINED 01 or 11;
            TODO("Not implemented")
        }

        val hintsattrs = ConvertAttrsHints(ttbr[4..3].toInt())
        val memattrs = MemoryAttributes(
                type = MemType.Normal,
                shareable = ttbr[1] == 1L,
                outersharable = ttbr[5] == 0L,

                outerattrs = hintsattrs[1..0],
                outerhints = hintsattrs[3..2],

                innerattrs = innerattrs,
                innerhints = innerhints
        )

        l1descaddr = AddressDescriptor(
                memattrs = memattrs,
                paddress = paddress
        )

        val l1descaddr2 = if (!acore.cpu.haveVirtExt || acore.cpu.IsSecure()) {
            l1descaddr // if only 1 stage of translation
        } else
            SecondStageTranslate(l1descaddr, mva, 4, is_write)

        val l1desc = ports.outp.inl(l1descaddr2.paddress.physicalAddress)

        val sctlr = acore.cpu.vmsa.sctlr
        if (sctlr.ee)
            TODO("Only Little endian")

        val texcb: Int
        val S: Boolean
        val ap: Int
        val xn: Boolean
        val pxn: Boolean
        val nG: Boolean
        val level: Int
        val NS: Boolean
        val blocksize: Int
        val physicaladdressext: Long
        val physicaladdress: Long
        when (l1desc[1..0].toInt()) {
            0b00 -> { // Fault, Reserved
                level = 1
                DataAbort(mva, IA, domain, level, is_write, DAbort.Translation,
                        taketohypmode, stage2, ipavalid, LDFSRformat, s2fs1walk)
                throw IllegalStateException("Unreachable code")
            }
            0b01 -> { // Large page or Small page
                domain = l1desc[8..5].toInt()
                level = 2
                pxn = l1desc[2].toBool()
                NS = l1desc[3].toBool()

                // Obtain Second level descriptor.
                l2descaddr = AddressDescriptor(
                        memattrs = l1descaddr.memattrs,
                        paddress = FullAddress(
                                physicalAddress = (l1desc and bitMask(31..10)) or (mva[19..12] shl 2),
                                ns = /* if IsSecure() then '0' else '1'; */ false
                        )

                )
                val l2descaddr2 = if (!acore.cpu.haveVirtExt || acore.cpu.IsSecure()) {
                    // if only 1 stage of translation
                     l2descaddr
                }
                else
                    SecondStageTranslate(l2descaddr, mva, 4, is_write)

                val l2desc = ports.outp.inl(l2descaddr2.paddress.physicalAddress)
                if (acore.cpu.vmsa.sctlr.ee)
                    TODO("Only Little endian")
//                        l2desc = BigEndianReverse(l2desc,4);

                // Process Second level descriptor.
                if (l2desc[1..0] == 0b00L)
                    DataAbort(mva, IA, domain, level, is_write, DAbort.Translation,
                            taketohypmode, stage2, ipavalid, LDFSRformat, s2fs1walk)

                S = l2desc[10].toBool()
                ap = ((l2desc[9] shl 2) or l2desc[5..4]).toInt()
                nG = l2desc[11].toBool()
                if (acore.cpu.vmsa.sctlr.afe && l2desc[4] == 0L) {
                    when {
                        acore.cpu.vmsa.sctlr.ha -> DataAbort(mva, IA, domain, level, is_write, DAbort.AccessFlag,
                                taketohypmode, stage2, ipavalid, LDFSRformat, s2fs1walk)
                        // Hardware-managed Access flag must be set in memory
                        acore.cpu.vmsa.sctlr.ee -> ports.outp.outl(l2descaddr2.paddress.physicalAddress, l2desc.set(28))
                        else -> ports.outp.outl(l2descaddr2.paddress.physicalAddress, l2desc.set(4))
                    }
                }

                if (l2desc[1]== 0L) {// Large page
//                    texcb = l2desc < 14:12, 3, 2>;
                    xn = l2desc[15].toBool()
                    blocksize = 64
                    physicaladdressext = 0b00000000L
                    physicaladdress = (l2desc and bitMask(31..16)) or (mva and bitMask(15..0))
                }
                else { // Small page
//                    texcb = l2desc<8:6,3,2>;
                    xn = l2desc[0].toBool()
                    blocksize = 4
                    physicaladdressext = 0b00000000L
                    physicaladdress = (l2desc and bitMask(31..12)) or (mva and bitMask(11..0))
                }
            }
            else -> { // Section or Supersection
                texcb = ((l1desc[14..12] shl 2) or (l1desc[3] shl 1) or l1desc[2]).toInt()
                S = l1desc[16].toBool()
                ap = ((l1desc[15] shl 2) or l1desc[11..10]).toInt()
                xn = l1desc[4].toBool()
                pxn = l1desc[0].toBool()
                nG = l1desc[17].toBool()
                level = 1
                NS = l1desc[19].toBool()

                // Not implemented in VMSAv6
//                if (sctlr.afe && l1desc[10] == 0L) {
//                    if (!sctlr.ha) {
////                        DataAbort(mva, IA, domain, level, is_write,
////                                DAbort_AccessFlag, taketohypmode, stage2,
////                                ipavalid, LDFSRformat, s2fs1walk);
//                        throw NotImplementedError("DataAbort")
//                    }
//                    else // Hardware-managed Access flag must be set in memory
//                        if (sctlr.ee)
//                            ports.outp.outl(l1descaddr2.paddress.physicalAddress, l1desc.set(18))
//                        else
//                            ports.outp.outl(l1descaddr2.paddress.physicalAddress, l1desc.set(10))
//                }


                if (l1desc[18] == 0L) { // Section
                    domain = l1desc[8..5].toInt()
                    blocksize = 1024
                    physicaladdressext = 0b00000000L
                    physicaladdress = (l1desc and bitMask(31..20)) or (mva and bitMask(19..0))
                }
                else {// Supersection
//                    domain = 0b0000
//                    blocksize = 16384;
//                    physicaladdressext = l1desc < 8:5, 23:20>;
//                    physicaladdress = l1desc < 31:24>:mva<23:0>;
                    throw NotImplementedError("Not implemented supersection logic")
                }
            }

        }

        // Decode the TEX, C, B and S bits to produce the TLBRecord's memory attributes
        // Not implemented in VMSAv6
//        if (!sctlr.tre) {
//            if RemapRegsHaveResetValues() then
//                    result.addrdesc.memattrs = DefaultTEXDecode(texcb, S);
//            else
//                IMPLEMENTATION_DEFINED setting of result . addrdesc . memattrs;
//        }
//        else
        // TODO: implement this later
//        result.addrdesc.memattrs = RemappedTEXDecode(texcb, S);


        result = TLBRecord(
                addrdesc = AddressDescriptor(
                        memattrs = MemoryAttributes(
                                // transient bits are not supported in this format
                                innertransient = false,
                                outertransient = false
                        ),
                        paddress = FullAddress(
                                physicalAddress = (physicaladdressext shl 32) or physicaladdress,
                                ns = /* if IsSecure() then NS else '1' */ true
                        )
                ),
                perms = Permissions(
                        ap = ap,
                        xn = xn,
                        pxn = pxn
                ),
                nG = nG,
                domain = domain,
                level = level,
                blocksize = blocksize
        )

//        this.tlb[mva[(31-n)..20].toInt()] = result

        return result
    }

    // See B2.4.8
    // CheckPermission()
    // =================
    // Function used for permission checking at stage 1 of the translation process
    // for the:
    // VMSA Long-descriptor format
    // VMSA Short-descriptor format
    // PMSA format.
    fun CheckPermission(perms: Permissions, mva: Long, level: Int, domain: Int, isWrite: Boolean,
                        isPriv: Boolean, taketohypmode: Boolean, LDFSRformat: Boolean) {

        // variable for the DataAbort function with fixed values
        val secondstageabort = false
        val ipavalid = false
        val s2fs1walk = false
        val ipa = 0L //bits(40) UNKNOWN;
        if (acore.cpu.vmsa.sctlr.afe)
            perms.ap = perms.ap or 1

        val abort: Boolean
        when(perms.ap) {
            0b000 -> abort = true
            0b001 -> abort = !isPriv
            0b010 -> abort = !isPriv && isWrite
            0b011 -> abort = false
            0b100 -> throw ARMHardwareException.Unpredictable
            0b101 -> abort = !isPriv || isWrite
            0b110 -> abort = isWrite
            0b111 -> {
                if (/*MemorySystemArchitecture() == MemArch_VMSA*/ true)
                    abort = isWrite
                else
                    throw ARMHardwareException.Unpredictable
            }
            else -> throw IllegalStateException("Unreachable code")
        }
        if (abort)
            DataAbort(mva, ipa, domain, level, isWrite, DAbort.Permission, taketohypmode,
                    secondstageabort, ipavalid, LDFSRformat, s2fs1walk)
    }

    // See B3.19.4
    fun CheckDomain(domain: Int, mva: Long, level: Int, isWrite: Boolean): Boolean {
        // variables used for dataabort function
        val ipaddress = 0L // UNKNOWN
        val taketohypmode = false
        val secondstageabort = false
        val ipavalid = false
        val LDFSRformat = false
        val s2fs1walk = false
        val bitpos = 2*domain
        val permissionCheck: Boolean
        when (acore.cpu.vmsa.dacr.value[(bitpos + 1)..bitpos] ) {
            0b00L -> {
                DataAbort(mva, ipaddress, domain, level, isWrite, DAbort.Domain, taketohypmode, secondstageabort,
                        ipavalid, LDFSRformat, s2fs1walk)
                throw IllegalStateException("Unreachable code")
            }
            0b01L -> permissionCheck = true
            0b10L -> throw ARMHardwareException.Unpredictable
            0b11L -> permissionCheck = false
            else -> throw IllegalStateException("Unreachable code")
        }
        return permissionCheck
    }

    // See B3.19.3
    inline fun TranslateAddressV(ea: Long, ss: Int, size: Int, LorS: AccessAction): Long {
        if (!enabled) // TODO: refactor
            return ea

        val wasaligned = true

        val s2fs1walk = false
        val mva = FCSETranslate(ea)
        val ishyp = acore.cpu.CurrentModeIsHyp()

        if (ishyp)
            throw NotImplementedError("Hyper mode isn't implemented")

        val tlbrecordS1: TLBRecord
        val checkDomain: Boolean
        var checkPermission: Boolean
        val usesLD: Boolean

        if (/*(ishyp && HSCTLR.M) || */(!ishyp && acore.cpu.vmsa.sctlr.m)) {
            if (acore.cpu.haveSecurityExt /* && !IsSecure() && !ishyp && HCR.TGE == '1'*/)
                throw ARMHardwareException.Unpredictable
            usesLD = ishyp || acore.cpu.vmsa.ttbcr.eae

            if (usesLD) {
//                val ia_in =
                TODO("Check usesLD")
            }
            else {
                try {
                    tlbrecordS1 = TranslationTableWalkSD(mva, LorS == AccessAction.STORE, size)
                    checkDomain = true
                    checkPermission = true
                }
                catch (e: ARMHardwareException.DataAbortException) {
                    if (LorS == AccessAction.FETCH)
                        throw ARMHardwareException.PrefetchAbortException
                    throw e
                }
            }

        }
        else {
//            tlbrecordS1 = TranslateAddressVS1Off(mva)
//            checkDomain = false
//            checkPermission = false
            TODO("Legacy no MMU turned off")
        }

        // Check for alignment issues if memory type is SO or Device
        if (!wasaligned && tlbrecordS1.addrdesc.memattrs.type != MemType.Normal) {
            if (!acore.cpu.haveVirtExt) throw ARMHardwareException.Unpredictable
            val secondstageabort = false
            throw ARMHardwareException.AligmentFault // TODO: Not implemented
//        AlignmentFaultV(mva, iswrite, ishyp, secondstageabort);
        }

        // Check domain and permissions
        if (checkDomain)
            checkPermission = CheckDomain(tlbrecordS1.domain, mva, tlbrecordS1.level, LorS == AccessAction.STORE)

        if (checkPermission)
            CheckPermission(tlbrecordS1.perms, mva, tlbrecordS1.level, tlbrecordS1.domain, LorS == AccessAction.STORE,
                    /*ispriv*/acore.cpu.CurrentModeIsNotUser(), /*ishyp*/false, usesLD)

        val result: AddressDescriptor
        if (acore.cpu.haveVirtExt && !acore.cpu.IsSecure() && true /*!ishyp*/) {
            if (acore.cpu.ver.hcr.vm) {
                // Stage 2 translation enabled
                val s1outputaddr = tlbrecordS1.addrdesc.paddress.physicalAddress
                val tlbrecordS2 = TranslationTableWalkLD(s1outputaddr, mva, LorS == AccessAction.STORE,
                        false, s2fs1walk, size);
                // Check for alignment issues if memory type is SO or Device
                if (!wasaligned && tlbrecordS2.addrdesc.memattrs.type != MemType.Normal) {
//                    taketohypmode = TRUE;
//                    secondstageabort = TRUE;
//                    AlignmentFaultV(mva, is_write, taketohypmode, secondstageabort);
                    throw ARMHardwareException.AligmentFault // TODO: Not implemented
                }
                // Check permissions
                CheckPermissionS2(tlbrecordS2.perms, mva, s1outputaddr, tlbrecordS2.level,
                        LorS == AccessAction.STORE, s2fs1walk)
                result = CombineS1S2Desc(tlbrecordS1.addrdesc, tlbrecordS2.addrdesc)
            }
            else
                result = tlbrecordS1.addrdesc
        }
        else
            result = tlbrecordS1.addrdesc
        return result.paddress.physicalAddress

    }

    override fun translate(ea: Long, ss: Int, size: Int, LorS: AccessAction): Long  = TranslateAddressVFast(ea, ss, size, LorS) //TranslateAddressV(ea, ss, size, LorS)

    /*
    snapshot = mapOf(
        "tlb_fast" to mapOf(
            null

            or

            "l1desc" to l1desc: Long
            "entry" to entry.serialize()
        )
    )
    */
    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        val tlbstored = tlb_fast.map {
            if (it == null)
                null
            else
                storeValues(
                        "l1desc" to it.l1desc,
                        "entry" to it.serialize(ctxt)
                )
        }

        return super.serialize(ctxt) + storeValues(
                "privEnabled" to privEnabled,
                "tlb_fast" to tlbstored
        )
    }


    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        privEnabled = loadValue(snapshot, "privEnabled")

        (snapshot["tlb_fast"] as ArrayList<Map<String, Any>?>).forEachIndexed { i, recordSnapshot ->
            tlb_fast[i] = if (recordSnapshot == null) null else {
                val l1desc = recordSnapshot["l1desc"] as Long
                val entry = recordSnapshot["entry"] as Map<String, Any>
                when(l1desc[1..0].toInt()) {
                    0b00 -> { // Fault, Reserved
                        Fault(l1desc)
                    }
                    0b01 -> { // Large page or Small page
                        Page(l1desc).apply { deserialize(ctxt, entry) }
                    }
                    else -> { // Section or Supersection
                        Section(l1desc, 0L).apply { deserialize(ctxt, entry) }
                    }
                }
            }
        }
        super.deserialize(ctxt, snapshot)
    }

    override fun reset() {
        super.reset()
        privEnabled = false
        tlbInvalidate()
    }
}