/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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
package ru.inforion.lab403.kopycat.cores.mips.hardware.registers

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBankNG
import ru.inforion.lab403.kopycat.cores.mips.Microarchitecture
import ru.inforion.lab403.kopycat.modules.cores.MipsCore
import java.lang.Integer.reverse
import java.nio.ByteOrder
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class HWRBank(val core: MipsCore) : ARegistersBankNG<MipsCore>("Hardware MIPS Registers", 65536, 64) {

    inner class ReadOnly(
            name: String,
            id: Int,
            val output: (ReadOnly) -> ULong
    ) : Register(name, id) {
        override var value: ULong
            get() = output(this)
            set(value) = throw NotImplementedError("Write to $this not implemented!")
    }

    /**
     * Number of the CPU on which the program is currently running. This register
     * provides read access to the coprocessor 0 EBaseCPUNum field.
     */
    val hw0 = ReadOnly("hw0", 0) { core.cop.regs.EBase.value[9..0] }

    /**
     * Address step size to be used with the SYNCI instruction,
     * or zero if no caches need be synchronized.
     * See that instruction’s description for the use of this value.
     */
    val hw1 = ReadOnly("hw1", 1) { 0u }

    /**
     * High-resolution cycle counter. This register provides read access to the coprocessor 0 Count Register.
     */
    val hw2 = ReadOnly("hw2", 2) { core.cop.regs.Count.value }

    /**
     * 1 CC register increments every CPU cycle
     * 2 CC register increments every second CPU cycle
     * 3 CC register increments every third CPU cycle
     */
    val hw3 = ReadOnly("hw3", 3) { 1u }

    /**
     * User Local Register. This register provides read access to the coprocessor 0
     * UserLocal register, if it is implemented. In some operating environments, the
     * UserLocal register is a pointer to a thread-specific storage block.
     */
    val hw29 = ReadOnly("hw29", 29) { core.cop.regs.UserLocal.value }

    val cvmCount = if (core.microarchitecture == Microarchitecture.cnMips) {
        ReadOnly("CvmCount", 31) { core.cop.regs.Count.value }
    } else {
        null
    }

    val cvmHashIVw = if (core.microarchitecture == Microarchitecture.cnMips) {
        Array(8) {
            Register("CVM_MT_HSH_IVW$it", 0x0250 + it)
        }
    } else {
        null
    }

    val cvmHashDatW = if (core.microarchitecture == Microarchitecture.cnMips) {
        Array(15) {
            Register("CVM_MT_HSH_DATW$it", 0x0240 + it)
        }
    } else {
        null
    }

    val cvmAesResInp = if (core.microarchitecture == Microarchitecture.cnMips) {
        Array(2) {
            Register("CVM_MT_AES_RESINP$it", 0x0100 + it)
        }
    } else {
        null
    }

    val cvmAesIV = if (core.microarchitecture == Microarchitecture.cnMips) {
        Array(2) {
            Register("CVM_MT_AES_IV$it", 0x0102 + it)
        }
    } else {
        null
    }

    val cvmAesKey = if (core.microarchitecture == Microarchitecture.cnMips) {
        Array(4) {
            Register("CVM_MT_AES_KEY$it", 0x0104 + it)
        }
    } else {
        null
    }

    val cvmAesKeyLen = if (core.microarchitecture == Microarchitecture.cnMips) {
        Register("CVM_MT_AES_KEYLENGTH", 0x0110)
    } else {
        null
    }

    val cvmTripleDesKey = if (core.microarchitecture == Microarchitecture.cnMips) {
        Array(3) {
            Register("CVM_MF_3DES_KEY$it", 0x0080 + it)
        }
    } else {
        null
    }

    val cvmTripleDesIV = if (core.microarchitecture == Microarchitecture.cnMips) {
        Register("CVM_MT_3DES_IV", 0x0084)
    } else {
        null
    }

    val cvmTripleDesResult = if (core.microarchitecture == Microarchitecture.cnMips) {
        Register("CVM_MF_3DES_RESULT", 0x0088)
    } else {
        null
    }

    val cvmTripleDesEncCBC = if (core.microarchitecture == Microarchitecture.cnMips) {
        // Logic is not implemented
        Register("CVM_MT_3DES_ENC_CBC", 0x4088)
    } else {
        null
    }

    val cvmTripleDesDecCBC = if (core.microarchitecture == Microarchitecture.cnMips) {
        // Logic is not implemented
        Register("CVM_MT_3DES_DEC_CBC", 0x408C)
    } else {
        null
    }

    val cvmCrcPoly = if (core.microarchitecture == Microarchitecture.cnMips) {
        Register("CVM_MT_CRC_POLYNOMIAL", 0x4200)
    } else {
        null
    }

    val cvmCrcIV = if (core.microarchitecture == Microarchitecture.cnMips) {
        Register("CVM_MT_CRC_IV", 0x0201)
    } else {
        null
    }

    init {
        if (core.microarchitecture == Microarchitecture.cnMips) {
            (0 until 4).forEach {
                object : Register("CVM_MT_HSH_IV$it", 0x0048 + it) {
                    override var value: ULong
                        get() = super.value
                        set(value) {
                            cvmHashIVw!![it * 2].value = value[63..32]
                            cvmHashIVw[it * 2 + 1].value = value[31..0]
                        }
                }
            }

            (0 until 7).forEach {
                object : Register("CVM_MT_HSH_DAT$it", 0x0040 + it) {
                    override var value: ULong
                        get() = super.value
                        set(value) {
                            cvmHashDatW!![it * 2].value = value[63..32]
                            cvmHashDatW[it * 2 + 1].value = value[31..0]
                        }
                }
            }

            fun<T: Iterable<ULong>> T.u64ToByteArray() = this.map {
                it.pack(8, ByteOrder.BIG_ENDIAN).toList()
            }.flatten().toByteArray()

            fun List<ULong>.u64ToByteArray() = this.asIterable().u64ToByteArray()
            fun Array<ULong>.u64ToByteArray() = this.asIterable().u64ToByteArray()

            // Untested
            object : Register("CVM_MT_HSH_STARTSHA256", 0x404F) {
                override var value: ULong
                    get() = super.value
                    set(value) {
                        // TODO: IV
                        // val tiv = Array(4) {
                        //     (cvmHashIVw!![2 * it].value[31..0] shl 31) or (cvmHashIVw[2 * it + 1].value[31..0])
                        // }.u64ToByteArray

                        val digest = (
                            Array(7) {
                                (cvmHashDatW!![2 * it].value[31..0] shl 31) or (cvmHashDatW[2 * it + 1].value[31..0])
                            } + arrayOf(value)
                        ).u64ToByteArray().sha256().chunks(8).filter(ByteArray::isNotEmpty).map {
                            it.getUInt64(0, ByteOrder.BIG_ENDIAN)
                        }

                        (0 until 4).forEach {
                            cvmHashIVw!![2 * it].value = digest[it][63..32]
                            cvmHashIVw[2 * it + 1].value = digest[it][31..0]
                        }

                        super.value = 0uL
                    }
            }

            object : Register("CVM_MT_AES_ENC_CBC0", 0x0108) {
                override var value: ULong
                    get() = super.value
                    set(value) {
                        cvmAesResInp!![0].value = value xor cvmAesIV!![0].value // plaintext ^ IV
                        super.value = 0uL
                    }
            }

            fun aes(input: ByteArray) = Cipher.getInstance("AES/CBC/NoPadding").run {
                init(
                    Cipher.ENCRYPT_MODE,
                    SecretKeySpec(
                        when (cvmAesKeyLen!!.value) {
                            1uL -> cvmAesKey!![0 until 2] // 128 bit
                            2uL -> cvmAesKey!![0 until 3] // 192 bit
                            3uL -> cvmAesKey!! // 256 bit
                            else -> return null // invalid
                        }.map { it.value }.u64ToByteArray(),
                        "AES",
                    ),
                    IvParameterSpec(ByteArray(16) { 0 }),
                )
                doFinal(input)
            }

            // Untested
            object : Register("CVM_MT_AES_ENC_CBC1", 0x3109) {
                override var value: ULong
                    get() = super.value
                    set(value) {
                        val ciphertext = aes(
                            byteArrayOf(
                                // plaintext ^ IV from CVM_MT_AES_ENC_CBC0
                                *(cvmAesResInp!![0].value).pack(8, ByteOrder.BIG_ENDIAN),
                                // plaintext ^ IV
                                *(value xor cvmAesIV!![1].value).pack(8, ByteOrder.BIG_ENDIAN),
                            )
                        )
                        cvmAesResInp[0].value = ciphertext.sliceArray(0..7)
                            .getUInt64(0, ByteOrder.BIG_ENDIAN)
                        cvmAesResInp[1].value = ciphertext.sliceArray(8..15)
                            .getUInt64(0, ByteOrder.BIG_ENDIAN)
                        cvmAesIV[0].value = cvmAesResInp[0].value
                        cvmAesIV[1].value = cvmAesResInp[1].value
                        super.value = 0uL
                    }
            }

            object : Register("CVM_MT_AES_ENC0", 0x010A) {
                override var value: ULong
                    get() = super.value
                    set(value) {
                        cvmAesResInp!![0].value = value
                        super.value = 0uL
                    }
            }

            // Untested
            object : Register("CVM_MT_AES_ENC1", 0x310B) {
                override var value: ULong
                    get() = super.value
                    set(value) {
                        val ciphertext = aes(
                            byteArrayOf(
                                *(cvmAesResInp!![0].value).pack(8, ByteOrder.BIG_ENDIAN),
                                *value.pack(8, ByteOrder.BIG_ENDIAN),
                            )
                        )
                        cvmAesResInp[0].value = ciphertext.sliceArray(0..7)
                            .getUInt64(0, ByteOrder.BIG_ENDIAN)
                        cvmAesResInp[1].value = ciphertext.sliceArray(8..15)
                            .getUInt64(0, ByteOrder.BIG_ENDIAN)
                        super.value = 0uL
                    }
            }

            fun UInt.reverse() = reverse(int).uint
            // Tested; poorly optimized
            fun crc32(
                data: ByteArray,
                poly: UInt = 0x04C11DB7u,
                initial: UInt = 0u,
                reflectIn: Boolean = true,
                reflectOut: Boolean = true,
            ) = data.asSequence()
                .flatMap {  b -> sequence(8) { i -> b[i].uint } }
                .fold(
                    initial.run {
                        if (reflectIn) {
                            reverse()
                        } else {
                            this
                        }
                    }.inv()
                ) { crc, set ->
                    if (crc[31] != set) {
                        (crc shl 1) xor poly
                    } else {
                        crc shl 1
                    }
                }.run {
                    if (reflectOut) {
                        reverse()
                    } else {
                        this
                    }
                }
                .inv()

            // Untested
            object : Register("CVM_MT_CRC_DWORD", 0x1207) {
                override var value: ULong
                    get() = super.value
                    set(value) {
                        cvmCrcIV!!.value = crc32(
                            value.pack(8, ByteOrder.BIG_ENDIAN),
                            poly = cvmCrcPoly!!.value.uint,
                            initial = cvmCrcIV.value.uint,
                            reflectIn = false,
                            reflectOut = false,
                        ).ulong_z
                        super.value = 0uL
                    }
            }
        }
    }
}
