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
package ru.inforion.lab403.kopycat.veos.loader.peloader.structs

import ru.inforion.lab403.kopycat.veos.loader.peloader.enums.ImageSCN


 
class PECharacteristic(val data: Long) {
    val typeNoPad get() = (data and ImageSCN.TYPE_NO_PAD.value != 0L)
    val cntCode get() = (data and ImageSCN.CNT_CODE.value != 0L)
    val cntInitializedData get() = (data and ImageSCN.CNT_INITIALIZED_DATA.value != 0L)
    val cntUninitializedData get() = (data and ImageSCN.CNT_UNINITIALIZED_DATA.value != 0L)
    val lnkOther get() = (data and ImageSCN.LNK_OTHER.value != 0L)
    val lnkInfo get() = (data and ImageSCN.LNK_INFO.value != 0L)
    val lnkRemove get() = (data and ImageSCN.LNK_REMOVE.value != 0L)
    val lnkComdat get() = (data and ImageSCN.LNK_COMDAT.value != 0L)
    val memFardata get() = (data and ImageSCN.MEM_FARDATA.value != 0L)
    val memPurgable get() = (data and ImageSCN.MEM_PURGEABLE.value != 0L)
    val mem16bit get() = (data and ImageSCN.MEM_16BIT.value != 0L)
    val memLocked get() = (data and ImageSCN.MEM_LOCKED.value != 0L)
    val memPreload get() = (data and ImageSCN.MEM_PRELOAD.value != 0L)
    val align1bytes get() = (data and ImageSCN.ALIGN_1BYTES.value != 0L)
    val align8bytes get() = (data and ImageSCN.ALIGN_8BYTES.value != 0L)
    val align4bytes get() = (data and ImageSCN.ALIGN_4BYTES.value != 0L)
    val align2bytes get() = (data and ImageSCN.ALIGN_2BYTES.value != 0L)
    val align16bytes get() = (data and ImageSCN.ALIGN_16BYTES.value != 0L)
    val align32bytes get() = (data and ImageSCN.ALIGN_32BYTES.value != 0L)
    val align64bytes get() = (data and ImageSCN.ALIGN_64BYTES.value != 0L)
    val align128bytes get() = (data and ImageSCN.ALIGN_128BYTES.value != 0L)
    val align256bytes get() = (data and ImageSCN.ALIGN_256BYTES.value != 0L)
    val align1024bytes get() = (data and ImageSCN.ALIGN_1024BYTES.value != 0L)
    val align2048bytes get() = (data and ImageSCN.ALIGN_2048BYTES.value != 0L)
    val align4096bytes get() = (data and ImageSCN.ALIGN_4096BYTES.value != 0L)
    val align8192bytes get() = (data and ImageSCN.ALIGN_8192BYTES.value != 0L)
//    val typeNoPad get() = (data and ImageSCN.ALIGN_MASK.value != 0L)
    val lnkRelocOVFL get() = (data and ImageSCN.LNK_NRELOC_OVFL.value != 0L)
    val memDiscardable get() = (data and ImageSCN.MEM_DISCARDABLE.value != 0L)
    val memNotCached get() = (data and ImageSCN.MEM_NOT_CACHED.value != 0L)
    val memNotPaged get() = (data and ImageSCN.MEM_NOT_PAGED.value != 0L)
    val memShared get() = (data and ImageSCN.MEM_SHARED.value != 0L)
    val memExecute get() = (data and ImageSCN.MEM_EXECUTE.value != 0L)
    val memRead get() = (data and ImageSCN.MEM_READ.value != 0L)
    val memWrite get() = (data and ImageSCN.MEM_WRITE.value != 0L)
}