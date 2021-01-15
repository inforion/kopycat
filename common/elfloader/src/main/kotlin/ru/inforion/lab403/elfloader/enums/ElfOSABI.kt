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
package ru.inforion.lab403.elfloader.enums

import ru.inforion.lab403.common.extensions.*


 
enum class ElfOSABI(val id : Byte) {
    ELFOSABI_NONE(0),               //No extensions or unspecified
    ELFOSABI_HPUX(1),               //Hewlett-Packard HP-UX
    ELFOSABI_NETBSD(2),             //NetBSD
    ELFOSABI_GNU(3),                //GNU
    ELFOSABI_LINUX(3),              //Linux, historical - alias for ELFOSABI_GNU
    ELFOSABI_SOLARIS(6),            //Sun Solaris
    ELFOSABI_AIX(7),                //AIX
    ELFOSABI_IRIX(8),               //IRIX
    ELFOSABI_FREEBSD(9),            //FreeBSD
    ELFOSABI_TRU64(10),             //Compaq TRU64 UNIX
    ELFOSABI_MODESTO(11),           //Novell Modesto
    ELFOSABI_OPENBSD(12),           //Open BSD
    ELFOSABI_OPENVMS(13),           //Open VMS
    ELFOSABI_NSK(14),               //Hewlett-Packard Non-Stop Kernel
    ELFOSABI_AROS(15),              //Amiga Research OS
    ELFOSABI_FENIXOS(16),           //The FenixOS highly scalable multi-core OS
    ELFOSABI_CLOUDABI(17),          //Nuxi CloudABI
    ELFOSABI_OPENVOS(18),           //Stratus Technologies OpenVOS
    ELFOSABI_LOPROC(0x40),          //Processor-specific
    ELFOSABI_HIPROC(0xFF.toByte());

    companion object {
        fun getNameById(id: Byte): String = find<ElfOSABI>{ it.id == id }!!.name.substring(9)
    }
}