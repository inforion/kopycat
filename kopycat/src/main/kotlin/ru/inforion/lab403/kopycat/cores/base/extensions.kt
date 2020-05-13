@file:Suppress("NOTHING_TO_INLINE")

package ru.inforion.lab403.kopycat.cores.base

import ru.inforion.lab403.common.extensions.mask
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype



inline infix fun Long.like(dtyp: Datatype): Long = this mask dtyp.bits
inline infix fun Int.like(dtyp: Datatype): Int = this mask dtyp.bits
inline infix fun Short.like(dtyp: Datatype): Short = this mask dtyp.bits
inline infix fun Byte.like(dtyp: Datatype): Byte = this mask dtyp.bits