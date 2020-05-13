package ru.inforion.lab403.kopycat.modules.stm32f042

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import java.util.logging.Level

@Suppress("PrivatePropertyName", "PropertyName", "SpellCheckingInspection")
class EXTI(parent: Module, name: String) : Module(parent, name) {
    companion object {
        private val log = logger(Level.ALL)
        private enum class RegisterType(val offset: Long) {
            EXTI_IMR    (0x00),
            EXTI_EMR    (0x04),
            EXTI_RTSR   (0x08),
            EXTI_FTSR   (0x0C),
            EXTI_SWIER  (0x10),
            EXTI_PR     (0x14)
        }
    }

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", 0x40)
    }

    override val ports = Ports()

    private open inner class RegisterBase(
            register: RegisterType,
            default: Long = 0x0000_0000,
            writable: Boolean = true,
            readable: Boolean = true,
            level: Level = Level.FINE
    ) : Register(ports.mem, register.offset, Datatype.DWORD, register.name, default, writable, readable, level)

    init {
        RegisterType.values().forEach {
            if (it == RegisterType.EXTI_IMR)
                RegisterBase(it, default = 0x7FF4_0000)
            else
                RegisterBase(it)
        }
    }

}