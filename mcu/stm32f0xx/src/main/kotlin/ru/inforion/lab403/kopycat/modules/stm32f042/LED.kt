package ru.inforion.lab403.kopycat.modules.stm32f042

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.find
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS
import ru.inforion.lab403.kopycat.modules.PIN
import java.util.logging.Level
import kotlin.properties.Delegates

class LED(parent: Module, name: String) : Module(parent, name) {
    companion object {
        private val log = logger(Level.ALL)
        enum class STATE(val id: Int) {
            OFF     (0),
            ON      (1),
            UNKNOWN (-1)
        }
    }

    inner class Ports : ModulePorts(this) {
        val pin = Slave("pin", PIN)
    }

    override val ports = Ports()

    private val ledControl = object : Area(ports.pin, 0, 0, "GPIO_INPUT", ACCESS.R_W) {
        override fun fetch(ea: Long, ss: Int, size: Int) = throw IllegalAccessException("$name may not be fetched!")
        override fun read(ea: Long, ss: Int, size: Int) = state.id.toLong()
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            state = find<STATE> { it.id == value.asInt } ?: STATE.UNKNOWN
        }
    }

    var state by Delegates.observable(STATE.UNKNOWN) { _, old, new ->
        if (old != new && core.clock.time() != 0L) log.info { stringify() }
    }

    override fun stringify(): String {
        val time = if (isCorePresent) core.clock.time() else -1
        return "LED [ %5s ] state is %s @ %,d us".format(name, state, time)
    }

    override fun reset() {
        super.reset()
        state = STATE.OFF
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> = mapOf("state" to state.toString())

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        state = STATE.valueOf(snapshot["state"] as String)
    }
}
