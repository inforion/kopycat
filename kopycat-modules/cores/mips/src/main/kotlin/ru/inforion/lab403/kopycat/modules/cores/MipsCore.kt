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
package ru.inforion.lab403.kopycat.modules.cores

import ru.inforion.lab403.kopycat.cores.base.abstracts.ACore
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.mips.MIPSABI
import ru.inforion.lab403.kopycat.cores.mips.hardware.processors.*
import ru.inforion.lab403.kopycat.modules.BUS32

/**
 * @param frequency частота ядра (используется при вычислении прошедшего времени в [SystemClock])
 * @param multiplier умножение частоты ядра
 * @param PABITS ширина физической адресной шины
 * @param ArchitectureRevision ревизия архитектуры MIPS
 * @param countOfShadowGPR количество теневых наборов регистров MIPS
 * @param Config0Preset начальное значение регистра Config0
 * @param Config1Preset начальное значение регистра Config1
 * @param Config2Preset начальное значение регистра Config2
 * @param Config3Preset начальное значение регистра Config3
 * @param IntCtlPreset  начальное значение регистра IntCtl
 * @param syncSupported инструкция sync не будет вызывать исключение процессора
 * @param EIC_option1 опция обработки прерываний 1 (см. MIPS)
 * @param EIC_option2 опция обработки прерываний 2 (см. MIPS)
 * @param EIC_option3 опция обработки прерываний 3 (см. MIPS)
 * @param dspExtension поддержка инструкций dsp
 * @param countRateFactor скоростью счета таймера регистра Count относительно циклов ядра (количество циклов умножается
 *                        на заданное в этой переменной значении и добавляется к счетчик). То есть значение в регистре
 *                        Count будет равно количество_циклов_ядра * [countRate]
 * @param countCompareSupported внутренний таймер архитектуры MIPS построенный на регистрах Count/Compare
 *                              если данный таймер не используется, то рекомендуется отключить его для
 *                              повышения быстродействия.
 */
class MipsCore constructor(
        parent: Module,
        name: String,
        frequency: Long,
        ipc: Double,
        val multiplier: Long,
        val PRId: Long,
        val PABITS: Int,
        val ArchitectureRevision: Int = 1,
        val countOfShadowGPR: Int = 0,
        val Config0Preset: Long = 0,
        val Config1Preset: Long = 0,
        val Config2Preset: Long = 0,
        val Config3Preset: Long = 0,
        val IntCtlPreset: Long = 0,
        val countRateFactor: Int = 2,
        val syncSupported: Boolean = false,
        val countCompareSupported: Boolean = false,
        val EIC_option1: Boolean = false,
        val EIC_option2: Boolean = false,
        val EIC_option3: Boolean = false,
        val dspExtension: Boolean = false,
        val useMMU: Boolean = true
) : ACore<MipsCore, MipsCPU, ACOP0>(parent, name, frequency * multiplier, ipc) {

    /**
     * {EN}For simplifying instantiating from json{EN}
     */
    constructor(parent: Module, name: String, frequency: Long, ipc: Double, PRId: Long, PABITS: Int) :
            this(parent, name, frequency, ipc, 1, PRId, PABITS)

    private val VASIZE = BUS32  // always 32 bit
    private val PASIZE = 1L shl PABITS

    override val cpu = MipsCPU(this, "cpu")

    override val fpu = MipsFPU(this, "fpu")

    override val cop: ACOP0 = when (ArchitectureRevision) {
        1 -> COP0v1(this, "cop")
        else -> COP0v2(this, "cop")
    }

    override val mmu = MipsMMU(this, "mmu", PASIZE)

    override fun abi() = MIPSABI(this, false)

    inner class Buses : ModuleBuses(this) {
        val physical = Bus("physical", PASIZE)
        val virtual = Bus("virtual", VASIZE)
    }

    inner class Ports : ModulePorts(this) {
        val mem = Proxy("mem", PASIZE)
    }

    override val buses = Buses()
    override val ports = Ports()

    init {
        // ToCheck: bad but working solution
        if (useMMU) {
            cpu.ports.mem.connect(buses.virtual)
            mmu.ports.inp.connect(buses.virtual)

            mmu.ports.outp.connect(buses.physical)
            ports.mem.connect(buses.physical)
        }
        else {
            cpu.ports.mem.connect(buses.virtual)
            ports.mem.connect(buses.virtual)
        }
    }
}