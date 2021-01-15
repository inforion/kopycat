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
//package ru.inforion.lab403.kopycat.cores.x86
//
//import ru.inforion.lab403.common.logging.logger
//import ru.inforion.lab403.common.extensions.*
//import ru.inforion.lab403.kopycat.cores.x86.enums.SSR
//import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
//import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRDW.*
//import ru.inforion.lab403.kopycat.gdbstub.GDB_BPT
//import java.util.logging.Level
//
//class SimisDebugger : x86Debugger() {
//    companion object {
//        @Suppress("unused")
//        @Transient private val log = logger(Level.WARNING)
//
//        private val table = arrayOf(
//                Segment(0xFFEF8FB4, 0x04633, 0x1698),
//                Segment(0x001EC1A4, 0x01257, 0x2108),
//                Segment(0x001ED3FC, 0x003DD, 0x2110),
//                Segment(0xFFFB57B8, 0x029F2, 0x720),
//                Segment(0xFFF0D418, 0x267C6, 0x420),
//                Segment(0x0007158C, 0x006E6, 0x428),  // faulty(?) translation see KC-275
//                Segment(0x000EBD8C, 0x08AE4, 0x44),
//                Segment(0xFFFBC8DC, 0x03642, 0x950),
//
//                Segment(0xFFF621B8, 0x1753A, 0x214),
//                Segment(0xFFF4AB54, 0x16626, 0x154),
//                Segment(0xFFFA116C, 0x053BD, 0xC4),
//                Segment(0xFFFCB024, 0x020AE, 0x114),
//
//                Segment(0xFFFB9A04, 0x00D2A, 0x2C),  // MAY BE = 0
//                Segment(0x000B7284, 0x00016, 0x28C),
//                Segment(0x000B723C, 0x0001A, 0x20C), // faulty translation see KC-275
//                Segment(0x00071C74, 0x00B52, 0x328),
//                Segment(0x000727C8, 0x00034, 0x6F8),
//                Segment(0x001EFBDC, 0x07FFF, 0x8770),
//
//                Segment(0xFFEFD6A4, 0x013C1, 0x16B0),
//
//                Segment(0x001F7FA4, 0x07FFF, 0x87A8),
//
//                Segment(0xFFD99B38, 0xFDB42, 0x2C),    // icontrol:CS [LDT]
//                Segment(0x000E3D8C, 0x07FFF, 0x3C),    // icontrol:SS [LDT]
//                Segment(0x000BC574, 0x25AE2, 0x34),    // icontrol:DS [LDT]
//                Segment(0x000A7188, 0x07FFF, 0x8638),  // icontrol:ES [LDT]
//
//                // empty record -> if not found above
//                Segment(0x00000000, 0xFFFF_FFFF, -1)
//        )
//
//        // Bloody IDA Pro address translation problem quirk fix
//        fun backwardSimisAddressTranslation(vAddr: Long): Long {
//            val ill = table.find { vAddr in it }!!
//            val result = (vAddr - ill.offset) and 0x00FF_FFFF
//            // log.warning { "Backward SIMIS-W address translate: ${vAddr.hex8} -> ${result.hex8} [ss=${ill.ss.hex4}]" }
//            return result
//        }
//
//        fun forwardSimisAddressTranslation(vAddr: Long): Long {
//            val tmp = if (vAddr in 0x00C0_0000..0x0100_0000) vAddr + 0xFF00_0000 else vAddr
//            val ill = table.find { tmp in it }!!
//            val result = tmp + ill.offset
//            // log.severe { "Forward  SIMIS-W address translate: ${vAddr.hex8} -> ${result.hex8} [ss=${ill.ss.hex4}]" }
//            return result
//        }
//
//    }
//
//    override fun readRegister(index: Int): Long {
//        var result = super.readRegister(index)
//        // add quirk for eip
//        if (index == eip.reg) result = backwardSimisAddressTranslation(result)
//        return result
//    }
//
//    override fun writeRegister(index: Int, value: Long) {
//        // NOTE: we need not to translate physical <-> virtual (or another side)
//        // IDA Pro already send virtual address so it suits us if in the same segment
//        // WARNING: Cross segment teleportation not supported
//        val tmp = if (index == eip.reg) forwardSimisAddressTranslation(value) else value
//        super.writeRegister(index, tmp)
//    }
//
//    private data class Segment(val base: Long, val limit: Long, val ss: Int) {
//        val range = base..base + limit
//        val offset = base and 0xF
//        operator fun contains(address: Long): Boolean = address in range
//    }
//
//    override fun readMemoryEx(vAddr: Long, count: Int): ByteArray {
//        // log.severe { "Read memory at %08X".format(vAddr) }
//        return when (vAddr.asInt) {
//            in 0x0FFF_0000..0x0FFF_0014 -> {
//                val ss = when (vAddr.asInt) {
//                // WARNING: This is debugger way to get regs value! Don't use it in emulation
//                // This is not triggered GDT cache invalidation
//                    0x0FFF_0000 -> dev.cpu.sregs.readIntern(SSR.CS.id)
//                    0x0FFF_0004 -> dev.cpu.sregs.readIntern(SSR.SS.id)
//                    0x0FFF_0008 -> dev.cpu.sregs.readIntern(SSR.DS.id)
//                    0x0FFF_000C -> dev.cpu.sregs.readIntern(SSR.ES.id)
//                    0x0FFF_0010 -> dev.cpu.sregs.readIntern(SSR.FS.id)
//                    0x0FFF_0014 -> dev.cpu.sregs.readIntern(SSR.GS.id)
//                    else -> throw RuntimeException("Can't be...")
//                }
//                val desc = dev.mmu.gdt(ss)
//                desc.base.pack(4)
//            }
//            0x0FFF_0018 -> dev.mmu.ldtr.pack(4)
//            0x0FFF_001C -> dev.mmu.gdtr.base.pack(4)
//            0x0FFF_0020 -> dev.cop.idtr.base.pack(4)
//            0x0FFF_0024 -> 0xDEADBEEF.pack(4) // just stub for the mark end
//            else -> {
//                // Fixes of KC-275
//                // We need to read byte by byte segment data due to IDA Pro insanity with segment paragraphs
//                // Such a mess results in segments boundary reading if segment has different offsets
//                // It may lead to debugger performance decrease but we have no other choice...
//                return ByteArray(count).apply {
//                    indices.forEach {
//                        val pAddr = forwardSimisAddressTranslation(vAddr + it)
//                        this[it] = super.readMemoryEx(pAddr, 1)[0]
//                    }
//                }
//            }
//        }
//    }
//
//    override fun writeMemory(vAddr: Long, data: ByteArray) {
//        val pAddr = forwardSimisAddressTranslation(vAddr)
//        super.writeMemory(pAddr, data)
//    }
//
//    override fun setBreakpoint(bpType: GDB_BPT, vAddr: Long, count: Int, comment: String?): Boolean {
//        val pAddr = forwardSimisAddressTranslation(vAddr)
//        // log.fine { "Setup breakpoint at %08X".format(pAddr) }
//        return super.setBreakpoint(bpType, pAddr, count, comment)
//    }
//
//    override fun clearBreakpoint(bpType: GDB_BPT, vAddr: Long, count: Int): Boolean {
//        val pAddr = forwardSimisAddressTranslation(vAddr)
//        // log.fine { "Clear breakpoint at %08X".format(pAddr) }
//        return super.clearBreakpoint(bpType, pAddr, count)
//    }
//}