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
@file:Suppress("NOTHING_TO_INLINE")

package ru.inforion.lab403.kopycat.cores.base.extensions

import ru.inforion.lab403.kopycat.cores.base.MasterPort

const val TRACER_EVENT_PRE_EXECUTE = 0
const val TRACER_EVENT_POST_EXECUTE = 1
const val TRACER_EVENT_START = 2
const val TRACER_EVENT_STOP = 3
const val TRACER_BUS_SIZE = 4

/**
 * {RU}Запросить выполнение хука трассировщика до исполнения инструкции{RU}
 */
inline fun MasterPort.preExecute(status: Int): Boolean = read(0, TRACER_EVENT_PRE_EXECUTE, status) == 1L

/**
 * {RU}Запросить выполнение хука трассировщика после исполнения инструкции{RU}
 */
inline fun MasterPort.postExecute(status: Int): Boolean = read(0, TRACER_EVENT_POST_EXECUTE, status) == 1L

/**
 * {RU}Запросить выполнение хука трассировщика перед запуском эмуляции дебаггером{RU}
 */
inline fun MasterPort.start(status: Int): Boolean = read(0, TRACER_EVENT_START, status) == 1L

/**
 * {RU}Запросить выполнение хука трассировщика после остановки эмуляции дебаггером{RU}
 */
inline fun MasterPort.stop(status: Int): Boolean = read(0, TRACER_EVENT_STOP, status) == 1L

fun MasterPort.isTracerOk(): Boolean = (hasOuterConnection
        && access(0L, TRACER_EVENT_PRE_EXECUTE)
        && access(0L, TRACER_EVENT_POST_EXECUTE)
        && access(0L, TRACER_EVENT_START)
        && access(0L, TRACER_EVENT_STOP))