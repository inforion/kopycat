/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2023 INFORION, LLC
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Non-free licenses may also be purchased from INFORION, LLC,
 * for users who do not want their programs protected by the GPL.
 * Contact us for details kopycat@inforion.ru
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package ru.inforion.lab403.kopycat.cores.base.common

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.INFO
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.*
import ru.inforion.lab403.kopycat.cores.base.abstracts.ATracer
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction.*
import ru.inforion.lab403.kopycat.cores.base.exceptions.CrossPrimitiveAccessException
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import ru.inforion.lab403.kopycat.interfaces.IFetchReadWrite
import ru.inforion.lab403.kopycat.interfaces.IReadWrite
import ru.inforion.lab403.kopycat.interfaces.ISerializable
import ru.inforion.lab403.kopycat.settings
import java.io.Serializable
import java.nio.ByteOrder
import kotlin.collections.HashSet

/**
 * {RU}
 * Класс, реализующий методы быстрого поиска примитивов,
 * к которым можно получить доступ через порты, прикрепленные к текущей шине
 *
 *
 * @param myBus Родительская шина, для которой используется этот кэш
 * {RU}
 *
 * {EN}
 * Class for fast search of primitives, that can be accessed through ports, that connected to current bus
 *
 * @param myBus Parent bus for which this cache is used
 * {EN}
 */
@Suppress("NOTHING_TO_INLINE")
internal class BusCache(private val myBus: Bus): Serializable {
    companion object {
        @Transient val log = logger(INFO)

        /**
         * {RU}
         * Этот метод вызывается перед чтением или записью [LorS] для всех примитивов [rw], которые находятся
         * по требуемому адресу. [ea] НЕ ЯВЛЯЕТСЯ АДРЕСОМ, который был выставлен на шину, а является смещением,
         * относительно начала примитива
         *
         * @param source Порт, который инициировал поиск примитивов на шинах
         * @param ea Смещение, относительно начала примитива, по которому будет произведено чтение или запись [LorS]
         * @param rw Найденный текущий примитив, который будет опрощен
         * @param LorS Действие, которе может быть совершено - чтение [AccessAction.LOAD] или запись [AccessAction.STORE]
         * @param value Значение для передачи в [IReadWrite.beforeWrite]
         * @return true если метод [IReadWrite.beforeRead] или [IReadWrite.beforeWrite] вернул true разрешил чтение или запись, иначе - false
         * {RU}
         *
         *
         * {EN}
         * This method is called before read or write [LorS] for all primitives [rw], which contains current address.
         * [ea] IS NOT ADDRESS which was placed on bus, it is offset in current primitive
         *
         * @param source master port that initiated the search for primitives on the bus
         * @param ea - offset from primitive start to read or write action
         * @param rw - founded current primitive which will be called
         * @param LorS - action for primitive - read ([AccessAction.LOAD]) or write ([AccessAction.STORE])
         * @param value - value for [IReadWrite.beforeWrite]
         * @return true if method [IReadWrite.beforeRead] or [IReadWrite.beforeWrite] return true and allow action (write or read), else false
         * {EN}
         */
        private inline fun <T: IFetchReadWrite>beforeAction(
                source: Port,
                ea: ULong,
                size: Int,
                rw: T,
                LorS: AccessAction,
                value: ULong) = when (LorS) {
            FETCH -> rw.beforeFetch(source, ea, size)
            LOAD -> rw.beforeRead(source, ea, size)
            STORE -> rw.beforeWrite(source, ea, size, value)
        }

        /**
         * {RU}
         * Осуществить копирование областей памяти с кэша шины [src] в кэш шины [dst]
         *
         * @param dst Кэш шины, на которую будет осуществлено копирование областей памяти
         * @param src Кэш шины, с которого будет осуществляться копирование областей памяти
         * @param endian Порядок байтов
         * {RU}
         *
         * {EN}
         * Copy areas cache from bus [src] to bus cache [dst]
         *
         * @param dst destination bus cache to copy areas
         * @param src source bus cache to copy areas
         * @param endian endianness
         * {EN}
         */
        private fun copyCache(dst: BusCache, src: BusCache, endian: ByteOrder) {
            if (src != dst) {
                dst.cachedAreas.addAll(
                    src.cachedAreas.mapNotNull {
                        val cached = it.changeEndian { e -> e xor endian }
                        if (dst.cachedAreas[cached.range.first]?.contains(cached) == true) {
                            null
                        } else {
                            cached
                        }
                    }
                )

                dst.cachedRegs.addAll(
                    src.cachedRegs.mapNotNull {
                        val cached = it.changeEndian { e -> e xor endian }
                        if (dst.cachedRegs[cached.range.first]?.contains(cached) == true) {
                            null
                        } else {
                            cached
                        }
                    }
                )
            }
        }

        private fun copyTranslators(dst: BusCache, src: BusCache, endian: ByteOrder) {
            if (src != dst) {
                dst.myBus.translators.addAll(
                    src.myBus.translators.map {
                        ModuleBuses.Connection(it.port, it.offset, endian xor it.endian)
                    }.filter { it !in dst.myBus.translators }
                )
            }
        }

        /**
         * {RU}
         * Функция выполняет рекурсивный спуск для заданной шины и кеша примитивов шины
         *
         * @param newCache Заполняемый кеш
         * @param bus Шина, для которой проводится кеширование примитивов
         * @param buses Множество шин, обработанных во время рекурсивного спуска
         * @param endian Порядок байтов
         * {RU}
         *
         * {EN}
         * This method make recursive calls from this bus to put all primitives from this bus and
         * from proxies bus to this cache
         *
         * @param newCache cache to fill
         * @param bus bus for which primitives are cached
         * @param buses Set of buses processed during recursive descent
         * @param endian Endianness
         * {EN}
         */
        private fun cacheBusPrimitives(newCache: BusCache, bus: Bus, buses: HashSet<Bus>, endian: ByteOrder) {
            if (buses.add(bus)) {
                copyCache(newCache, bus.cache, endian)
                copyTranslators(newCache, bus.cache, endian)
                bus.proxies.forEach { cacheProxyPortPrimitives(newCache, it.port, endian, buses) }
            }
        }

        private inline infix fun ByteOrder.xor(other: ByteOrder): ByteOrder {
            val thisBE = this === ByteOrder.BIG_ENDIAN
            val otherBE = other === ByteOrder.BIG_ENDIAN

            return if ((thisBE || otherBE) && !(thisBE && otherBE)) {
                ByteOrder.BIG_ENDIAN
            } else {
                ByteOrder.LITTLE_ENDIAN
            }
        }

        /**
         * {RU}
         * Функция выполняет рекурсивный спуск для заданного прокси порта и кеширует примитивы, которые расположены в шинах, присоединенных к этому прокси порту
         *
         * @param newCache Заполняемый кеш
         * @param proxy Прокси порт, подключенный к шине, для которого проводится кеширование примитивов
         * @param endian Порядок байтов
         * @param buses Обработанное во время рекурсивного спуска множество шин
         * {RU}
         *
         * {EN}
         * This method make recursive calls from selected proxy port and cache all primitives from buses connected to current proxy port
         *
         * @param newCache cache to fill
         * @param proxy proxy port from which to search
         * @param endian endianness
         * @param buses Set of buses processed during recursive descent
         * {EN}
         */
        private fun cacheProxyPortPrimitives(
            newCache: BusCache,
            proxy: ProxyPort,
            endian: ByteOrder,
            buses: HashSet<Bus>,
        ) = proxy.connections.forEach { conn ->
            cacheBusPrimitives(
                newCache,
                conn.bus,
                buses,
                endian xor conn.bus.proxies.find { it.port === proxy }!!.endian,
            )
        }

        private inline fun findPrimitive(
            primitives: RangeMultiMap<out BusCached>,
            source: Port,
            ea: ULong,
            size: Int,
            LorS: AccessAction,
            value: ULong,
            endian: ByteOrder,
        ): Entry? {
            var result: Entry? = null
            val valueBE = value.swap(size)

            val primitive = primitives[ea] ?: return null
            for (it in primitive) {
                val relativeAddress = ea - it.portOffset

                val finalEndian = it.endian xor endian
                val swapped = if (finalEndian === ByteOrder.BIG_ENDIAN) {
                    valueBE
                } else {
                    value
                }

                if (beforeAction(source, relativeAddress, size, it.rw, LorS, swapped)) {
                    if (result != null) {
                        throw MemoryAccessError(
                            ULONG_MAX,
                            ea,
                            LorS,
                            "More than one primitive in address ${ea.hex8}" +
                                    "\n1'st: ${result.cached}" +
                                    "\n2'nd: $it\n"
                        )
                    }
                    result = Entry(it, relativeAddress, finalEndian)
                }
            }

            if (result != null && size > 0 && ea + size - 1u !in result.cached.range) {
                // Cross-primitive access
                throw CrossPrimitiveAccessException(
                    result.cached.module.let { module ->
                        if (module.isCorePresent) {
                            module.core.pc
                        } else {
                            0uL
                        }
                    },
                    result.offset,
                    order = result.endian,
                )
            }

            return result
        }
    }

    /**
     * {RU}
     * Класс, который хранит в себе найденный примитив, и смешение относительно его начала
     *
     * @property cached найденный закешированный примитив
     * @property offset смещение относительно начала
     * @property endian порядок байтов
     * {RU}
     *
     * {EN}
     * The class that stores the primitive that was found and offset relative to its beginning
     *
     * @property cached cached primitive that was found
     * @property offset offset from the start of the primitive
     * @property endian endianness
     * {EN}
     */
    internal class Entry(val cached: BusCached, val offset: ULong, val endian: ByteOrder): Serializable {
        fun fetch(ss: Int, size: Int): ULong = cached.rw.fetch(offset, ss, size)
        fun read(ss: Int, size: Int): ULong = cached.rw.read(offset, ss, size)
        fun write(ss: Int, size: Int, value: ULong) = cached.rw.write(offset, ss, size, value)

        override fun toString() = "offset=0x${offset.hex8} ${cached.rw}"
    }

    internal sealed class BusCached(
        val rw: IFetchReadWrite,
        val connection: ModuleBuses.Connection<Port>,
        val endian: ByteOrder,
    ) {
        abstract val range: ULongRange
        abstract val module: Module
        abstract val dstPort: APort
        abstract fun changeEndian(fn: (ByteOrder) -> ByteOrder): BusCached

        val portOffset get() = connection.offset

        class Register(
            val reg: Module.Register,
            connection: ModuleBuses.Connection<Port>,
            endian: ByteOrder,
        ) : BusCached(reg, connection, endian), ISerializable {
            override val range = (reg.address + portOffset) until reg.address + portOffset + reg.datatype.bytes
            override val module get() = reg.module
            override val dstPort get() = reg.port
            override fun toString(): String = "${range.first.hex}..${range.last.hex} -> $reg ($endian)"
            override fun equals(other: Any?) = other is Register && reg === other.reg && portOffset == other.portOffset
            override fun hashCode() = 31 * reg.hashCode() + portOffset.hashCode()
            override fun changeEndian(fn: (ByteOrder) -> ByteOrder) = Register(reg, connection, fn(endian))
        }

        class Area(
            val area: Module.Area,
            connection: ModuleBuses.Connection<Port>,
            endian: ByteOrder,
        ) : BusCached(area, connection, endian), ISerializable {
            override val range = (area.start + portOffset)..(area.endInclusively + portOffset)
            override val module get() = area.module
            override val dstPort get() = area.port
            override fun toString(): String = "${range.first.hex8}..${range.last.hex8} -> $area ($endian)"
            override fun equals(other: Any?) = other is Area && area === other.area && portOffset == other.portOffset
            override fun hashCode() = 31 * area.hashCode() + portOffset.hashCode()
            override fun changeEndian(fn: (ByteOrder) -> ByteOrder) = Area(area, connection, fn(endian))
        }
    }

    private val cachedAreas = RangeMultiMap<BusCached.Area>()
    private val cachedRegs = RangeMultiMap<BusCached.Register>()

    /**
     * {RU}
     * Этот метод инициализирует кэш для текущей шины.
     * Метод получает все примитивы от портов, подключенных к шине, и,
     * при помощи дата-классов [BusCached.Area] и [BusCached.Register] переносит их в кэш для текущей шины.
     * Этот метод является первым этапом в процессе инициализации кэша всех шин.
     * Обратитесь к методу [Module.initializePortsAndBuses] для получения полной информации об инициализации кэша в шинах.
     * {RU}
     *
     * {EN}
     * This method will initialize cache for current bus.
     * It will get all primitives from all ports, which are connected to this bus and cache all
     * the primitives using data classes [BusCached.Area] and [BusCached.Register].
     * This is first step in buses cache initialization.
     * See method [Module.initializePortsAndBuses] to get full information about buses cache initialization.
     * {EN}
     */
    fun resolveSlaves() {
        cachedAreas.clear()
        cachedRegs.clear()
        myBus.ports.forEach { conn ->
            conn.port.areas
                .map { BusCached.Area(it, conn, conn.endian) }
                .forEach { cachedAreas.add(it.range, it) }

            conn.port.registers
                .map { BusCached.Register(it, conn, conn.endian) }
                .forEach { cachedRegs.add(it.range, it) }

            log.fine {
                val areas = if (cachedAreas.isNotEmpty())
                    cachedAreas.values().joinToString(prefix = "\n\tAreas:\n\t\t", separator = "\n\t\t") else ""
                val regs = if (cachedRegs.isNotEmpty())
                    cachedRegs.values().joinToString(prefix = "\n\tRegs:\n\t\t", separator = "\n\t\t") else ""
                "Cache update for bus ${this.myBus}:$areas$regs"
            }
        }
    }

    /**
     * {RU}
     * Этот метод разрешает все прокси-порты, делая их прозрачными для эмулятора. Это реализовано при помощи
     * копирования всего кэша (который был получен во время первого этапа инициализации) со всех шин, подключенных
     * друг к другу черед прокси-порты. В следствии чего, весь кэш примитивов со всех шин, соединенных портами,
     * окажется на каждой из этих шин, произойдет, можно сказать, обмен кэшем.
     * Этот метод является вторым этапом в процессе инициализации кэша всех шин.
     * Обратитесь к методу [Module.initializePortsAndBuses] для получения полной информации об инициализаии кэша в шинах.
     * {RU}
     *
     * {EN}
     * This method will resolve all proxy ports - make them transparent for emulator by copying all cache
     * (which inited in first step) from buses connected with Proxy ports, so that all cached primitives
     * are propagated to each of the buses.
     * This is second step in buses cache initialization.
     * See method [Module.initializePortsAndBuses] to get full information about buses cache initialization.
     * {EN}
     */
    fun resolveProxies() {
        cacheBusPrimitives(this, myBus, HashSet(), ByteOrder.LITTLE_ENDIAN)
        // фильтр с дебаггером связан с тем, что он охватывает все адресное пространство и в сумме оно всегда будет большое
        val totalRamSize = cachedAreas.values().filter { it.area.port.module !is Debugger }.sumOf { it.area.size }
        require(!settings.hasConstraints || totalRamSize <= settings.maxPossibleRamSize) {
            "RAM address space too large [${totalRamSize.hex8} <= ${settings.maxPossibleRamSize.hex8}]"
        }
    }

    /**
     * {RU}
     * Метод, который формирует строковое представление адресной карты примитивов, которые принадлежат текущей шине.
     * Для формирования берутся все примитивы (области памяти и регистры), которые находятся в кэше.
     * {RU}
     *
     * {EN}
     * This method generate string representation of the address map of primitives that connected to the current bus.
     * All primitives (memory areas and registers) that are in the cache are used to form the string.
     * @return string with memory map representation
     * {EN}
     */
    fun getPrintableMemoryMap(): String {
        val areas = cachedAreas.values()
            .joinToString(separator = "\n") { "\t$it" }

        val regs = cachedRegs.values()
            .joinToString(separator = "\n") { "\t$it" }

        val areasString = if (areas.isNotBlank()) "\nAreas:\n$areas" else ""
        val regsString = if (regs.isNotBlank()) "\nRegisters:\n$regs" else ""

        return "$areasString$regsString"
    }

    private inline fun findTranslator(
        source: Port,
        ea: ULong,
        ss: Int,
        size: Int,
        LorS: AccessAction,
        value: ULong,
        endian: ByteOrder,
    ): Entry? {
        var result: Entry? = null

        for (conn in myBus.translators) {
//            log.finest { "$source request $LorS ea=0x${ea.hex8} ss=0x${ss.hex} translating using ${conn.port}" }

            val found = conn.port.find(
                source,
                ea - conn.offset,
                ss,
                size,
                LorS,
                value,
                conn.endian xor endian,
            )
            if (found != null) {
                if (result != null)
                    throw MemoryAccessError(ULONG_MAX, ea, LorS, "More than one rw in address ${ea.hex8}")
                result = found
            }
        }

        return result
    }

    /**
     * {RU}
     * Метод, который осуществляет поиск примитивов на шине и сквозь все транслирующие порты.
     * Важно! В случае, если для записи или чтения по выбранному адресу было найдено несколько примитивов,
     * которые подходят для этого, то будет брошено исключение. Допустимо, чтобы по адресу было доступно
     * не более одного примитива, который может быть прочитан или записан.
     *
     * @param source Мастер-порт, который инициировал поиск примитивов на шинах
     * @param ea Адрес, по которому должен быть найден примитив для чтения или записи
     * @param ss Значение добавочного адреса (используется в некоторых процессорных архитектурах, например, x86),
     * @param size Количество байт, которые должны быть прочитаны или записаны
     * @param LorS Действие, которое будет произведено с найденным примитивом - чтение или запись
     * @param value Значение для [IFetchReadWrite.beforeWrite]
     * @param endian Порядок байтов
     * @return Найденный примитив в классе [BusCache.Entry] или null, если не было найдено примитива
     * {RU}
     *
     * {EN}
     * Method that search primitives on bus and through all proxy port.
     * Attention! If this  method find more then one available primitive exception will be thrown.
     *
     * @param source master port that initiated the search for primitives on the bus
     * @param ea bus address to find
     * @param ss the value of the additional address (used in some processor architectures, for example, x86), otherwise it is ignored
     * @param size bytes count to be written or read
     * @param LorS Action for found primitive - write or read
     * @param value Value for [IFetchReadWrite.beforeWrite]
     * @param endian Endianness
     * @return found primitive in class [BusCache.Entry] or null if no primitive found
     * {EN}
     */
    internal fun find(
        source: Port,
        ea: ULong,
        ss: Int,
        size: Int,
        LorS: AccessAction,
        value: ULong,
        endian: ByteOrder,
    ): Entry? {
        // It is not error! You MUST call findTranslator and findArea at any time e.g. debugger
        // because we need to trigger beforeRead and beforeWrite methods of areas
        // to make possible handle breakpoints
        val foundTrans = findTranslator(source, ea, ss, size, LorS, value, endian)
        val foundArea = findPrimitive(cachedAreas, source, ea, size, LorS, value, endian)
        if (foundTrans != null) {
            if (foundArea != null)
                throw MemoryAccessError(-1uL, ea, LorS, "More than one primitive in address ${ea.hex8}")
            return foundTrans
        }

        if (foundArea != null) {
//            log.finest { "$source request $LorS 0x${ea.hex8} resolved to area $foundArea" }
            return foundArea
        }

        // Debugger can't read register due to possible side effect when register is reading
        val foundReg = findPrimitive(cachedRegs, source, ea, size, LorS, value, endian)

        if (foundReg != null) {
            // If it is tracer register TRACE_IO then it's ok anyway even for debugger
            if (foundReg.cached.module is ATracer<*>)
                return foundReg

            // Otherwise don't permit debugger to work with register
            if (source.module !is AGenericDebugger)
                return foundReg
        }

        return null
    }

    override fun toString() = "Bus cache for $myBus"
}
