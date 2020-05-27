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
package ru.inforion.lab403.kopycat.cores.mips

import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.extensions.toULong
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.AContext
import ru.inforion.lab403.kopycat.cores.mips.hardware.processors.MipsCPU



class MIPSContext(cpu: MipsCPU) : AContext<MipsCPU>(cpu) {
    override fun setRegisters(map: Map<String, Long>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    var v0: Long = 0
    var v1: Long = 0

    var k0: Long = 0
    var k1: Long = 0

    var a0: Long = 0
    var a1: Long = 0
    var a2: Long = 0
    var a3: Long = 0

    var s0: Long = 0
    var s1: Long = 0
    var s2: Long = 0
    var s3: Long = 0
    var s4: Long = 0
    var s5: Long = 0
    var s6: Long = 0
    var s7: Long = 0

    var t0: Long = 0
    var t1: Long = 0
    var t2: Long = 0
    var t3: Long = 0
    var t4: Long = 0
    var t5: Long = 0
    var t6: Long = 0
    var t7: Long = 0
    var t8: Long = 0
    var t9: Long = 0

    var ra: Long = 0
    var gp: Long = 0
    var fp: Long = 0
    var sp: Long = 0

    var pc: Long = 0

    override var vpc: Long
        get() = pc
        set(value) { pc = value }

    override var vsp: Long
        get() = sp
        set(value) { sp = value }

    override var vra: Long
        get() = ra
        set(value) { ra = value }

    override var vRetValue: Long
        get() = v0
        set(value) { v0 = value }

    override fun load() {
        cpu.regs.v0 = v0
        cpu.regs.v1 = v1

        cpu.regs.k0 = k0
        cpu.regs.k1 = k1

        cpu.regs.a0 = a0
        cpu.regs.a1 = a1
        cpu.regs.a2 = a2
        cpu.regs.a3 = a3

        cpu.regs.s0 = s0
        cpu.regs.s1 = s1
        cpu.regs.s2 = s2
        cpu.regs.s3 = s3
        cpu.regs.s4 = s4
        cpu.regs.s5 = s5
        cpu.regs.s6 = s6
        cpu.regs.s7 = s7

        cpu.regs.t0 = t0
        cpu.regs.t1 = t1
        cpu.regs.t2 = t2
        cpu.regs.t3 = t3
        cpu.regs.t4 = t4
        cpu.regs.t5 = t5
        cpu.regs.t6 = t6
        cpu.regs.t7 = t7
        cpu.regs.t8 = t8
        cpu.regs.t9 = t9

        cpu.regs.ra = ra
        cpu.regs.gp = gp
        cpu.regs.fp = fp
        cpu.regs.sp = sp

//        dev.cpu.regs.pc = pc
    }

    override fun save() {
        v0 = cpu.regs.v0
        v1 = cpu.regs.v1

        k0 = cpu.regs.k0
        k1 = cpu.regs.k1

        a0 = cpu.regs.a0
        a1 = cpu.regs.a1
        a2 = cpu.regs.a2
        a3 = cpu.regs.a3

        s0 = cpu.regs.s0
        s1 = cpu.regs.s1
        s2 = cpu.regs.s2
        s3 = cpu.regs.s3
        s4 = cpu.regs.s4
        s5 = cpu.regs.s5
        s6 = cpu.regs.s6
        s7 = cpu.regs.s7

        t0 = cpu.regs.t0
        t1 = cpu.regs.t1
        t2 = cpu.regs.t2
        t3 = cpu.regs.t3
        t4 = cpu.regs.t4
        t5 = cpu.regs.t5
        t6 = cpu.regs.t6
        t7 = cpu.regs.t7
        t8 = cpu.regs.t8
        t9 = cpu.regs.t9

        ra = cpu.regs.ra
        gp = cpu.regs.gp
        fp = cpu.regs.fp
        sp = cpu.regs.sp

//        pc = dev.cpu.regs.pc
    }


    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return mapOf(
                "v0" to v0.hex,
        "v1" to v1.hex,
        "k0" to k0.hex,
        "k1" to k1.hex,
        "a0" to a0.hex,
        "a1" to a1.hex,
        "a2" to a2.hex,
        "a3" to a3.hex,
        "s0" to s0.hex,
        "s1" to s1.hex,
        "s2" to s2.hex,
        "s3" to s3.hex,
        "s4" to s4.hex,
        "s5" to s5.hex,
        "s6" to s6.hex,
        "s7" to s7.hex,
        "t0" to t0.hex,
        "t1" to t1.hex,
        "t2" to t2.hex,
        "t3" to t3.hex,
        "t4" to t4.hex,
        "t5" to t5.hex,
        "t6" to t6.hex,
        "t7" to t7.hex,
        "t8" to t8.hex,
        "t9" to t9.hex,
        "ra" to ra.hex,
        "gp" to gp.hex,
        "fp" to fp.hex,
        "sp" to sp.hex,
        "pc" to pc.hex
        )
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        v0 = (snapshot["v0"] as String).toULong(16)
        v1 = (snapshot["v1"] as String).toULong(16)
        k0 = (snapshot["k0"] as String).toULong(16)
        k1 = (snapshot["k1"] as String).toULong(16)
        a0 = (snapshot["a0"] as String).toULong(16)
        a1 = (snapshot["a1"] as String).toULong(16)
        a2 = (snapshot["a2"] as String).toULong(16)
        a3 = (snapshot["a3"] as String).toULong(16)
        s0 = (snapshot["s0"] as String).toULong(16)
        s1 = (snapshot["s1"] as String).toULong(16)
        s2 = (snapshot["s2"] as String).toULong(16)
        s3 = (snapshot["s3"] as String).toULong(16)
        s4 = (snapshot["s4"] as String).toULong(16)
        s5 = (snapshot["s5"] as String).toULong(16)
        s6 = (snapshot["s6"] as String).toULong(16)
        s7 = (snapshot["s7"] as String).toULong(16)
        t0 = (snapshot["t0"] as String).toULong(16)
        t1 = (snapshot["t1"] as String).toULong(16)
        t2 = (snapshot["t2"] as String).toULong(16)
        t3 = (snapshot["t3"] as String).toULong(16)
        t4 = (snapshot["t4"] as String).toULong(16)
        t5 = (snapshot["t5"] as String).toULong(16)
        t6 = (snapshot["t6"] as String).toULong(16)
        t7 = (snapshot["t7"] as String).toULong(16)
        t8 = (snapshot["t8"] as String).toULong(16)
        t9 = (snapshot["t9"] as String).toULong(16)
        ra = (snapshot["ra"] as String).toULong(16)
        gp = (snapshot["gp"] as String).toULong(16)
        fp = (snapshot["fp"] as String).toULong(16)
        sp = (snapshot["sp"] as String).toULong(16)
        pc = (snapshot["pc"] as String).toULong(16)
        
    }
}