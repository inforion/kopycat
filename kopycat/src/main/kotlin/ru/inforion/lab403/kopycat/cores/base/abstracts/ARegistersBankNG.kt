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
package ru.inforion.lab403.kopycat.cores.base.abstracts

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.INFO
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.utils.BitOf
import ru.inforion.lab403.kopycat.cores.base.abstracts.utils.FieldOf
import ru.inforion.lab403.kopycat.cores.base.abstracts.utils.TrackableBitOf
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.ARegisterNG
import ru.inforion.lab403.kopycat.interfaces.ICoreUnit
import ru.inforion.lab403.kopycat.interfaces.IResettable
import ru.inforion.lab403.kopycat.interfaces.ISerializable
import ru.inforion.lab403.kopycat.serializer.loadValue
import ru.inforion.lab403.kopycat.serializer.storeValues


abstract class ARegistersBankNG<T: AGenericCore>(
        final override val name: String,
        @Deprecated("What is the reason to use count instead of #register.size?")
        val count: Int,
        @Deprecated("What is the reason to use bits instead of #register.dtype.bits?")
        val bits: Int,
        val registersInRow: Int = 4 // for stringify configuration
): Iterable<ARegistersBankNG<T>.Register>, ICoreUnit {

    companion object {
        @Transient val log = logger(INFO)
    }

    val msb = bits - 1
    val lsb = 0

    protected val mask = ubitMask64(msb..lsb)

    private val registers = Array<Register?>(count) { null }

    private val defined get() = registers.filterNotNull()

    operator fun get(index: Int) = registers[index].sure { "Register with index $index not found!" }

    fun read(index: Int) = this[index].value

    fun write(index: Int, value: ULong) {
        this[index].value = value
    }

    open inner class Register(
        val name: String,
        val id: Int,
        var default: ULong = 0u,
        val dtype: Datatype = Datatype.DWORD,
        val extra: Int = 0
    ) : ISerializable, IResettable {
        protected val bank = this@ARegistersBankNG

        /**
         * {EN} Actual data of register {EN}
         */
        private var data: ULong = 0u

        /**
         * {EN}
         * Defines register behaviour and should be overridden only in case when register may have
         *   several values depend on state of processor or coprocessor or other hardware states
         *
         * WARNING: DO NOT use a bit-access like [bitOf] or [fieldOf] when override this field! It leads to recursion!
         *   Use [data] for this purpose.
         *
         * NOTE: If you override this field you should also override:
         *  - serialize()/deserialize()
         *  - reset()
         *
         * Otherwise, result may be unexpected (see ARMv6M GPRBank.SP).
         * {EN}
         */
        open var value: ULong
            get() = data and mask
            set(value) = run { data = value and mask }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ARegistersBankNG<*>.Register) return false

            if (id != other.id) return false

            return true
        }

        override fun hashCode() = id.hashCode()

        override fun reset() = run { data = default and mask }

        override fun serialize(ctxt: GenericSerializer) = storeValues("data" to data)

        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            log.finest { "Loading register $this value = $data" }
            data = loadValue(snapshot, "data")
        }

        fun bitOf(bit: Int, track: T? = null) = when (track) {
            null -> BitOf(this, bit)
            else -> TrackableBitOf(this, bit, track)
        }

        fun fieldOf(vararg list: Pair<IntRange, IntRange>) = FieldOf(this, *list)

        fun fieldOf(bits: IntRange) = FieldOf(this, bits to bits.first - bits.last..0)

        override fun toString() = "$name[$id]"

        fun toOperand() = object : ARegisterNG<T>(this@Register, Access.ANY, dtype) {
            override fun toString() = name
            override fun value(core: T) = value
            override fun value(core: T, data: ULong) = run { value = data }
        }

        val next get() = bank[id + 1] // TODO("Filter not null")
        val prev get() = bank[id - 1] // TODO("Filter not null")

        init {
            val duplicate = this@ARegistersBankNG.find { it.name == name || it.id == id }
            check(duplicate == null) { "CPU register $this has duplicated id or name with ${duplicate}!" }
            registers[id] = this
        }
    }

    public fun Register.hex() = "%0${dtype.bits / 4}X".format(value.long)

    override fun reset() {
        super.reset()
        forEach { it.reset() }
    }

    override operator fun iterator() = defined.iterator()

    override fun serialize(ctxt: GenericSerializer) = associate { it.name to it.serialize(ctxt) }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) = forEach {
        val info = snapshot[it.name]

        if (info == null) {
            log.warning { "Register $it not found in snapshot and it will be reset..." }
            it.reset()
            return@forEach
        }

        @Suppress("UNCHECKED_CAST")
        it.deserialize(ctxt, info as Map<String, Any>)
    }

    /**
     * {EN}
     * Formats registers as follow:
     *     ```
     *     0    8    16    24
     *     1    9    17    25
     *     2   10    18    26
     *     3   11    19    27
     *     4   12    20    28
     *     5   13    21    29
     *     6   14    22    30
     *     7   15    23    31
     *     ```
     * {EN}
     */
    override fun stringify() = buildString {
        appendLine("$name:")
        val cols = registersInRow
        val rows = count ceil cols

        repeat(cols * rows) { ord ->
            val row = ord / cols
            val col = ord % cols

            val idx = col * rows + row

            // in case when row * col != count
            // 0    9    18    27
            // 1   10    19    28
            // 2   11    20    29
            // 3   12    21    30
            // 4   13    22    31
            // 5   14    23    32
            // 6   15    24    33
            // 7   16    25
            // 8   17    26
            // or
            // 0    7    14    21     28
            // 1    8    15    22     29
            // 2    9    16    23     30
            // 3   10    17    24     31
            // 4   11    18    25
            // 5   12    19    26
            // 6   13    20    27
            if (idx < count) {
                if (row != 0 && col == 0) appendLine()

                val reg = registers[idx]

                val name: String
                val value: String

                val id = "%3d".format(idx)

                if (reg != null) {
                    name = "[$id] ${reg.name}"
                    value = "0x${reg.hex()}"
                } else {
                    name = "[$id] UNK"
                    value = "UNDEF"
                }

                val nm = name.stretch(15)
                val vl = value.stretch(18)

                append("$nm = $vl".stretch(15 + 18 + 5))
            }
        }
    }
}


