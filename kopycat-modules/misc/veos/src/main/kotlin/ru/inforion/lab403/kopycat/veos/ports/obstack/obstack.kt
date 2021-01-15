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
package ru.inforion.lab403.kopycat.veos.ports.obstack

import ru.inforion.lab403.kopycat.veos.api.pointers.BytePointer
import ru.inforion.lab403.kopycat.veos.api.pointers.StructPointer
import ru.inforion.lab403.kopycat.veos.kernel.System

class obstack(sys: System, address: Long) : StructPointer(sys, address) {
    // type: long
    /* 00 */ var chunkSize by int(0x00) /* preferred size to allocate chunks in */
    /* 04 */ private var chunkPointer by pointer(0x04) /* address of current struct obstack_chunk */
    /* 08 */ private var objectBasePointer by pointer(0x08) /* address of object we are building */
    /* 0C */ var nextFree by pointer(0x0C) /* where to add next char to current object */
    /* 10 */ var chunkLimit by pointer(0x10) /* address of char after current chunk */
    /* 14 */ var temp by pointer(0x14) /* Temporary for some macros.  */
    /* 18 */ var alignmentMask by int(0x18)  /* Mask of alignment for each object. */
    /* These prototypes vary based on 'use_extra_arg', and we use
       casts to the prototypeless function type in all assignments,
       but having prototypes here quiets -Wstrict-prototypes.  */
    /* 1C */ var chunkfun by pointer(0x1C)
    /* 20 */ var freefun by pointer(0x20)
    /* 24 */ var extraArg by pointer(0x24)    /* first arg for chunk alloc/dealloc funcs */
    /* 28 */ private var bitField by int(0x28)

    var useExtraArg by bit(::bitField, 0)       /*  chunk alloc/dealloc funcs take extra arg */
    var maybeEmptyObject by bit(::bitField, 1)  /*  There is a possibility that the current
                                                                chunk contains a zero-length object.  This
                                                                prevents freeing the chunk if we allocate
                                                                a bigger chunk to replace it. */
    var allocFailed by bit(::bitField, 2)       /*  No longer used, as we now call the failed
                                                                handler on error, but retained for binary
                                                                compatibility.  */

    var chunk: _obstack_chunk
        get() = _obstack_chunk(sys, chunkPointer)
        set(value) { chunkPointer = value.address }

    var objectBase: BytePointer
        get() = BytePointer(sys, objectBasePointer)
        set(value) { objectBasePointer = value.address }
}
