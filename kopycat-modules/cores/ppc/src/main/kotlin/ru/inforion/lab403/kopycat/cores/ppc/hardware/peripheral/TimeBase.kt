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
package ru.inforion.lab403.kopycat.cores.ppc.hardware.peripheral

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.SystemClock
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.ppc.enums.eTCR
import ru.inforion.lab403.kopycat.cores.ppc.enums.systems.embedded.eOEA_Embedded
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.interfaces.ISerializable
import ru.inforion.lab403.kopycat.modules.cores.PPCCore
import ru.inforion.lab403.kopycat.modules.cores.PPCCoreEmbedded


class TimeBase(parent: Module, name: String, freq: Long) : Module(parent, name) {
    val ppccore = parent as PPCCoreEmbedded

    private inner class Incrementer {
        private fun valueHigh(core: PPCCore, data: Long) = PPCRegister.OEA.TBU.value(core, data)
        private fun valueLow(core: PPCCore, data: Long) = PPCRegister.OEA.TBL.value(core, data)
        private fun valueHigh(core: PPCCore): Long = PPCRegister.VEA.TBU.value(core)
        private fun valueLow(core: PPCCore): Long = PPCRegister.VEA.TBL.value(core)
        private fun inc(core: PPCCore): Boolean {
            val low = valueLow(core) + 1L
            valueLow(core, low)
            if (low == 0L) {
                val high = valueHigh(core) + 1L
                valueHigh(core, high)
                if (high == 0L)
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

        private fun value(core: PPCCore, data: Long) = PPCRegister.OEA.DEC.value(core, data)
        private fun value(core: PPCCore): Long = PPCRegister.OEA.DEC.value(core)
        private fun dec(core: PPCCore): Boolean {
            val data = value(core)
            if (data != 0L)
            {
                val newData = data - 1L
                val outValue = if (newData == 0L && autoreload) {
                    core.cpu.sprRegs.readIntern(eOEA_Embedded.DECAR.id)
                }
                else
                    newData
                value(core, outValue)
                return newData == 0L
            }
            return false
        }

        fun trigger() {
            dec(ppccore)

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
            autoreload = (snapshot["autoreload"] as String?)?.toBoolean() ?: false
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



    fun operateTCR(value: Long) {
        decrementer.autoreload = value[eTCR.ARE.bit].toBool()
        if (value and (1L shl eTCR.ARE.bit).inv() != 0L)
            throw GeneralException("Now we have to implement some fields to continue")
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
            decrementer.deserialize(ctxt, dec)
    }

}