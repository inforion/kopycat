/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACore
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.mips.MIPSABI
import ru.inforion.lab403.kopycat.cores.mips.Microarchitecture
import ru.inforion.lab403.kopycat.cores.mips.enums.MipsCallingConvention
import ru.inforion.lab403.kopycat.cores.mips.hardware.processors.*
import ru.inforion.lab403.kopycat.cores.mips.hardware.processors.mips64.COP064
import ru.inforion.lab403.kopycat.modules.BUS32
import ru.inforion.lab403.kopycat.modules.BUS64

/**
 * @param frequency частота ядра (используется при вычислении прошедшего времени в [SystemClock])
 * @param multiplier умножение частоты ядра
 * @param PABITS ширина физической адресной шины
 * @param ArchitectureRevision ревизия архитектуры MIPS
 * @param countOfShadowGPR количество теневых наборов регистров MIPS
 * @param IntCtlPreset начальное значение регистра С0 IntClt
 * @param PRId processor identification and revision
 * @param Config0Preset начальное значение регистра Config0
 * @param Config1Preset начальное значение регистра Config1
 * @param Config2Preset начальное значение регистра Config2
 * @param Config3Preset начальное значение регистра Config3
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
 *                              * @param PABITS ширина физической адресной шины (размер физического адресного пр-ва 2^PABITS)
 * @param SEGBITS регулирует величину виртуального адреса. Реальное количество бит, реализованных для каждого
 *                        64-битного сегмента.
 *                        Например, реализовано 40 бит вирт.адреса -> реальный размер сегмента 2^SEGBITS = 2^40 байт.
 */
class MipsCore constructor(
    parent: Module,
    name: String,
    frequency: Long,
    ipc: Double,
    val multiplier: Long,
    val PRId: ULong,
    val PABITS: Int,
    val SEGBITS: Int = 32, // mips32 default
    val ArchitectureRevision: Int = 1,
    val countOfShadowGPR: Int = 0,
    val Config0Preset: ULong = 0u,
    val Config1Preset: ULong = 0u,
    val Config2Preset: ULong = 0u,
    val Config3Preset: ULong = 0u,
    val Config4Preset: ULong = 0u,
    val IntCtlPreset: ULong = 0u,
    val countRateFactor: Int = 2,
    val syncSupported: Boolean = false,
    var countCompareSupported: Boolean = false,
    val EIC_option1: Boolean = false,
    val EIC_option2: Boolean = false,
    val EIC_option3: Boolean = false,
    val dspExtension: Boolean = false,
    val useMMU: Boolean = true,
    val fpuDtype: Datatype = Datatype.DWORD,
    val microarchitecture: Microarchitecture = Microarchitecture.None,
    val mmuTlbEntries: Int? = null,
    val abi: MipsCallingConvention = MipsCallingConvention.O64
) : ACore<MipsCore, MipsCPU, ACOP0>(parent, name, frequency * multiplier, ipc) {
    /**
     * {EN}For simplifying instantiating from json{EN}
     */
    constructor(parent: Module, name: String, frequency: Long, ipc: Double, PRId: ULong, PABITS: Int) :
            this(parent, name, frequency, ipc, 1, PRId, PABITS)

    override val cpu = MipsCPU(this, "cpu")

    val segmask = ubitMask64(SEGBITS) or if (is64bit) 0xc000_0000_0000_0000uL else 0uL

    val is32bit get() = cpu.mode == MipsCPU.Mode.R32
    val is64bit get() = cpu.mode == MipsCPU.Mode.R64

    val PASIZE = 1uL shl PABITS // if (is32bit) 1uL shl PABITS else 0xFFFFFFFFFFFFFFFFuL
    // always 32 bit if mips32
    val VASIZE = if (is32bit) BUS32 else BUS64

    override val fpu = MipsFPU(this, "fpu", dtype = fpuDtype)

    override val cop: ACOP0 = when {
        ArchitectureRevision == 1 && is32bit -> COP0v1(this, "cop")
        ArchitectureRevision != 1 && is32bit -> COP0v2(this, "cop")
        else -> COP064(this, "cop")
    }

    override val mmu = when {
        mmuTlbEntries != null -> MipsMMU(this, "mmu", PASIZE, mmuTlbEntries)
        // p. 263 PRA
        Config1Preset.truth -> MipsMMU(this, "mmu", PASIZE, Config1Preset[30..25].int + 1)
        else -> MipsMMU(this, "mmu", PASIZE)
    }

    // See MIPS PRA Chapter 3 "MIPS64 and microMIPS64 Operating Modes"
    val isRingUserMode get() = cop.regs.Status.KSU == 0b10uL && !cop.regs.Status.EXL && !cop.regs.Status.ERL
    val isRingSupervisorMode get() = cop.regs.Status.KSU == 0b01uL && !cop.regs.Status.EXL && !cop.regs.Status.ERL
    val isRingKernelMode get() = cop.regs.Status.KSU == 0b00uL || cop.regs.Status.EXL || cop.regs.Status.ERL

    override fun abi() = MIPSABI(this, false)

    val dspModule = if (dspExtension) DSPModule(this, "DSP Module") else null

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
        } else {
            cpu.ports.mem.connect(buses.virtual)
            ports.mem.connect(buses.virtual)
        }
    }
    override fun stringify() = buildString {
        if (useMMU) {
            val mmuAddress = mmu.translate(cpu.pc, 0, 4, AccessAction.FETCH)
            appendLine(
                "PC [0x${cpu.pc.hex16}] --|MMU|--> " +
                        "[0x${mmuAddress.hex16}]"
            )
        }

        appendLine(super.stringify())
    }
}