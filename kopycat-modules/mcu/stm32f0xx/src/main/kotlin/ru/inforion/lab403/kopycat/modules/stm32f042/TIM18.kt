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
package ru.inforion.lab403.kopycat.modules.stm32f042

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.common.SystemClock
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.cores.base.field
import java.util.logging.Level


class TIM18(parent: Module, name: String) : Module(parent, name) {
    companion object {
        @Transient private val log = logger(Level.ALL)
    }

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem")
        val irq = Master("irq")
    }
    override val ports = Ports()

    private val counter = object : SystemClock.PeriodicalTimer("STK") {
        override fun trigger() {
            super.trigger()
        }
    }

    private inner class TIMx_CR1_TYP : Register(ports.mem, 0x00, WORD, "TIMx_CR1", 0x0000) {
        var CKD  by field(9..8)
        var ARPE by bit(7)
        var CMS  by field(6..5)
        var DIR  by bit(4)
        var OPM  by bit(3)
        var USR  by bit(2)
        var UDIS by bit(1)
        var CEN  by bit(0)
    }

    private inner class TIMx_CR2_TYP : Register(ports.mem, 0x04, WORD, "TIMx_CR2", 0x0000) {
        var OIS4  by bit(15)
        var OIS3N by bit(13)
        var OIS3  by bit(12)
        var OIS2N by bit(11)
        var OIS2  by bit(10)
        var OIS1N by bit(9)
        var OIS1  by bit(8)
        var TI1S  by bit(7)
        var MMS   by field(6..4)
        var CCDS  by bit(3)
        var CCUS  by bit(2)
        var CCPS  by bit(0)
    }

    private inner class TIMx_SMCR_TYP : Register(ports.mem, 0x08, WORD, "TIMx_SMCR", 0x0000) {
        var ETP  by bit(15)
        var ECE  by bit(14)
        var ETPS by field(13..12)
        var ETF  by field(11..8)
        var MSM  by bit(7)
        var TS   by field(6..4)
        var SMS  by field(2..0)
    }

    private inner class TIMx_DIER_TYP : Register(ports.mem, 0x0C, WORD, "TIMx_DIER", 0x0000) {
        var TDE   by bit(14)
        var COMDE by bit(13)
        var CC4DE by bit(12)
        var CC3DE by bit(11)
        var CC2DE by bit(10)
        var CC1DE by bit(9)
        var UDE   by bit(8)
        var BIE   by bit(7)
        var TIE   by bit(6)
        var COMIE by bit(5)
        var CC4IE by bit(4)
        var CC3IE by bit(3)
        var CC2IE by bit(2)
        var CC1IE by bit(1)
        var UIE   by bit(0)
    }

    private inner class TIMx_SR_TYP : Register(ports.mem, 0x10, WORD, "TIMx_SR", 0x0000) {
        var CC4OF by bit(12)
        var CC3OF by bit(11)
        var CC2OF by bit(10)
        var CC1OF by bit(9)
        var BIF   by bit(7)
        var TIF   by bit(6)
        var COMIF by bit(5)
        var CC4IF by bit(4)
        var CC3IF by bit(3)
        var CC2IF by bit(2)
        var CC1IF by bit(1)
        var UIF   by bit(0)
    }

    private inner class TIMx_EGR_TYP : Register(ports.mem, 0x14, WORD, "TIMx_EGR", 0x0000) {
        var BG    by bit(7)
        var TG    by bit(6)
        var COMG  by bit(5)
        var CC4G  by bit(4)
        var CC3G  by bit(3)
        var CC2G  by bit(2)
        var CC1G  by bit(1)
        var UG    by bit(0)
    }

    private inner class TIMx_CCMR1_TYP : Register(ports.mem, 0x18, WORD, "TIMx_CCMR1", 0x0000) {
        var OC2CE by bit(15)
        var OC2M  by field(14..12)
        var OC2PE by bit(11)
        var OC2FE by bit(10)
        var CC2S  by field(9..8)
        var OC1CE by bit(7)
        var OC1M  by field(6..4)
        var OC1PE by bit(3)
        var OC1FE by bit(2)
        var CC1S  by field(1..0)

        var IC2F   by field(15..12)
        var IC2PSC by field(11..10)
        var IC1F   by field(7..4)
        var IC1PSC by field(3..2)
    }

    private inner class TIMx_CCMR2_TYP : Register(ports.mem, 0x1C, WORD, "TIMx_CCMR2", 0x0000) {
        var OC4CE by bit(15)
        var OC4M  by field(14..12)
        var OC4PE by bit(11)
        var OC4FE by bit(10)
        var CC4S  by field(9..8)
        var OC3CE by bit(7)
        var OC3M  by field(6..4)
        var OC3PE by bit(3)
        var OC3FE by bit(2)
        var CC3S  by field(1..0)

        var IC4F   by field(15..12)
        var IC4PSC by field(11..10)
        var IC3F   by field(7..4)
        var IC3PSC by field(3..2)
    }

    private inner class TIMx_CCER_TYP : Register(ports.mem, 0x20, WORD, "TIMx_CCER", 0x0000) {
        var CC4P  by bit(13)
        var CC4E  by bit(12)
        var CC3NP by bit(11)
        var CC3NE by bit(10)
        var CC3P  by bit(9)
        var CC3E  by bit(8)
        var CC2NP by bit(7)
        var CC2NE by bit(6)
        var CC2P  by bit(5)
        var CC2E  by bit(4)
        var CC1NP by bit(3)
        var CC1NE by bit(2)
        var CC1P  by bit(1)
        var CC1E  by bit(0)
    }

    private inner class TIMx_CNT_TYP : Register(ports.mem, 0x24, WORD, "TIMx_CNT", 0x0000) {
        var CNT by field(15..0)
    }

    private inner class TIMx_PSC_TYP : Register(ports.mem, 0x28, WORD, "TIMx_PSC", 0x0000) {
        var PSC by field(15..0)
    }

    private inner class TIMx_ARR_TYP : Register(ports.mem, 0x2C, WORD, "TIMx_ARR", 0xFFFF) {
        var ARR by field(15..0)
    }

    private inner class TIMx_RCR_TYP : Register(ports.mem, 0x30, WORD, "TIMx_RCR", 0x0000) {
        var REP by field(7..0)
    }

    private inner class TIMx_CCR1_TYP : Register(ports.mem, 0x34, WORD, "TIMx_CCR1", 0x0000) {
        var CCR1 by field(15..0)
    }

    private inner class TIMx_CCR2_TYP : Register(ports.mem, 0x38, WORD, "TIMx_CCR2", 0x0000) {
        var CCR2 by field(15..0)
    }

    private inner class TIMx_CCR3_TYP : Register(ports.mem, 0x3C, WORD, "TIMx_CCR3", 0x0000) {
        var CCR3 by field(15..0)
    }

    private inner class TIMx_CCR4_TYP : Register(ports.mem, 0x40, WORD, "TIMx_CCR4", 0x0000) {
        var CCR4 by field(15..0)
    }

    private inner class TIMx_BDTR_TYP : Register(ports.mem, 0x44, WORD, "TIMx_BDTR", 0x0000) {
        var MOE  by bit(15)
        var AOE  by bit(14)
        var BKP  by bit(13)
        var BKE  by bit(12)
        var OSSR by bit(11)
        var OSSL by bit(10)
        var LOCK by field(9..8)
        var DTG  by field(7..0)
    }

    private inner class TIMx_DCR_TYP : Register(ports.mem, 0x48, WORD, "TIMx_DCR", 0x0000) {
        var DBL by field(12..8)
        var DBA by field(4..0)
    }

    private inner class TIMx_DMAR_TYP : Register(ports.mem, 0x4C, DWORD, "TIMx_DMAR", 0x0000) {
        var DMAB by field(31..0)
    }
}