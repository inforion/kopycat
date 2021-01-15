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
package ru.inforion.lab403.kopycat.cores.ppc.exceptions

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.common.extensions.toLong
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.ppc.enums.eIrq
import ru.inforion.lab403.kopycat.cores.ppc.enums.systems.e500v2.eESR
import ru.inforion.lab403.kopycat.cores.ppc.enums.systems.embedded.mmufsl.*
import ru.inforion.lab403.kopycat.cores.ppc.operands.systems.PPCRegister_Embedded
import ru.inforion.lab403.kopycat.cores.ppc.operands.systems.PPCRegister_EmbeddedMMUFSL
import ru.inforion.lab403.kopycat.cores.ppc.operands.systems.PPCRegister_e500v2
import ru.inforion.lab403.kopycat.modules.cores.PPCCore


object PPCExceptionHolder_e500v2 : IPPCExceptionHolder {

    fun buildESR(write: Boolean): Long {
        val esrST = 0L.insert(write.toLong(), eESR.ST.bit)
        val esrDLK = esrST // We haven't dcbtls, dcbtstls and dcblc
        val esrBO = esrDLK // Can't get byte ordering exception
        return esrBO
    }


    // Data storage interrupt
    class AccessDataException(where: Long, val write: Boolean) : PPCHardwareException(eIrq.DataStorage, where, "Data access Denied ") {

        override fun interrupt(core: PPCCore) {
            super.interrupt(core) // SRR0, SRR1, MSR
            PPCRegister_e500v2.OEAext.ESR.value(core, buildESR(write))
        }
    }

    // Instruction storage interrupt
    class AccessInstructionException(where: Long) : PPCHardwareException(eIrq.DataStorage, where, "Instruction access Denied ") {
        override fun interrupt(core: PPCCore) {
            super.interrupt(core) // SRR0, SRR1, MSR
            PPCRegister_e500v2.OEAext.ESR.value(core, buildESR(false)) // TODO: separate if BO will be implemented
        }
    }

    open class TLBException(where: Long,
                       val write: Boolean,
                       val ea: Long,
                       val AS: Long,
                       val inst: Boolean
    ): PPCHardwareException(
            if (inst) eIrq.InstTLBError else eIrq.DataTLBError,
            where,
            "TLB ${if (inst) "instruction" else "data"} error"
    ) {
        fun buildMAS0(core: PPCCore): Long {
            val mas4 = PPCRegister_EmbeddedMMUFSL.OEAext.MAS4.value(core)

            val masTLBSEL = 0L.insert(mas4[eMAS4.TLBSELD], eMAS0.TLBSEL)
            val masESEL = masTLBSEL // Unknown hardware hint
            val masNV = masESEL // Unknown hardware hint
            return masNV
        }

        fun buildMAS1(core: PPCCore): Long {
            val mas4 = PPCRegister_EmbeddedMMUFSL.OEAext.MAS4.value(core)

            val masV_IPROT = 0x80000000 // V = 1, IPROT=0

            val pid = when (mas4[eMAS4.TIDSELD]) {
                0L -> PPCRegister_Embedded.OEAext.PID0
                1L -> PPCRegister_EmbeddedMMUFSL.OEAext.PID1
                2L -> PPCRegister_EmbeddedMMUFSL.OEAext.PID2
                else -> throw GeneralException("Wrong TIDSELD value: ${mas4[eMAS4.TIDSELD]}")
            }.value(core)

            val masTID = masV_IPROT.insert(pid[7..0], (eMAS1.TIDLow.bit + 7)..eMAS1.TIDLow.bit)
            val masTS = masTID.insert(AS, eMAS1.TS.bit)
            val masTSIZE = masTS.insert(mas4[eMAS4.TSIZED], eMAS1.TSIZE)
            return masTSIZE
        }

        fun buildMAS2(core: PPCCore): Long {
            val mas4 = PPCRegister_EmbeddedMMUFSL.OEAext.MAS4.value(core)

            val masEPN = ea[31..12]
            val masX0 = masEPN.insert(mas4[eMAS4.X0D.bit], 6)
            val masX1 = masX0.insert(mas4[eMAS4.X1D.bit], 5)
            val masW = masX1.insert(mas4[eMAS4.WD.bit], eMAS2.W.bit)
            val masI = masW.insert(mas4[eMAS4.ID.bit], eMAS2.I.bit)
            val masM = masI.insert(mas4[eMAS4.MD.bit], eMAS2.M.bit)
            val masG = masM.insert(mas4[eMAS4.GD.bit], eMAS2.G.bit)
            val masE = masG.insert(mas4[eMAS4.ED.bit], eMAS2.E.bit)
            return masE
        }

        fun buildMAS6(core: PPCCore): Long {
            val masSPID0 = 0L.insert(PPCRegister_Embedded.OEAext.PID0.value(core)[7..0], eMAS6.SPID0)
            val masSAS = masSPID0.insert(AS, eMAS6.SAS.bit)
            return masSAS
        }

        override fun interrupt(core: PPCCore) {
            super.interrupt(core) // SRR0, SRR1, MSR
            if (!inst)
                PPCRegister_e500v2.OEAext.ESR.value(core, buildESR(write))
            PPCRegister_EmbeddedMMUFSL.OEAext.MAS0.value(core, buildMAS0(core))
            PPCRegister_EmbeddedMMUFSL.OEAext.MAS1.value(core, buildMAS1(core))
            PPCRegister_EmbeddedMMUFSL.OEAext.MAS2.value(core, buildMAS2(core))
            PPCRegister_EmbeddedMMUFSL.OEAext.MAS3.value(core, 0L)
            PPCRegister_EmbeddedMMUFSL.OEAext.MAS6.value(core, buildMAS6(core))
        }

    }

    // Data TLB error interrupt
    class TLBDataException(where: Long, write: Boolean, ea: Long, AS: Long): TLBException(where, write, ea, AS, false) {

        override fun interrupt(core: PPCCore) {
            super.interrupt(core) // SRR0, SRR1, MSR + all TLB assist
            PPCRegister_e500v2.OEAext.DEAR.value(core, ea)
        }
    }

    // Instruction TLB error interrupt
    class TLBInstructionException(where: Long, ea: Long, AS: Long): TLBException(where, false, ea, AS, true) {

        override fun interrupt(core: PPCCore) {
            super.interrupt(core) // SRR0, SRR1, MSR + all TLB assist
            PPCRegister_e500v2.OEAext.DEAR.value(core, ea)
        }
    }

    override fun accessDataException(where: Long, write: Boolean) = AccessDataException(where, write)
    override fun accessInstructionException(where: Long) = AccessInstructionException(where)
    override fun tlbDataException(where: Long, write: Boolean, ea: Long, AS: Long) = TLBDataException(where, write, ea, AS)
    override fun tlbInstructionException(where: Long, ea: Long, AS: Long) = TLBInstructionException(where, ea, AS)
}