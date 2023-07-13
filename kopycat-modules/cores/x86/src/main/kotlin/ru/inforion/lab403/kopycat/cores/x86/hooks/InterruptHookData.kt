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
import ru.inforion.lab403.kopycat.modules.cores.x86Core

typealias IntHookFunction = (core: x86Core, irq: ULong, data: InterruptHookData) -> Unit;

/**
 * Hooks CPU interrupt with kotlin function.
 * Can be used for extended functionality, that can be called from the emulated program.
 *
 * @param skipInstrExecution Skip the CPU instruction processing.
 * If enabled, instruction is equal to NOP.
 */
data class InterruptHookData constructor(
    val interruptId: ULong,
    val skipInstrExecution: Boolean = false,
    val callback: IntHookFunction,
)
