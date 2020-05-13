package ru.inforion.lab403.kopycat.modules.cores

import ru.inforion.lab403.kopycat.cores.base.abstracts.ABI
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACore
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.v850es.hardware.processors.v850ESCOP
import ru.inforion.lab403.kopycat.cores.v850es.hardware.processors.v850ESCPU

/**
 * {RU}
 * Ядро v850ES
 *
 *
 * @property parent модуль, куда встраивается ядро
 * @property name произвольное имя объекта
 * @property frequency частота работы ядра
 * {RU}
 */
class v850ESCore(parent: Module, name: String, frequency: Long):
        ACore<v850ESCore, v850ESCPU, v850ESCOP>(parent, name, frequency, 1.0) {

    inner class Ports : ModulePorts(this) {
        val mem = Proxy("mem")
    }

    inner class Buses: ModuleBuses(this) {
        val mem = Bus("mem")
    }

    override val ports = Ports()
    override val buses = Buses()

    override val cpu = v850ESCPU(this, "cpu")
    override val cop = v850ESCOP(this, "cop")
    override val mmu = null
    override val fpu = null

    override fun abi(heap: LongRange, stack: LongRange): ABI<v850ESCore> =
            throw NotImplementedError("Operating system not supported")

    init {
        cpu.ports.mem.connect(buses.mem)
        ports.mem.connect(buses.mem)
    }
}