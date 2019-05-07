package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.support

import ru.inforion.lab403.common.extensions.clr
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.set

class Mask private constructor(val pandm: Long, val porm: Long, val mandm: Long, val morm: Long) {
    companion object {
        private fun preprocess(raw: String): String = raw.filter { it.isDigit() || it == 'x' }.reversed()

        private fun m0(raw: String, offset: Int): Long =
                preprocess(raw).foldIndexed(0x0000_0000L) { k, r, c ->
                    if (c == '1') r set (k + offset) else r }

        private fun m1(raw: String, offset: Int): Long =
                preprocess(raw).foldIndexed(0xFFFF_FFFFL) { k, r, c ->
                    if (c == '0') r clr (k + offset) else r }

        private fun getPosPart(raw: String): String {
            val missingDelimiterValue = "?"
            val result = raw.substringBefore("not", missingDelimiterValue)
            return if (result != missingDelimiterValue) result.trim() else raw.trim()
        }

        private fun getNegPart(raw: String): String {
            val missingDelimiterValue = "?"
            val result = raw.substringAfter("not", missingDelimiterValue)
            return if (result != missingDelimiterValue) result.trim() else ""
        }

        private fun getRaw(andm: Long, orm: Long): String {
            return (31 downTo 0).joinToString("") { k ->
                val b1 = andm[k]
                val b2 = orm[k]
                if (b1 == b2) "$b1" else "x"
            }
        }

        fun create(raw: String, offset: Int): Mask {
            val pos = getPosPart(raw)
            val neg = getNegPart(raw)

            var m0p = 0x0000_0000L
            var m1p = 0xFFFF_FFFFL
            if (!pos.isBlank() && pos != "-" && pos.any { it != 'x' }) {
                m0p = m0(pos, offset)
                m1p = m1(pos, offset)
            }

            var m0n = 0xFFFF_FFFFL
            var m1n = 0x0000_0000L

            // neg.all { it == 'x' } = true for empty collections
            if (neg.isNotBlank()) {
                // throw out mask like "not -" or "not xxxxx"
                if (neg == "-" || neg.all { it == 'x' })
                    throw IllegalArgumentException("Wrong negative mask: $neg")

                m0n = m0(neg, offset)
                m1n = m1(neg, offset)
            }

            return Mask(m0p, m1p, m0n, m1n)
        }

        private fun makeOffsets(bits: Array<Any>): List<Int> {
            return bits.map {
                when (it) {
                    is Int -> it
                    is IntRange -> it.last
                    else -> throw IllegalArgumentException("Wrong bits argument (Int and IntRange): $it!")
                }
            }
        }

        fun fromPattern(pattern: String, bits: Array<Any>): Mask {
            val offsets = makeOffsets(bits)
            val masks = pattern
                    .split(',')
                    .zip(offsets)
                    .map { (raw, offset) -> if (raw.isBlank() || raw == "-")
                        Mask.any else Mask.create(raw, offset)
                    }

            return masks.fold(any) { acc, elm -> acc + elm }
        }

        val any = Mask()
    }

    private val me = "${getRaw(pandm, porm)}|${getRaw(mandm, morm)}"

    private constructor() : this(
            0x0000_0000,
            0xFFFF_FFFF,
            0xFFFF_FFFF,
            0x0000_0000)

    fun suit(value: Long): Boolean =
            ((value and pandm == pandm) && (value or porm == porm)) &&
            ((value and mandm == mandm) && (value or morm == morm)).not()

    fun isNegEmpty(): Boolean = mandm == 0xFFFF_FFFFL && morm == 0x0000_0000L
    fun isPosEmpty(): Boolean = pandm == 0x0000_0000L && porm == 0xFFFF_FFFFL
    fun isEmpty(): Boolean = isPosEmpty() && isNegEmpty()

    operator fun plus(other: Mask): Mask {
        val nMandm: Long
        val nMorm: Long

        val nPandm = pandm or other.pandm
        val nPorm = porm and other.porm
        if (isNegEmpty() || other.isNegEmpty()) {
            nMandm = mandm and other.mandm
            nMorm = morm or other.morm
        } else {
            nMandm = mandm or other.mandm
            nMorm = morm and other.morm
        }
        return Mask(nPandm, nPorm, nMandm, nMorm)
    }

    override fun toString(): String = "[$me]"
}