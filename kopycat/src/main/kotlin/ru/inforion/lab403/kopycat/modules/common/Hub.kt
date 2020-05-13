package ru.inforion.lab403.kopycat.modules.common

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.MasterPort
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import ru.inforion.lab403.kopycat.modules.BUS32
import java.util.logging.Level

/**
 * ```
 *           _______
 *          |  HUB ||>-->-->-->
 *  Master  |      |
 * -->->->->|      ||>-->-->-->
 *   Bus    |      |
 *          |______||>-->-->-->
 * ```
 */

class Hub(parent: Module, name: String, vararg val outs: Pair<String, Long>) : Module(parent, name) {

    companion object {
        val log = logger(Level.CONFIG)
    }

    inner class Ports : ModulePorts(this) {
        val input = Slave("input", BUS32)
        val outputs = Array(outs.size) { k -> Master(outs[k].first, outs[k].second) }
    }

    override val ports = Ports()

    val MEM_SPACE = object : Area(ports.input, 0L, BUS32 - 1, "HUB_AREA") {
        override fun fetch(ea: Long, ss: Int, size: Int) = throw IllegalAccessException("$name may not be fetched!")

        override fun read(ea: Long, ss: Int, size: Int): Long {
            val ports = ports.outputs.filter { output -> output.find(output, ea, ss, size, AccessAction.LOAD, 0) != null }
            if (ports.size != 1) {
                throw MemoryAccessError(-1, ea, AccessAction.LOAD,
                        if (ports.isEmpty())
                            "No area or register found at address ${ea.hex8} at hub ${this.name}"
                        else
                            "More then one area or register found at address ${ea.hex8} at hub ${this.name}")
            }
            return ports.first().read(ea, ss, size)
        }

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            val areas = ports.outputs.filter { output -> output.find(output, ea, ss, size, AccessAction.STORE, value) != null }
            if (areas.isEmpty())
                throw MemoryAccessError(-1, ea, AccessAction.STORE, "No area or register found at address ${ea.hex8} at hub ${this.name}")
            areas.forEach { it.write(ea, ss, size, value) }
        }
    }
}