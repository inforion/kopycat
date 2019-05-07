package ru.inforion.lab403.kopycat.modules.stm32f042

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import java.util.logging.Level

@Suppress("PrivatePropertyName", "PropertyName", "SpellCheckingInspection")
class SYSCFG(parent: Module, name: String) : Module(parent, name) {
    companion object {
        private val log = logger(Level.ALL)
        private enum class RegisterType(val offset: Long) {
            SYSCFG_CFGR1    (0x00),
            SYSCFG_EXTICR1  (0x08),
            SYSCFG_EXTICR2  (0x0C),
            SYSCFG_EXTICR3  (0x10),
            SYSCFG_EXTICR4  (0x14),
            SYSCFG_CFGR2    (0x18),
            SYSCFG_ITLINE0  (0x80),
            SYSCFG_ITLINE1  (0x84),
            SYSCFG_ITLINE2  (0x88),
            SYSCFG_ITLINE3  (0x8C),
            SYSCFG_ITLINE4  (0x90),
            SYSCFG_ITLINE5  (0x94),
            SYSCFG_ITLINE6  (0x98),
            SYSCFG_ITLINE7  (0x9C),
            SYSCFG_ITLINE8  (0xA0),
            SYSCFG_ITLINE9  (0xA4),
            SYSCFG_ITLINE10 (0xA8),
            SYSCFG_ITLINE11 (0xAC),
            SYSCFG_ITLINE12 (0xB0),
            SYSCFG_ITLINE13 (0xB4),
            SYSCFG_ITLINE14 (0xB8),
            SYSCFG_ITLINE15 (0xBC),
            SYSCFG_ITLINE16 (0xC0),
            SYSCFG_ITLINE17 (0xC4),
            SYSCFG_ITLINE18 (0xC8),
            SYSCFG_ITLINE19 (0xCC),
            SYSCFG_ITLINE20 (0xD0),
            SYSCFG_ITLINE21 (0xD4),
            SYSCFG_ITLINE22 (0xD8),
            SYSCFG_ITLINE23 (0xDC),
            SYSCFG_ITLINE24 (0xE0),
            SYSCFG_ITLINE25 (0xE4),
            SYSCFG_ITLINE26 (0xE8),
            SYSCFG_ITLINE27 (0xEC),
            SYSCFG_ITLINE28 (0xF0),
            SYSCFG_ITLINE29 (0xF4),
            SYSCFG_ITLINE30 (0xF8)
        }
    }

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", 0x200)
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