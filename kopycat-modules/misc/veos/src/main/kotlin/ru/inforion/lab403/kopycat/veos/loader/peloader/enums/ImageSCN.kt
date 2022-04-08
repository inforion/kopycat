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
package ru.inforion.lab403.kopycat.veos.loader.peloader.enums


 
enum class ImageSCN(val value: ULong) {
    /* These defines are for the Characteristics bitfield. */
    /* TYPE_REG(0x00000000), - Reserved */
    /* TYPE_DSECT(0x00000001), - Reserved */
    /* TYPE_NOLOAD(0x00000002), - Reserved */
    /* TYPE_GROUP(0x00000004), - Reserved */
    TYPE_NO_PAD(0x00000008u), /* Reserved */
    /* TYPE_COPY(0x00000010), - Reserved */

    CNT_CODE(0x00000020u),
    CNT_INITIALIZED_DATA(0x00000040u),
    CNT_UNINITIALIZED_DATA(0x00000080u),

    LNK_OTHER(0x00000100u),
    LNK_INFO(0x00000200u),

    /* TYPE_OVER(0x00000400), - Reserved */
    LNK_REMOVE(0x00000800u),
    LNK_COMDAT(0x00001000u),

    /*                0x00002000 - Reserved */
    /* MEM_PROTECTED (0x00004000), - Obsolete */
    MEM_FARDATA(0x00008000u),

    /* MEM_SYSHEAP(0x00010000), - Obsolete */
    MEM_PURGEABLE(0x00020000u),
    MEM_16BIT(0x00020000u),
    MEM_LOCKED(0x00040000u),
    MEM_PRELOAD(0x00080000u),

    ALIGN_1BYTES(0x00100000u),
    ALIGN_2BYTES(0x00200000u),
    ALIGN_4BYTES(0x00300000u),
    ALIGN_8BYTES(0x00400000u),
    ALIGN_16BYTES(0x00500000u),  /* Default */
    ALIGN_32BYTES(0x00600000u),
    ALIGN_64BYTES(0x00700000u),
    ALIGN_128BYTES(0x00800000u),
    ALIGN_256BYTES(0x00900000u),
    ALIGN_512BYTES(0x00A00000u),
    ALIGN_1024BYTES(0x00B00000u),
    ALIGN_2048BYTES(0x00C00000u),
    ALIGN_4096BYTES(0x00D00000u),
    ALIGN_8192BYTES(0x00E00000u),

    /*                0x00F00000 - Unused */
    ALIGN_MASK(0x00F00000u),

    LNK_NRELOC_OVFL(0x01000000u),


    MEM_DISCARDABLE(0x02000000u),
    MEM_NOT_CACHED(0x04000000u),
    MEM_NOT_PAGED(0x08000000u),
    MEM_SHARED(0x10000000u),
    MEM_EXECUTE(0x20000000u),
    MEM_READ(0x40000000u),
    MEM_WRITE(0x80000000u)

}