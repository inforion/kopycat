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
package ru.inforion.lab403.kopycat.cores.arm.hardware.processors

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.FINER
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.hardware.registers.VMSABank
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ARMDecoder
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.Thumb16Decoder
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.Thumb32Decoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.modules.cores.AARMCore.InstructionSet
import ru.inforion.lab403.kopycat.modules.cores.AARMv6Core


class ARMv6CPU(
        core: AARMv6Core,
        name: String,
        haveSecurityExt: Boolean = false,
        haveVirtExt: Boolean = false
) : AARMCPU(core, name, haveSecurityExt, haveVirtExt) {

    companion object {
        @Transient val log = logger(FINER)
    }

    override fun InITBlock(): Boolean = status.ITSTATE[3..0] != 0b0000L

    override fun LastInITBlock(): Boolean = status.ITSTATE[3..0] == 0b1000L

    private val thumb16 = Thumb16Decoder(core)
    private val thumb32 = Thumb32Decoder(core)
    private val armDc = ARMDecoder(core)

    override fun reset() {
        super.reset()

        // ID for ARMv6
        // According to proc-v6.S from linux kernel sources:
        // 	.long	0x0007b000
        //	.long	0x0007f000
        // The first one is the masked value
        // The second one is the mask
        vmsa.midr.implementer = VMSABank.Implementer.ArmLimited.data
        vmsa.midr.architecture = VMSABank.Architecture.ARMv6.data
        vmsa.midr.primaryPartNumber = 0xB00

        // Cache type, see B4.1.42
        vmsa.ctr.format = VMSABank.CacheTypeFormat.ARMv6.data
        vmsa.ctr.cwg = 0b0000 // Not provide Cache Write-back Granule information
        vmsa.ctr.erg = 0b0000 // Not provide Cache Exclusives Reservation Granule information
        vmsa.ctr.dminLine = 0b0000 // Log2 of the number of words in the smallest cache line of all the data caches...
        vmsa.ctr.l1lp = VMSABank.L1IP.AIVIVT.data
        vmsa.ctr.iminLine = 0b0000 // Log2 of the number of words in the smallest cache line of all the instruction caches...

        val sp = 0x0000_0000L
        val pc = 0x0000_0000L

        log.fine { "pc=0x${pc.hex8} sp=0x${sp.hex8}" }

        BXWritePC(pc)
        regs.sp.value = sp
        regs.lr.value = 0xFFFF_FFFF

        pipelineRefillRequired = false
    }

    private fun fetch(where: Long): Long = core.fetch(where, 0 ,4)

    private fun swapByte(data: Long): Long {
        val high = data and 0xFFFF_0000
        val low = data and 0xFFFF
        return (high shr 16) or (low shl 16)
    }

    private var offset: Int = 0

    override fun decode() {
        var data: Long
        val decoder: ADecoder<AARMInstruction>

        when (CurrentInstrSet()) {
            InstructionSet.ARM -> {
                data = fetch(pc)
                decoder = armDc
                offset = 4
            }
            InstructionSet.THUMB -> {
                data = fetch(pc clr 0)
                val type = data[15..11]
                // 16 bits thumb instruction
                if (type != 0b11101L && type != 0b11110L && type != 0b11111L){
                    data = data[15..0]
                    decoder = thumb16
                    offset = 2
                } else { // 32 bits thumb instruction
                    data = swapByte(data)
                    decoder = thumb32
                    offset = 0
                    TODO("CHECK IT (OFFSET)")
                }
            }
            else -> throw ARMHardwareException.Undefined
        }

        insn = decoder.decode(data)
        insn.ea = pc

//        println("[${pc.hex8}] ${insn.opcode.hex8} $insn")
    }

    override fun execute(): Int {
        pc += insn.size + offset
        val lrBefore = regs.lr.value
        try {
            insn.execute()

            // check A7.3.3 about ITSTATE in ARMv7-M ref. manual
            if (InITBlock())
                ITAdvance()
        } catch (error: Throwable) {
            pc = insn.ea
            throw error
        }

        if (!pipelineRefillRequired) {
            // PC points at the address for fetched instruction when executing
            // restore normal code flow if no jump occurred
            pc -= offset
            callOccurred = false
        } else {
            // PC has been changed during instruction execution-> nothing to fix up
            pipelineRefillRequired = false
            callOccurred = regs.lr.value != lrBefore
        }

        return 1  // TODO: get from insn.execute()
    }

}