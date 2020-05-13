package ru.inforion.lab403.kopycat.modules.common

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import java.util.logging.Level

class Signals(parent: Module, name: String, val size: Long, val value: Long) : Module(parent, name) {

    companion object {
        private val log = logger(Level.FINE)
    }

    inner class Ports : ModulePorts(this) {
        val wires = Slave("wires", this@Signals.size)
    }

    override val ports = Ports()

    val area = object : Area(ports.wires, 0, size - 1, "SIGNALS") {
        override fun fetch(ea: Long, ss: Int, size: Int): Long = throw IllegalAccessException("Can't fetch $name")
        override fun read(ea: Long, ss: Int, size: Int): Long = value[ea.asInt]
        override fun write(ea: Long, ss: Int, size: Int, value: Long) = Unit
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> = mapOf(
            "size" to size.hex8,
            "value" to value.hex16
    )

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>){
        val sizeSnapshot = (snapshot["size"] as String).hexAsULong
        check(sizeSnapshot == size) { "size: %08X != %08X".format(size, sizeSnapshot) }

        val valueSnapshot = (snapshot["value"] as String).hexAsULong
        check(valueSnapshot == value) { "value: %16X != %16X".format(value, valueSnapshot) }
    }
}