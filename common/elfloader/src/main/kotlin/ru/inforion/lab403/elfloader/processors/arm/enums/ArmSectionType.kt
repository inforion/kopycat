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
package ru.inforion.lab403.elfloader.processors.arm.enums


 
enum class ArmSectionType(val id: Int) {
    SHT_ARM_EXIDX           (0x70000001),   //Exception Index table
    SHT_ARM_PREEMPTMAP      (0x70000002),   //BPABI DLL dynamic linking pre-emption map
    SHT_ARM_ATTRIBUTES      (0x70000003),   //Object file compatibility attributes
    SHT_ARM_DEBUGOVERLAY    (0x70000004),   //See DBGOVL for details
    SHT_ARM_OVERLAYSECTION  (0x70000005);

}