package ru.inforion.lab403.kopycat.cores.arm

import ru.inforion.lab403.kopycat.cores.arm.hardware.processors.AARMCPU
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.AContext

/**
 * Created by p.rusanov on 14.02.18.
 */

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
        cpu.regs.r0 = r0
        cpu.regs.r1 = r1
        cpu.regs.r2 = r2
        cpu.regs.r3 = r3
        cpu.regs.r4 = r4
        cpu.regs.r5 = r5
        cpu.regs.r6 = r6
        cpu.regs.r7 = r7
        cpu.regs.r8 = r8
        cpu.regs.r9 = r9
        cpu.regs.r10 = r10
        cpu.regs.r11 = r11
        cpu.regs.r12 = r12
        cpu.regs.spMain = r13
        cpu.regs.lr = r14
        cpu.regs.pc = r15
    }

    override fun save() {
        r0 = cpu.regs.r0
        r1 = cpu.regs.r1
        r2 = cpu.regs.r2
        r3 = cpu.regs.r3
        r4 = cpu.regs.r4
        r5 = cpu.regs.r5
        r6 = cpu.regs.r6
        r7 = cpu.regs.r7
        r8 = cpu.regs.r8
        r9 = cpu.regs.r9
        r10 = cpu.regs.r10
        r11 = cpu.regs.r11
        r12 = cpu.regs.r12
        r13 = cpu.regs.spMain
        r14 = cpu.regs.lr
        r15 = cpu.regs.pc
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}