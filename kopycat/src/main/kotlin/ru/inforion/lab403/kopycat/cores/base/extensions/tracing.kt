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

import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.base.MasterPort

const val TRACER_EVENT_PRE_EXECUTE = 0
const val TRACER_EVENT_POST_EXECUTE = 1
const val TRACER_EVENT_START = 2
const val TRACER_EVENT_STOP = 3
const val TRACER_EVENT_WORKING = 4
const val TRACER_BUS_SIZE = 5

// Don't change order of status because ComponentTracer look
//   for minimal status in method processing. So if any tracer want:
//   - to skip instruction - it will be skipped
//   - to stop execution - execution will be stopped
const val TRACER_STATUS_STOP = 0L
const val TRACER_STATUS_SKIP = 1L
const val TRACER_STATUS_SUCCESS = 2L

const val TRACER_REGISTER_EA: Long = 0

/**
 * {RU}Запросить выполнение хука трассировщика до исполнения инструкции{RU}
 */
inline fun MasterPort.preExecute(status: Int): Long = read(TRACER_REGISTER_EA, TRACER_EVENT_PRE_EXECUTE, status)

/**
 * {RU}Запросить выполнение хука трассировщика после исполнения инструкции{RU}
 */
inline fun MasterPort.postExecute(status: Int): Long = read(TRACER_REGISTER_EA, TRACER_EVENT_POST_EXECUTE, status)

/**
 * {RU}Запросить выполнение хука трассировщика перед запуском эмуляции дебаггером{RU}
 */
inline fun MasterPort.start(status: Int) = read(TRACER_REGISTER_EA, TRACER_EVENT_START, status)

/**
 * {RU}Запросить выполнение хука трассировщика после остановки эмуляции дебаггером{RU}
 */
inline fun MasterPort.stop(status: Int) = read(TRACER_REGISTER_EA, TRACER_EVENT_STOP, status)

/**
 * {RU}Запросить состояние трассировщика - включен или нет{RU}
 */
inline fun MasterPort.working() = read(TRACER_REGISTER_EA, TRACER_EVENT_WORKING, 0).toBool()

fun MasterPort.isTracerOk() = hasOuterConnection && access(TRACER_REGISTER_EA, -1) && working()