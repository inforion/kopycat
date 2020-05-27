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
package ru.inforion.lab403.kopycat.cores.base.enums

/**
 * Describes core state after step through conveyor (interrupts, decode, execute)
 */
enum class Status(val resume: Boolean) {
    /**
     * Exception will be passed to user (or debugger) ... no way to continue without outer interference
     * Effect: keep exception in CPU and stop
     */
    UNBEARABLE_EXCEPTION(false), // emulator not know what to do with it

    /**
     * Exception will be passed to debugger and should be cleared on next execute
     * Effect: reset exception in CPU and stop
     */
    BREAKPOINT_EXCEPTION(false),

    /**
     * Exception handled by coprocessor
     * Effect: if possible reset exception in CPU and resume
     *  otherwise keep exception and stop
     */
    INTERNAL_EXCEPTION(true),

    /**
     * Core successfully execute conveyor step (enter, decode, execute)
     * Effect: just resume
     */
    CORE_EXECUTED(true),

    /**
     * Something weird occurred before core start pace through enter, decode and execute
     * This may happen on start in tracer component
     * Effect: no exceptions and stop
     */
    NOT_EXECUTED(false)
}