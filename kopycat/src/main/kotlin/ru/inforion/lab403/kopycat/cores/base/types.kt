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
package ru.inforion.lab403.kopycat.cores.base

import ru.inforion.lab403.kopycat.cores.base.abstracts.*
import ru.inforion.lab403.kopycat.cores.base.common.Debugger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.exceptions.HardwareException
import ru.inforion.lab403.kopycat.serializer.Serializer
import java.util.*


typealias AGenericCore = ACore<*, *, *>
typealias AGenericCOP = ACOP<*, *>
typealias AGenericCPU = ACPU<*, *, *, *>
typealias AGenericTracer = ATracer<AGenericCore>
typealias AGenericDebugger = Debugger

typealias GenericSerializer = Serializer<*>

typealias Register = Module.Register
typealias Area = Module.Area

typealias Bus = ModuleBuses.Bus

typealias APort = ModulePorts.APort
typealias MasterPort = ModulePorts.Master
typealias SlavePort = ModulePorts.Slave
typealias TranslatorPort = ModulePorts.Translator
typealias ProxyPort = ModulePorts.Proxy

typealias HardwareErrorHandler = (HardwareException) -> Long

typealias StackOfStrings = LinkedList<String>

typealias CpuRegister = ARegistersBankNG<*>.Register