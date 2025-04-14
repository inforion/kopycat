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
package ru.inforion.lab403.kopycat.cores.arm.hardware.processors

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.FINER
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ARMDecoder
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.Thumb16Decoder
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.Thumb32Decoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.modules.cores.AARMCore.InstructionSet
import ru.inforion.lab403.kopycat.modules.cores.ARMv7Core
import ru.inforion.lab403.kopycat.interfaces.*


class ARMv7CPU(core: ARMv7Core, name: String) : AARMCPU(core, name) {
    companion object {
        @Transient val log = logger(FINER)
    }

    override fun InITBlock(): Boolean = status.ITSTATE[3..0] != 0b0000uL

    override fun LastInITBlock(): Boolean = status.ITSTATE[3..0] == 0b1000uL

    private val thumb16 = Thumb16Decoder(core)
    private val thumb32 = Thumb32Decoder(core)
    private val armDc = ARMDecoder(core)

    override fun reset() {
        super.reset()

        val sp = core.inl(0x0800_0000u)
        val pc = core.inl(0x0800_0004u)

        log.fine { "pc=${pc.hex8} sp=${sp.hex8}" }

        BXWritePC(pc)
        regs.sp.value = sp
        regs.lr.value = 0xFFFF_FFFFu

        pipelineRefillRequired = false
    }

    private fun fetch(where: ULong): ULong = core.fetch(where, 0 ,4)

    private fun swapByte(data: ULong): ULong {
        val high = data and 0xFFFF_0000u
        val low = data and 0xFFFFu
        return (high ushr 16) or (low shl 16)
    }

    private var offset: UInt = 0u

    override fun decode() {
        var data: ULong
        val decoder: ADecoder<AARMInstruction>

        when (CurrentInstrSet()) {
            InstructionSet.ARM -> {
                data = fetch(pc)
                decoder = armDc
                offset = 4u
            }
            InstructionSet.THUMB -> {
                data = fetch(pc clr 0)
                val type = data[15..11]
                // 16 bits thumb instruction
                if (type != 0b11101uL && type != 0b11110uL && type != 0b11111uL){
                    data = data[15..0]
                    decoder = thumb16
                    offset = 2u
                } else { // 32 bits thumb instruction
                    data = swapByte(data)
                    decoder = thumb32
                    offset = 0u
                }
            }
            else -> throw ARMHardwareException.Undefined
        }

        insn = decoder.decode(data)
        insn.ea = pc

        println("[${pc.hex8}] ${insn.opcode.hex8} $insn")
    }

    override fun execute(): Int {
        pc += offset + insn.size

        try {
            if (ConditionPassed(insn.cond)) insn.execute()

            if (InITBlock()) // check A7.3.3 about ITSTATE in ARMv7-M ref. manual
                ITAdvance()

        } catch (error: Throwable) {
            pc = insn.ea
            throw error
        }

        if (!pipelineRefillRequired) {
            // PC points at the address for fetched instruction when executing
            // restore normal code flow if no jump occurred
            pc -= offset
        } else {
            // PC has been changed during instruction execution-> nothing to fix up
            pipelineRefillRequired = false
        }

        return 1  // TODO: get from insn.execute()
    }
}