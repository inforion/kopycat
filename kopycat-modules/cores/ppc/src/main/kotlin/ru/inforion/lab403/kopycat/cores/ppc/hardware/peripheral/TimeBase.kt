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
package ru.inforion.lab403.kopycat.cores.ppc.hardware.peripheral

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.SystemClock
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.ppc.enums.eIrq
import ru.inforion.lab403.kopycat.cores.ppc.enums.eTCR
import ru.inforion.lab403.kopycat.cores.ppc.enums.eTSR
import ru.inforion.lab403.kopycat.cores.ppc.enums.systems.embedded.eOEA_Embedded
import ru.inforion.lab403.kopycat.cores.ppc.exceptions.PPCExceptionHolder_e500v2
import ru.inforion.lab403.kopycat.cores.ppc.exceptions.PPCHardwareException
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.cores.ppc.operands.systems.PPCRegister_Embedded
import ru.inforion.lab403.kopycat.cores.ppc.operands.systems.PPCRegister_e500v2
import ru.inforion.lab403.kopycat.interfaces.ISerializable
import ru.inforion.lab403.kopycat.modules.cores.PPCCore
import ru.inforion.lab403.kopycat.modules.cores.PPCCoreEmbedded


class TimeBase(parent: Module, name: String, freq: Long) : Module(parent, name) {
    val ppccore = parent as PPCCoreEmbedded


    private inner class Incrementer {
        private fun valueHigh(core: PPCCore, data: ULong) = PPCRegister.OEA.TBU.value(core, data)
        private fun valueLow(core: PPCCore, data: ULong) = PPCRegister.OEA.TBL.value(core, data)
        private fun valueHigh(core: PPCCore) = PPCRegister.VEA.TBU.value(core)
        private fun valueLow(core: PPCCore) = PPCRegister.VEA.TBL.value(core)
        private fun inc(core: PPCCore): Boolean {
            val low = valueLow(core) + 1uL
            valueLow(core, low)
            if (low == 0uL) {
                val high = valueHigh(core) + 1uL
                valueHigh(core, high)
                if (high == 0uL)
                    return true
            }
            return false
        }

        fun trigger() {
            inc(ppccore)

        }
    }

    private val incrementer = Incrementer()

    private inner class Decrementer : ISerializable{
        var autoreload = false
        var doInterrupts = false

        private fun value(core: PPCCore, data: ULong) = PPCRegister.OEA.DEC.value(core, data)
        private fun value(core: PPCCore): ULong = PPCRegister.OEA.DEC.value(core)
        private fun dec(core: PPCCore): Boolean {
            val data = value(core)
            if (data != 0uL)
            {
                val newData = data - 1uL
                val outValue = if (newData == 0uL && autoreload)
                    core.cpu.sprRegs.readIntern(eOEA_Embedded.DECAR.id) else newData

                value(core, outValue)
                return newData == 0uL
            }
            return false
        }

        fun trigger() {
            if (dec(ppccore)) {
                if (ppccore.cpu.msrBits.EE && doInterrupts)
                    throw PPCExceptionHolder_e500v2.DecrementerInterrupt(core.pc)
            }

            /*
            if (ppccore.cpu)
            val data = dec()
            if (data == 0L) {

            }*/
        }

        override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
            return mapOf("autoreload" to autoreload.toString())
        }

        @Suppress("UNCHECKED_CAST")
        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            autoreload = (snapshot["autoreload"] as String?)?.bool ?: false
        }
    }

    private val decrementer = Decrementer()

    private inner class Timer : SystemClock.PeriodicalTimer("TimeBase") {
        override fun trigger() {
            super.trigger()
            incrementer.trigger()
            decrementer.trigger()
        }
    }

    private val timer = Timer()



    fun operateTCR(value: ULong) {
        decrementer.autoreload = value[eTCR.ARE.bit].truth
        decrementer.doInterrupts = value[eTCR.DIE.bit].truth
        val todoMask = inv((1uL shl eTCR.ARE.bit) or (1uL shl eTCR.DIE.bit))
        if (value and todoMask != 0uL)
            TODO("Now we have to implement some fields to continue")
    }

    fun connect() {
        ppccore.clock.connect(timer, 1) // For debug
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return mapOf(
                "Decrementer" to decrementer.serialize(ctxt)
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        val dec = snapshot["Decrementer"]
        if (dec != null)
            decrementer.deserialize(ctxt, dec as Map<String, Any>)
    }

}