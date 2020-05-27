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
package ru.inforion.lab403.kopycat.cores.ppc.operands.systems

import ru.inforion.lab403.common.extensions.first
import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.enums.Regtype
import ru.inforion.lab403.kopycat.cores.ppc.enums.systems.e500v2.eOEA_e500v2
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



abstract class PPCRegister_e500v2(
        reg: Int,
        rtyp: Regtype,
        access: AOperand.Access = AOperand.Access.ANY) :
        PPCRegister(reg, rtyp, access) {

    override fun toString() = when (rtyp) {
        Regtype.E500_OEA -> first<eOEA_e500v2> { it.id == reg }.name
        else -> super.toString()
    }.toLowerCase()


    sealed class OEAext(id: Int) : PPCRegister_e500v2(id, Regtype.E500_OEA) {
        override fun value(core: PPCCore): Long = core.cpu.sprRegs.readIntern(reg)
        override fun value(core: PPCCore, data: Long) = core.cpu.sprRegs.writeIntern(reg, data)

        open class REG_DBG_DENIED(id: Int) : OEAext(id) {
            override fun value(core: PPCCore) = denied_read(reg)
            override fun value(core: PPCCore, data: Long) = denied_write(reg)
        }

        open class REG_DBG_READ(id: Int) : OEAext(id) {
            override fun value(core: PPCCore, data: Long) = denied_write(reg)
        }

        open class REG_DBG_WRITE(id: Int) : OEAext(id) {
            override fun value(core: PPCCore) = denied_read(reg)
        }

        //Interrupt registers

        //Critical SRR 0/1
        //There aren't special functions
        object CSRR0 : OEAext(eOEA_e500v2.CSRR0.id)
        object CSRR1 : OEAext(eOEA_e500v2.CSRR1.id)

        //Machine check SRR 0/1 [1]
        //There aren't special functions
        object MCSRR0 : OEAext(eOEA_e500v2.MCSRR0.id)
        object MCSRR1 : OEAext(eOEA_e500v2.MCSRR1.id)

        //Exception syndrome register
        //Waiting for read point
        object ESR : REG_DBG_WRITE(eOEA_e500v2.ESR.id)

        //Machine check syndrome register [1]
        //Waiting for read point
        object MCSR : REG_DBG_WRITE(eOEA_e500v2.MCSR.id)

        //MCAR - 573

        //Data exception address register
        //Waiting for read point
        object DEAR : REG_DBG_WRITE(eOEA_e500v2.DEAR.id)

        //Interrupt vector offset registers 0-15
        object IVOR0 : OEAext(eOEA_e500v2.IVOR0.id)
        object IVOR1 : OEAext(eOEA_e500v2.IVOR1.id)
        object IVOR2 : OEAext(eOEA_e500v2.IVOR2.id)
        object IVOR3 : OEAext(eOEA_e500v2.IVOR3.id)
        object IVOR4 : OEAext(eOEA_e500v2.IVOR4.id)
        object IVOR5 : OEAext(eOEA_e500v2.IVOR5.id)
        object IVOR6 : OEAext(eOEA_e500v2.IVOR6.id)
        object IVOR7 : OEAext(eOEA_e500v2.IVOR7.id)
        object IVOR8 : OEAext(eOEA_e500v2.IVOR8.id)
        object IVOR9 : OEAext(eOEA_e500v2.IVOR9.id)
        object IVOR10 : OEAext(eOEA_e500v2.IVOR10.id)
        object IVOR11 : OEAext(eOEA_e500v2.IVOR11.id)
        object IVOR12 : OEAext(eOEA_e500v2.IVOR12.id)
        object IVOR13 : OEAext(eOEA_e500v2.IVOR13.id)
        object IVOR14 : OEAext(eOEA_e500v2.IVOR14.id)
        object IVOR15 : OEAext(eOEA_e500v2.IVOR15.id)

        object L1CFG0 : OEAext(eOEA_e500v2.L1CFG0.id)
        object L1CFG1 : OEAext(eOEA_e500v2.L1CFG1.id)

        //Interrupt vector offset registers 32-35 [1]
        object IVOR32 : REG_DBG_WRITE(eOEA_e500v2.IVOR32.id)
        object IVOR33 : REG_DBG_WRITE(eOEA_e500v2.IVOR33.id)
        object IVOR34 : REG_DBG_WRITE(eOEA_e500v2.IVOR34.id)
        object IVOR35 : REG_DBG_WRITE(eOEA_e500v2.IVOR35.id)

        //Debug registers

        //Debug control registers 0-2
        //Waiting for read point
        object DBCR0 : REG_DBG_WRITE(eOEA_e500v2.DBCR0.id)
        object DBCR1 : REG_DBG_WRITE(eOEA_e500v2.DBCR1.id)
        object DBCR2 : REG_DBG_WRITE(eOEA_e500v2.DBCR2.id)

        //Debug status register
        //TODO: was reassign as is
        object DBSR : OEAext(eOEA_e500v2.DBSR.id)

        //Instruction address compare registers 1 and 2
        //Waiting for read point
        object IAC1 : REG_DBG_WRITE(eOEA_e500v2.IAC1.id)
        object IAC2 : REG_DBG_WRITE(eOEA_e500v2.IAC2.id)

        //Data address compare registers 1 and 2
        //Waiting for read point
        object DAC1 : REG_DBG_WRITE(eOEA_e500v2.DAC1.id)
        object DAC2 : REG_DBG_WRITE(eOEA_e500v2.DAC2.id)

        //MMU control and status (Read only)

        //MMU configuration [1]
        //MMUCFG : OEAext(1015)

        //TLB configuration 0/1 [1]
        //TLB0CFG - 688
        //TLB1CFG - 689

        //L1 Cache  : OEAext(Read/Write)

        //L1 cache control/status 0/1 [1]
        //No need because we don't use
        object L1CSR0 : OEAext(eOEA_e500v2.L1CSR0.id)
        object L1CSR1 : OEAext(eOEA_e500v2.L1CSR1.id)

        // Configuration registers
        object SVR : OEAext(eOEA_e500v2.SVR.id) {
            override fun value(core: PPCCore): Long {
                log.severe { "Read from SVR!"}
                return 0x80800000
            }

            override fun value(core: PPCCore, data: Long) = denied_write(reg)
        }
        object PIR : OEAext(eOEA_e500v2.PIR.id)

        //TODO: Many skipped

        //Hardware implementation-dependent register 0
        //Waiting for write point
        object HID0 : OEAext(eOEA_e500v2.HID1.id) {

            override fun value(core: PPCCore, data: Long) {
                //TODO: NOT IMPLEMENTED
                super.value(core, data)
                val TBEN = bit(core, 14)
                if (TBEN.toBool())
                    log.severe { "Now time base enabled" }
            }
        }

        //Hardware implementation-dependent register 1
        object HID1 : OEAext(eOEA_e500v2.HID1.id) {

            override fun value(core: PPCCore): Long {
                val pllCfg = 0b110001_01L //Ratio of 2:1
                return pllCfg shl 24
                //super.value(core)
            }

            override fun value(core: PPCCore, data: Long) {
                log.warning { "PID1 value: ${data.hex8}" }
                super.value(core, data)
            }


        }

        //Miscellaneous registers
        //Branch control and status register [3]
        //TODO: Maybe important - branch buffer
        object BUCSR : OEAext(eOEA_e500v2.BUCSR.id)

    }
}