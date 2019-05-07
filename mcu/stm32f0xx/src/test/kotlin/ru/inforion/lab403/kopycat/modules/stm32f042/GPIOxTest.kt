package ru.inforion.lab403.kopycat.modules.stm32f042

import org.junit.Test
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.hex4
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS
import ru.inforion.lab403.kopycat.modules.BUS16
import ru.inforion.lab403.kopycat.modules.BUS32
import ru.inforion.lab403.kopycat.modules.stm32f042.GPIOx.Companion.RegisterType
import java.util.logging.Level
import kotlin.test.expect

abstract class GPIOxTest(val register: RegisterType) : Module(null, "GPIOx_test_module") {
    companion object {
        private val log = logger(Level.ALL)
    }

    class SimpleControllerTestModule(parent: Module?) : Module(parent, "controller_test_module") {
        inner class Ports : ModulePorts(this) {
            val mem = Master("mem", BUS32)
            val pin_input = Master("pin_input", BUS16)
            val pin_output = Slave("pin_output", BUS16)
        }

        override val ports = Ports()

    }

    val controller = SimpleControllerTestModule(this)
    val gpio = GPIOx(this, "gpio", 1)

    val pins = object : Area(controller.ports.pin_output, 0, 0x10, "GPIO_INPUT", ACCESS.I_W) {
        override fun read(ea: Long, ss: Int, size: Int): Long = 0L
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            log.info("gpio send output signal [${value.hex4}]")
        }
    }

    init {
        buses.connect(gpio.ports.mem, controller.ports.mem)
        buses.connect(gpio.ports.pin_input, controller.ports.pin_input)
        buses.connect(gpio.ports.pin_output, controller.ports.pin_output)
        buses.resolveSlaves()
    }

    fun memWrite(ea: Long, value: Long) = controller.ports.mem.write(ea, 0, 0, value)
    fun memRead(ea: Long) = controller.ports.mem.read(ea, 0, 0)

    fun ioWrite(value: Long) = controller.ports.pin_input.write(0, 0, 0, value)

    fun writeLockValues() {
        memWrite(RegisterType.LCKR.offset, 0b0000_0000_0000_0001__0000_0000_0000_0011)
        memWrite(RegisterType.LCKR.offset, 0b0000_0000_0000_0000__0000_0000_0000_0011)
        memWrite(RegisterType.LCKR.offset, 0b0000_0000_0000_0001__0000_0000_0000_0011)
    }

    fun setLock() {
        writeLockValues()
        memRead(RegisterType.LCKR.offset)
    }

    fun write(value: Long) = memWrite(register.offset, value)
    fun read() = memRead(register.offset)
}

abstract class GPIOxTestWithLock(register: RegisterType) : GPIOxTest(register) {

    @Test
    fun modeOfLockedPinCantChange() {
        write(0x0000_0001)
        expect(0x0000_0001) { read() }

        setLock()
        expect(0x0000_0001) { read() }

        write(0x0000_0002)
        expect(0x0000_0001) { read() }
    }

    @Test
    fun modeLockResetTest() {
        gpio.reset()
        write(0x0000_0001)
        setLock()
        expect(0x0000_0001) { read() }

        gpio.reset()
        write(0x0000_0002)
        setLock()
        expect(0x0000_0002) { read() }
    }

    @Test
    fun lockWriteNotAffectToModeChange() {
        write(0x0000_0001)
        expect(0x0000_0001) { read() }

        writeLockValues()
        expect(0x0000_0001) { read() }

        write(0x0000_0002)
        expect(0x0000_0002) { read() }

    }
}

class GPIOx_MODER_Test : GPIOxTestWithLock(RegisterType.MODER)

class GPIOx_OTYPER_Test : GPIOxTestWithLock(RegisterType.OTYPER)

class GPIOx_OSPEEDR_Test : GPIOxTestWithLock(RegisterType.OSPEEDR)

class GPIOx_PUPDR_Test : GPIOxTestWithLock(RegisterType.PUPDR)

class GPIOx_IDR_Test : GPIOxTest(RegisterType.IDR) {
    @Test
    fun inputSetTest() {
        ioWrite(0x0000_0005)
        expect(0x0000_0005) { read() }
    }
}

class GPIOx_BSRR_Test : GPIOxTest(RegisterType.BSRR) {
    @Test
    fun setResetTest() {
        write(0x0000_FFFE)
        expect(0x0000_FFFE) { memRead(0x14) }

        write(0xE000_0001)
        expect(0x0000_1FFF) { memRead(0x14) }
    }
}

class GPIOx_LCKR_Test : GPIOxTest(RegisterType.LCKR) {
    @Test
    fun lockSetTest() {
        expect(0L) { read()[16] }
        setLock()
        expect(1L) { read()[16] }
    }

    @Test
    fun lockNoChangeTest() {
        setLock()

        write(0x00011110)
        expect(0b0000_0000_0000_0001__0000_0000_0000_0011) { read() }
    }

    @Test
    fun lockResetTest() {
        setLock()
        gpio.reset()
        expect(0L) { read()[16] }

        setLock()
        expect(1L) { read()[16] }
    }
}

class GPIOx_AFRL_Test : GPIOxTestWithLock(RegisterType.AFRL)

class GPIOx_AFRH_Test : GPIOxTestWithLock(RegisterType.AFRH)

class GPIOx_BRR_Test : GPIOxTest(RegisterType.BRR) {
    @Test
    fun resetTest() {
        memWrite(0x14, 0x0000_FFFF)
        write(0x0000_0001)
        expect(0x0000_FFFE) { memRead(0x14) }
    }
}