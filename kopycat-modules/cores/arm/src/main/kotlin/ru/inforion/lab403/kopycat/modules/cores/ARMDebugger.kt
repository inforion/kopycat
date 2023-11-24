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
package ru.inforion.lab403.kopycat.modules.cores

import ru.inforion.lab403.common.extensions.bigint
import ru.inforion.lab403.common.extensions.set
import ru.inforion.lab403.common.extensions.swap32
import ru.inforion.lab403.common.extensions.ulong
import ru.inforion.lab403.common.logging.WARNING
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.arm.hardware.processors.AARMCPU
import ru.inforion.lab403.kopycat.cores.base.common.Debugger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.Endian
import java.math.BigInteger

/**
 * {RU}
 * Отладчик ARM
 *
 * @property parent родительский модуль, в который встраивается отладчик
 * @property name произвольное имя объекта отладчика
 * @property endian порядок байтов
 * @property sendTargetXml передавать ли описание отлаживаемой архитектуры
 * ([документация](https://sourceware.org/gdb/current/onlinedocs/gdb.html/Target-Descriptions.html#Target-Descriptions))
 * при подключении gdb.
 * - Если `false`, то при подключении IDA будет выведено сообщение
 * "The GDB stub did not report a remote target configuration".
 * - Если `true`, то IDA может посчитать, что порядок байтов отлаживаемой системы отличается от порядка байтов
 * базы и сломать базу. Пример сообщения, выводимого IDA в таком случае: "The configuration reported by GDB
 * stub (ARM Little-endian) differs from the configuration in the IDB (ARM Big-endian)".
 * - По умолчанию `false` чтобы предотвратить случайную поломку баз IDA.
 * {RU}
 *
 * {EN}
 * ARM debugger
 *
 * @property parent parent module
 * @property name module name
 * @property endian byte order (endianness)
 * @property sendTargetXml whether to send target architecture description or not
 * ([documentation](https://sourceware.org/gdb/current/onlinedocs/gdb.html/Target-Descriptions.html#Target-Descriptions))
 * when connecting using gdb protocol.
 * - If `false`, then the following message will be displayed when connecting with IDA:
 * "The GDB stub did not report a remote target configuration".
 * - If `true`, then IDA may think that target endianness differs from that of the CPU that is set in the database
 * and then corrupt the database. Example of a message in that case: "The configuration reported by GDB
 * stub (ARM Little-endian) differs from the configuration in the IDB (ARM Big-endian)".
 * - `false` by default to prevent accidental IDA database corruption.
 * {EN}
 */
class ARMDebugger(
    parent: Module,
    name: String,
    val endian: Endian = Endian.LITTLE,
    private val sendTargetXml: Boolean = false,
): Debugger(parent, name) {

    companion object {
        @Transient val log = logger(WARNING)
        const val GDB_REGS_COUNT = 26
    }

    inline val cpu get() = core.cpu as AARMCPU

    override fun ident() = "arm"

    override fun target() = if (sendTargetXml) {
        "arm.xml"
    } else {
        "empty.xml"
    }

    override fun registers() = Array(GDB_REGS_COUNT) { regRead(it) }.toList()

    private var wrongRegisterIndex = mutableSetOf<Int>()

    override fun regRead(index: Int): BigInteger {
        val value = when (index) {
            // GPR
            in 0..14 -> cpu.reg(index)
            // PC
            15 -> {
                val pc = cpu.reg(index)
                if (cpu.CurrentInstrSet() == AARMCore.InstructionSet.THUMB) (pc set 0) else pc
            }
            // FPU
            in 16..24 -> 0u
            // CPSR
            25 -> cpu.flags()
            else -> {
                if (wrongRegisterIndex.add(index))
                    log.severe { "Reading unknown register index = $index -> This message will be print only once!" }
                0u
            }
        }
        val dataToRead = if (endian == Endian.BIG) value.swap32() else value
//        log.warning { "Read ${dataToRead.hex8} from $index" }
        return dataToRead.bigint
    }

    override fun regWrite(index: Int, value: BigInteger) {
        val dataToWrite = if (endian == Endian.BIG) value.ulong.swap32() else value.ulong
        when (index) {
            // GPR
            in 0..14 -> cpu.reg(index, dataToWrite)
            // PC
            15 -> {
                cpu.reg(index, dataToWrite)
                // dirty hack to make possible reset exception bypassing IDA Pro
                core.cpu.resetFault()
            }
            // FPU
            in 16..24 -> {

            }
            // CPSR
            25 -> if (core is ARMv6MCore)
                cpu.sregs.apsr.value = dataToWrite
            else
                cpu.sregs.cpsr.value = dataToWrite

            else -> log.severe { "Writing unknown register index = $index" }
        }
    }
}
