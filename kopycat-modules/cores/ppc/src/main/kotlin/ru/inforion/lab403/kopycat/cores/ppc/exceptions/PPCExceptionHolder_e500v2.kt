/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2023 INFORION, LLC
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
import ru.inforion.lab403.common.extensions.truth
import ru.inforion.lab403.common.extensions.ulong
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.ppc.enums.eIrq
import ru.inforion.lab403.kopycat.cores.ppc.enums.eMSR
import ru.inforion.lab403.kopycat.cores.ppc.enums.eTSR
import ru.inforion.lab403.kopycat.cores.ppc.enums.systems.e500v2.eESR
import ru.inforion.lab403.kopycat.cores.ppc.enums.systems.embedded.mmufsl.*
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.cores.ppc.operands.systems.PPCRegister_Embedded
import ru.inforion.lab403.kopycat.cores.ppc.operands.systems.PPCRegister_EmbeddedMMUFSL
import ru.inforion.lab403.kopycat.cores.ppc.operands.systems.PPCRegister_e500v2
import ru.inforion.lab403.kopycat.modules.cores.PPCCore


object PPCExceptionHolder_e500v2 : IPPCExceptionHolder {

    fun buildESR(write: Boolean): ULong {
        val esrST = insert(write.ulong, eESR.ST.bit)
        val esrDLK = esrST // We haven't dcbtls, dcbtstls and dcblc
        val esrBO = esrDLK // Can't get byte ordering exception
        return esrBO
    }

    abstract class ATLBException(irq: eIrq, where: ULong, what: String) : PPCHardwareException(irq, where, what) {

        override fun interrupt(core: PPCCore) {
            super.interrupt(core) // SRR0, pc

            val msr = PPCRegister.OEA.MSR.value(core)
            PPCRegister.OEA.SRR1.value(core, msr)
            PPCRegister.OEA.MSR.value(core, 0uL)
            core.cpu.msrBits.CE = msr[eMSR.CE.bit].truth
            core.cpu.msrBits.ME = msr[eMSR.ME.bit].truth
            core.cpu.msrBits.DE = msr[eMSR.DE.bit].truth
        }
    }

    // Exception with SRR1 predefined behaviour
    abstract class ASRR01Exception(irq: eIrq, where: ULong, what: String) : PPCHardwareException(irq, where, what) {

        override fun interrupt(core: PPCCore) {
            super.interrupt(core) // SRR0, pc


            val msr = PPCRegister.OEA.MSR.value(core)
            PPCRegister.OEA.SRR1.value(core, msr and 0x87C0FFFFu)  // 33:36 Set to 0.
                                                                        // 42:47 Set to 0.
        }
    }


    // Data storage interrupt
    class AccessDataException(where: ULong, val write: Boolean) : ASRR01Exception(eIrq.DataStorage, where, "Data access Denied ") {

        override fun interrupt(core: PPCCore) {
            super.interrupt(core) // SRR0, SRR1, pc
            TODO("Please, implement my interrupt actions by PowerISA V2.05")
            PPCRegister_e500v2.OEAext.ESR.value(core, buildESR(write))
        }
    }

    // Instruction storage interrupt
    class AccessInstructionException(where: ULong) : ASRR01Exception(eIrq.DataStorage, where, "Instruction access Denied ") {
        override fun interrupt(core: PPCCore) {
            super.interrupt(core) // SRR0, SRR1, pc
            TODO("Please, implement my interrupt actions by PowerISA V2.05")
            PPCRegister_e500v2.OEAext.ESR.value(core, buildESR(false)) // TODO: separate if BO will be implemented
        }
    }

    // Decrementer Interrupt
    class DecrementerInterrupt(where: ULong) : ASRR01Exception(eIrq.Decrementer, where, "Decrementer Interrupt") {

        override fun interrupt(core: PPCCore) {
            super.interrupt(core) // SRR0, SRR1, pc

            val msr = PPCRegister.OEA.MSR.value(core)
            PPCRegister.OEA.MSR.value(core, 0uL)
            core.cpu.msrBits.ME = msr[eMSR.ME.bit].truth // TODO: HV

            val tsr = core.cpu.sprRegs.readIntern(PPCRegister_Embedded.OEAext.TSR.reg).insert(1, eTSR.DIS.bit)
            core.cpu.sprRegs.writeIntern(PPCRegister_Embedded.OEAext.TSR.reg, tsr) // TODO: Register access
        }
    }

    // System Call Interrupt
    class SystemCallInterrupt(where: ULong) : ASRR01Exception(eIrq.SystemCall, where, "System Call Interrupt") {

        override fun interrupt(core: PPCCore) {
            super.interrupt(core) // SRR0, SRR1, MSR

            val msr = PPCRegister.OEA.MSR.value(core)
            PPCRegister.OEA.MSR.value(core, 0uL)
            core.cpu.msrBits.ME = msr[eMSR.ME.bit].truth // TODO: HV
        }
    }


    open class TLBException(where: ULong,
                       val write: Boolean,
                       val ea: ULong,
                       val AS: ULong,
                       val inst: Boolean
    ): ATLBException(
            if (inst) eIrq.InstTLBError else eIrq.DataTLBError,
            where,
            "TLB ${if (inst) "instruction" else "data"} error"
    ) {
        fun buildMAS0(core: PPCCore): ULong {
            val mas4 = PPCRegister_EmbeddedMMUFSL.OEAext.MAS4.value(core)

            val masTLBSEL = insert(mas4[eMAS4.TLBSELD], eMAS0.TLBSEL)
            val masESEL = masTLBSEL // Unknown hardware hint
            val masNV = masESEL // Unknown hardware hint
            return masNV
        }

        fun buildMAS1(core: PPCCore): ULong {
            val mas4 = PPCRegister_EmbeddedMMUFSL.OEAext.MAS4.value(core)

            val masV_IPROT = 0x80000000uL // V = 1, IPROT=0

            val pid = when (mas4[eMAS4.TIDSELD]) {
                0uL -> PPCRegister_Embedded.OEAext.PID0
                1uL -> PPCRegister_EmbeddedMMUFSL.OEAext.PID1
                2uL -> PPCRegister_EmbeddedMMUFSL.OEAext.PID2
                else -> throw GeneralException("Wrong TIDSELD value: ${mas4[eMAS4.TIDSELD]}")
            }.value(core)

            val masTID = masV_IPROT.insert(pid[7..0], (eMAS1.TIDLow.bit + 7)..eMAS1.TIDLow.bit)
            val masTS = masTID.insert(AS, eMAS1.TS.bit)
            val masTSIZE = masTS.insert(mas4[eMAS4.TSIZED], eMAS1.TSIZE)
            return masTSIZE
        }

        fun buildMAS2(core: PPCCore): ULong {
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

        fun buildMAS6(core: PPCCore): ULong {
            val masSPID0 = insert(PPCRegister_Embedded.OEAext.PID0.value(core)[7..0], eMAS6.SPID0)
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
            PPCRegister_EmbeddedMMUFSL.OEAext.MAS3.value(core, 0uL)
            PPCRegister_EmbeddedMMUFSL.OEAext.MAS6.value(core, buildMAS6(core))
        }

    }

    // Data TLB error interrupt
    class TLBDataException(where: ULong, write: Boolean, ea: ULong, AS: ULong): TLBException(where, write, ea, AS, false) {

        override fun interrupt(core: PPCCore) {
            super.interrupt(core) // SRR0, SRR1, MSR + all TLB assist
            PPCRegister_e500v2.OEAext.DEAR.value(core, ea)
        }
    }

    // Instruction TLB error interrupt
    class TLBInstructionException(where: ULong, ea: ULong, AS: ULong): TLBException(where, false, ea, AS, true) {

        override fun interrupt(core: PPCCore) {
            super.interrupt(core) // SRR0, SRR1, MSR + all TLB assist
            PPCRegister_e500v2.OEAext.DEAR.value(core, ea)
        }
    }




    override fun accessDataException(where: ULong, write: Boolean) = AccessDataException(where, write)
    override fun accessInstructionException(where: ULong) = AccessInstructionException(where)
    override fun tlbDataException(where: ULong, write: Boolean, ea: ULong, AS: ULong) = TLBDataException(where, write, ea, AS)
    override fun tlbInstructionException(where: ULong, ea: ULong, AS: ULong) = TLBInstructionException(where, ea, AS)
    override fun systemCallException(where: ULong): PPCHardwareException {
        TODO("Not yet implemented")
    }
}