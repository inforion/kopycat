package ru.inforion.lab403.kopycat.modules.stm32f042

import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import java.util.logging.Level

@Suppress("PrivatePropertyName", "PropertyName", "SpellCheckingInspection")
class TSC(parent: Module, name: String) : Module(parent, name) {
    companion object {
        private val log = logger(Level.ALL)
        private enum class RegisterType(val offset: Long) {
            TSC_CR          (0x00),
            TSC_IER         (0x04),
            TSC_ICR         (0x08),
            TSC_ISR         (0x0C),
            TSC_IOHCR       (0x10),
            TSC_IOASCR      (0x18),
            TSC_IOSCR       (0x20),
            TSC_IOCCR       (0x28),
            TSC_IOGCSR      (0x30),
            TSC_IOG1CR      (0x34),
            TSC_IOG2CR      (0x38),
            TSC_IOG3CR      (0x3C),
            TSC_IOG4CR      (0x40),
            TSC_IOG5CR      (0x44),
            TSC_IOG6CR      (0x48),
            TSC_IOG7CR      (0x4C),
            TSC_IOG8CR      (0x50)
        }
    }

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", 0x80)
    }

    override val ports = Ports()

    private open inner class RegisterBase(
            register: RegisterType,
            default: Long = 0x0000_0000,
            writable: Boolean = true,
            readable: Boolean = true,
            level: Level = Level.FINE
    ) : Register(ports.mem, register.offset, Datatype.DWORD, register.name, default, writable, readable, level)

    private open inner class TSC_IOGCSR_TYP : RegisterBase(RegisterType.TSC_IOGCSR, default = 0x00FF_00FF, level = Level.ALL) { // def value signalize that acquisition enable and complete
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            val filtered = value.insert(default, (23..16)) // this pins are read only
            super.write(ea, ss, size, filtered)
        }
    }

    init {
        RegisterType.values().forEach {
            when (it) {
                RegisterType.TSC_ISR -> RegisterBase(it, default = 0x0000_0001)
                RegisterType.TSC_IOHCR -> RegisterBase(it, default = 0xFFFF_FFFF)
                RegisterType.TSC_IOGCSR -> TSC_IOGCSR_TYP()
                RegisterType.TSC_IOG1CR,
                RegisterType.TSC_IOG2CR,
                RegisterType.TSC_IOG3CR,
                RegisterType.TSC_IOG4CR,
                RegisterType.TSC_IOG5CR,
                RegisterType.TSC_IOG6CR,
                RegisterType.TSC_IOG7CR,
                RegisterType.TSC_IOG8CR -> RegisterBase(it, default = 0x0000_000F)  // little charge
                else -> RegisterBase(it)

            }
        }
    }
}