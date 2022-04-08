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
package ru.inforion.lab403.kopycat.cores.x86.hardware.processors

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACPU
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.x86.IA32_SMBASE
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.hardware.registers.*
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.x86SystemDecoder
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.control.Nop
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import ru.inforion.lab403.kopycat.modules.BUS32
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class x86CPU(val x86: x86Core, name: String, busSize: ULong = BUS32):
    ACPU<x86CPU, x86Core, AX86Instruction, x86GPR>(x86, name, busSize), IAutoSerializable {

    companion object {
        const val LME = 8 // Long Mode Enable

        const val SMM_ENTRY_OFFSET = 0x8000uL
    }

    enum class Mode { R16, R32, R64 }

    // Cached CS.D value
    var csd = false
    // Cached CS.L value
    var csl = false

    // Operand and Address size mode! Not real or protected!
    // Real or protected mode defined by cregs.vpe
    val mode: Mode get() = when {
        csl && cregs.cr4.pae && x86.config.efer[LME].truth && cregs.cr0.pg -> Mode.R64
        csd && cregs.cr0.pe -> Mode.R32
        else -> Mode.R16
    }

    override fun reg(index: Int): ULong = regs[index].value
    override fun reg(index: Int, value: ULong) = run { regs[index].value = value }
    override fun count() = regs.count()
    override fun flags() = flags.eflags.value

    /**
     * Stub for debugging purpose of x86CPU core
     * Read - returns physical CPU.PC address
     * Write - setup only offset of virtual address within cs segment
     */
    override var pc: ULong
        get() = regs.rip.value
        set(value) = run { regs.rip.value = value }

    val regs = GPRBank()
    val flags = FLBank(x86) // TODO: split to flags and eflags
    val sregs = SSRBank(x86)
    val dregs = DBGBank()
    val cregs = CTRLBank(x86)

    private fun saveStateBeforeSmm64(smBase: ULong) = with (x86) {
        // Table 34-3. SMRAM State Save Map for Intel 64 Architecture

        fun save(offset: ULong, size: Int, value: ULong) =
            write(smBase + offset + SMM_ENTRY_OFFSET, UNDEF, size, value)

        save(0x7FF8u, 8, cpu.cregs.cr0.value)
        save(0x7FF0u, 8, cpu.cregs.cr3.value)

        save(0x7FE8u, 8, cpu.flags.eflags.value)  // rflags

        save(0x7FE0u, 8, x86.config.efer)

        save(0x7FD8u, 8, cpu.regs.rip.value)

        save(0x7FD0u, 8, cpu.dregs.dr6.value)
        save(0x7FC8u, 8, cpu.dregs.dr7.value)

        save(0x7FC4u, 4, x86.cop.tssr)  // TR_SEL
        save(0x7FC0u, 4, x86.mmu.ldtr)  // LDTR_SEL

        save(0x7FBCu, 4, cpu.sregs.gs.value)
        save(0x7FB8u, 4, cpu.sregs.fs.value)
        save(0x7FB4u, 4, cpu.sregs.ds.value)
        save(0x7FB0u, 4, cpu.sregs.ss.value)
        save(0x7FACu, 4, cpu.sregs.cs.value)
        save(0x7FA8u, 4, cpu.sregs.es.value)

        save(0x7FA4u, 4, 0x0u)  // IO_MISC
        save(0x7F9Cu, 8, 0x0u)  // IO_MEM_ADDR

        save(0x7F94u, 8, cpu.regs.rdi.value)
        save(0x7F8Cu, 8, cpu.regs.rsi.value)
        save(0x7F84u, 8, cpu.regs.rbp.value)
        save(0x7F7Cu, 8, cpu.regs.rsp.value)
        save(0x7F74u, 8, cpu.regs.rbx.value)
        save(0x7F6Cu, 8, cpu.regs.rdx.value)
        save(0x7F64u, 8, cpu.regs.rcx.value)
        save(0x7F5Cu, 8, cpu.regs.rax.value)
        save(0x7F54u, 8, cpu.regs.r8.value)
        save(0x7F4Cu, 8, cpu.regs.r9.value)
        save(0x7F44u, 8, cpu.regs.r10.value)
        save(0x7F3Cu, 8, cpu.regs.r11.value)
        save(0x7F34u, 8, cpu.regs.r12.value)
        save(0x7F2Cu, 8, cpu.regs.r13.value)
        save(0x7F24u, 8, cpu.regs.r14.value)
        save(0x7F1Cu, 8, cpu.regs.r15.value)

        // 7F1BH-7F04H Reserved
                
        save(0x7F02u, 2, 0x0u) // Auto HALT Restart Field (Word)
        save(0x7F00u, 2, 0x0u) // I/O Instruction Restart Field (Word)
        // SMM Revision Identifier Field (Doubleword)
        //  I/O instruction restart = 0
        //  SMRAM base address relocation is supported  = 1
        save(0x7EFCu, 4, 0x2_0001u)

        save(0x7EF8u, 4, smBase) //  SMBASE Field (Doubleword)

        save(0x7EE0u, 4, 0x0u) // Setting of “enable EPT” VM-execution control
        save(0x7ED8u, 4, 0x0u) // Value of EPTP VM-execution control field
        save(0x7E9Cu, 4, 0x0u)  //  LDT Base (lower 32 bits)
        save(0x7E94u, 4, x86.cop.idtr.base[31..0])
        save(0x7E8Cu, 4, x86.mmu.gdtr.base[31..0])
        save(0x7E40u, 4, cpu.cregs.cr3.value)
        save(0x7DE8u, 4, 0x0u)  // IO_RIP
        save(0x7DD8u, 4, x86.cop.idtr.base[63..32])
        save(0x7DD4u, 4, 0x0u)  //  LDT Base (Upper 32 bits)
        save(0x7DD0u, 4, x86.mmu.gdtr.base[63..32])
    }

    private fun loadStateAfterSmm64(smBase: ULong) = with (x86) {
        fun load(offset: ULong, size: Int) =
            read(smBase + offset + SMM_ENTRY_OFFSET, UNDEF, size)

        cpu.cregs.cr0.value = load(0x7FF8u, 8)
        cpu.cregs.cr3.value = load(0x7FF0u, 8)

        cpu.flags.eflags.value = load(0x7FE8u, 8)  // rflags

        x86.config.efer = load(0x7FE0u, 8)

        cpu.regs.rip.value = load(0x7FD8u, 8)

        cpu.dregs.dr6.value = load(0x7FD0u, 8)
        cpu.dregs.dr7.value = load(0x7FC8u, 8)

        x86.cop.tssr = load(0x7FC4u, 4)  // TR_SEL
        x86.mmu.ldtr = load(0x7FC0u, 4)  // LDTR_SEL

//        load(0x7FA4u, 4)  // IO_MISC
//        load(0x7F9Cu, 8)  // IO_MEM_ADDR

        cpu.regs.rdi.value = load(0x7F94u, 8)
        cpu.regs.rsi.value = load(0x7F8Cu, 8)
        cpu.regs.rbp.value = load(0x7F84u, 8)
        cpu.regs.rsp.value = load(0x7F7Cu, 8)
        cpu.regs.rbx.value = load(0x7F74u, 8)
        cpu.regs.rdx.value = load(0x7F6Cu, 8)
        cpu.regs.rcx.value = load(0x7F64u, 8)
        cpu.regs.rax.value = load(0x7F5Cu, 8)
        cpu.regs.r8.value = load(0x7F54u, 8)
        cpu.regs.r9.value = load(0x7F4Cu, 8)
        cpu.regs.r10.value = load(0x7F44u, 8)
        cpu.regs.r11.value = load(0x7F3Cu, 8)
        cpu.regs.r12.value = load(0x7F34u, 8)
        cpu.regs.r13.value = load(0x7F2Cu, 8)
        cpu.regs.r14.value = load(0x7F24u, 8)
        cpu.regs.r15.value = load(0x7F1Cu, 8)

        // 7F1BH-7F04H Reserved

//        load(0x7F02u, 2) // Auto HALT Restart Field (Word)
//        load(0x7F00u, 2) // I/O Instruction Restart Field (Word)

        // SMM Revision Identifier Field (Doubleword)
        //  I/O instruction restart = 0
        //  SMRAM base address relocation is supported  = 1

        val newSmBase = load(0x7EF8u, 4) //  SMBASE Field (Doubleword)
        config.wrmsr(IA32_SMBASE, newSmBase)

//        load(0x7EE0u, 4) // Setting of “enable EPT” VM-execution control
//        load(0x7ED8u, 4) // Value of EPTP VM-execution control field
//        load(0x7E9Cu, 4)  //  LDT Base (lower 32 bits)

        val idtrBaseLo = load(0x7E94u, 4)
        val idtrBaseHi = load(0x7DD8u, 4)

        val gdtrBaseLo = load(0x7E8Cu, 4)
        val gdtrBaseHi = load(0x7DD0u, 4)

        mmu.gdtr.base = gdtrBaseLo.insert(gdtrBaseHi, 63..32)
        cop.idtr.base = idtrBaseLo.insert(idtrBaseHi, 63..32)

        cpu.cregs.cr3.value = load(0x7E40u, 4)

        cpu.sregs.gs.value = load(0x7FBCu, 4)
        cpu.sregs.fs.value = load(0x7FB8u, 4)
        cpu.sregs.ds.value = load(0x7FB4u, 4)
        cpu.sregs.ss.value = load(0x7FB0u, 4)
        cpu.sregs.cs.value = load(0x7FACu, 4)
        cpu.sregs.es.value = load(0x7FA8u, 4)

        mmu.invalidateGdtCache()
        mmu.invalidateProtectedMode()
        mmu.invalidatePagingCache()

//        load(0x7DE8u, 4, 0x0u)  // IO_RIP
//        load(0x7DD4u, 4, 0x0u)  //  LDT Base (Upper 32 bits)
    }

    private fun saveStateBeforeSmm32(smBase: ULong): Unit = throw NotImplementedError()

    private fun loadStateAfterSmm32(smBase: ULong): Unit = throw NotImplementedError()

    fun enterSmmMode(value: ULong) {
        val smBase = x86.config.rdmsrOrThrow(IA32_SMBASE)

        log.severe { "Enter SMM value=0x${value.hex} base=0x${smBase.hex} cs=0x${sregs.cs.value.hex} ip=0x${regs.rip.value.hex}" }

        when {
            csl && !csd -> saveStateBeforeSmm64(smBase)
            !csl && csd -> saveStateBeforeSmm32(smBase)
            else -> error("SMM can't be entered from mode where csd=$csd csl=$csl")
        }

        // 34.5.1 Initial SMM Execution Environment
        csd = false
        csl = false

        cregs.cr0.pe = false
        cregs.cr0.em = false
        cregs.cr0.ts = false
        cregs.cr0.pg = false

        cregs.cr4.value = 0u

        dregs.dr6.value = 0xDEADBEEFu  // undefined
        dregs.dr7.value = 0x400u

        flags.eflags.value = 0x2u

        regs.rip.value = SMM_ENTRY_OFFSET
        sregs.cs.value = smBase ushr 4

        sregs.ds.value = 0x0u
        sregs.es.value = 0x0u
        sregs.fs.value = 0x0u
        sregs.gs.value = 0x0u
        sregs.ss.value = 0x0u

//        debugger.isRunning = false
    }

    fun leaveSmmMode() {
        val oldSmBase = x86.config.rdmsrOrThrow(IA32_SMBASE)

        val newSmBase = x86.read(oldSmBase + 0x7EF8u + SMM_ENTRY_OFFSET, UNDEF, 4)
        log.severe { "Leave SMM base=0x${newSmBase.hex} cs=0x${sregs.cs.value.hex} ip=0x${regs.rip.value.hex}" }

        loadStateAfterSmm64(oldSmBase)
    }

    internal fun invalidateDecoderCache() = decoder.invalidateCacheIfRequired()

    private val decoder = x86SystemDecoder(x86, this)

    override fun reset() {
        super.reset()
        decoder.reset()
        regs.reset()
        flags.reset()
        sregs.reset()
        insn = Nop.create(x86)
    }

    override fun decode() {
        insn = decoder.decode(regs.rip.value)
//        log.info { "cs=${sregs.cs.value.hex} eip=${regs.eip.value.hex} $insn" }
    }

    override fun execute(): Int {
        regs.rip.value += insn.size.uint
        insn.execute()
        return 1  // TODO: get from insn.execute()
    }

    override fun stringify() = buildString {
        val where = x86.mmu.translate(regs.rip.value, sregs.cs.id, 1, AccessAction.LOAD)
        appendLine("x86 CPU: PC = 0x${where.hex8}")
        appendLine(regs.stringify())
        append(sregs.stringify())
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return super<IAutoSerializable>.serialize(ctxt)
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super<IAutoSerializable>.deserialize(ctxt, snapshot)
    }
}