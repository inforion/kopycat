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
package ru.inforion.lab403.kopycat.modules.cores

import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.ppc.exceptions.PPCExceptionHolder_e500v2
import ru.inforion.lab403.kopycat.cores.ppc.hardware.processors.systems.PPCCPU_e500v2
import ru.inforion.lab403.kopycat.cores.ppc.hardware.processors.systems.PPCMMU_EmbeddedMMUFSL



/*
*  +----------------------------------------------------------------------------+
*  |                                                                            |
*  |  +---------+                      +-----------------+                      |
*  |  |         |                      |                 |                      |
*  |  | PPCCPU [>]====Internal bus====[X] PPCMMU [E.MF] [X]====External bus====[X] Proxy
*  |  |         |                      |                 |                      |
*  |  +---------+                      +-----------------+                      |
*  |  +---------+                                                               |
*  |  |         |                                                               |
*  |  | PPCCOP  |                                                               |
*  |  |         |                                                               |
*  |  +---------+                                                               |
*  |                                                                 e500^2 core|
*  +----------------------------------------------------------------------------+
* */
class E500v2(parent: Module, name: String, frequency: Long):
        PPCCoreEmbedded(parent, name, frequency, PPCExceptionHolder_e500v2, ::PPCCPU_e500v2) {

    override val mmu = PPCMMU_EmbeddedMMUFSL(this, "mmu")

    init {
        initRoutine()
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return super.serialize(ctxt) + mapOf(
                "ppcemb" to super.serialize(ctxt),
                "mmu" to mmu.serialize(ctxt)
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super.deserialize(ctxt, snapshot["ppcemb"] as Map<String, String>)
        mmu.deserialize(ctxt, snapshot["mmu"] as Map<String, String>)
    }
}