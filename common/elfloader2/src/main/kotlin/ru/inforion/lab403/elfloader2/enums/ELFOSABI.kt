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
package ru.inforion.lab403.elfloader2.enums


enum class ELFOSABI(val low: UByte, val high: UByte = low) {
    ELFOSABI_NONE(0u),               //No extensions or unspecified
    ELFOSABI_HPUX(1u),               //Hewlett-Packard HP-UX
    ELFOSABI_NETBSD(2u),             //NetBSD
    ELFOSABI_GNU(3u),                //GNU
    ELFOSABI_LINUX(3u),              //Linux, historical - alias for ELFOSABI_GNU
    ELFOSABI_SOLARIS(6u),            //Sun Solaris
    ELFOSABI_AIX(7u),                //AIX
    ELFOSABI_IRIX(8u),               //IRIX
    ELFOSABI_FREEBSD(9u),            //FreeBSD
    ELFOSABI_TRU64(10u),             //Compaq TRU64 UNIX
    ELFOSABI_MODESTO(11u),           //Novell Modesto
    ELFOSABI_OPENBSD(12u),           //Open BSD
    ELFOSABI_OPENVMS(13u),           //Open VMS
    ELFOSABI_NSK(14u),               //Hewlett-Packard Non-Stop Kernel
    ELFOSABI_AROS(15u),              //Amiga Research OS
    ELFOSABI_FENIXOS(16u),           //The FenixOS highly scalable multi-core OS
    ELFOSABI_CLOUDABI(17u),          //Nuxi CloudABI
    ELFOSABI_OPENVOS(18u),           //Stratus Technologies OpenVOS
    ELFOSABI_PROC(0x40u, 0xFFu); //Processor-specific

    val range = low..high
    val shortName get() = name.removePrefix("ELFOSABI_")

    companion object {
        fun cast(id: UByte, onFail: (UByte) -> ELFOSABI?) = values().find { id in it.range} ?: onFail(id)
    }
}