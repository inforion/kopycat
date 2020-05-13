package ru.inforion.lab403.kopycat.cores.base.common

import gnu.trove.map.hash.THashMap
import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Breakpoint.Type.ANY
import ru.inforion.lab403.kopycat.cores.base.common.Breakpoint.Type.HARDWARE
import java.util.logging.Level

/**
 * {RU}
 * Менеджер точек останова.
 *
 *
 * @property store хранилище-отображение точек останова (HashMap)
 * {RU}
 *
 * {EN}
 * Manager of breakpoints
 *
 * @property store storage for breakpoints (HashMap)
 * {EN}
 */
class BreakpointController {
    companion object {
        private val log = logger(Level.FINE)
    }

//    var enabled = true

    private val store = THashMap<Long, Breakpoint>(0x1000)

    /**
     * {RU}
     * Добавление новой точки останова
     *
     * @param ea Адрес точки останова
     * @param bpAccess Тип срабатывания точки останова
     * @param onBreak Функция-обработчик срабатывания точки останова
     * @return добавлена или нет точка оставнова (true/false)
     * {RU}
     *
     * {EN}
     * Add new breakpoint to this BreakpointController
     *
     * @param ea new breakpoint address
     * @param bpAccess type of action to breakpoint access
     * @param onBreak function to process breakpoint action
     * @return is success (true/false)
     * {EN}
     */
    fun add(ea: Long, bpAccess: Breakpoint.Access, onBreak: ((ea: Long) -> Unit)? = null): Boolean {
        if (ea in store) {
            log.warning { "Breakpoint already setup here ea=0x${ea.hex}" }
            return false
        }

        store[ea] = Breakpoint(ea, bpAccess, onBreak)
        return true
    }

    /**
     * {RU}
     * Удаление точки останова
     * @param ea адрес точки останова
     * @return удалена или нет точка останова (true/false)
     * {RU}
     *
     * {EN}
     * Delete breakpoint
     * @param ea breakpoint address
     * @return is success (true/false)
     * {EN}
     */
    fun remove(ea: Long) = store.remove(ea) != null

    fun oneshot(ea: Long, bpAccess: Breakpoint.Access, onBreak: (ea: Long) -> Unit) = add(ea, bpAccess) {
        onBreak(it)
        remove(it)
    }

    /**
     * {RU}
     * Проверка характеристик точки останова
     *
     * @param pAddr Физический адрес точки останова
     * @param vAddr Виртуальный адрес точки останова (не используется)
     * @param bpAccess Тип срабатывания точки останова
     * @return is success (true/false)
     * {RU}
     *
     * {EN}
     * Check breakpoint characteristic
     *
     * @param pAddr address of breakpoint
     * @param vAddr virtual address of breakpoint (it is not in use)
     * @param bpAccess type of action to breakpoint access
     * @return is success (true/false)
     * {EN}
     */
    @Suppress("UNUSED_PARAMETER")
    fun check(pAddr: Long, vAddr: Long, bpAccess: Breakpoint.Access): Boolean {
        val bpt = lookup(pAddr)
        if (bpt != null) return bpt.check(bpAccess)
        return false
    }

    /**
     * {RU}
     * Получение точки останова из пула [BreakpointController] по адресу
     *
     * @param addr Адрес точки останова
     * @return найдена или нет точка остановка по заданному адресу
     * {RU}
     *
     * {EN}
     * Get breakpoint from [BreakpointController] store by address
     *
     * @param addr breakpoint address
     * @return found breakpoint or null is it not found.
     * {EN}
     */
    fun lookup(addr: Long): Breakpoint? {
//        if (!enabled)
//            return null
        return store[addr]
    }

    /**
     * {EN}Delete all breakpoints from [BreakpointController]{EN}
     */
    fun clear() = store.clear()
}