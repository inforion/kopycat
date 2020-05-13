package ru.inforion.lab403.kopycat.modules.virtarm

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.extensions.request
import ru.inforion.lab403.kopycat.modules.*
import java.util.logging.Level



// Based on https://linux-sunxi.org/images/d/d2/Dw_apb_uart_db.pdf
class NS16550(parent: Module, name: String) : Module(parent, name) {

    companion object {
        val log = logger(Level.SEVERE)
    }

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", 0x100)
        val tx = Master("tx", UART_MASTER_BUS_SIZE)
        val rx = Slave("rx", UART_SLAVE_BUS_SIZE)
        val irq = Master("irq", PIN)
    }
    override val ports = Ports()

    inner class RBR_THR_DLL_Register : Register(ports.mem, 0x00, Datatype.DWORD, "UART_RBR_THR_DLL") {
        var dll: Long = 0x00
        val bytesOut = mutableListOf<Char>() // to terminal
        val bytesIn = mutableListOf<Char>() // from terminal

        override fun read(ea: Long, ss: Int, size: Int): Long {
            // Driven by DDL
            return if (LCR.data[7].toBool()) {
                NS16550.log.info { "Read from DLL: ${dll.hex8}" }
                dll
            }
            // Driven by RBR
            else if (bytesIn.isEmpty())
                0L
            else
                bytesIn.removeAt(0).toLong()
        }

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            // Driven by DLL
            if (LCR.data[7].toBool()) {
                NS16550.log.info { "Write to DLL: ${value.hex8}" }
                dll = value
            }
            // Driven by THR
            else {
                if (value.toChar() == '\n') {
                    log.finest { "${this.name}:\n${bytesOut.joinToString("")}" }
                    bytesOut.clear()
                    writeData('\r'.toInt())
                }
                else
                    bytesOut.add(value.toByte().toChar())
                writeData(value.toByte().toInt())
                if (IER.ier and 0x2L != 0L) {
                    IIR.iir = 0b0000
                    ports.irq.request(0)
                }
            }
        }
    }
    // Receive Buffer Register (DWORD, R)
    // Reset value: 0x0
    // Dependencies: LCR[7] = 0
    val RBR = RBR_THR_DLL_Register()
    // Transmit Holding Register (DWORD, W)
    // Reset value: 0x0
    // Dependencies: LCR[7] = 0
    val THR
        get() = RBR
    // Divisor Latch (Low) (DWORD, R/W)
    // Reset value: 0x0
    // Dependencies: LCR[7] = 1
    val DLL
        get() = RBR



    inner class UART_IER_DLH: Register(ports.mem, 0x04, Datatype.DWORD, "UART_IER_DLH") {
        var ier: Long = 0x00
        var dlh: Long = 0x00

        override fun read(ea: Long, ss: Int, size: Int): Long {
            // Driven by DLH
            return if (LCR.data[7].toBool()) {
                NS16550.log.info { "Read from DLH: ${dlh.hex8}" }
                dlh
            }
            // Driven by IER
            else {
                NS16550.log.info { "Read from IER: ${ier.hex8}" }
                ier
            }
        }
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            // Driven by DLH
            if (LCR.data[7].toBool()) {
                NS16550.log.info { "Write to DLH: ${value.hex8}" }
                dlh = value
            }
            // Driven by IER
            else {
                NS16550.log.info { "Write to IER: ${value.hex8}" }
                ier = value
            }
        }
    }

    // Interrupt Enable Register (DWORD, R/W)
    // Reset value: 0x0
    // Dependencies: LCR[7] = 0
    val IER = UART_IER_DLH()
    // Divisor Latch (High) (DWORD, R/W)
    // Reset value: 0x0
    // Dependencies: LCR[7] = 1
    val DLH
        get() = IER

    inner class UART_FCR_IIR : Register(ports.mem, 0x08, Datatype.DWORD, "UART_FCR_IIR") {
        var iir: Long = 0x01
        var fcr: Long = 0x00

        // Driven by IIR
        override fun read(ea: Long, ss: Int, size: Int): Long {
            NS16550.log.info { "Read from IIR: ${iir.hex8}" }
            return iir
        }

        // Driven by FCR
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            NS16550.log.info { "Write to FCR: ${value.hex8}" }
            fcr = value
        }
    }


    // FIFO Control Register (DWORD, W)
    // Reset value: 0x0
    val FCR = UART_FCR_IIR()

    // Interrupt Identification Register (DWORD, R)
    // Reset value: 0x01
    val IIR
        get() = FCR

    // Line Control Register (DWORD, R/W)
    // Reset value: 0x0
    val LCR = object : Register(ports.mem, 0x0C, Datatype.DWORD, "UART_LCR") {
        override fun read(ea: Long, ss: Int, size: Int): Long {
            NS16550.log.info { "Read from ${this.name}: ${data.hex8}" }
            return super.read(ea, ss, size)
        }
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            NS16550.log.info { "Write to ${this.name}: ${value.hex8}" }
            super.write(ea, ss, size, value)
        }
    }

    // Modem Control Register (DWORD, R/W)
    // Reset value: 0x0
    val MCR = object : Register(ports.mem, 0x10, Datatype.DWORD, "UART_MCR") {
        override fun read(ea: Long, ss: Int, size: Int): Long {
            NS16550.log.info { "Read from ${this.name}: ${data.hex8}" }
            return super.read(ea, ss, size)
        }
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            NS16550.log.info { "Write to ${this.name}: ${value.hex8}" }
            super.write(ea, ss, size, value)
        }
    }

     inner class LSR_Register : Register(ports.mem, 0x14, Datatype.DWORD, "UART_LSR", 0x60, writable = false) {
        var RFE by bit(7)
        var TEMT by bit(6)
        var THRE by bit(5)
        var BI by bit(4)
        var FE by bit(3)
        var PE by bit(2)
        var OE by bit(1)
        var DR by bit(0)


        override fun read(ea: Long, ss: Int, size: Int): Long {
            DR = RBR.bytesIn.isNotEmpty().toInt()
            return super.read(ea, ss, size)
        }
    }
    // Line Status Register (DWORD, R)
    // Reset value: 0x60
    val LSR = LSR_Register()

    // Modem Status Register (DWORD, R)
    // Reset value: 0x0
    val MSR = object : Register(ports.mem, 0x18, Datatype.DWORD, "UART_MSR", writable = false) {
        override fun read(ea: Long, ss: Int, size: Int): Long {
            NS16550.log.info { "Read from ${this.name}: ${data.hex8}" }
            return super.read(ea, ss, size)
        }
    }

    // Scratchpad Register (DWORD, R/W)
    // Reset value: 0x0
    val SPR = object : Register(ports.mem, 0x1C, Datatype.DWORD, "UART_SPR") {
        override fun read(ea: Long, ss: Int, size: Int): Long {
            NS16550.log.info { "Read from ${this.name}: ${data.hex8}" }
            return super.read(ea, ss, size)
        }
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            NS16550.log.info { "Write to ${this.name}: ${value.hex8}" }
            super.write(ea, ss, size, value)
        }
    }



    private fun writeData(value: Int) = ports.tx.write(UART_MASTER_BUS_DATA, 0, 1, value.asULong)
    private fun readData() = ports.tx.read(UART_MASTER_BUS_DATA, 0, 1).asInt

    val TERMINAL_REQUEST_REG = object : Register(
            ports.rx,
            UART_SLAVE_BUS_REQUEST,
            Datatype.DWORD,
            "TERMINAL_REQUEST_REG",
            readable = false,
            level = Level.SEVERE
    ) {
        val buffer = mutableListOf<Char>()


        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            when (ss) {
                UART_SLAVE_DATA_RECEIVED -> {
                    val x = readData().toChar()

                    // loopback
//                    writeData(x.toInt())

                    when (x) {
                        '\r' -> {
                            //writeData('\n'.toInt())
//                            buffer.add('\n')
//                            log.severe { "Not implemented write to serial: \"${buffer.joinToString { "" }}\"" }
//                            RBR.bytesIn.addAll(buffer)
//                            buffer.clear()
                            RBR.bytesIn.add('\n')
                        }
//                        0xFF.toChar() -> buffer.removeAt(buffer.size - 1)
                        else -> RBR.bytesIn.add(x) //buffer.add(x)
                    }

                    if (IER.ier and 0x1L != 0L) {
                        IIR.iir = 0b0100
                        ports.irq.request(0)
                    }
                }

                UART_SLAVE_DATA_TRANSMITTED -> {
                }
            }
        }
    }


    fun sendText(string: String) = RBR.bytesIn.addAll(string.toCharArray().toList())
}
