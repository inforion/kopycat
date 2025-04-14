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
package ru.inforion.lab403.kopycat.modules.common.sata

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.truth
import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.common.extensions.untruth
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.auxiliary.fields.delegates.offsetField
import ru.inforion.lab403.kopycat.auxiliary.fields.interfaces.IOffsetable

/**
 * Received FIS, D2H.
 *
 * AHCI 1.0 specification, страница 26
 *
 * @param port порт SATA контроллера
 */
internal class FIS(private val port: Port) : IOffsetable {
    companion object {
        /** PSFIS offset */
        private const val FIS_PSFIS_OFFT = 0x20uL

        /** RFIS offset */
        private const val FIS_RFIS_OFFT = 0x40uL

        /** PIO D2H FIS type */
        const val FIS_PIO_D2H = 0x5FuL

        /** Register D2H FIS type */
        const val FIS_REGISTER_D2H = 0x34uL

        /** Register H2D FIS type */
        const val FIS_REGISTER_H2D = 0x27uL
    }

    override val memory = port.hba.dmam
    override val baseAddress = port.FB.data

    fun writeD2H(): Boolean {
        if (port.CMD.FRE.untruth || port.FB.data.untruth) {
            return false
        }

        RFIS_TYPE = FIS_REGISTER_D2H
        RFIS_PMP = 1uL shl 6
        RFIS_STATUS = port.status.data
        RFIS_ERROR = port.error.data
        RFIS_LBA0 = port.sector.ulong_z
        RFIS_LBA1 = port.lcyl.ulong_z
        RFIS_LBA2 = port.hcyl.ulong_z
        RFIS_DEV = port.select.ulong_z
        RFIS_LBA3 = port.hob_sector.ulong_z
        RFIS_LBA4 = port.hob_lcyl.ulong_z
        RFIS_LBA5 = port.hob_hcyl.ulong_z
        RFIS_COUNTL = port.nsector[7..0]
        RFIS_COUNTH = port.nsector[15..8]

        port.TFD.data = (port.error.data shl 8) or port.status.data

        if (port.status.err.truth) {
            port.IS.TFES = 1
            port.hba.checkIrq()
        }

        port.IS.DHRS = 1
        port.hba.checkIrq()

        return true
    }

    fun writePIO(len: UShort, pioFisI: Boolean) {
        if (port.CMD.FRE.untruth || port.FB.data.untruth) {
            return
        }

        PSFIS_TYPE = FIS_PIO_D2H
        PSFIS_PMP = if (pioFisI) 1uL shl 6 else 0uL
        PSFIS_STATUS = port.status.data
        PSFIS_ERROR = port.error.data

        PSFIS_LBA0 = port.sector.ulong_z
        PSFIS_LBA1 = port.lcyl.ulong_z
        PSFIS_LBA2 = port.hcyl.ulong_z
        PSFIS_DEV = port.select.ulong_z

        PSFIS_LBA3 = port.hob_sector.ulong_z
        PSFIS_LBA4 = port.hob_lcyl.ulong_z
        PSFIS_LBA5 = port.hob_hcyl.ulong_z

        PSFIS_COUNTL = port.nsector[7..0]
        PSFIS_COUNTH = port.nsector[15..8]
        PSFIS_ESTATUS = port.status.data
        PSFIS_TC = len.ulong_z

        port.TFD.data = (port.error.data shl 8) or port.status.data

        if (port.status.err.truth) {
            port.IS.TFES = 1
            port.hba.checkIrq()
        }
    }

    /** FIS_PIO_D2H */
    private var PSFIS_TYPE by offsetField("PSFIS_TYPE", FIS_PSFIS_OFFT + 0uL, Datatype.BYTE)
    private var PSFIS_PMP by offsetField("PSFIS_PMP", FIS_PSFIS_OFFT + 1uL, Datatype.BYTE)
    private var PSFIS_STATUS by offsetField("PSFIS_STATUS", FIS_PSFIS_OFFT + 2uL, Datatype.BYTE)
    private var PSFIS_ERROR by offsetField("PSFIS_ERROR", FIS_PSFIS_OFFT + 3uL, Datatype.BYTE)

    private var PSFIS_LBA0 by offsetField("PSFIS_LBA0", FIS_PSFIS_OFFT + 4uL, Datatype.BYTE)
    private var PSFIS_LBA1 by offsetField("PSFIS_LBA1", FIS_PSFIS_OFFT + 5uL, Datatype.BYTE)
    private var PSFIS_LBA2 by offsetField("PSFIS_LBA2", FIS_PSFIS_OFFT + 6uL, Datatype.BYTE)
    private var PSFIS_DEV by offsetField("PSFIS_DEV", FIS_PSFIS_OFFT + 7uL, Datatype.BYTE)

    private var PSFIS_LBA3 by offsetField("PSFIS_LBA3", FIS_PSFIS_OFFT + 8uL, Datatype.BYTE)
    private var PSFIS_LBA4 by offsetField("PSFIS_LBA4", FIS_PSFIS_OFFT + 9uL, Datatype.BYTE)
    private var PSFIS_LBA5 by offsetField("PSFIS_LBA5", FIS_PSFIS_OFFT + 10uL, Datatype.BYTE)

    private var PSFIS_COUNTL by offsetField("PSFIS_COUNTL", FIS_PSFIS_OFFT + 12uL, Datatype.BYTE)
    private var PSFIS_COUNTH by offsetField("PSFIS_COUNTH", FIS_PSFIS_OFFT + 13uL, Datatype.BYTE)
    private var PSFIS_ESTATUS by offsetField("PSFIS_ESTATUS", FIS_PSFIS_OFFT + 15uL, Datatype.BYTE)

    private var PSFIS_TC by offsetField("PSFIS_TC", FIS_PSFIS_OFFT + 16uL, Datatype.WORD)

    /** FIS_REGISTER_D2H */
    private var RFIS_TYPE by offsetField("RFIS_TYPE", FIS_RFIS_OFFT + 0uL, Datatype.BYTE)
    private var RFIS_PMP by offsetField("RFIS_PMP", FIS_RFIS_OFFT + 1uL, Datatype.BYTE)
    private var RFIS_STATUS by offsetField("RFIS_STATUS", FIS_RFIS_OFFT + 2uL, Datatype.BYTE)
    private var RFIS_ERROR by offsetField("RFIS_ERROR", FIS_RFIS_OFFT + 3uL, Datatype.BYTE)

    private var RFIS_LBA0 by offsetField("RFIS_LBA0", FIS_RFIS_OFFT + 4uL, Datatype.BYTE)
    private var RFIS_LBA1 by offsetField("RFIS_LBA1", FIS_RFIS_OFFT + 5uL, Datatype.BYTE)
    private var RFIS_LBA2 by offsetField("RFIS_LBA2", FIS_RFIS_OFFT + 6uL, Datatype.BYTE)
    private var RFIS_DEV by offsetField("RFIS_DEV", FIS_RFIS_OFFT + 7uL, Datatype.BYTE)

    private var RFIS_LBA3 by offsetField("RFIS_LBA3", FIS_RFIS_OFFT + 8uL, Datatype.BYTE)
    private var RFIS_LBA4 by offsetField("RFIS_LBA4", FIS_RFIS_OFFT + 9uL, Datatype.BYTE)
    private var RFIS_LBA5 by offsetField("RFIS_LBA5", FIS_RFIS_OFFT + 10uL, Datatype.BYTE)

    private var RFIS_COUNTL by offsetField("RFIS_COUNTL", FIS_RFIS_OFFT + 12uL, Datatype.BYTE)
    private var RFIS_COUNTH by offsetField("RFIS_COUNTH", FIS_RFIS_OFFT + 13uL, Datatype.BYTE)
}
