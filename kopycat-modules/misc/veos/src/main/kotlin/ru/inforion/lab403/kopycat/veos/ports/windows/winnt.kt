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
package ru.inforion.lab403.kopycat.veos.ports.windows

 
enum class ProcessorFeature(val value: Int) {
    FLOATING_POINT_PRECISION_ERRATA(0),
    FLOATING_POINT_EMULATED(1),
    COMPARE_EXCHANGE_DOUBLE(2),
    MMX_INSTRUCTIONS_AVAILABLE(3),
    PPC_MOVEMEM_64BIT_OK(4),
    ALPHA_BYTE_INSTRUCTIONS(5),
    XMMI_INSTRUCTIONS_AVAILABLE(6),
    _3DNOW_INSTRUCTIONS_AVAILABLE(7),
    RDTSC_INSTRUCTION_AVAILABLE(8),
    PAE_ENABLED(9),
    XMMI64_INSTRUCTIONS_AVAILABLE(10),
    SSE_DAZ_MODE_AVAILABLE(11),
    NX_ENABLED(12),
    SSE3_INSTRUCTIONS_AVAILABLE(13),
    COMPARE_EXCHANGE128(14),
    COMPARE64_EXCHANGE128(15),
    CHANNELS_ENABLED(16),
    XSAVE_ENABLED(17),
    ARM_VFP_32_REGISTERS_AVAILABLE(18),
    ARM_NEON_INSTRUCTIONS_AVAILABLE(19),
    SECOND_LEVEL_ADDRESS_TRANSLATION(20),
    VIRT_FIRMWARE_ENABLED(21),
    RDWRFSGSBASE_AVAILABLE(22),
    FASTFAIL_AVAILABLE(23),
    ARM_DIVIDE_INSTRUCTION_AVAILABLE(24),
    ARM_64BIT_LOADSTORE_ATOMIC(25),
    ARM_EXTERNAL_CACHE_AVAILABLE(26),
    ARM_FMAC_INSTRUCTIONS_AVAILABLE(27),
    RDRAND_INSTRUCTION_AVAILABLE(28),
    ARM_V8_INSTRUCTIONS_AVAILABLE(29),
    ARM_V8_CRYPTO_INSTRUCTIONS_AVAILABLE(30),
    ARM_V8_CRC32_INSTRUCTIONS_AVAILABLE(31),
    RDTSCP_INSTRUCTION_AVAILABLE(32),
    RDPID_INSTRUCTION_AVAILABLE(33),

}