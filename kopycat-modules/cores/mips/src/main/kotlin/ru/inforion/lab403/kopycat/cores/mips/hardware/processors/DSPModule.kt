/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2023 INFORION, LLC
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
package ru.inforion.lab403.kopycat.cores.mips.hardware.processors

import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.Component
import ru.inforion.lab403.kopycat.cores.mips.hardware.registers.DSPBank
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

class DSPModule(core: MipsCore, name: String) : Component(core, name) {
    val regs = DSPBank()

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return mapOf(
            "name" to name,
            "regs" to regs.serialize(ctxt)
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        val snapshotName = snapshot["name"] as String
        if (name != snapshotName) {
            throw IllegalStateException("Wrong module name %s != %s".format(name, snapshotName))
        }
        try {
            regs.deserialize(ctxt, snapshot["regs"] as Map<String, Any>)
        } catch (e: java.lang.NullPointerException) {
            Component.log.severe { "Can't deserialize DSP regs -> DSP regs left unchanged" }
        }
    }

}