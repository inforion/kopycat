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
@file:Suppress("MemberVisibilityCanBePrivate")

package ru.inforion.lab403.kopycat.cores.x86.hardware.registers

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBankNG
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR.*
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class GPRBank: ARegistersBankNG<x86Core>("CPU General Purpose Registers", 75, 64) {

    // GPRQW : Register - is to solve recursive problem of .value call
    inner class GPRQW(name: String, index: Int, id: Int, default: ULong = 0u) : Register(name, id, default, QWORD, index)

    open inner class GPRDW(name: String, val index: Int, id: Int, default: ULong = 0u)
        : Register(name, id, default, DWORD, index) {
        override var value: ULong
            get() = read(index) like dtype
            set(value) = write(index, value like dtype)
    }

    open inner class GPRW(name: String, val index: Int, id: Int, default: ULong = 0u)
        : Register(name, id, default, WORD, index) {
        override var value: ULong
            get() = read(index) like dtype
            set(value) = write(index, read(index).insert(value like dtype, dtype.msb..dtype.lsb) )
    }

//    inner class GPRDW(name: String, index: Int, id: Int) : GPR(name, index, id, )
//    inner class GPRW(name: String, index: Int, id: Int, default: ULong = 0u) : GPR(name, index, id, Datatype.WORD, default)

    inner class GPRB(name: String, val index: Int, id: Int, val isHigh: Boolean) : Register(name, id, dtype = BYTE, extra = index) {
        val range = if (isHigh) 15..8 else 7..0

        override var value: ULong
            get() = read(index)[range]
            set(value) = write(index, read(index).insert(value, range))
    }

    fun gpr(reg: x86GPR, dtype: Datatype, alt: Boolean = false) = when (dtype) {
        BYTE -> when (reg) {
            RAX -> al
            RCX -> cl
            RDX -> dl
            RBX -> bl
            RSP, RBP, RSI, RDI -> if (alt) when (reg) {
                RSP -> spl
                RBP -> bpl
                RSI -> sil
                RDI -> dil
                else -> error("Unknown register id: $reg (alt=$alt)")
            } else when (reg) {
                RSP -> ah
                RBP -> ch
                RSI -> dh
                RDI -> bh
                else -> error("Unknown register id: $reg (alt=$alt)")
            }
            R8 -> r8b
            R9 -> r9b
            R10 -> r10b
            R11 -> r11b
            R12 -> r12b
            R13 -> r13b
            R14 -> r14b
            R15 -> r15b
            else -> error("Unknown register id: $reg")
        }
        WORD -> when (reg) {
            RAX -> ax
            RCX -> cx
            RDX -> dx
            RBX -> bx
            RSP -> sp
            RBP -> bp
            RSI -> si
            RDI -> di
            R8 -> r8w
            R9 -> r9w
            R10 -> r10w
            R11 -> r11w
            R12 -> r12w
            R13 -> r13w
            R14 -> r14w
            R15 -> r15w
            RIP -> ip
            NONE -> none16
        }
        DWORD -> when (reg) {
            RAX -> eax
            RCX -> ecx
            RDX -> edx
            RBX -> ebx
            RSP -> esp
            RBP -> ebp
            RSI -> esi
            RDI -> edi
            R8 -> r8d
            R9 -> r9d
            R10 -> r10d
            R11 -> r11d
            R12 -> r12d
            R13 -> r13d
            R14 -> r14d
            R15 -> r15d
            RIP -> eip
            NONE -> none32
        }
        QWORD -> when (reg) {
            RAX -> rax
            RCX -> rcx
            RDX -> rdx
            RBX -> rbx
            RSP -> rsp
            RBP -> rbp
            RSI -> rsi
            RDI -> rdi
            R8 -> r8
            R9 -> r9
            R10 -> r10
            R11 -> r11
            R12 -> r12
            R13 -> r13
            R14 -> r14
            R15 -> r15
            RIP -> rip
            NONE -> none64
        }
        else -> error("Unknown register id: $reg")
    }

    val rax  = GPRQW(RAX.n64, RAX.index, 0)
    val rcx  = GPRQW(RCX.n64, RCX.index, 1)
    val rdx  = GPRQW(RDX.n64, RDX.index, 2)
    val rbx  = GPRQW(RBX.n64, RBX.index, 3)
    val rsp  = GPRQW(RSP.n64, RSP.index, 4)
    val rbp  = GPRQW(RBP.n64, RBP.index, 5)
    val rsi  = GPRQW(RSI.n64, RSI.index, 6)
    val rdi  = GPRQW(RDI.n64, RDI.index, 7)
    val r8  = GPRQW(R8.n64, R8.index, 8)
    val r9  = GPRQW(R9.n64, R9.index, 9)
    val r10  = GPRQW(R10.n64, R10.index, 10)
    val r11  = GPRQW(R11.n64, R11.index, 11)
    val r12  = GPRQW(R12.n64, R12.index, 12)
    val r13  = GPRQW(R13.n64, R13.index, 13)
    val r14  = GPRQW(R14.n64, R14.index, 14)
    val r15  = GPRQW(R15.n64, R15.index, 15)
    val rip  = GPRQW(RIP.n64, RIP.index, 16, 0xFFF0u)

    val eax  = GPRDW(RAX.n32, RAX.index, 18)
    val ecx  = GPRDW(RCX.n32, RCX.index, 19)
    val edx  = GPRDW(RDX.n32, RDX.index, 20)
    val ebx  = GPRDW(RBX.n32, RBX.index, 21)
    val esp  = GPRDW(RSP.n32, RSP.index, 22)
    val ebp  = GPRDW(RBP.n32, RBP.index, 23)
    val esi  = GPRDW(RSI.n32, RSI.index, 24)
    val edi  = GPRDW(RDI.n32, RDI.index, 25)
    val r8d  = GPRDW(R8.n32, R8.index, 26)
    val r9d  = GPRDW(R9.n32, R9.index, 27)
    val r10d  = GPRDW(R10.n32, R10.index, 28)
    val r11d  = GPRDW(R11.n32, R11.index, 29)
    val r12d  = GPRDW(R12.n32, R12.index, 30)
    val r13d  = GPRDW(R13.n32, R13.index, 31)
    val r14d  = GPRDW(R14.n32, R14.index, 32)
    val r15d  = GPRDW(R15.n32, R15.index, 33)
    val eip  = GPRDW(RIP.n32, RIP.index, 34)

    val ax  = GPRW(RAX.n16, RAX.index, 35)
    val cx  = GPRW(RCX.n16, RCX.index, 36)
    val dx  = GPRW(RDX.n16, RDX.index, 37)
    val bx  = GPRW(RBX.n16, RBX.index, 38)
    val sp  = GPRW(RSP.n16, RSP.index, 39)
    val bp  = GPRW(RBP.n16, RBP.index, 40)
    val si  = GPRW(RSI.n16, RSI.index, 41)
    val di  = GPRW(RDI.n16, RDI.index, 42)
    val r8w  = GPRW(R8.n16, R8.index, 43)
    val r9w  = GPRW(R9.n16, R9.index, 44)
    val r10w = GPRW(R10.n16, R10.index, 45)
    val r11w = GPRW(R11.n16, R11.index, 46)
    val r12w = GPRW(R12.n16, R12.index, 47)
    val r13w = GPRW(R13.n16, R13.index, 48)
    val r14w = GPRW(R14.n16, R14.index, 49)
    val r15w = GPRW(R15.n16, R15.index, 50)
    val ip = GPRW(RIP.n16, RIP.index, 51)

    val al  = GPRB(RAX.n8, RAX.index, 52, false)
    val cl  = GPRB(RCX.n8, RCX.index, 53, false)
    val dl  = GPRB(RDX.n8, RDX.index, 54, false)
    val bl  = GPRB(RBX.n8, RBX.index, 55, false)

    val ah  = GPRB("ah", RAX.index, 56, true)
    val ch  = GPRB("ch", RCX.index, 57, true)
    val dh  = GPRB("dh", RDX.index, 58, true)
    val bh  = GPRB("bh", RBX.index, 59, true)

    val spl  = GPRB(RSP.n8, RSP.index, 60, false)
    val bpl  = GPRB(RBP.n8, RBP.index, 61, false)
    val sil  = GPRB(RSI.n8, RSI.index, 62, false)
    val dil  = GPRB(RDI.n8, RDI.index, 63, false)

    val r8b  = GPRB(R8.n8, R8.index, 64, false)
    val r9b  = GPRB(R9.n8, R9.index, 65, false)
    val r10b  = GPRB(R10.n8,  R10.index, 66, false)
    val r11b  = GPRB(R11.n8,  R11.index, 67, false)
    val r12b  = GPRB(R12.n8,  R12.index, 68, false)
    val r13b  = GPRB(R13.n8,  R13.index, 69, false)
    val r14b  = GPRB(R14.n8,  R14.index, 70, false)
    val r15b  = GPRB(R15.n8,  R15.index, 71, false)

    val none16  = Register("none16", 72, dtype = WORD, extra = NONE.index)
    val none32  = Register("none32", 73, dtype = DWORD, extra = NONE.index)
    val none64  = Register("none64", 74, dtype = QWORD, extra = NONE.index)
}