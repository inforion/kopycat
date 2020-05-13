@file:Suppress("NOTHING_TO_INLINE")

package ru.inforion.lab403.kopycat.cores.arm.hardware.registers

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.proposal.isTypeSubtypeOf
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.interfaces.ICoreUnit
import ru.inforion.lab403.kopycat.interfaces.IResettable
import java.util.logging.Level
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties


abstract class ARegisterBankNG(bits: Int): Iterable<Long>, ICoreUnit {

    companion object {
        val log = logger(Level.INFO)
    }

    val msb = bits - 1
    val lsb = 0
    val mask = bitMask(msb..lsb)

    lateinit var data: LongArray
        private set
    lateinit var registers: List<Register>
        private set

    private fun enumerateRegisters() = this::class.memberProperties
            .filter { it isTypeSubtypeOf Register::class.java }
            .map { field ->
                val register = field.getter.call(this) as Register
                register.also { it.name = field.name }
            }.sortedBy { it.reg }

    fun initialize() {
        registers = enumerateRegisters()
        data = LongArray(count)
        log.finest { "Registers[$count] of ${javaClass.simpleName}: ${registers.joinToString(" ") { it.name }}" }
    }

    inline fun read(reg: Int): Long = data[reg]
    inline fun write(reg: Int, value: Long) { data[reg] = value and mask }

    // Ordinal system
    var count: Int = 0
        private set

    open inner class Register(val defaultValue: Long = 0L) : IResettable {

        // TODO: Don't know how to make it hidden from modification but it definitely must be field of Register class
        lateinit var name: String

        val reg = count++

        open var value by valueOf()

        override fun reset() {
            value = defaultValue
        }

        private inner class valueOf {
            operator fun getValue(thisRef: Register, property: KProperty<*>) = read(reg)
            operator fun setValue(thisRef: Register, property: KProperty<*>, value: Long) = write(reg, value)
        }

        inner class bitOf(val bit: Int) {
            operator fun getValue(thisRef: Register, property: KProperty<*>) = value[bit].toBool()
            operator fun setValue(thisRef: Register, property: KProperty<*>, newValue: Boolean) {
                value = value.insert(newValue.toInt(), bit)
            }
        }

        inner class fieldOf(vararg rp: Pair<IntRange, IntRange>) {
            /**
             * {RU}
             * @property msb верхняя граница диапазона бит
             * @property lsb нижняя граница диапазона бит
             * {RU}
             */
            constructor(msb: Int, lsb: Int): this(msb..lsb to msb-lsb..0)
            val list = rp.asList()

            operator fun getValue(thisRef: Register, property: KProperty<*>): Long {
                var answer = 0L
                val cached = value
                for((src, dst) in list)
                    answer = answer.insert(cached[src], dst)
                return answer
            }
            operator fun setValue(thisRef: Register, property: KProperty<*>, newValue: Long) {
                var answer = value
                for((src, dst) in list)
                    answer = answer.insert(newValue[dst], src)
                value = answer
            }
        }

        init {
            if (::data.isInitialized)
                throw RuntimeException("Put initialize() in init { ... } block of child class!")
        }
    }

    override fun reset() {
        super.reset()
        data.fill(0)
        registers.forEach { it.reset() }
    }

    override operator fun iterator(): Iterator<Long> = object : Iterator<Long> {
        private var pos = 0

        override fun next(): Long {
            if (!hasNext()) {
                throw NoSuchElementException()
            }
            return data[pos++]
        }

        override fun hasNext(): Boolean {
            return pos < count
        }
    }

    override fun serialize(ctxt: GenericSerializer) = registers.map { it.name to read(it.reg).hex }.toMap()

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        registers.forEach { register ->
            val data = snapshot[register.name]
            if (data != null) {
                log.finest { "Loading register ${register.name}[${register.reg}] value = $data" }
                write(register.reg, (data as String).hexAsULong)
            } else log.warning { "Register ${register.name}[${register.reg}] value ng.abstract.not found! " +
                    "Possible your've made snapshot at earlie version -> results may be incorrect!" }
        }
    }
}


