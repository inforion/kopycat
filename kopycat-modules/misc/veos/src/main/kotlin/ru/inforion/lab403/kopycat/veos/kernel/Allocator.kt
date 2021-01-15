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
package ru.inforion.lab403.kopycat.veos.kernel

import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.common.extensions.sure
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import ru.inforion.lab403.kopycat.interfaces.IConstructorSerializable

class Allocator(
        val sys: System,
        // vars because of serializer
        // TODO: solve this problem
        var startAddr: Long = 0,
        var endAddr: Long = 0x0FFFL,
        var alignment: Int = 4
): IAutoSerializable {

    constructor(orig: Allocator) : this(orig.sys, orig.startAddr, orig.endAddr, orig.alignment) {
        currentFreePos = orig.currentFreePos
        usedBlocks.clear()
        usedBlocks.addAll(orig.usedBlocks)
        freedBlocks.clear()
        freedBlocks.addAll(orig.freedBlocks)
    }

    data class MBlock(var addr: Long, var size: Int): IAutoSerializable, IConstructorSerializable

    private var currentFreePos = startAddr
    private val usedBlocks = ArrayList<MBlock>()
    private val freedBlocks = ArrayList<MBlock>()

    fun getAlignedSize(size: Int) = (size / alignment + 1) * alignment

    private fun getAddrFromUnused(sizeAligned: Int): Long {
        val addr = currentFreePos
        return if (addr + sizeAligned < endAddr) {
            currentFreePos += sizeAligned
            addr
        } else -1
    }

    val size = endAddr - startAddr + 1

    /**
     * Never allocated memory
     */
    val sizeOfUnused get() = endAddr - currentFreePos + 1

    /**
     * All available non-allocated memory
     */
    val sizeOfAvailable get() = sizeOfUnused + freedBlocks.sumBy { it.size }

    /**
     * All available freed memory
     */
    val sizeOfFreed get() = freedBlocks.sumBy { it.size }

    /**
     * Allocated size
     */
    val sizeOfUsed get() = usedBlocks.sumBy { it.size }

    private fun getAddrFromFreed(sizeAligned: Int): Long {
        val block = freedBlocks.filter { it.size >= sizeAligned }.minBy { it.size }!!
        val addr = block.addr
        if (block.size == sizeAligned)
            freedBlocks.remove(block)
        else {
            block.addr += sizeAligned
            block.size -= sizeAligned
        }
        return addr
    }

    fun allocate(size: Int): Long{
        val sizeAligned = getAlignedSize(size)
        val newAddr = if (freedBlocks.find { it.size >= sizeAligned  } != null)
            getAddrFromFreed(sizeAligned)
        else
            getAddrFromUnused(sizeAligned)
        if (newAddr >= 0) {
            usedBlocks.add(MBlock(newAddr, sizeAligned))
            usedBlocks.sortBy { it.addr }
        }

        return newAddr
    }

    fun blockSize(address: Long) = usedBlocks
            .find { it.addr == address }?.size
            .sure { "Invalid pointer or double free at 0x${sys.abi.programCounterValue.hex8} called from 0x${sys.abi.returnAddressValue.hex8}" }

    val breakAddress get() = usedBlocks.maxOf { it.addr + it.size }

    fun byte() = allocate(sys.sizeOf.char)
    fun half() = allocate(sys.sizeOf.short)
    fun word() = allocate(sys.sizeOf.int)
    fun pointer() = allocate(sys.sizeOf.pointer)
    fun long() = allocate(sys.sizeOf.longLong)

    fun free(addr: Long): Boolean{
        val block = usedBlocks.find { it.addr == addr }
        if (block != null) {
            usedBlocks.remove(block)

            val neighborLeft = freedBlocks.firstOrNull { it.addr + it.size == block.addr }
            if (neighborLeft != null){
                block.addr = neighborLeft.addr
                block.size += neighborLeft.size
                freedBlocks.remove(neighborLeft)
            }

            val neighborRight = freedBlocks.firstOrNull { it.addr == block.addr + block.size }
            if (neighborRight != null){
                block.size += neighborRight.size
                freedBlocks.remove(neighborRight)
            }

            freedBlocks.add(block)
            freedBlocks.sortBy { it.addr }

            return true
        }
        return false
    }

    fun reset(){
        currentFreePos = startAddr
        freedBlocks.clear()
        usedBlocks.clear()
    }
}