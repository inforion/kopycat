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