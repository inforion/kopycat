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