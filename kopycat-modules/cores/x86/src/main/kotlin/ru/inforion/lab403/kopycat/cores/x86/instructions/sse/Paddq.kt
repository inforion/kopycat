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
package ru.inforion.lab403.kopycat.cores.x86.instructions.sse

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import java.math.BigInteger

class Paddq(core: x86Core, opcode: ByteArray, prefs: Prefixes, val t: Datatype, vararg operands: AOperand<x86Core>) :
    ASSEInstruction(core, opcode, prefs, *operands) {

    override val mnem = when (t) {
        Datatype.BYTE -> "paddb"
        Datatype.WORD -> "paddw"
        Datatype.DWORD -> "paddd"
        else -> "paddq"
    }

    override fun executeSSEInstruction() {
        when (op1.dtyp) {
            Datatype.MMXWORD -> {
                // MMX
                val a1 = op1.value(core)
                val a2 = op2.value(core)

                val result = when (t) {
                    Datatype.BYTE -> {
                        /*
                          DEST[7:0] := DEST[7:0] + SRC[7:0];
                          (* Repeat add operation for 2nd through 7th byte *)
                          DEST[63:56] := DEST[63:56] + SRC[63:56];
                        */

                        var result = 0uL
                        (0 until op2.dtyp.bytes).forEach {
                            val lsb = it * 8
                            val msb = (it + 1) * 8 - 1
                            val range = msb..lsb
                            result = result.insert(a1[range] + a2[range], range)
                        }

                        result
                    }
                    Datatype.WORD -> {
                        /*
                          DEST[15:0] := DEST[15:0] + SRC[15:0];
                          (* Repeat add operation for 2nd and 3th word *)
                          DEST[63:48] := DEST[63:48] + SRC[63:48];
                        */

                        var result = 0uL
                        (0 until op2.dtyp.bytes / 2).forEach {
                            val lsb = it * 16
                            val msb = (it + 1) * 16 - 1
                            val range = msb..lsb
                            result = result.insert(a1[range] + a2[range], range)
                        }

                        result
                    }
                    Datatype.DWORD -> {
                        /*
                          DEST[31:0] := DEST[31:0] + SRC[31:0];
                          DEST[63:32] := DEST[63:32] + SRC[63:32];
                        */
                        0uL
                            .insert(a1[31..0] + a2[31..0], 31..0)
                            .insert(a1[63..32] + a2[63..32], 63..32)
                    }
                    else -> {
                        // DEST[63:0] := DEST[63:0] + SRC[63:0];
                        a1 + a2
                    }
                }

                op1.value(core, result)
            }
            else -> {
                // SSE2
                val a1 = op1.extValue(core)
                val a2 = op2.extValue(core)

                val result = when (t) {
                    Datatype.BYTE -> {
                        /*
                          DEST[7:0] := DEST[7:0] + SRC[7:0];
                          (* Repeat add operation for 2nd through 15th byte *)
                          DEST[127:120] := DEST[127:120] + SRC[127:120];
                        */

                        var result = BigInteger.ZERO
                        (0 until op2.dtyp.bytes).forEach {
                            val lsb = it * 8
                            val msb = (it + 1) * 8 - 1
                            val range = msb..lsb
                            result = result.insert(a1[range] + a2[range], range)
                        }

                        result
                    }
                    Datatype.WORD -> {
                        /*
                          DEST[15:0] := DEST[15:0] + SRC[15:0];
                          (* Repeat add operation for 2nd through 7th word *)
                          DEST[127:112] := DEST[127:112] + SRC[127:112];
                          DEST[MAXVL-1:128] (Unmodified)
                        */

                        var result = BigInteger.ZERO
                        (0 until op2.dtyp.bytes / 2).forEach {
                            val lsb = it * 16
                            val msb = (it + 1) * 16 - 1
                            val range = msb..lsb
                            result = result.insert(a1[range] + a2[range], range)
                        }

                        result
                    }
                    Datatype.DWORD -> {
                        /*
                          DEST[31:0] := DEST[31:0] + SRC[31:0];
                          (* Repeat add operation for 2nd and 3th doubleword *)
                          DEST[127:96] := DEST[127:96] + SRC[127:96];
                        */
                        BigInteger.ZERO
                            .insert(a1[31..0] + a2[31..0], 31..0)
                            .insert(a1[63..32] + a2[63..32], 63..32)
                            .insert(a1[95..64] + a2[95..64], 95..64)
                            .insert(a1[127..96] + a2[127..96], 127..96)
                    }
                    else -> {
                        /*
                          DEST[63:0] := DEST[63:0] + SRC[63:0];
                          DEST[127:64] := DEST[127:64] + SRC[127:64];
                        */
                        BigInteger.ZERO
                            .insert(a1[63..0] + a2[63..0], 63..0)
                            .insert(a1[127..64] + a2[127..64], 127..64)
                    }
                }

                op1.extValue(core, result)
            }
        }
    }
}
