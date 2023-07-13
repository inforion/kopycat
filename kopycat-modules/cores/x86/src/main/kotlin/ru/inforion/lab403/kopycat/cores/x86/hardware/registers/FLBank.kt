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
package ru.inforion.lab403.kopycat.cores.x86.hardware.registers

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBankNG
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.cores.x86.config.Generation
import ru.inforion.lab403.kopycat.cores.x86.enums.Flags
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class FLBank(val core: x86Core) : ARegistersBankNG<x86Core>("Flags Register", 3, 32) {

    open inner class EFlagsRegister(name: String, id: Int) : Register(name, id) {

        var cf by bitOf(0, track = core)
        var pf by bitOf(2, track = core)
        var af by bitOf(4, track = core)
        var zf by bitOf(6, track = core)
        var sf by bitOf(7, track = core)
        var tf by bitOf(8)

        /** Interrupt enable flag */
        var ifq
            get() = value[9].truth
            set(value) {
                if (value) {
                    core.cop.intShadow = 2
                }
                this@EFlagsRegister.value = this@EFlagsRegister.value.insert(value.ulong, 9)
            }

        var df by bitOf(10)
        var of by bitOf(11, track = core)
        var iopl by fieldOf(13..12)
        var nt by bitOf(14)

        var rf by bitOf(16)
        var vm by bitOf(17)
        var ac by bitOf(18)
        var vif by bitOf(19)
        var vip by bitOf(20)
        var idq by bitOf(21)

        protected fun flagProcess(core: x86Core, data: ULong): ULong {
            var tmp = data
//            if (core.generation == Generation.Atom) {
//                tmp = tmp.clr(Flags.RF.bit)
//                    .insert(vm, Flags.VM.bit)
//                    .insert(vip, Flags.VIP.bit)
//                    .insert(vif, Flags.VIF.bit)
//            }
//            else {
                if (core.generation < Generation.i286)
                    tmp = tmp clr Flags.IOPLH.bit..Flags.IOPLL.bit
                if (core.generation < Generation.i386) {
                    tmp = tmp clr Flags.RF.bit
                    tmp = tmp clr Flags.VM.bit
                }
                if (core.generation < Generation.i486)
                    tmp = tmp clr Flags.AC.bit
                if (core.generation < Generation.Pentium) {
                    //                TODO: Solve this mess flags!
                    //                tmp = tmp clr VIF.bit
                    //                tmp = tmp clr VIP.bit
                    tmp = tmp clr Flags.ID.bit
                }
//            }
            return tmp
        }

        override var value: ULong
            get() = flagProcess(core, read(0) like dtype)
            set(value) { write(0, flagProcess(core, value) like dtype) }
    }



    inner class FlagsRegister(name: String, id: Int) : EFlagsRegister(name, id) {

        private val highBits get() = read(0) and inv(ubitMask64(32))

        override var value: ULong
            get() = flagProcess(core, read(0) like Datatype.WORD)
            set(value) { write(0, flagProcess(core, highBits or (value like Datatype.WORD)) ) }

    }

//
//    open inner class XFlagsRegister(name: String, id: Int, dtype: Datatype) : Register(name, id, dtype = dtype) {
//
//        private fun flagProcess(core: x86Core, data: ULong): ULong {
//            var tmp = data
//            if (core.generation == Generation.Atom) {
//                tmp = tmp.clr(Flags.RF.bit)
//                    .insert(vm, Flags.VM.bit)
//                    .insert(vip, Flags.VIP.bit)
//                    .insert(vif, Flags.VIF.bit)
//            }
//            else {
//                if (core.generation < Generation.i286)
//                    tmp = tmp clr Flags.IOPLH.bit..Flags.IOPLL.bit
//                if (core.generation < Generation.i386) {
//                    tmp = tmp clr Flags.RF.bit
//                    tmp = tmp clr Flags.VM.bit
//                }
//                if (core.generation < Generation.i486)
//                    tmp = tmp clr Flags.AC.bit
//                if (core.generation < Generation.Pentium) {
//    //                TODO: Solve this mess flags!
//    //                tmp = tmp clr VIF.bit
//    //                tmp = tmp clr VIP.bit
//                    tmp = tmp clr Flags.ID.bit
//                }
//            }
//            return tmp
//        }
//
//        override var value: ULong
//            get() = flagProcess(core, read(0) like dtype)
//            set(value) { write(0, flagProcess(core, value) like dtype) }
//    }
//
//    inner class XEFlagsRegister(name: String, id: Int, dtyp: Datatype) : XFlagsRegister(name, id, dtyp) {
//
//        var cf by bitOf(0)
//        var pf by bitOf(2)
//        var af by bitOf(4)
//        var zf by bitOf(6)
//        var sf by bitOf(7)
//        var tf by bitOf(8)
//        var ifq by bitOf(9)
//        var df by bitOf(10)
//        var of by bitOf(11)
//        var iopl by fieldOf(13..12)
//        var nt by bitOf(14)
//        var rf by bitOf(16)
//        var vm by bitOf(17)
//        var ac by bitOf(18)
//        var vif by bitOf(19)
//        var vip by bitOf(20)
//        var idq by bitOf(21)
//    }
//
    private val flagsRegister = Register("flagsRegister", 0)

    val rflags get() = eflags
    val eflags = EFlagsRegister("eflags", 1)//XEFlagsRegister("eflags", 2, Datatype.DWORD)
    val flags = FlagsRegister("flags", 2) //XFlagsRegister("flags", 1, Datatype.WORD)
    // 3.4.3.4 RFLAGS Register in 64-Bit Mode
    // In 64-bit mode, EFLAGS is extended to 64 bits and called RFLAGS. The upper 32 bits of RFLAGS register is
    // reserved. The lower 32 bits of RFLAGS is the same as EFLAGS.
    // So use eflags instead

    fun flags(dtype: Datatype) = when(dtype) {
        Datatype.WORD -> flags
        Datatype.DWORD, Datatype.QWORD -> eflags
        else -> throw GeneralException("Unknown datatype: $dtype")
    }

    var cf      get() = eflags.cf  ; set(value) { eflags.cf   = value }
    var pf      get() = eflags.pf  ; set(value) { eflags.pf   = value }
    var af      get() = eflags.af  ; set(value) { eflags.af   = value }
    var zf      get() = eflags.zf  ; set(value) { eflags.zf   = value }
    var sf      get() = eflags.sf  ; set(value) { eflags.sf   = value }
    var tf      get() = eflags.tf  ; set(value) { eflags.tf   = value }
    var ifq     get() = eflags.ifq ; set(value) { eflags.ifq  = value }
    var df      get() = eflags.df  ; set(value) { eflags.df   = value }
    var of      get() = eflags.of  ; set(value) { eflags.of   = value }
    var iopl    get() = eflags.iopl; set(value) { eflags.iopl = value }
    var nt      get() = eflags.nt  ; set(value) { eflags.nt   = value }
    var rf      get() = eflags.rf  ; set(value) { eflags.rf   = value }
    var vm      get() = eflags.vm  ; set(value) { eflags.vm   = value }
    var ac      get() = eflags.ac  ; set(value) { eflags.ac   = value }
    var vif     get() = eflags.vif ; set(value) { eflags.vif  = value }
    var vip     get() = eflags.vip ; set(value) { eflags.vip  = value }
    var idq     get() = eflags.idq ; set(value) { eflags.idq  = value }

}