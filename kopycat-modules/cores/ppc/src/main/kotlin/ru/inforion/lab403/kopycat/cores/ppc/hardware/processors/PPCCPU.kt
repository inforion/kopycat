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

import ru.inforion.lab403.common.extensions.sure
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACPU
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.ppc.enums.eSystem
import ru.inforion.lab403.kopycat.cores.ppc.enums.eUISA
import ru.inforion.lab403.kopycat.cores.ppc.hardware.registers.*
import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.PPCSystemDecoder
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.SPR
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



class PPCCPU(val ppc: PPCCore, name: String, vararg systems: eSystem):
        ACPU<PPCCPU, PPCCore, APPCInstruction, eUISA>(ppc, name) {

    override var pc : Long
        get() = regs.PC
        set(value) { regs.PC = value }

    override fun reg(index: Int): Long = regs[index].value(ppc)
    override fun reg(index: Int, value: Long) = regs[index].value(ppc, value)
    override fun count() = regs.count()

    val regs = UISABank(ppc)
    val veaRegs = VEABank(ppc)
    val oeaRegs = OEABank(ppc)
    val xerBits = XERBank(ppc)
    val crBits = CRBank(ppc)
    val msrBits = MSRBank(ppc)
    val sprRegs = SPRBank(ppc, *systems)
    val sprMap = mutableMapOf<Int, SPR>().apply {
        for (s in systems)
            s.sprs.forEach {
                if (it.id in this.keys)
                    throw GeneralException("SPR ${it.id} (${it.name}) has an collision with (${this[it.id]!!.name})")
                this[it.id] = it
            }

    }

    private val decoder = PPCSystemDecoder(ppc, *systems)

    fun spr(id: Int) : SPR = sprMap[id].sure { "Unknown SPR: $id" }
    fun sprSwap(id: Int) : SPR = spr(SPR.swap(id))

    override fun reset() {
        super.reset()
        decoder.reset()
        regs.reset()
        regs.PC = 0xFFFF_FFFC

        /*for (i: Int in 0..31)
            regs.gpr(i).value(core as PPCCore, (0xFF00 or i).toLong())
        oeaRegs.MSR = 0x12345678
        regs.CR = 0x5555
        regs.LR = 0x6666
        regs.CTR = 0x8765
        regs.XER = 0xFFF1234*/
    }

    override fun decode() {
        insn = decoder.decode(pc)
    }

    override fun execute(): Int {
        pc += 4
        insn.execute()
        return 1  // TODO: get from insn.execute()
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return mapOf(
                "uisa" to regs.serialize(ctxt),
                "vea" to veaRegs.serialize(ctxt),
                "oea" to oeaRegs.serialize(ctxt),
                "spr" to sprRegs.serialize(ctxt)
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        regs.deserialize(ctxt, snapshot["uisa"] as Map<String, String>)
        veaRegs.deserialize(ctxt, snapshot["vea"] as Map<String, String>)
        oeaRegs.deserialize(ctxt, snapshot["oea"] as Map<String, String>)
        sprRegs.deserialize(ctxt, snapshot["spr"] as Map<String, String>)
    }

}