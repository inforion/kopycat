package ru.inforion.lab403.kopycat.cores.base.operands

import org.junit.Test
import ru.inforion.lab403.common.extensions.asLong
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.device.ATest
import ru.inforion.lab403.kopycat.device.TestCore

class MemoryTest: ATest() {
    private fun error(value: Long, expected: Long, actual: Long, test: String): String =
            error(value, expected, actual, "memory", test)

    @Test fun test1_1() {
        address = 0x8_AB56
        value = 0xDEAD_BEEF
        store(address, value)
        expected = 0xDEAD_BEEF
        actual = memory(address).value(testCore)
        assert(error(address, expected, actual, "value"), expected, actual)
    }

    @Test fun test1_2() {
        address = 0x8_AB56
        value = 0xDEAD_BEEF
        store(address, value)
        expected = 1
        actual = memory(address, atyp = WORD).isZero(testCore).asLong
        assert(error(address, expected, actual, "value"), expected, actual)
    }

    @Test fun test2_1() {
        address = 0x8_AB56
        value = 0x4A78
        store(address, value)
        actual = memory(address).ssext(testCore)
        expected = 0x4A78L
        assert(error(value, expected, actual, "ssext"), expected, actual)
    }

    @Test fun test2_2() {
        address = 0x8_AB56
        value = 0x4A78
        store(address, value)
        actual = memory(address).usext(testCore)
        expected = 0x4A78
        assert(error(value, expected, actual, "usext"), expected, actual)
    }

    @Test fun test2_3() {
        address = 0x8_AB56
        value = 0x4A78
        store(address, value)
        actual = memory(address).zext(testCore)
        expected = 0x4A78
        assert(error(value, expected, actual, "zext"), expected, actual)
    }

    @Test fun test2_4() {
        address = 0x8_AB56
        value = 0x4A78
        store(address, value)
        actual = memory(address).bit(testCore, 6).asLong
        expected = 1
        assert(error(value, expected, actual, "bit"), expected, actual)
    }

    @Test fun test2_5() {
        address = 0x8_AB56
        value = 0x4A78
        store(address, value)
        actual = memory(address).msb(testCore).asLong
        expected = 0
        assert(error(value, expected, actual, "msb"), expected, actual)
    }

    @Test fun test2_6() {
        address = 0x8_AB56
        value = 0x4A78
        store(address, value)
        actual = memory(address).lsb(testCore).asLong
        expected = 0
        assert(error(value, expected, actual, "lsb"), expected, actual)
    }

    @Test fun test2_7() {
        address = 0x8_AB56
        value = 0x4A78
        store(address, value)
        actual = memory(address).lsb(testCore).asLong
        expected = 0
        assert(error(value, expected, actual, "lsb"), expected, actual)
    }
}