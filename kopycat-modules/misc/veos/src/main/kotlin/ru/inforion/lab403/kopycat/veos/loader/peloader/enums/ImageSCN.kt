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
package ru.inforion.lab403.kopycat.veos.loader.peloader.enums


 
enum class ImageSCN(val value: Long) {
    /* These defines are for the Characteristics bitfield. */
    /* TYPE_REG(0x00000000), - Reserved */
    /* TYPE_DSECT(0x00000001), - Reserved */
    /* TYPE_NOLOAD(0x00000002), - Reserved */
    /* TYPE_GROUP(0x00000004), - Reserved */
    TYPE_NO_PAD(0x00000008), /* Reserved */
    /* TYPE_COPY(0x00000010), - Reserved */

    CNT_CODE(0x00000020),
    CNT_INITIALIZED_DATA(0x00000040),
    CNT_UNINITIALIZED_DATA(0x00000080),

    LNK_OTHER(0x00000100),
    LNK_INFO(0x00000200),

    /* TYPE_OVER(0x00000400), - Reserved */
    LNK_REMOVE(0x00000800),
    LNK_COMDAT(0x00001000),

    /*                0x00002000 - Reserved */
    /* MEM_PROTECTED (0x00004000), - Obsolete */
    MEM_FARDATA(0x00008000),

    /* MEM_SYSHEAP(0x00010000), - Obsolete */
    MEM_PURGEABLE(0x00020000),
    MEM_16BIT(0x00020000),
    MEM_LOCKED(0x00040000),
    MEM_PRELOAD(0x00080000),

    ALIGN_1BYTES(0x00100000),
    ALIGN_2BYTES(0x00200000),
    ALIGN_4BYTES(0x00300000),
    ALIGN_8BYTES(0x00400000),
    ALIGN_16BYTES(0x00500000),  /* Default */
    ALIGN_32BYTES(0x00600000),
    ALIGN_64BYTES(0x00700000),
    ALIGN_128BYTES(0x00800000),
    ALIGN_256BYTES(0x00900000),
    ALIGN_512BYTES(0x00A00000),
    ALIGN_1024BYTES(0x00B00000),
    ALIGN_2048BYTES(0x00C00000),
    ALIGN_4096BYTES(0x00D00000),
    ALIGN_8192BYTES(0x00E00000),

    /*                0x00F00000 - Unused */
    ALIGN_MASK(0x00F00000),

    LNK_NRELOC_OVFL(0x01000000),


    MEM_DISCARDABLE(0x02000000),
    MEM_NOT_CACHED(0x04000000),
    MEM_NOT_PAGED(0x08000000),
    MEM_SHARED(0x10000000),
    MEM_EXECUTE(0x20000000),
    MEM_READ(0x40000000),
    MEM_WRITE(0x80000000)

}