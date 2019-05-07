package ru.inforion.lab403.kopycat.modules.stm32f042

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import java.util.logging.Level

@Suppress("PrivatePropertyName", "PropertyName", "SpellCheckingInspection")
class FLASH(parent: Module, name: String) : Module(parent, name) {
    companion object {
        private val log = logger(Level.ALL)
        private enum class RegisterType(val offset: Long) {
            FLASH_ACR       (0x00),
            FLASH_KEYR      (0x04),
            FLASH_OPTKEYR   (0x08),
            FLASH_SR        (0x0C),
            FLASH_CR        (0x10),
            FLASH_AR        (0x14),
            FLASH_OBR       (0x1C),
            FLASH_WRPR      (0x20)
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
        RegisterType.values().forEach { RegisterBase(it) }
    }
}