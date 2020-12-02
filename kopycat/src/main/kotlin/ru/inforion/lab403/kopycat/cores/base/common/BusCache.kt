/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2020 INFORION, LLC
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

import gnu.trove.map.hash.THashMap
import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.common.extensions.sumByLong
import ru.inforion.lab403.common.extensions.sure
import ru.inforion.lab403.common.logging.INFO
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.AGenericDebugger
import ru.inforion.lab403.kopycat.cores.base.Bus
import ru.inforion.lab403.kopycat.cores.base.MasterPort
import ru.inforion.lab403.kopycat.cores.base.ProxyPort
import ru.inforion.lab403.kopycat.cores.base.abstracts.ATracer
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction.*
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import ru.inforion.lab403.kopycat.interfaces.IFetchReadWrite
import ru.inforion.lab403.kopycat.interfaces.IReadWrite
import ru.inforion.lab403.kopycat.settings
import java.io.Serializable
import java.util.logging.Level

/**
 * {RU}
 * Класс, реализующий методы быстрого поиска примитивов, к которым можно получить доступ через порты, которые прикреплены к текущей шине
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
         * @param source Мастер-порт, который инициировал поиск примитивов на шинах
         * @param ea Смещение, относительно начала примитива, по которому будет произведено чтение или запись [LorS]
         * @param rw Найденный текущий примитив, который будет опрощен
         * @param LorS Действие, которе может быть совершено - чтение [AccessAction.LOAD] или запись [AccessAction.STORE]
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
         * @return true if method [IReadWrite.beforeRead] or [IReadWrite.beforeWrite] return true and allow action (write or read), else false
         * {EN}
         */
        private inline fun <T: IFetchReadWrite>beforeAction(
                source: MasterPort,
                ea: Long,
                rw: T,
                LorS: AccessAction,
                value: Long) = when (LorS) {
            FETCH -> rw.beforeFetch(source, ea)
            LOAD -> rw.beforeRead(source, ea)
            STORE -> rw.beforeWrite(source, ea, value)
        }

        /**
         * {RU}
         * Осуществить копирование областей памяти с кэша шины [src] в кэш шины [dst]
         *
         * @param dst Кэш шины, на которую будет осуществлено копирование областей памяти
         * @param src Кэш шины, с которого будет осуществляться копирование областей памяти
         * {RU}
         *
         * {EN}
         * Copy areas cache from bus [src] to bus cache [dst]
         *
         * @param dst destination bus cache to copy areas
         * @param src source bus cache to copy areas
         * {EN}
         */
        private fun copyAreas(dst: BusCache, src: BusCache) {
            if (src != dst) {
                val copingAreas = src.cachedAreas.filter { it !in dst.cachedAreas }
                dst.cachedAreas.addAll(copingAreas)
            }
        }

        /**
         * {RU}
         * Осуществить копирование регистров с кэша шины [src] в кэш шины [dst]
         *
         * @param dst destination bus cache to copy registers
         * @param src source bus cache to copy registers
         * {RU}
         *
         * {EN}
         * Copy registers cache from bus [src] to bus cache [dst]
         *
         * @param dst destination bus cache to copy registers
         * @param src source bus cache to copy registers
         * {EN}
         */
        private fun copyRegs(dst: BusCache, src: BusCache) {
            if (src != dst) {
                src.cachedRegs.forEach { (ea, srcRegs) ->
                    val dstRegs = dst.cachedRegs.getOrPut(ea) { ArrayList() }
                    val copingRegs = srcRegs.filter { it !in dstRegs }
                    dstRegs.addAll(copingRegs)
                }
            }
        }

        private fun copyTranslators(dst: BusCache, src: BusCache) {
            if (src != dst) {
                val copingTranslators = src.myBus.translators.filter { it !in dst.myBus.translators }
                dst.myBus.translators.addAll(copingTranslators)
            }
        }


        /**
         * {RU}
         * Функция выполняет рекурсивный спуск для заданной шины и кеша примитивов шины
         *
         * @param newCache Заполняемый кеш
         * @param bus Шина, для которой проводится кеширование примитивов
         * @param buses Множество шин, обработанных во время рекурсивного спуска
         * {RU}
         *
         * {EN}
         * This method make recursive calls from this bus to put all primitives from this bus and
         * from proxies bus to this cache
         *
         * @param newCache cache to fill
         * @param bus bus for which primitives are cached
         * @param buses Set of buses processed during recursive descent
         * {EN}
         */
        private fun cacheBusPrimitives(newCache: BusCache, bus: Bus, buses: HashSet<Bus>) {
            if (buses.add(bus)) {
                copyAreas(newCache, bus.cache)
                copyRegs(newCache, bus.cache)
                copyTranslators(newCache, bus.cache)
                bus.proxies.forEach { cacheProxyPortPrimitives(newCache, it.port, buses) }
            }
        }

        /**
         * {RU}
         * Функция выполняет рекурсивный спуск для заданного прокси порта и кеширует примитивы, которые расположены в шинах, присоединенных к этому прокси порту
         *
         * @param newCache Заполняемый кеш
         * @param proxy Прокси порт, подключенный к шине, для которого проводится кеширование примитивов
         * @param buses Обработанное во время рекурсивного спуска множество шин
         * {RU}
         *
         * {EN}
         * This method make recursive calls from selected proxy port and cache all primitives from buses connected to current proxy port
         *
         * @param newCache cache to fill
         * @param proxy proxy port from which to search
         * @param buses Set of buses processed during recursive descent
         * {EN}
         */
        private fun cacheProxyPortPrimitives(newCache: BusCache, proxy: ProxyPort, buses: HashSet<Bus>) {
            val innerBus = proxy.innerBus.sure { "Port $proxy has no inner bus!" }

            cacheBusPrimitives(newCache, innerBus, buses)

            proxy.outerConnections.forEach { cacheBusPrimitives(newCache, it.bus, buses) }
        }
    }

    /**
     * {RU}
     * Класс, который хранит в себе найденный примитив, к которому должно осуществляться обращеие, и смешение, относительно его начала
     *
     * @property rw найденный примитив для чтения или записи
     * @property offset Смещение, относительно начала примитива, по которому будет обращение
     * {RU}
     *
     * {EN}
     * The class that stores the primitive that was found, to which the reference should be made, and the mixing, relative to its beginning
     *
     * @property rw founded primitive for write or read
     * @property offset offset from primitive start
     * {EN}
     */
    class Entry(var rw: IFetchReadWrite?, var offset: Long, var module: Module?): Serializable {
        fun fetch(ss: Int, size: Int): Long = rw!!.fetch(offset, ss, size)
        fun read(ss: Int, size: Int): Long = rw!!.read(offset, ss, size)
        fun write(ss: Int, size: Int, value: Long) = rw!!.write(offset, ss, size, value)

        override fun toString() = "offset=0x${offset.hex8} $rw"
    }

    /**
     * {RU}
     * Класс, который используется для кэширования областей памяти на шинах
     *
     * @property area Сама кэшированная область
     * @property portOffset Смещение порта, к которому прикреплена данная область памяти относительно шины
     * @property startAddress Адрес начала области памяти относительно текущей шины
     * @property endAddress Адрес окончания области памяти относительно текущей шины
     * {RU}
     *
     * {EN}
     * Class for caching areas on buses
     *
     * @property area caching area
     * @property portOffset offset of the port to which the memory is connected to the bus
     * @property startAddress start address of area relative to the current bus
     * @property endAddress  end address of area relative to the current bus
     * {EN}
     */
    private data class BusCachedArea(val area: Module.Area, val portOffset: Long): Serializable {
        val startAddress = area.start + portOffset
        val endAddress = area.end + portOffset

        override fun toString(): String = "${startAddress.hex8}..${endAddress.hex8} -> $area"
    }

    /**
     * {RU}
     * Класс, который используется для кэширования регистров на шинах
     *
     * @property register Сам кэшируемый регистр
     * @property portOffset Смещение порта, к которому прикреплена данная область памяти относительно шины
     * @property address Адрес регистра относительно текущей шины
     * {RU}
     *
     * {EN}
     * Class for caching registers on buses
     *
     * @property register caching register
     * @property portOffset offset of the port to which the memory is connected to the bus
     * @property address register address relative current bus
     * {EN}
     */
    private data class BusCachedRegister(val register: Module.Register, val portOffset: Long): Serializable {
        val address = register.address + portOffset

        override fun toString(): String = "${address.hex8} -> $register"
    }

    private val entry = Entry(null, 0, null)
    private val cachedAreas = ArrayList<BusCachedArea>()
    private val cachedRegs = THashMap<Long, ArrayList<BusCachedRegister>>()

    /**
     * {RU}
     * Этот метод инициализирует кэш для текущей шины.
     * Метод получает все примитивы от Slave-портов, подключенных к шине (так как только Slave-порты могут
     * содержать примитивы), и, при помощи дата-классов BusCachedArea и BusCachedRegister переносит их в кэш для текущей шины.
     * Этот метод является первым этапом в процессе инициализации кэша всех шин.
     * Обратитесь к методу [Module.initializePortsAndBuses] для получения полной информации об инициализаии кэша в шинах.
     * {RU}
     *
     * {EN}
     * This method will initialize cache for current bus.
     * It will get all primitives from all slaves port, which connected to this bus (because only Slave ports
     * can have primitives), and put all primitives to this bus via data classes BusCachedArea and BusCachedRegister.
     * This is first step in buses cache initialization.
     * See method [Module.initializePortsAndBuses] to get full information about buses cache initialization.
     * {EN}
     */
    fun resolveSlaves() {
        cachedAreas.clear()
        cachedRegs.clear()
        myBus.slaves.forEach { conn ->
            conn.port.areas.mapTo(cachedAreas) { BusCachedArea(it, conn.offset) }.sortBy { it.startAddress }
            conn.port.registers
                    .map { BusCachedRegister(it, conn.offset) }
                    .forEach { cachedRegs.getOrPut(it.address) { ArrayList() }.add(it) }

            log.fine {
                val areas = if (cachedAreas.isNotEmpty())
                    cachedAreas.joinToString(prefix = "\n\tAreas:\n\t\t", separator = "\n\t\t") else ""
                val regs = if (cachedRegs.isNotEmpty())
                    cachedRegs.values.joinToString(prefix = "\n\tRegs:\n\t\t", separator = "\n\t\t") else ""
                "Cache update for bus ${this.myBus}:$areas$regs"
            }
        }

    }

    /**
     * {RU}
     * Этот метод разрешает все проки-порты, делая их прозрачными для эмулятора. Это реализовано при помощи
     * копирования всего кэша (который был получен во время первого этапа инициализации) со всех шин, поключенных
     * друг к другу черед прокси-порты. В следствии чего, весь кэш примитивов со всех шин, соединенных прокси-портами,
     * окажется на каждой из этих шин, произойдет, можно сказать, обмен кэшем.
     * Этот метод является вторым этапом в процессе инициализации кэша всех шин.
     * Обратитесь к методу [Module.initializePortsAndBuses] для получения полной информации об инициализаии кэша в шинах.
     * {RU}
     *
     * {EN}
     * This method will resolve all proxy ports - make them transparent for emulator via copying all cache
     * (which inited in first step) from buses, which connected by Proxy-port to this buses, so all
     * cache-primitives will be at each buses connected by Proxy-ports.
     * This is second step in buses cache initialization.
     * See method [Module.initializePortsAndBuses] to get full information about buses cache initialization.
     * {EN}
     */
    fun resolveProxies() {
        cacheBusPrimitives(this, myBus, HashSet())
        cachedAreas.sortBy { it.startAddress }
        cachedRegs.values.forEach { regs -> regs.sortBy { it.address } }
        // фильтр с дебагером связан с тем, что он охватывает все адресное пространство и в сумме оно всегда будет большое
        val totalRamSize = cachedAreas.filter { it.area.port.module !is Debugger }.sumByLong { it.area.size }
        require(!settings.hasConstraints || totalRamSize <= settings.maxPossibleRamSize) {
            "RAM address space too large [${totalRamSize.hex8} <= ${settings.maxPossibleRamSize.hex8}]"
        }
    }

    /**
     * {RU}
     * Строку с представлением карты примитивов для текущей шины
     * Метод, который формирует строковое представление адресной карты примитивов, которые принадлежат текущей шине.
     * Для формирования берутся все примитивы (области памяти и регистры), которые находятся в кэше.
     * {RU}
     *
     * {EN}
     * This method generate string representation of the address map of primitives that connected to the current bus.
     * For the formation, all primitives (memory areas and registers) that are in the cache are taken.
     * @return string with memory map representation
     * {EN}
     */
    fun getPrintableMemoryMap(): String {
        val areas = cachedAreas
                .sortedBy { it.startAddress }
                .joinToString(separator = "\n") { "\t$it" }

        val regs = cachedRegs.values
                .flatten()
                .sortedBy { it.address }
                .joinToString(separator = "\n") { "\t$it" }

        val areasString = if (areas.isNotBlank()) "\nAreas:\n$areas" else ""
        val regsString = if (regs.isNotBlank()) "\nRegisters:\n$regs" else ""

        return "$areasString$regsString"
    }

    private inline fun findTranslator(source: MasterPort, ea: Long, ss: Int, size: Int, LorS: AccessAction, value: Long): Entry? {
        var result: Entry? = null

        for (conn in myBus.translators) {
//            log.finest { "$source request $LorS ea=0x${ea.hex8} ss=0x${ss.hex} translating using ${conn.port}" }
            val found = conn.port.find(source, ea - conn.offset, ss, size, LorS, value)
            if (found != null) {
                if (result != null)
                    throw MemoryAccessError(-1, ea, LorS, "More then one rw in address ${ea.hex8}")
                result = found
            }
        }

        return result
    }

    private inline fun findArea(source: MasterPort, ea: Long, LorS: AccessAction, value: Long): Entry? {
        var result: Entry? = null

        for (k in cachedAreas.indices) {
            val it = cachedAreas[k]
            if (ea <= it.endAddress) {
                if (ea >= it.startAddress) {
                    val relativeAddress = ea - it.portOffset
                    if (beforeAction(source, relativeAddress, it.area, LorS, value)) {
                        check(result == null) {
                            "More then one area in address ${ea.hex8}" +
                                    "\n1'st: ${result!!.rw} offset=${result!!.offset.hex8}" +
                                    "\n2'nd: ${it.area} offset=${relativeAddress.hex8}\n"
                        }
                        with (entry) {
                            rw = it.area
                            offset = relativeAddress
                            module = it.area.module
                        }
                        result = entry
                    }
                } else break
            }
        }

        return result
    }

    private inline fun findReg(source: MasterPort, ea: Long, LorS: AccessAction, value: Long): Entry? {
        val regs = cachedRegs[ea] ?: return null
        var result: Entry? = null

        for (k in regs.indices) {
            val it = regs[k]
            val relativeAddress = ea - it.portOffset
            if (beforeAction(source, relativeAddress, it.register, LorS, value)) {
                if (result != null)
                    throw MemoryAccessError(-1, ea, LorS,
                            "More then one register in address ${ea.hex8}" +
                                    "\n${result.rw} and ${it.register}")
                with(entry) {
                    rw = it.register
                    offset = relativeAddress
                    module = it.register.module
                }
                result = entry
            }
        }

//        log.finest { "$source request $LorS 0x${ea.hex8} resolved to register $result" }
        return result
    }

    /**
     * {RU}
     * Метод, который осуществляет поиск примитивов на шине и сквозь все транслирующие порты.
     * Важно! В случае, если для записи или чтения по выбранному адресу было найдено несколько примитивов,
     * которые подходят для этого, то будет брошено исключение. Допустимо, чтобы по адресу было доступно
     * не более одного примитива, который может быть прочитан или записан.
     *
     * @property source Мастер-порт, который инициировал поиск примитивов на шинах
     * @property ea Адрес, по которому должен быть найден примитив для чтения или записи
     * @property ss Значение добавочного адреса (используется в некоторых процессорных архитектурах, например, x86),
     * @property size Количество байт, которые должны быть прочитаны или записаны
     * @property LorS Действие, которое будет произведено с найденным примитивом - чтение или запись
     * @return Найденный примитив в классе [BusCache.Entry] или null, если не было найдено примитива
     * {RU}
     *
     * {EN}
     * Method that search primitives on bus and through all proxy port.
     * Attention! If this  method find more then one available primitive exception will be thrown.
     *
     * @property source master port that initiated the search for primitives on the bus
     * @property ea bus address to find
     * @property ss the value of the additional address (used in some processor architectures, for example, x86), otherwise it is ignored
     * @property size bytes count to be written or read
     * @property LorS Action for found primitive - write or read
     * @return found primitive in class [BusCache.Entry] or null if no primitive found
     * {EN}
     */
    internal inline fun find(source: MasterPort, ea: Long, ss: Int, size: Int, LorS: AccessAction, value: Long): Entry? {
        // It is not error! You MUST call findTranslator and findArea at any time e.g. debugger
        // because we need to trigger beforeRead and beforeWrite methods of areas
        // to make possible handle breakpoints
        val foundTrans = findTranslator(source, ea, ss, size, LorS, value)
        val foundArea = findArea(source, ea, LorS, value)
        if (foundTrans != null) {
            if (foundArea != null)
                throw MemoryAccessError(-1, ea, LorS, "More then one primitives in address ${ea.hex8}")
            return foundTrans
        }

        if (foundArea != null) {
//            log.finest { "$source request $LorS 0x${ea.hex8} resolved to area $foundArea" }
            return foundArea
        }

        // Debugger can't read register due to possible side effect when register is reading
        val foundReg = findReg(source, ea, LorS, value)

        if (foundReg != null) {
            // If it is tracer register TRACE_IO then it's ok anyway even for debugger
            if (foundReg.module is ATracer<*>)
                return foundReg

            // Otherwise don't permit debugger to work with register
            if (source.module !is AGenericDebugger)
                return foundReg
        }

        return null
    }
}