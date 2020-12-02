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
package ru.inforion.lab403.kopycat.cores.arm.hardware.processors

import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.base.CpuRegister
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.Component
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import ru.inforion.lab403.kopycat.modules.cores.AARMv6Core
import ru.inforion.lab403.kopycat.serializer.loadValue
import ru.inforion.lab403.kopycat.serializer.storeValues


class ARMv6COP(cpu: AARMCore, name: String) : AARMCOP(cpu, name) {

    var waitingForInterrupt = false

    // TODO: enum for cp0-cp15
    inner class COP(parent: Component, val ind: Int) : Component(parent, "cop$ind") {

        inline fun assert_value(a: Int, b: Int, msg: String) {
            if (a != b) throw GeneralException("Forbidden combination for $msg: $a != $b")
        }

        inline fun assert_access(a: AccessAction, b: AccessAction, msg: String) {
            if (a != b) throw GeneralException("${a.name} access denied for group \"$msg\"")
        }

        inline fun operate_register(
                reg: CpuRegister,
                value: Long?,
                access: AccessAction,
                logName: String? = null
        ): Long? {

            when (access) {
                AccessAction.LOAD -> {
//                    if (logName != null)
//                        log.info { "Read from $logName: ${reg.value.hex8}" }
                    return reg.value
                }
                AccessAction.STORE -> {
                    value!!
//                    if (logName != null)
//                        log.info { "Write to $logName: ${reg.value.hex8} -> ${value.hex8}" }
                    reg.value = value
                }
                else -> throw GeneralException("Only LOAD and STORE allowed")
            }
            return null
        }

        fun execute(opc1: Int, opc2: Int, crn: Int, crm: Int, value: Long?, access: AccessAction): Long? {
            if (ind != 15) // Now only cp15
                TODO("Not implemented")
            // See D12.7.1 in ARM architecture reference manual
            if (crn != 11 && crn != 15)
                assert_value(opc1, 0, "opc1")

            when (crn) {
                0 -> { // See D12.7.3
                    assert_value(crm, 0, "crm") // Others not implemented
                    assert_access(access, AccessAction.LOAD, "CP15 c0, ID support")
                    when (opc2) {
                        0 -> {
                            // MIDR, Main ID Register, R
                            return operate_register(core.cpu.vmsa.midr, value, access, "MIDR")
                        }
                        1 -> {
                            // CTR, Cache Type Register, R
                            return operate_register(core.cpu.vmsa.ctr, value, access, "MIDR")
                        }
                        2 -> TODO("TCMTR, TCM Type Register")
                        3 -> TODO("TLBTR, TLB Type Register")
                        5 -> TODO("MPIDR, Multiprocessor Affinity Register/Aliases of Main ID Register")
                        4,6,7 -> TODO("Aliases of Main ID Register")
                        else -> throw GeneralException("Unknown combination")
                    }

                }
                1 -> { // See D12.7.4
                    when (crm) {
                        0 -> {
                            when(opc2) {
                                0 -> { // SCTLR, System control register, RW
                                    val reg = core.cpu.vmsa.sctlr

                                    when (access) {
                                        AccessAction.LOAD -> {
//                                            log.info { "Read from SCTLR: ${reg.value.hex8}" }
                                            return reg.value
                                        }
                                        AccessAction.STORE -> {
                                            // Get rid of deprecated (fixed) values
                                            val maskedValue = (value!! and reg.mask) or reg.default

//                                            log.info { "Write to SCTLR: ${reg.value.hex8} -> ${maskedValue.hex8}" }

                                            reg.value = maskedValue

                                            // U = 1 - ARMv6/ARMv7 alignment model
                                            // XP = 1 - ARMv6/ARMv7 virtual memory support model
                                            // See description of D12.7.4 for details
                                            if (!reg.u || !reg.xp)
                                                throw GeneralException(
                                                        "Attempt of use deprecated MMU mode: U=${reg.u} XP=${reg.xp}"
                                                )

                                            (core as AARMv6Core).mmu.enabled = reg.m
                                        }
                                        else -> throw GeneralException("Only LOAD and STORE allowed")
                                    }
                                    return null
                                }
                                else -> TODO("Not implemented: $opc2")
                            }
                        }
                        else -> TODO("Not implemented: $crm")
                    }
                }
                2 -> { // See Figure B3-29
                    assert_value(crm, 0, "crm")
                    if (access == AccessAction.STORE)
                        (core as AARMv6Core).mmu.tlbInvalidate()
                    when (opc2) {
                        0 -> { // TTBR0, Translation Table Base Register 0, RW
                            return operate_register(core.cpu.vmsa.ttbr0, value, access, "TTBR0")
                        }
                        1 -> { // TTBR1, Translation Table Base Register 1, RW
                            return operate_register(core.cpu.vmsa.ttbr1, value, access, "TTBR1")
                        }
                        2 -> { // TTBRCR, Translation Table Base Control Register, RW
                            val ret = operate_register(core.cpu.vmsa.ttbcr, value, access, "TTBCR")
                            if (core.cpu.vmsa.ttbcr.eae)
                                TODO("usesLD")
                            return ret
                        }
                        else -> TODO("Not implemented: $opc2")
                    }
                }
                3 -> { // See Figure B3-29
                    assert_value(crm, 0, "crm")
                    assert_value(opc2, 0, "opc2")
                    return operate_register(core.cpu.vmsa.dacr, value, access)//"DACR")
                }
                5 -> { // Fault Status Registers
                    assert_value(crm, 0, "crm")
                    when (opc2) { // See B3.18.3
                        0 -> { // DFSR, Data Fault Status Register, RW
                            return operate_register(core.cpu.vmsa.dfsr, value, access, "DFSR")
                        }
                        1 -> TODO("IFSR, Instruction Fault Status Register")
                        else -> throw GeneralException("Unknown combination")
                    }
                }
                6 -> { // Fault Address Registers
                    assert_value(crm, 0, "crm")
                    when (opc2) { // See B3.18.3
                        0 -> { // DFAR, Data Fault Address Register, RW
                            return operate_register(core.cpu.vmsa.dfar, value, access, "DFAR")
                        }
                        1 -> TODO("Unknown")
                        2 -> TODO("IFSR, Instruction Fault Status Register")
                        else -> throw GeneralException("Unknown combination")
                    }
                }
                7 -> {
                    when (crm) {
                        0 -> {
                            when (opc2) {
                                4 -> { // See D12.7.11
                                    assert_access(access, AccessAction.STORE, "Wait For Interrupt, CP15WFI")
                                    /* TODO: Wait For Interrupt, CP15WFI */
                                    core.cpu.halted = true
                                    waitingForInterrupt = true
                                }
                                else -> TODO("Not implemented: $opc2")
                            }
                        }
                        5 -> {
                            when (opc2) {
                                0 -> { // See D12.7.10
                                    assert_access(access, AccessAction.STORE, "Cache and branch predictor maintenance operations (0)")
                                    /* TODO: Invalidate instruction cache */
                                }
                                4 -> { // See D12.7.11
                                    assert_access(access, AccessAction.STORE, "CP15ISB, Instruction barrier operation")
                                    /* TODO: Instruction Synchronization Barrier, CP15ISB */
                                }
                                6 -> {
                                    assert_access(access, AccessAction.STORE, "Cache and branch predictor maintenance operations (6)")
                                    /* TODO: Invalidate all branch predictors */
                                }
                                else -> TODO("Not implemented: $opc2")
                            }
                        }
                        7 -> {
                            assert_access(access, AccessAction.STORE, "Cache maintenance operations (c7)")
                            when(opc2) {
                                // See D12.7.10
                                0 -> { /* TODO: Invalidate unified cache, or instruction cache and data cache */ }
                                else -> TODO("Not implemented: $opc2")
                            }
                        }
                        10 -> {
                            when (opc2) {
                                0 -> { // See D12.7.10
                                    assert_access(access, AccessAction.STORE, "Cache maintenance operations (c10, 0)")
                                    /* TODO: Clean data cache */
                                }
                                1 -> {
                                    assert_access(access, AccessAction.STORE, "Cache maintenance operations (c10, 1)")
                                    /* TODO: Clean data cache line by MVA */
                                }
                                4 -> { // See D12.7.11
                                    assert_access(access, AccessAction.STORE, "Data barrier operations (4)")
                                    /* TODO: Data Synchronization Barrier, CP15DSBb */
                                }
                                5 -> { // See D12.7.11
                                    assert_access(access, AccessAction.STORE, "Data barrier operations (5)")
                                    /* TODO: Data memory barrier, CP15DMB */
                                }
                                else -> TODO("Not implemented: $opc2")
                            }
                        }
                        14 -> {
                            when (opc2) {
                                0 -> { // See D12.7.10
                                    assert_access(access, AccessAction.STORE, "Cache maintenance operations (c14, 1)")
                                    /* TODO: Clean and Invalidate data cache */
                                }
                                1 -> {
                                    assert_access(access, AccessAction.STORE, "Cache maintenance operations (c14, 1)")
                                    /* TODO: Clean and Invalidate data cache line by MVA */
                                }
                                else -> TODO("Not implemented: $opc2")
                            }
                        }
                        15 -> {
                            when (opc2) {
                                0 -> {
                                    // See nowhere. This instruction is not documented.
                                    // Found in proc-v6.S of Linux kernel sources:
                                    // mcr	p15, 0, r0, c7, c15, 0		@ clean+invalidate cache
                                    assert_access(access, AccessAction.STORE, "Cache maintenance operations (c15)")
                                    /* TODO: Clean and Invalidate cache */
                                }
                                else -> TODO("Not implemented: $opc2")
                            }
                        }
                        else -> TODO("Not implemented: $crm")
                    }
                }
                8 -> { // See B3.18.7
                    assert_access(access, AccessAction.STORE, "TLB maintenance operations")
                    when (crm) {
                        5 -> {
                            when(opc2) {
                                0 -> { /* Invalidate entire instruction TLB */
                                    (core as AARMv6Core).mmu.tlbInvalidate()
                                }
                                1 -> { /* Invalidate instruction TLB by MVA */
                                    (core as AARMv6Core).mmu.tlbInvalidate()
                                }
                                2 -> { /* Invalidate instruction TLB by ASID */
                                    (core as AARMv6Core).mmu.tlbInvalidate()
                                }
                                else -> TODO("Not implemented: $opc2")
                            }
                        }
                        6 -> {
                            when(opc2) {
                                0 -> { /* Invalidate entire data TLB */
                                    (core as AARMv6Core).mmu.tlbInvalidate()
                                }
                                1 -> { /* Invalidate data TLB by ASID */
                                    (core as AARMv6Core).mmu.tlbInvalidate()
                                }
                                2 -> { /* Invalidate data TLB entry by MVA */
                                    (core as AARMv6Core).mmu.tlbInvalidate()
                                }
                                else -> TODO("Not implemented: $opc2")
                            }
                        }
                        7 -> {
                            when(opc2) {
                                0 -> { /* Invalidate entire unified TLB */
                                    (core as AARMv6Core).mmu.tlbInvalidate()
                                }
                                else -> TODO("Not implemented: $opc2")
                            }
                        }
                        else -> TODO("Not implemented: $crm")
                    }
                }
                12 -> {
                    when (crm) {
                        0 -> {
                            when(opc2) {
                                0 -> {
                                    return operate_register(core.cpu.ser.vbar, value, access, "VBAR")
                                }
                                else -> TODO("Not implemented: $opc2")
                            }
                        }
                        else -> TODO("Not implemented: $crm")
                    }
                }
                13 -> {
                    when (crm) {
                        0 -> {
                            when(opc2) {
                                1 -> { // CONTEXTIDR, Context ID Register, RW
//                                    TODO("First CONTEXTIDR use")
                                    return operate_register(core.cpu.vmsa.contextidr, value, access, "CONTEXTIDR")
                                }
                                3 -> { // TPIDRURO, User Read Only, RW
                                    return operate_register(core.cpu.vmsa.tpidruro, value, access)
                                }
                                else -> TODO("Not implemented: $opc2")
                            }
                        }
                        else -> TODO("Not implemented: $crm")
                    }
                }
                else -> TODO("Not implemented: $crn")
            }
            return null
        }

    }

    val cops = Array(16) { i -> COP(this, i) }

    override fun Coproc_SendOneWord(opc1: Int, opc2: Int, crn: Int, crm: Int, cp_num: Int, value: Long) {
        cops[cp_num].execute(opc1, opc2, crn, crm, value, AccessAction.STORE)
    }

    override fun Coproc_GetOneWord(opc1: Int, opc2: Int, crn: Int, crm: Int, cp_num: Int): Long {
        return cops[cp_num].execute(opc1, opc2, crn, crm, null, AccessAction.LOAD)!!
    }

    override fun handleException(exception: GeneralException?): GeneralException? {
        when(exception) {
            is ARMHardwareException.CVCException -> core.cpu.TakeSVCException()
            is ARMHardwareException.DataAbortException -> core.cpu.TakeDataAbortException()
            is ARMHardwareException.PrefetchAbortException -> core.cpu.TakePrefetchAbortException()
            else -> return exception
        }
        return null
    }

    override fun processInterrupts() {
        val interrupt = pending(!core.cpu.sregs.cpsr.i || waitingForInterrupt)
        if (interrupt != null) {
            waitingForInterrupt = false
            core.cpu.TakePhysicalIRQException()
        }
    }

    override fun serialize(ctxt: GenericSerializer) =
            super.serialize(ctxt) + storeValues("waitingForInterrupt" to waitingForInterrupt)

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super.deserialize(ctxt, snapshot)
        waitingForInterrupt = loadValue(snapshot, "waitingForInterrupt")
    }
}
