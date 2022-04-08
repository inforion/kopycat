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
@file:Suppress("NOTHING_TO_INLINE")

package ru.inforion.lab403.kopycat.cores.x86.enums

enum class x86GPR(val index: Int, val n8: String, val n16: String, val n32: String, val n64: String) {
    RAX(0, "al", "ax", "eax", "rax"),
    RCX(1, "cl", "cx", "ecx", "rcx"),
    RDX(2, "dl", "dx", "edx", "rdx"),
    RBX(3, "bl", "bx", "ebx", "rbx"),
    RSP(4, "spl", "sp", "esp", "rsp"),
    RBP(5, "bpl", "bp", "ebp", "rbp"),
    RSI(6, "sil", "si", "esi", "rsi"),
    RDI(7, "dil", "di", "edi", "rdi"),

    R8 (8,  "r8b", "r8w", "r8d", "r8"),
    R9 (9,  "r9b", "r9w", "r9d", "r9"),
    R10(10, "r10b", "r10w", "r10d", "r10"),
    R11(11, "r11b", "r11w", "r11d", "r11"),
    R12(12, "r12b", "r12w", "r12d", "r12"),
    R13(13, "r13b", "r13w", "r13d", "r13"),
    R14(14, "r14b", "r14w", "r14d", "r14"),
    R15(15, "r15b", "r15w", "r15d", "r15"),

    RIP(16, "??", "ip", "eip", "rip"),

    NONE(17, "noneB", "noneW", "noneD", "noneR");

    companion object {
        private val index2GPR = enumValues<x86GPR>()
            .sortedBy { it.index }
            .toTypedArray()

        fun byIndex(index: Int) = index2GPR[index]
    }
}