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
package ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.ppc.enums.eSystem
import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.support.InstructionTable
import ru.inforion.lab403.kopycat.modules.cores.PPCCore


class PPCSystemDecoder(core: PPCCore, vararg systems: eSystem) : APPCSystemDecoder(core) {

    override val name: String = "PowerPC System Decoder"

    override val baseOpcode = InstructionTable(
            8, 8,
            { data: Long -> data[31..29] },
            { data: Long -> data[28..26] },
            /////               0,0,0       0,0,1       0,1,0       0,1,1       1,0,0       1,0,1       1,1,0       1,1,1
            /*0,0,0*/  null,       null,       null,       null,       null,       null,       null,       null,
            /*1,0,0*/           null,       null,       null,       null,       null,       null,       null,       null,
            /*0,1,0*/           null,       null,       null,       group13,    null,       null,       null,       null,
            /*1,1,0*/           null,       null,       null,       null,       null,       null,       null,       group31,
            /*0,0,1*/           null,       null,       null,       null,       null,       null,       null,       null,
            /*1,0,1*/           null,       null,       null,       null,       null,       null,       null,       null,
            /*0,1,1*/           null,       null,       null,       null,       null,       null,       null,       null,
            /*1,1,1*/           null,       null,       null,       null,       null,       null,       null,       null
    )

    init {
        for (s in systems) {
            val decoder = s.decoder(core)
            baseOpcode += decoder.baseOpcode
            group13 += decoder.group13
            group31 += decoder.group31
        }
    }
}

