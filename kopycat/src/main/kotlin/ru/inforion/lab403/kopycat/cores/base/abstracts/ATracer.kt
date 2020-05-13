package ru.inforion.lab403.kopycat.cores.base.abstracts

import ru.inforion.lab403.common.extensions.toLong
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.enums.Status
import ru.inforion.lab403.kopycat.cores.base.extensions.*
import ru.inforion.lab403.kopycat.interfaces.ITracer
import java.util.logging.Level

/**
 * {RU}
 * Абстрактный класс трассировщика.
 * Служит для выполнения дополнительных пре- и пост-обработок в процессе выполнения инструкции.
 *
 *
 * @param parent родительский модуль, в который встраивается Ядро
 * @param name произвольное имя объекта Ядра
 * @property ports набор портов
 * @property status статус трассировщика
 * @property io регистр для взаимодействия с трассировщиком
 * {RU}
 */
abstract class ATracer<R: AGenericCore>(
        parent: Module,
        name: String
): Module(parent, name), ITracer<R> {

    /**
     * {RU}Объект-логгер{RU}
     */
    companion object {
        val log = logger(Level.FINE)
    }

    /**
     * {RU}
     * Внутренний класс набора портов
     *
     * @property mem порт памяти
     * @property trace порт трейсера
     * {RU}
     */
    inner class Ports : ModulePorts(this) {
        val mem = Proxy("mem")
        val trace = Slave("trace", TRACER_BUS_SIZE)
    }

    final override val ports = Ports()

    private val status = Status.values()

    private val io = object : Register(ports.trace, 0, DWORD, "TRACE_IO") {
        /**
         * {EN}
         * @param ss - Trace command
         * @param size - Core execute status
         * {EN}
         */
        override fun read(ea: Long, ss: Int, size: Int): Long {
            @Suppress("UNCHECKED_CAST") val core = core as R
            return when (ss) {
                TRACER_EVENT_PRE_EXECUTE -> preExecute(core)
                TRACER_EVENT_POST_EXECUTE -> postExecute(core, status[size])
                TRACER_EVENT_START -> {
                    onStart()
                    true
                }
                TRACER_EVENT_STOP -> {
                    onStop()
                    true
                }
                else -> throw IllegalArgumentException("Unknown tracer command!")
            }.toLong()
        }
    }
}