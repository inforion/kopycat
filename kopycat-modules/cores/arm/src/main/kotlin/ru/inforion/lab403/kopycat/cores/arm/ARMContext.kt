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
package ru.inforion.lab403.kopycat.cores.arm

import ru.inforion.lab403.kopycat.cores.arm.hardware.processors.AARMCPU
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.AContext



class ARMContext(cpu: AARMCPU): AContext<AARMCPU>(cpu) {
    override fun setRegisters(map: Map<String, Long>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    var r0: Long = 0
    var r1: Long = 0
    var r2: Long = 0
    var r3: Long = 0
    var r4: Long = 0
    var r5: Long = 0
    var r6: Long = 0
    var r7: Long = 0
    var r8: Long = 0
    var r9: Long = 0
    var r10: Long = 0
    var r11: Long = 0
    var r12: Long = 0
    var r13: Long = 0
    var r14: Long = 0
    var r15: Long = 0

    override var vpc: Long
        get() = r15
        set(value) { r15 = value }

    override var vsp: Long
        get() = r13
        set(value) { r13 = value }

    override var vra: Long
        get() = r14
        set(value) { r14 = value }

    override var vRetValue: Long
        get() = r0
        set(value) { r0 = value }

    override fun load() {
        cpu.regs.r0.value = r0
        cpu.regs.r1.value = r1
        cpu.regs.r2.value = r2
        cpu.regs.r3.value = r3
        cpu.regs.r4.value = r4
        cpu.regs.r5.value = r5
        cpu.regs.r6.value = r6
        cpu.regs.r7.value = r7
        cpu.regs.r8.value = r8
        cpu.regs.r9.value = r9
        cpu.regs.r10.value = r10
        cpu.regs.r11.value = r11
        cpu.regs.r12.value = r12
        cpu.regs.spMain.value = r13
        cpu.regs.lr.value = r14
        cpu.regs.pc.value = r15
    }

    override fun save() {
        r0 = cpu.regs.r0.value
        r1 = cpu.regs.r1.value
        r2 = cpu.regs.r2.value
        r3 = cpu.regs.r3.value
        r4 = cpu.regs.r4.value
        r5 = cpu.regs.r5.value
        r6 = cpu.regs.r6.value
        r7 = cpu.regs.r7.value
        r8 = cpu.regs.r8.value
        r9 = cpu.regs.r9.value
        r10 = cpu.regs.r10.value
        r11 = cpu.regs.r11.value
        r12 = cpu.regs.r12.value
        r13 = cpu.regs.spMain.value
        r14 = cpu.regs.lr.value
        r15 = cpu.regs.pc.value
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}