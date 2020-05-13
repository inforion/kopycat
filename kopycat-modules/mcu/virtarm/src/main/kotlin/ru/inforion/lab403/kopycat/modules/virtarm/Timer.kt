package ru.inforion.lab403.kopycat.modules.virtarm

import ru.inforion.lab403.common.extensions.mask
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.common.SystemClock
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.extensions.pending
import ru.inforion.lab403.kopycat.cores.base.extensions.request
import ru.inforion.lab403.kopycat.modules.PIN
import java.util.logging.Level

class Timer(parent: Module, name: String, private val divider: Long) : Module(parent, name) {
    companion object {
        private val log = logger(Level.FINE)
    }

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", 0x14)
        val irq = Master("irq", PIN)
    }

    override val ports = Ports()

    private inner class Timer : SystemClock.PeriodicalTimer("Timer Counter") {
        var mode = false

        override fun trigger() {
            super.trigger()
            CURRENT_VALUE_REG.data = (CURRENT_VALUE_REG.data - 1) mask 32

            if (CURRENT_VALUE_REG.data == 0L) {
                CURRENT_VALUE_REG.data = LOAD_COUNT_REG.data

                if (CONTROL_REG.interruptMask == 0 && EOI_REG.data == 0L) {
                    EOI_REG.data = 1
                    ports.irq.request(0)
                }
            }
        }
    }

    private val timer = Timer()



    override fun initialize(): Boolean {
        if (!super.initialize()) return false
        core.clock.connect(timer, divider, false)
        return true
    }

    inner class CONTROL : Register(ports.mem, 0x08, Datatype.DWORD, "CONTROL_REG") {
        var enabled by bit(0)
        var mode by bit(1)
        var interruptMask by bit(2)

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)

            timer.enabled = enabled.toBool()
            timer.mode = mode.toBool()
            // TODO: configure interrupt
        }
    }


    private var LOAD_COUNT_REG = Register(ports.mem, 0x00, Datatype.DWORD, "LOAD_COUNT_REG")
    private var CURRENT_VALUE_REG = Register(ports.mem, 0x04, Datatype.DWORD, "CURRENT_VALUE_REG")
    private var CONTROL_REG = CONTROL()
    private var EOI_REG = object : Register(ports.mem, 0x0C, Datatype.DWORD, "EOI_REG", writable = false) {
        override fun read(ea: Long, ss: Int, size: Int): Long {
            val ret = super.read(ea, ss, size)
            write(ea, ss, size, 0L)
            return ret
        }

    }
    private var INT_STATUS_REG = Register(ports.mem, 0x10, Datatype.DWORD, "INT_STATUS_REG")
}