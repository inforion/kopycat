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
package ru.inforion.lab403.kopycat.cores.x86

const val IA32_PLATFORM_ID: ULong = 0x17u
const val IA32_APIC_BASE: ULong = 0x1Bu
const val IA32_FEATURE_CONTROL: ULong = 0x3Au
const val IA32_BIOS_SIGN_ID: ULong = 0x8Bu
const val IA32_MTRRCAP: ULong = 0xFEu
const val MSR_BBL_CR_CTL3: ULong = 0x11Eu
const val IA32_MISC_ENABLE: ULong = 0x1A0u
const val MSR_FSB_FREQ: ULong = 0xCDu
const val MSR_PLATFORM_INFO: ULong = 0xCEu

const val IA32_PERFEVTSEL0: ULong = 0x186u
const val IA32_PERFEVTSEL1: ULong = 0x187u
const val IA32_PERFEVTSEL2: ULong = 0x188u
const val IA32_PERFEVTSEL3: ULong = 0x189u

const val IA32_PERF_STATUS: ULong = 0x198u

const val MSR_TURBO_RATIO_LIMIT: ULong = 0x1ADu
const val IA32_MCG_CAP: ULong = 0x179u
const val IA32_MCG_STATUS: ULong = 0x17Au
const val MSR_POWER_CTL: ULong = 0x1FCu
const val IA32_ENERGY_PERF_BIAS: ULong = 0x1B0u
const val IA32_CLOCK_MODULATION: ULong = 0x19Au
const val MSR_PKG_CST_CONFIG_CONTROL: ULong = 0xE2u

const val IA32_MTRR_PHYSBASE0: ULong = 0x200u
const val IA32_MTRR_PHYSBASE1: ULong = 0x202u
const val IA32_MTRR_PHYSBASE2: ULong = 0x204u
const val IA32_MTRR_PHYSBASE3: ULong = 0x206u
const val IA32_MTRR_PHYSBASE4: ULong = 0x208u
const val IA32_MTRR_PHYSBASE5: ULong = 0x20Au
const val IA32_MTRR_PHYSBASE6: ULong = 0x20Cu
const val IA32_MTRR_PHYSBASE7: ULong = 0x20Eu

const val IA32_MTRR_PHYSMASK0: ULong = 0x201u
const val IA32_MTRR_PHYSMASK1: ULong = 0x203u
const val IA32_MTRR_PHYSMASK2: ULong = 0x205u
const val IA32_MTRR_PHYSMASK3: ULong = 0x207u
const val IA32_MTRR_PHYSMASK4: ULong = 0x209u
const val IA32_MTRR_PHYSMASK5: ULong = 0x20Bu
const val IA32_MTRR_PHYSMASK6: ULong = 0x20Du
const val IA32_MTRR_PHYSMASK7: ULong = 0x20Fu

const val IA32_MTRR_FIX64K_00000: ULong = 0x250u
const val IA32_MTRR_FIX16K_80000: ULong = 0x258u
const val IA32_MTRR_FIX16K_A0000: ULong = 0x259u
const val IA32_MTRR_FIX4K_C0000: ULong = 0x268u
const val IA32_MTRR_FIX4K_C8000: ULong = 0x269u
const val IA32_MTRR_FIX4K_D0000: ULong = 0x26Au
const val IA32_MTRR_FIX4K_D8000: ULong = 0x26Bu
const val IA32_MTRR_FIX4K_E0000: ULong = 0x26Cu
const val IA32_MTRR_FIX4K_E8000: ULong = 0x26Du
const val IA32_MTRR_FIX4K_F0000: ULong = 0x26Eu
const val IA32_MTRR_FIX4K_F8000: ULong = 0x26Fu

const val IA32_THERM_INTERRUPT: ULong = 0x19Bu
const val MSR_TEMPERATURE_TARGET: ULong = 0x1A2u
const val IA32_PAT: ULong = 0x277u
const val MSR_EVICT_CTL: ULong = 0x2E0u
const val IA32_MTRR_DEF_TYPE: ULong = 0x2FFu
const val MSR_PKG_POWER_SKU_UNIT: ULong = 0x606u
const val MSR_PKG_POWER_LIMIT: ULong = 0x610u
const val MSR_PP1_POWER_LIMIT: ULong = 0x638u
const val MSR_IACORE_RATIOS: ULong = 0x66au
const val MSR_IACORE_TURBO_RATIOS: ULong = 0x66cu
const val MSR_IACORE_VIDS: ULong = 0x66bu
const val MSR_IACORE_TURBO_VIDS: ULong = 0x66du
const val MSR_PKG_TURBO_CFG1: ULong = 0x670u
const val MSR_CPU_TURBO_WKLD_CFG1: ULong = 0x671u
const val MSR_CPU_TURBO_WKLD_CFG2: ULong = 0x672u
const val MSR_CPU_THERM_CFG1: ULong = 0x673u
const val MSR_CPU_THERM_CFG2: ULong = 0x674u
const val MSR_CPU_THERM_SENS_CFG: ULong = 0x675u

const val MSR_IA32_SYSENTER_CS: ULong = 0x174u
const val MSR_IA32_SYSENTER_EIP: ULong = 0x176u
const val MSR_IA32_SYSENTER_ESP: ULong = 0x175u

// arch/x86/include/asm/msr-index.h
const val MSR_MISC_FEATURES_ENABLES: ULong = 0x140u
const val MSR_SMI_COUNT: ULong = 0x34u
const val MSR_CORE_C1_RES: ULong = 0x660u
const val MSR_PKG_C7_RESIDENCY: ULong = 0x3fau
const val MSR_CORE_C6_RESIDENCY: ULong = 0x3fdu

const val MSR_IA32_MCU_OPT_CTRL: ULong = 0x123u
const val MSR_IA32_SPEC_CTRL: ULong = 0x48u
const val MSR_IA32_TSX_CTRL: ULong = 0x122u
const val MSR_TSX_FORCE_ABORT: ULong = 0x10Fu
const val MSR_AMD64_LS_CFG: ULong = 0xc0011020u
const val  MSR_AMD64_DE_CFG:ULong=  0xc0011029u