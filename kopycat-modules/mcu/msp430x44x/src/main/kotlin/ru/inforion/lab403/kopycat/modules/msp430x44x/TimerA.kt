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
package ru.inforion.lab403.kopycat.modules.msp430x44x

import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.common.SystemClock
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.cores.base.extensions.enabled
import ru.inforion.lab403.kopycat.cores.base.extensions.request
import ru.inforion.lab403.kopycat.cores.base.field
import ru.inforion.lab403.kopycat.modules.BUS16
import ru.inforion.lab403.kopycat.modules.PIN
import java.util.logging.Level



class TimerA(parent: Module, name: String) : Module(parent, name) {

    companion object {
        @Transient private val log = logger(Level.FINE)
    }

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", BUS16)
        val irq_reg = Master("irq_reg", PIN)
        val irq_taccr0 = Master("irq_taccr0", PIN)
    }

    override val ports = Ports()

    private inner class Timer : SystemClock.PeriodicalTimer("TimerA Counter") {
        var direction = 1

        override fun trigger() {
            super.trigger()
            //val mode =
            when (TACTL.MCx) {
                0b01 -> {
                    if (TAR.data >= TACCR0.data) {
                        TAR.data = 0
                        //TODO: Multiple TACCTLx
                        TACTL.TAIFG = 1
                        TAIV.data = 0xA0
                        if (TACTL.TAIE == 1)
                            ports.irq_reg.request(0)
                    } else {
                        TAR.data += direction
                        if ((TAR.data == TACCR0.data) and (TACCTL0.CCIE == 1))
                            ports.irq_taccr0.request(0)
                    }

                }
            }
        }
    }

    private val timer = Timer()

    //Timer_A control register
    private var TACTL = object : Register(ports.mem, 0x160, WORD, "TACTL") {
        var TAIFG by bit(0)       //Timer_A interrupt flag
        var TAIE by bit(1)       //Timer_A interrupt enable
        var TACLR by bit(2)       //Timer_A clear
        var MCx by field(5..4)  //Mode control
        var IDx by field(7..6)  //Input divider
        var TASSELx by field(9..8)  //Timer_A clock source select

        override fun stringify(): String = "${super.stringify()} [TAIFG=$TAIFG TAIE=$TAIE TACLR=$TACLR MCx=$MCx IDx=$IDx TASSELx=$TASSELx]"

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)

            log.warning { stringify() }

            //Timer_A interrupt flag
            if ((TAIFG == 1) and (TAIE == 1))
                ports.irq_reg.request(0)

            //Timer_A interrupt enable
            ports.irq_reg.enabled(0, TAIE.toBool())

            //Timer_A clear
            if (TACLR == 1) {
                TAR.data = 0
                IDx = 0
                timer.direction = 1
                TACLR = 0
            }

            //Input divider
            val divider = when (IDx) {
                0b00 -> 1L
                0b01 -> 2L
                0b10 -> 4L
                0b11 -> 8L
                else -> throw Exception("Wrong implementation of divider in TimerA.kt")
            }

            //Timer_A clock source select
            val freq = when (TASSELx) {
                0b00 -> 1
                0b01 -> core.frequency / 32768
                else -> TODO("Not implemented in TimerA.kt")
            }


            //Mode control
            when (MCx) {
                0b00 -> { /*TODO: stop clock*/
                }
                0b01 -> {
                    core.clock.connect(timer, freq * divider)
                    log.info { "TimerA initialized: ${freq * divider}" }
                }
                0b10 -> throw Exception("MCx mode 0b10 isn't implemented in TimerA.kt")
                0b11 -> throw Exception("MCx mode 0b11 isn't implemented in TimerA.kt")
            }
        }

    }

    //Timer_A counter
    private var TAR = Register(ports.mem, 0x170, WORD, "TAR")

    //Timer_A capture/compare control 0
    private var TACCTL0 = object : Register(ports.mem, 0x162, WORD, "TACCTL0") {
        var CCIFG by bit(0)           //Capture/compare interrupt flag
        var COV by bit(1)           //Capture overflow
        var OUT by bit(2)           //Output
        var CCI by bit(3)           //Capture/compare input
        var CCIE by bit(4)           //Capture/compare interrupt enable
        var OUTMODx by field(7..5)      //Output mode
        var CAP by bit(8)           //Capture mode (capture or compare)
        var Unused by bit(9)           //Unused, read only, always 0
        var SCCI by bit(10)          //Synchronized capture/compare input
        var SCS by bit(11)          //Synchronize capture source
        var CCIS by field(13..12)    //Capture/compare input select
        var CMx by field(15..14)    //Capture mode

        override fun stringify(): String = "${super.stringify()} [CCIFG=$CCIFG COV=$COV OUT=$OUT CCI=$CCI CCIE=$CCIE OUTMODx=$OUTMODx CAP=$CAP SCCI=$SCCI SCS=$SCS CCIS=$CCIS CMx=$CMx]"

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)

            //Capture/compare interrupt flag
            if ((CCIFG == 1) and (CCI == 1)) {
                CCIFG = 0
                ports.irq_taccr0.request(0)
            }

            //Capture overflow
            if (COV == 1)
                throw Exception("COV isn't implemented in TimerA.kt")

            //Output
            if (OUT == 1)
                throw Exception("OUT isn't implemented in TimerA.kt")

            //Capture/compare input
            if (CCI == 1)
                throw Exception("CCI isn't implemented in TimerA.kt")

            //Capture/compare interrupt enable
            ports.irq_taccr0.enabled(0, CCIE.toBool())

            //Output mode
            when (OUTMODx) {
                0b000 -> {/*TODO: There is nothing to do yet. Keep it simple, stupid*/
                }
                0b001 -> throw Exception("OUTMODx mode 0b001 isn't implemented in TimerA.kt")
                0b010 -> throw Exception("OUTMODx mode 0b010 isn't implemented in TimerA.kt")
                0b011 -> throw Exception("OUTMODx mode 0b011 isn't implemented in TimerA.kt")
                0b100 -> throw Exception("OUTMODx mode 0b100 isn't implemented in TimerA.kt")
                0b101 -> throw Exception("OUTMODx mode 0b101 isn't implemented in TimerA.kt")
                0b110 -> throw Exception("OUTMODx mode 0b110 isn't implemented in TimerA.kt")
                0b111 -> throw Exception("OUTMODx mode 0b111 isn't implemented in TimerA.kt")
            }

            //Capture mode (capture or compare)
            if (CAP == 1)
                throw Exception("CAP isn't implemented in TimerA.kt")

            if (Unused == 1)
                throw Exception("9th bit are read only for TimerA")

            //Synchronized capture/compare input
            if (SCCI == 1)
                throw Exception("SCCI isn't implemented in TimerA.kt")

            //Synchronize capture source
            if (SCS == 1)
                throw Exception("SCS isn't implemented in TimerA.kt")

            //Capture/compare input select
            when (CCIS) {
                0b00 -> {/*TODO: There is nothing to do yet. Keep it simple, stupid*/
                }
                0b01 -> throw Exception("CCIS mode 0b01 isn't implemented in TimerA.kt")
                0b10 -> throw Exception("CCIS mode 0b10 isn't implemented in TimerA.kt")
                0b11 -> throw Exception("CCIS mode 0b11 isn't implemented in TimerA.kt")
            }

            //Capture mode
            when (CMx) {
                0b00 -> {/*TODO: There is nothing to do yet. Keep it simple, stupid*/
                }
                0b01 -> throw Exception("CMx mode 0b01 isn't implemented in TimerA.kt")
                0b10 -> throw Exception("CMx mode 0b10 isn't implemented in TimerA.kt")
                0b11 -> throw Exception("CMx mode 0b11 isn't implemented in TimerA.kt")
            }
        }
    }

    //Timer_A capture/compare 0
    private var TACCR0 = Register(ports.mem, 0x172, WORD, "TACCR0")
    //Timer_A interrupt vector
    private var TAIV = object : Register(ports.mem, 0x12E, WORD, "TAIV") {

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            when {
                TACTL.TAIFG == 1 -> TACTL.TAIFG = 0
            //TODO: other timers
            }
        }

    }
}