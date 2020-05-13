package ru.inforion.lab403.kopycat.examples

import ru.inforion.lab403.common.extensions.asLong
import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.library.types.Resource
import ru.inforion.lab403.kopycat.modules.stm32f042.BT
import ru.inforion.lab403.kopycat.modules.stm32f042.LED
import ru.inforion.lab403.kopycat.modules.stm32f042.STM32F042
import ru.inforion.lab403.kopycat.modules.terminals.UartSerialTerminal
import java.io.File

/**
 * @param parent родительский класс (данный модуль может быть модулем верхнего уровня, null)
 * @param name идентификатор модуля
 * @param fw_bytes данные прошивки (если null, то без прошивки)
 * @param tty_dbg путь к tty для выводы отладочного UART (если null, то выводиться никуда не будет)
 * @param tty_bt путь к tty для вывода bluetooth (если null, то выводиться никуда не будет)
 *
 * Варианты создания устройства (базовый модуль и имя пропущены):
 * 1. fw_bytes,  tty_dbg,  tty_bt
 * 2. fw_bytes,  -------,  -------    - без UART'тов
 * 3. fw_file,   tty_dbg,  tty_bt
 * 4. fw_file,   -------,  -------    - без UART'тов
 * 5. fw_res,    tty_dbg,  tty_bt
 * 6. -------,   tty_dbg,  tty_bt     - прошивка "Носорога" по умолчанию
 * 7. <без дополнительных параметров> - прошивка "Носорога" по умолчанию, без UART'тов
 *
 * fw_bytes - прошивка в виде ByteArray
 * fw_file - прошивка в виде File
 * fw_res - прошивка в виде Resource (файл внутри Jar)
 * tty_dbg - отладочный UART
 * tty_bt - UART для bluetooth модуля
 * ПРИМЕЧАНИЕ: Если fw_bytes, fw_file или fw_res равен null, то будет создан эмулятор без загруженной прошивки
 *             рекомендуемый вариант, если прошивка будет загружаться в последствии через GDB RSP.
 *
 * ПРИМЕЧАНИЕ: Возможно три варианта создания tty:
 * 1. Подключение к уже созданному tty (например, физическому COM-порту)
 * 2. Автоматическое создание виртуального COM-порта через socat и подключение к нему (только для UNIX-систем)
 *    для этого необходимо путь в начале пути прописать socat: (например, socat:/dev/ttyMyDevice)
 * 3. Терминирование вывода без передачи данных куда-либо. То есть к шине будет подключено UART-совместимое устройство,
 *    но данные не будут далее никуда передаваться (только логироваться, при условии соответствуюшего уровня лога).
 */
@Suppress("PrivatePropertyName")
class stm32f042_rhino constructor(
        parent: Module?, name: String, fw_bytes: ByteArray?, tty_dbg: String?, tty_bt: String?
) : Module(parent, name) {

    companion object {
        const val DEFAULT_FIRMWARE_PATH = "binaries/rhino_pass.bin"
        const val LEDS_COUNT = 16

    }

    inner class Buses : ModuleBuses(this) {
        val gpioa_leds = Bus("gpioa_leds", LEDS_COUNT)
    }

    override val buses = Buses()

    // Firmware as bytes
    constructor(parent: Module?, name: String, fw_bytes: ByteArray?) :
            this(parent, name, fw_bytes, null, null)

    // Firmware as file
    constructor(parent: Module?, name: String, fw_file: File?, tty_dbg: String?, tty_bt: String?) :
            this(parent, name, fw_file?.readBytes(), tty_dbg, tty_bt)
    constructor(parent: Module?, name: String, fw_file: File?):
            this(parent, name, fw_file, null, null)

    // Firmware as resource
    constructor(parent: Module?, name: String, fw_res: Resource?, tty_dbg: String?, tty_bt: String?) :
            this(parent, name, fw_res?.readBytes(), tty_dbg, tty_bt)

    // Default rhino firmware
    constructor(parent: Module?, name: String, tty_dbg: String?, tty_bt: String?) :
            this(parent, name, Resource(DEFAULT_FIRMWARE_PATH), tty_dbg, tty_bt)
    constructor(parent: Module?, name: String) :
            this(parent, name, null, null)

    private val stm32f042 = STM32F042(this, "u1_stm32", fw_bytes ?: ByteArray(0))

    private val usart_debug = UartSerialTerminal(this, "usart_debug", tty_dbg)
    private val term_bt = UartSerialTerminal(this, "term_bt", tty_bt)

    private val bluetooth = BT(this, "bluetooth")

    private val leds = Array(16) { LED(this, "led_$it") }

    init {
        leds.forEachIndexed { offset, led -> led.ports.pin.connect(buses.gpioa_leds, offset.asULong) }

        buses.connect(stm32f042.ports.usart1_m, usart_debug.ports.term_s)
        buses.connect(stm32f042.ports.usart1_s, usart_debug.ports.term_m)

        buses.connect(stm32f042.ports.usart2_m, bluetooth.ports.usart_m)
        buses.connect(stm32f042.ports.usart2_s, bluetooth.ports.usart_s)

        buses.connect(bluetooth.ports.bt_s, term_bt.ports.term_m)
        buses.connect(bluetooth.ports.bt_m, term_bt.ports.term_s)
    }

}

