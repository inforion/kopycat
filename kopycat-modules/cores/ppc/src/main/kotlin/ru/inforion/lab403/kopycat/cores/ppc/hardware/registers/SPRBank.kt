package ru.inforion.lab403.kopycat.cores.ppc.hardware.registers

import ru.inforion.lab403.common.extensions.bitMask
import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.extensions.hexAsULong
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.ppc.enums.eSystem
import ru.inforion.lab403.kopycat.interfaces.ICoreUnit
import ru.inforion.lab403.kopycat.modules.cores.PPCCore
import kotlin.collections.Map
import kotlin.collections.contains
import kotlin.collections.forEach
import kotlin.collections.map
import kotlin.collections.mutableMapOf
import kotlin.collections.putAll
import kotlin.collections.set
import kotlin.collections.toMap



class SPRBank(val core: PPCCore, vararg systems: eSystem) : ICoreUnit/*, Iterable<Long>*/ {
    override val name = "SPR registers"

    val msb = 31
    val lsb = 0
    private val mask = bitMask(msb..lsb)
    private val data = mutableMapOf<Int, Long>()
    private val names = mutableMapOf<String, Int>()

    init {
        for (s in systems) {
            data.putAll(s.sprs.map { it.id to 0L })
            names.putAll(s.sprs.map { it.name to it.id })
        }
    }

    fun readIntern(index: Int): Long = data[index] ?: throw GeneralException("Unknown SPR: $index")

    fun writeIntern(index: Int, value: Long) {
        if (index !in data)
            throw GeneralException("Unknown SPR: $index")
        data[index] = value and mask
    }




    //override operator fun iterator() = data.iterator()
    /*object : Iterator<Long> {
        private var pos = 0

        override fun next(): Long {
            if (!hasNext()) {
                throw NoSuchElementException()
            }
            return data[pos++]
        }

        override fun hasNext(): Boolean {
            return pos < data.size
        }
    }*/

    /**
     * {RU}
     * Сохранение состояния (сериализация)
     *
     * @param ctxt контекст объекта-сериализатора
     * @return отображение сохраняемых свойств объекта
     * {RU}
     */
    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return names.map { it.key to data[it.value]!!.hex }.toMap()
    }

    /**
     * {RU}
     * Восстановление состояния (десериализация)
     *
     * @param ctxt контекст объекта-сериализатора
     * @param snapshot отображение восстанавливаемых свойств объекта
     * {RU}
     */
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        names.forEach {
            val value = snapshot[it.key]
            if (value != null) {
                ARegistersBank.log.finest { "Loading register ${it.key}[${it.value}] value = $value" }
                data[it.value] = (value as String).hexAsULong
            } else ARegistersBank.log.warning { "Register ${it.key}[${it.value}] value not found! " +
                    "Possible your've made snapshot at version 1.1.4 or earlie... -> results may be incorrect!" }
        }
    }
}