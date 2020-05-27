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
package ru.inforion.lab403.kopycat.cores.ppc.operands

import ru.inforion.lab403.common.extensions.first
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.base.operands.ARegister
import ru.inforion.lab403.kopycat.cores.ppc.enums.Regtype
import ru.inforion.lab403.kopycat.cores.ppc.enums.eOEA
import ru.inforion.lab403.kopycat.cores.ppc.enums.eUISA
import ru.inforion.lab403.kopycat.cores.ppc.enums.eVEA
import ru.inforion.lab403.kopycat.modules.cores.PPCCore




abstract class PPCRegister(
        reg: Int,
        val rtyp: Regtype,
        access: AOperand.Access = Access.ANY) :
        ARegister<PPCCore>(reg, access, Datatype.DWORD) {

    override fun toString() = when (rtyp) {
        Regtype.UISA -> first<eUISA> { it.id == reg }.name
        Regtype.VEA -> first<eVEA> { it.id == reg }.name
        Regtype.OEA -> first<eOEA> {it.id == reg }.name
        else -> throw GeneralException("Not supported register type: ${rtyp.name}")
    }.toLowerCase()

    companion object {
        fun uisa(id: Int) = when(id) {
            eUISA.GPR0.id -> UISA.GPR0
            eUISA.GPR1.id -> UISA.GPR1
            eUISA.GPR2.id -> UISA.GPR2
            eUISA.GPR3.id -> UISA.GPR3
            eUISA.GPR4.id -> UISA.GPR4
            eUISA.GPR5.id -> UISA.GPR5
            eUISA.GPR6.id -> UISA.GPR6
            eUISA.GPR7.id -> UISA.GPR7
            eUISA.GPR8.id -> UISA.GPR8
            eUISA.GPR9.id -> UISA.GPR9
            eUISA.GPR10.id -> UISA.GPR10
            eUISA.GPR11.id -> UISA.GPR11
            eUISA.GPR12.id -> UISA.GPR12
            eUISA.GPR13.id -> UISA.GPR13
            eUISA.GPR14.id -> UISA.GPR14
            eUISA.GPR15.id -> UISA.GPR15
            eUISA.GPR16.id -> UISA.GPR16
            eUISA.GPR17.id -> UISA.GPR17
            eUISA.GPR18.id -> UISA.GPR18
            eUISA.GPR19.id -> UISA.GPR19
            eUISA.GPR20.id -> UISA.GPR20
            eUISA.GPR21.id -> UISA.GPR21
            eUISA.GPR22.id -> UISA.GPR22
            eUISA.GPR23.id -> UISA.GPR23
            eUISA.GPR24.id -> UISA.GPR24
            eUISA.GPR25.id -> UISA.GPR25
            eUISA.GPR26.id -> UISA.GPR26
            eUISA.GPR27.id -> UISA.GPR27
            eUISA.GPR28.id -> UISA.GPR28
            eUISA.GPR29.id -> UISA.GPR29
            eUISA.GPR30.id -> UISA.GPR30
            eUISA.GPR31.id -> UISA.GPR31
            eUISA.FPR0.id -> UISA.FPR0
            eUISA.FPR1.id -> UISA.FPR1
            eUISA.FPR2.id -> UISA.FPR2
            eUISA.FPR3.id -> UISA.FPR3
            eUISA.FPR4.id -> UISA.FPR4
            eUISA.FPR5.id -> UISA.FPR5
            eUISA.FPR6.id -> UISA.FPR6
            eUISA.FPR7.id -> UISA.FPR7
            eUISA.FPR8.id -> UISA.FPR8
            eUISA.FPR9.id -> UISA.FPR9
            eUISA.FPR10.id -> UISA.FPR10
            eUISA.FPR11.id -> UISA.FPR11
            eUISA.FPR12.id -> UISA.FPR12
            eUISA.FPR13.id -> UISA.FPR13
            eUISA.FPR14.id -> UISA.FPR14
            eUISA.FPR15.id -> UISA.FPR15
            eUISA.FPR16.id -> UISA.FPR16
            eUISA.FPR17.id -> UISA.FPR17
            eUISA.FPR18.id -> UISA.FPR18
            eUISA.FPR19.id -> UISA.FPR19
            eUISA.FPR20.id -> UISA.FPR20
            eUISA.FPR21.id -> UISA.FPR21
            eUISA.FPR22.id -> UISA.FPR22
            eUISA.FPR23.id -> UISA.FPR23
            eUISA.FPR24.id -> UISA.FPR24
            eUISA.FPR25.id -> UISA.FPR25
            eUISA.FPR26.id -> UISA.FPR26
            eUISA.FPR27.id -> UISA.FPR27
            eUISA.FPR28.id -> UISA.FPR28
            eUISA.FPR29.id -> UISA.FPR29
            eUISA.FPR30.id -> UISA.FPR30
            eUISA.FPR31.id -> UISA.FPR31
            eUISA.CR.id -> UISA.CR
            eUISA.FPSCR.id -> UISA.FPSCR
            eUISA.XER.id -> UISA.XER
            eUISA.LR.id -> UISA.LR
            eUISA.CTR.id -> UISA.CTR
            eUISA.PC.id -> UISA.PC
            eUISA.RESERVE.id -> UISA.RESERVE
            eUISA.RESERVE_ADDR.id -> UISA.RESERVE_ADDR
            else -> throw GeneralException("Unknown UISA register id = $id")
        }

        fun gpr(id: Int) = if (id > 31)
            throw GeneralException("Not a GPR register")
        else
            uisa(id)

        fun vea(id: Int) = when(id) {
            eVEA.TBL.id -> VEA.TBL
            eVEA.TBU.id -> VEA.TBU
            else -> throw GeneralException("Unknown VEA register id = $id")
        }

        fun oea(id: Int) = when(id) {
            eOEA.MSR.id -> OEA.MSR
            eOEA.PVR.id -> OEA.PVR
            /*eOEA.IBAT0U.id -> OEA.IBAT0U
            eOEA.IBAT0L.id -> OEA.IBAT0L
            eOEA.IBAT1U.id -> OEA.IBAT1U
            eOEA.IBAT1L.id -> OEA.IBAT1L
            eOEA.IBAT2U.id -> OEA.IBAT2U
            eOEA.IBAT2L.id -> OEA.IBAT2L
            eOEA.IBAT3U.id -> OEA.IBAT3U
            eOEA.IBAT3L.id -> OEA.IBAT3L
            eOEA.DBAT0U.id -> OEA.DBAT0U
            eOEA.DBAT0L.id -> OEA.DBAT0L
            eOEA.DBAT1U.id -> OEA.DBAT1U
            eOEA.DBAT1L.id -> OEA.DBAT1L
            eOEA.DBAT2U.id -> OEA.DBAT2U
            eOEA.DBAT2L.id -> OEA.DBAT2L
            eOEA.DBAT3U.id -> OEA.DBAT3U
            eOEA.DBAT3L.id -> OEA.DBAT3L*/
            eOEA.SDR1.id -> OEA.SDR1
            eOEA.ASR.id -> OEA.ASR
            eOEA.SR0.id -> OEA.SR0
            eOEA.SR1.id -> OEA.SR1
            eOEA.SR2.id -> OEA.SR2
            eOEA.SR3.id -> OEA.SR3
            eOEA.SR4.id -> OEA.SR4
            eOEA.SR5.id -> OEA.SR5
            eOEA.SR6.id -> OEA.SR6
            eOEA.SR7.id -> OEA.SR7
            eOEA.SR8.id -> OEA.SR8
            eOEA.SR9.id -> OEA.SR9
            eOEA.SR10.id -> OEA.SR10
            eOEA.SR11.id -> OEA.SR11
            eOEA.SR12.id -> OEA.SR12
            eOEA.SR13.id -> OEA.SR13
            eOEA.SR14.id -> OEA.SR14
            eOEA.SR15.id -> OEA.SR15
            eOEA.DAR.id -> OEA.DAR
            eOEA.DSISR.id -> OEA.DSISR
            eOEA.SPRG0.id -> OEA.SPRG0
            eOEA.SPRG1.id -> OEA.SPRG1
            eOEA.SPRG2.id -> OEA.SPRG2
            eOEA.SPRG3.id -> OEA.SPRG3
            eOEA.SRR0.id -> OEA.SRR0
            eOEA.SRR1.id -> OEA.SRR1
            eOEA.FPECR.id -> OEA.FPECR
            eOEA.TBL.id -> OEA.TBL
            eOEA.TBU.id -> OEA.TBU
            eOEA.DABR.id -> OEA.DABR
            eOEA.DEC.id -> OEA.DEC
            eOEA.EAR.id -> OEA.EAR
            eOEA.PIR.id -> OEA.PIR
            else -> throw GeneralException("Unknown OEA register id = $id")
        }
    }




    sealed class UISA(id: Int) : PPCRegister(id, Regtype.UISA) {
        override fun value(core: PPCCore, data: Long) = core.cpu.regs.writeIntern(reg, data)
        override fun value(core: PPCCore): Long = core.cpu.regs.readIntern(reg)

        //General-purpose registers
        object GPR0 : UISA(eUISA.GPR0.id)
        object GPR1 : UISA(eUISA.GPR1.id)
        object GPR2 : UISA(eUISA.GPR2.id)
        object GPR3 : UISA(eUISA.GPR3.id)
        object GPR4 : UISA(eUISA.GPR4.id)
        object GPR5 : UISA(eUISA.GPR5.id)
        object GPR6 : UISA(eUISA.GPR6.id)
        object GPR7 : UISA(eUISA.GPR7.id)
        object GPR8 : UISA(eUISA.GPR8.id)
        object GPR9 : UISA(eUISA.GPR9.id)
        object GPR10 : UISA(eUISA.GPR10.id)
        object GPR11 : UISA(eUISA.GPR11.id)
        object GPR12 : UISA(eUISA.GPR12.id)
        object GPR13 : UISA(eUISA.GPR13.id)
        object GPR14 : UISA(eUISA.GPR14.id)
        object GPR15 : UISA(eUISA.GPR15.id)
        object GPR16 : UISA(eUISA.GPR16.id)
        object GPR17 : UISA(eUISA.GPR17.id)
        object GPR18 : UISA(eUISA.GPR18.id)
        object GPR19 : UISA(eUISA.GPR19.id)
        object GPR20 : UISA(eUISA.GPR20.id)
        object GPR21 : UISA(eUISA.GPR21.id)
        object GPR22 : UISA(eUISA.GPR22.id)
        object GPR23 : UISA(eUISA.GPR23.id)
        object GPR24 : UISA(eUISA.GPR24.id)
        object GPR25 : UISA(eUISA.GPR25.id)
        object GPR26 : UISA(eUISA.GPR26.id)
        object GPR27 : UISA(eUISA.GPR27.id)
        object GPR28 : UISA(eUISA.GPR28.id)
        object GPR29 : UISA(eUISA.GPR29.id)
        object GPR30 : UISA(eUISA.GPR30.id)
        object GPR31 : UISA(eUISA.GPR31.id)

        //Floating-point registers
        object FPR0 : UISA(eUISA.FPR0.id)
        object FPR1 : UISA(eUISA.FPR1.id)
        object FPR2 : UISA(eUISA.FPR2.id)
        object FPR3 : UISA(eUISA.FPR3.id)
        object FPR4 : UISA(eUISA.FPR4.id)
        object FPR5 : UISA(eUISA.FPR5.id)
        object FPR6 : UISA(eUISA.FPR6.id)
        object FPR7 : UISA(eUISA.FPR7.id)
        object FPR8 : UISA(eUISA.FPR8.id)
        object FPR9 : UISA(eUISA.FPR9.id)
        object FPR10 : UISA(eUISA.FPR10.id)
        object FPR11 : UISA(eUISA.FPR11.id)
        object FPR12 : UISA(eUISA.FPR12.id)
        object FPR13 : UISA(eUISA.FPR13.id)
        object FPR14 : UISA(eUISA.FPR14.id)
        object FPR15 : UISA(eUISA.FPR15.id)
        object FPR16 : UISA(eUISA.FPR16.id)
        object FPR17 : UISA(eUISA.FPR17.id)
        object FPR18 : UISA(eUISA.FPR18.id)
        object FPR19 : UISA(eUISA.FPR19.id)
        object FPR20 : UISA(eUISA.FPR20.id)
        object FPR21 : UISA(eUISA.FPR21.id)
        object FPR22 : UISA(eUISA.FPR22.id)
        object FPR23 : UISA(eUISA.FPR23.id)
        object FPR24 : UISA(eUISA.FPR24.id)
        object FPR25 : UISA(eUISA.FPR25.id)
        object FPR26 : UISA(eUISA.FPR26.id)
        object FPR27 : UISA(eUISA.FPR27.id)
        object FPR28 : UISA(eUISA.FPR28.id)
        object FPR29 : UISA(eUISA.FPR29.id)
        object FPR30 : UISA(eUISA.FPR30.id)
        object FPR31 : UISA(eUISA.FPR31.id)

        //Condition register
        object CR : UISA(eUISA.CR.id)

        //Floating-point status and control register
        object FPSCR : UISA(eUISA.FPSCR.id)

        //Fixed-point exception register
        object XER : UISA(eUISA.XER.id)

        //Link register
        object LR : UISA(eUISA.LR.id)

        //Count register
        object CTR : UISA(eUISA.CTR.id)

        //Program counter
        //Not a real register in PPC system
        object PC : UISA(eUISA.PC.id)

        //Memory synchronisation subsystem
        object RESERVE : UISA(eUISA.RESERVE.id)
        object RESERVE_ADDR : UISA(eUISA.RESERVE_ADDR.id)
    }

    fun denied(reg: Int, op: String) {
        throw GeneralException("Temporary block to $op: $reg (${toString()})")
    }
    fun denied_read(reg: Int) : Long {
        denied(reg, "read")
        return 0
    }

    fun denied_write(reg: Int) = denied(reg, "write")

    sealed class VEA(id: Int) : PPCRegister(id, Regtype.VEA) {
        override fun value(core: PPCCore, data: Long) = core.cpu.veaRegs.writeIntern(reg, data)
        override fun value(core: PPCCore): Long = core.cpu.veaRegs.readIntern(reg)

        open class REG_DBG_DENIED(id: Int) : VEA(id) {
            override fun value(core: PPCCore) = denied_read(reg)
            override fun value(core: PPCCore, data: Long) = denied_write(reg)
        }

        open class REG_DBG_READ(id: Int) : VEA(id) {
            override fun value(core: PPCCore, data: Long) = denied_write(reg)
        }

        open class REG_DBG_WRITE(id: Int) : VEA(id) {
            override fun value(core: PPCCore) = denied_read(reg)
        }

        //Time base facility (for reading)
        object TBL : REG_DBG_READ(eVEA.TBL.id) {
            override fun value(core: PPCCore): Long = core.cpu.oeaRegs.readIntern(eOEA.TBL.id)
        }
        object TBU : REG_DBG_READ(eVEA.TBU.id) {
            override fun value(core: PPCCore): Long = core.cpu.oeaRegs.readIntern(eOEA.TBU.id)
        }
    }


    sealed class OEA(id: Int) : PPCRegister(id, Regtype.OEA) {
        override fun value(core: PPCCore, data: Long) = core.cpu.oeaRegs.writeIntern(reg, data)
        override fun value(core: PPCCore): Long = core.cpu.oeaRegs.readIntern(reg)

        open class REG_DBG_DENIED(id: Int) : OEA(id) {
            override fun value(core: PPCCore) = denied_read(reg)
            override fun value(core: PPCCore, data: Long) = denied_write(reg)
        }

        open class REG_DBG_READ(id: Int) : OEA(id) {
            override fun value(core: PPCCore, data: Long) = denied_write(reg)
        }

        open class REG_DBG_WRITE(id: Int) : OEA(id) {
            override fun value(core: PPCCore) = denied_read(reg)
        }

        //<Configuration registers>
        //Machine state register
        object MSR : OEA(eOEA.MSR.id)

        //TODO: CHANGE FOR DIFFERENT PROCESSOR
        //Processor version register
        object PVR : OEA(eOEA.PVR.id) {

            override fun value(core: PPCCore): Long {
                log.severe { "Read from PVR!"}
                return 0x80210000
            }

            override fun value(core: PPCCore, data: Long) = denied_write(reg)

        }


        //<Memory management registers>
        //Instruction BAT registers
        /*object IBAT0U : OEA(eOEA.IBAT0U.id)
        object IBAT0L : OEA(eOEA.IBAT0L.id)
        object IBAT1U : OEA(eOEA.IBAT1U.id)
        object IBAT1L : OEA(eOEA.IBAT1L.id)
        object IBAT2U : OEA(eOEA.IBAT2U.id)
        object IBAT2L : OEA(eOEA.IBAT2L.id)
        object IBAT3U : OEA(eOEA.IBAT3U.id)
        object IBAT3L : OEA(eOEA.IBAT3L.id)

        //Data BAT registers
        object DBAT0U : OEA(eOEA.DBAT0U.id)
        object DBAT0L : OEA(eOEA.DBAT0L.id)
        object DBAT1U : OEA(eOEA.DBAT1U.id)
        object DBAT1L : OEA(eOEA.DBAT1L.id)
        object DBAT2U : OEA(eOEA.DBAT2U.id)
        object DBAT2L : OEA(eOEA.DBAT2L.id)
        object DBAT3U : OEA(eOEA.DBAT3U.id)
        object DBAT3L : OEA(eOEA.DBAT3L.id)*/

        //SDR1
        object SDR1 : REG_DBG_DENIED(eOEA.SDR1.id)

        //Address space register
        object ASR : REG_DBG_DENIED(eOEA.ASR.id)

        //Segment registers
        object SR0 : REG_DBG_DENIED(eOEA.SR0.id)
        object SR1 : REG_DBG_DENIED(eOEA.SR1.id)
        object SR2 : REG_DBG_DENIED(eOEA.SR2.id)
        object SR3 : REG_DBG_DENIED(eOEA.SR3.id)
        object SR4 : REG_DBG_DENIED(eOEA.SR4.id)
        object SR5 : REG_DBG_DENIED(eOEA.SR5.id)
        object SR6 : REG_DBG_DENIED(eOEA.SR6.id)
        object SR7 : REG_DBG_DENIED(eOEA.SR7.id)
        object SR8 : REG_DBG_DENIED(eOEA.SR8.id)
        object SR9 : REG_DBG_DENIED(eOEA.SR9.id)
        object SR10 : REG_DBG_DENIED(eOEA.SR10.id)
        object SR11 : REG_DBG_DENIED(eOEA.SR11.id)
        object SR12 : REG_DBG_DENIED(eOEA.SR12.id)
        object SR13 : REG_DBG_DENIED(eOEA.SR13.id)
        object SR14 : REG_DBG_DENIED(eOEA.SR14.id)
        object SR15 : REG_DBG_DENIED(eOEA.SR15.id)


        //<Exception handling registers>
        //Data address register
        object DAR : REG_DBG_DENIED(eOEA.DAR.id)

        //DSISR
        object DSISR : REG_DBG_DENIED(eOEA.DSISR.id)

        //SPRGs
        object SPRG0 : OEA(eOEA.SPRG0.id)
        object SPRG1 : OEA(eOEA.SPRG1.id)
        object SPRG2 : OEA(eOEA.SPRG2.id)
        object SPRG3 : OEA(eOEA.SPRG3.id)

        //Save and restore registers
        //There aren't special functions
        object SRR0 : OEA(eOEA.SRR0.id)
        object SRR1 : OEA(eOEA.SRR1.id)

        //Floating-point exception cause register (optional)
        object FPECR : REG_DBG_DENIED(eOEA.FPECR.id)


        //<Miscellaneous registers>
        //Time base facility (for writing)
        object TBL : REG_DBG_WRITE(eOEA.TBL.id)
        object TBU : REG_DBG_WRITE(eOEA.TBU.id)

        //Timer/decrement registers


        //Data address breakpoint register (optional)
        object DABR : REG_DBG_DENIED(eOEA.DABR.id)

        //TimeBase
        object DEC : OEA(eOEA.DEC.id)

        //External access register (optional)
        object EAR : REG_DBG_DENIED(eOEA.EAR.id)

        //Processor identification register (optional)
        object PIR : REG_DBG_DENIED(eOEA.PIR.id)
    }

}