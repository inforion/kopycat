package ru.inforion.lab403.kopycat.cores.arm.support

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import ru.inforion.lab403.common.extensions.MHz
import ru.inforion.lab403.kopycat.cores.arm.hardware.registers.CPSRBank
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.modules.cores.ARMv7Core

/**
 * Created by r.valitov on 02.02.18
 */

class ARMBankTest: Module(null, "ARM Bank Test") {
    private fun assert(expected: Long, actual: Long) {
        Assert.assertEquals(expected, actual)
    }

    private val arm = ARMv7Core(this, "arm", 48.MHz, 1.0)
    private val bank = CPSRBank(arm)

    @Before
    fun resetTest() {
        arm.reset()
        bank.reset()
    }

    @Test fun test1() {
        bank.ITSTATE = 0b1010_1010
        assert(0b0000_0100_0000_0000_1010_1000_0000_0000, arm.cpu.sregs.cpsr)
        Assert.assertEquals(bank.ITSTATE, 0b1010_1010)
    }

    @Test fun test2() {
        bank.ge = 0b0110
        bank.m  = 0b10011
        bank.ITSTATE = 0b1110_0011
        bank.f  = true
        assert(0b0000_0110_0000_0110_1110_0000_0101_0011, arm.cpu.sregs.cpsr)
        Assert.assertEquals(bank.ge,0b0110)
        Assert.assertEquals(bank.m, 0b10011)
        Assert.assertEquals(bank.ITSTATE,0b1110_0011)
        Assert.assertEquals(bank.f, true)
    }
}