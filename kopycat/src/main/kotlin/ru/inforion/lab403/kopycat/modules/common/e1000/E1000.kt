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
package ru.inforion.lab403.kopycat.modules.common.e1000

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.LogLevel
import ru.inforion.lab403.common.logging.SEVERE
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.SystemClock
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.extensions.request
import ru.inforion.lab403.kopycat.cores.base.field
import ru.inforion.lab403.kopycat.auxiliary.fields.common.AbsoluteField
import ru.inforion.lab403.kopycat.cores.base.Port
import ru.inforion.lab403.kopycat.modules.PCI_MEM_AREA
import ru.inforion.lab403.kopycat.modules.common.e1000.protocols.CarelessEthernet
import ru.inforion.lab403.kopycat.modules.common.e1000.sources.NullSource
import ru.inforion.lab403.kopycat.modules.common.pci.PciDevice
import ru.inforion.lab403.kopycat.modules.common.pci.PciMSICapability
import ru.inforion.lab403.kopycat.serializer.loadValue
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.min

/**
 * Gigabit ethernet controller от Intel. В Linux использует драйвер E1000E.
 *
 * @param parent родительский модуль
 * @param name название модуля
 * @param mac MAC адрес по умолчанию
 * @param deviceId id PCI устройства, по умолчанию 0x10D3 = Intel 82574
 */
class E1000(
    parent: Module,
    name: String,
    busMemIndex: Int,
    private val mac: ByteArray,
    deviceId: Int = 0x10D3,
    bar0Area: Int = PCI_MEM_AREA,

    /**
     * The started packet source
     */
    var packetSource: IPacketSource = NullSource(true),
) : PciDevice(
    parent,
    name,
    0x8086,
    deviceId,
    classCode = 0x020000,
) {
    companion object {
        const val BUS_MEM_SIZE = 0x10000

        const val REG_LOG_LEVEL: LogLevel = FINE

        /** PCI capabilities offset */
        private const val CAP_OFFSET = 0x40uL

        /** Tx Queue 0 Interrupt */
        private const val E1000_ICR_TXQ0 = 0x00400000uL

        /** Tx Queue 1 Interrupt */
        private const val E1000_ICR_TXQ1 = 0x00800000uL

        /** Rx Queue 0 Interrupt */
        private const val E1000_ICR_RXQ0 = 0x00100000uL

        /** Rx Queue 1 Interrupt */
        private const val E1000_ICR_RXQ1 = 0x00200000uL

        /** Rx timer intr (ring 0) */
        private const val E1000_ICR_RXT0 = 0x00000080uL

        /** Other Interrupts */
        private const val E1000_ICR_OTHER = 0x01000000uL

        /** Transmit desc written back */
        private const val E1000_ICR_TXDW = 0x00000001uL

        /** Transmit Queue empty */
        private const val E1000_ICR_TXQE = 0x00000002uL

        /** Link Status Change */
        internal const val E1000_ICR_LSC = 0x00000004uL

        /** Rx desc min. threshold (0) */
        private const val E1000_ICR_RXDMT0 = 0x00000010uL

        /** Rx overrun */
        private const val E1000_ICR_RXO = 0x00000040uL

        /** MDIO access complete */
        private const val E1000_ICR_MDAC = 0x00000200uL

        private const val E1000_ICR_SRPD = 0x00010000uL

        /** Receive Ack frame */
        private const val E1000_ICR_ACK = 0x00020000uL

        /** Manageability event */
        private const val E1000_ICR_MNG = 0x00040000uL

        private val E1000_ICR_OTHER_CAUSES = E1000_ICR_LSC or
                E1000_ICR_RXO or E1000_ICR_MDAC or E1000_ICR_SRPD or E1000_ICR_ACK or E1000_ICR_MNG

        /** Report Status */
        private const val E1000_TXD_CMD_RS = 0x08000000uL

        /** Enable Tidv register */
        private const val E1000_TXD_CMD_IDE = 0x80000000uL

        /** Data Descriptor */
        private const val E1000_TXD_DTYP_D = 0x00100000uL

        /** End of Packet */
        private const val E1000_TXD_CMD_EOP = 0x01000000uL

        /** Descriptor extension */
        private const val E1000_TXD_CMD_DEXT = 0x20000000uL

        /** Descriptor Done */
        private const val E1000_TXD_STAT_DD = 0x00000001uL

        /** Descriptor Done */
        private const val E1000_RXD_STAT_DD = 0x01uL

        /** End of Packet */
        private const val E1000_RXD_STAT_EOP = 0x02uL

        /** UDP xsum caculated */
        private const val E1000_RXD_STAT_UDPCS = 0x10uL

        /** TCP xsum calculated */
        private const val E1000_RXD_STAT_TCPCS = 0x20uL

        /** IP xsum calculated */
        private const val E1000_RXD_STAT_IPCS = 0x40uL

        /** IP identification valid */
        private const val E1000_RXD_STAT_IPIDV = 0x200uL

        /** ACK Packet indication */
        private const val E1000_RXD_STAT_ACK = 0x8000uL

        private const val E1000_RXDEXT_STATERR_TCPE = 0x20000000uL
        private const val E1000_RXDEXT_STATERR_IPE = 0x40000000uL

        private const val E1000_RXDPS_HDRSTAT_HDRSP = 0x00008000uL

        /** Rx packet - IPv4 */
        private const val E1000_RXD_PKT_IP4 = 1uL

        /** Rx packet - TCP/UDP over IPv4 */
        private const val E1000_RXD_PKT_IP4_XDP = 2uL

        /** Rx packet - IPv6 */
        private const val E1000_RXD_PKT_IP6 = 5uL

        /** Rx packet - TCP/UDP over IPv6 */
        private const val E1000_RXD_PKT_IP6_XDP = 6uL

        // /** IP packet */
        // private const val E1000_TXD_CMD_IP = 0x02000000uL

        // /** TCP packet */
        // private const val E1000_TXD_CMD_TCP = 0x01000000uL

        // /** TCP Seg enable */
        // private const val E1000_TXD_CMD_TSE = 0x04000000uL

        // /** Insert TCP/UDP checksum */
        // private val E1000_TXD_POPTS_TXSM = 0x02u.ubyte

        // /** Insert IP checksum */
        // private val E1000_TXD_POPTS_IXSM = 0x01u.ubyte
    }

    val mem = ports.Port("mem")
    val dmam = ports.Port("dmam")
    val irq = ports.Port("irq")

    private val phy = Phy(this, "${name}.phy")

    /** Internal registers and memories */
    @Suppress("unused")
    private val BAR0 = PCI_BAR(0x10, DWORD, "BAR0", BUS_MEM_SIZE, bar0Area, busMemIndex, SEVERE)

    /** 8254x Family of Gigabit Ethernet Controllers
     * Software Developer’s Manual
     * p. 85
     *
     * TODO: вроде это должны быть BAR-ы
     * */
    @Suppress("unused")
    private val BAR1 = PCI_CONF_FUNC_WR(0x14, DWORD, "BAR1")

    @Suppress("unused")
    private val BAR2 = PCI_CONF_FUNC_WR(0x18, DWORD, "BAR2")

    @Suppress("unused")
    private val BAR3 = PCI_CONF_FUNC_WR(0x1C, DWORD, "BAR3")

    @Suppress("unused")
    private val BAR4 = PCI_CONF_FUNC_WR(0x20, DWORD, "BAR4")

    @Suppress("unused")
    private val BAR5 = PCI_CONF_FUNC_WR(0x24, DWORD, "BAR5")

    /** PCI MSI capability */
    private val CAP_MSI = PciMSICapability(this, "${name}.CAP_MSI", CAP_OFFSET, 0u)

    private var eeprom = mutableListOf<UShort>(
        0x0000u, 0x0000u, 0x0000u, 0x0420u, 0xf746u, 0x2010u, 0xffffu, 0xffffu,
        0x0000u, 0x0000u, 0x026bu, 0x0000u, 0x8086u, 0x0000u, 0x0000u, 0x8058u,
        0x0000u, 0x2001u, 0x7e7cu, 0xffffu, 0x1000u, 0x00c8u, 0x0000u, 0x2704u,
        0x6cc9u, 0x3150u, 0x070eu, 0x460bu, 0x2d84u, 0x0100u, 0xf000u, 0x0706u,
        0x6000u, 0x0080u, 0x0f04u, 0x7fffu, 0x4f01u, 0xc600u, 0x0000u, 0x20ffu,
        0x0028u, 0x0003u, 0x0000u, 0x0000u, 0x0000u, 0x0003u, 0x0000u, 0xffffu,
        0x0100u, 0xc000u, 0x121cu, 0xc007u, 0xffffu, 0xffffu, 0xffffu, 0xffffu,
        0xffffu, 0xffffu, 0xffffu, 0xffffu, 0x0000u, 0x0120u, 0xffffu, 0x0000u,
    )

    private var rxbufSizes = MutableList(4) { 0u }
    private var rxDescBufSize = 0u
    private var rxDescLen = 0u
    private var rxbufMinShift = 5u
    private var delayedCauses = 0uL
    private var msiCausesPending = 0uL
    private var itrIntrPending = false

    // TODO: сохранять в снапшоте?
    private val packetRxBacklogMtx = ReentrantLock()
    private val packetRxBacklog = CopyOnWriteArrayList<Pair<List<Byte>, Dissection>>()

    private val rxTmr = object : SystemClock.PeriodicalTimer("rxTmr") {
        override fun trigger() {
            packetRxBacklogMtx.withLock {
                if (packetRxBacklog.isNotEmpty()) {
                    packetRxBacklog.removeFirst()
                } else {
                    null
                }
            }?.also { (packet, dissection) ->
                rxPacket(packet, dissection)
            }
        }
    }

    init {
        assert(mac.count() == 6)

        for (i in 0 until 3) {
            eeprom[i] = (mac[2 * i + 1].ushort_z shl 8).ushort or mac[2 * i].ushort_z
        }

        eeprom[11] = deviceId.ushort
        eeprom[13] = deviceId.ushort
        eeprom[63] = (0xBABAu - eeprom.take(63).sum().ushort).ushort
    }

    override fun serialize(ctxt: GenericSerializer) = super.serialize(ctxt) + mapOf(
        "eeprom" to eeprom.joinToString("") { "${it[15..8].byte.hex2}${it[7..0].byte.hex2}" },
        *(rxbufSizes.mapIndexed { i, s ->
            "rxbufSize$i" to s
        }.toTypedArray()),
        "rxDescBufSize" to rxDescBufSize,
        "rxDescLen" to rxDescLen,
        "rxbufMinShift" to rxbufMinShift,
        "delayedCauses" to delayedCauses,
        "msiCausesPending" to msiCausesPending,
        "itrIntrPending" to itrIntrPending,
        *(TxRingInfos.mapIndexed { it, ring ->
            "ring${it}cache" to ring.packetCache.toByteArray().hexlify()
        }.toTypedArray()),
    )

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super.deserialize(ctxt, snapshot)
        loadValue<String>(snapshot, "eeprom").let {
            eeprom = it
                .asSequence()
                .chunked(4)
                .map { charGroup ->
                    charGroup.mapIndexed { idx, c ->
                        Character.digit(c, 16).uint shl (3 - idx) * 4
                    }.sum().ushort
                }
                .toMutableList()
        }

        rxbufSizes.indices.forEach { i ->
            rxbufSizes[i] = loadValue(snapshot, "rxbufSize$i") { 0u }
        }

        rxDescBufSize = loadValue(snapshot, "rxDescBufSize")
        rxDescLen = loadValue(snapshot, "rxDescLen")
        rxbufMinShift = loadValue(snapshot, "rxbufMinShift") { 5u }
        delayedCauses = loadValue(snapshot, "delayedCauses")
        msiCausesPending = loadValue(snapshot, "msiCausesPending")
        itrIntrPending = loadValue(snapshot, "itrIntrPending")
        TxRingInfos.forEachIndexed { idx, ring ->
            ring.packetCache.addAll(loadValue(snapshot, "ring${idx}cache") { ByteArray(0) }.asIterable())
        }
    }

    override fun initialize(): Boolean {
        if (!super.initialize()) return false

        core.clock.connect(radvTmr, 1024, Time.ns, false)
        core.clock.connect(rdtrTmr, 1024, Time.ns, false)
        core.clock.connect(raidTmr, 1024, Time.ns, false)
        core.clock.connect(tadvTmr, 1024, Time.ns, false)
        core.clock.connect(tidvTmr, 1024, Time.ns, false)
        core.clock.connect(itrTmr, 256, Time.ns, false)
        core.clock.connect(rxTmr, 512, Time.ns, true)

        return true
    }

    private fun updateInterruptState() {
        ICR.ASSERTED = 0
        if (ICR.data.truth) {
            ICR.ASSERTED = 1
        }

        ICS.data = ICR.data

        if ((IMS.data and ICR.data).truth) {
            sendMSI()
        } else {
            msiCausesPending = 0u
        }
    }

    private fun sendMSI() {
        val oldICR = ICR.data
        ICR.ASSERTED = 0
        var causes = ICR.data and IMS.data
        ICR.data = oldICR

        msiCausesPending = msiCausesPending and causes
        causes = causes xor msiCausesPending
        if (causes.untruth) {
            return
        }
        msiCausesPending = msiCausesPending or causes

        if (!shouldPostponeITR()) {
            dmam.write(CAP_MSI.CAP_MA.data, 0, 2, CAP_MSI.CAP_MD.data)
            irq.request(CAP_MSI.CAP_MD.vector.int)
        }
    }

    private fun shouldPostponeITR() = if (itrTmr.enabled) {
        itrIntrPending = true
        true
    } else {
        if (ITR.data.truth) {
            itrTmr.rearm()
        }
        false
    }

    internal fun setInterruptCause(cause: ULong) {
        ICR.data = ICR.data or cause or delayedCauses
        delayedCauses = 0u

        stopDelayTimers()
        updateInterruptState()
    }

    private fun stopDelayTimers() {
        radvTmr.enabled = false
        rdtrTmr.enabled = false
        raidTmr.enabled = false
        tadvTmr.enabled = false
        tidvTmr.enabled = false
    }

    private fun parseRxbufsize() {
        for (i in rxbufSizes.indices) {
            rxbufSizes[i] = 0u
        }

        if (RCTL.DTYP.truth) {
            rxbufSizes[0] = PSRCTL.BSIZE0.uint * 128u
            rxbufSizes[1] = PSRCTL.BSIZE1.uint * 1024u
            rxbufSizes[2] = PSRCTL.BSIZE2.uint * 1024u
            rxbufSizes[3] = PSRCTL.BSIZE3.uint * 1024u
        } else if (RCTL.FLXBUF.truth) {
            rxbufSizes[0] = RCTL.FLXBUF.uint * 1024u
        } else {
            rxbufSizes[0] = when {
                RCTL.BSEX.truth && RCTL.BSIZE == 1uL -> 16384u
                RCTL.BSEX.truth && RCTL.BSIZE == 2uL -> 8192u
                RCTL.BSEX.truth && RCTL.BSIZE == 3uL -> 4096u
                RCTL.BSEX.untruth && RCTL.BSIZE == 1uL -> 1024u
                RCTL.BSEX.untruth && RCTL.BSIZE == 2uL -> 512u
                RCTL.BSEX.untruth && RCTL.BSIZE == 3uL -> 256u
                else -> 2048u
            }
        }

        rxDescBufSize = rxbufSizes.sum()
    }

    private fun calcRxdesclen() {
        rxDescLen = if (RFCTL.EXSTEN.untruth) {
            16u
        } else {
            if (RCTL.DTYP[0].truth) {
                32u
            } else {
                16u
            }
        }
    }

    /*
    private fun rssHashType(eth: Ethernet): RSSType {
        if (eth.nextLayer is IPv4) {
            val ip: IPv4 = eth.nextLayer

            if (!ip.fragment && ip.proto == PROTO_TCP && MRQC.TCPIPV4.truth) {
                return RSSType.IPv4Tcp
            }

            if (MRQC.IPV4.truth) {
                return RSSType.IPv4
            }
        } else if (eth.nextLayer is IPv6) {
            val ip: IPv6 = eth.nextLayer

            if ((RFCTL.IPV6_EX_DIS.untruth || !ip.hasIPv6ExtensionHeader) &&
                (RFCTL.NEW_IPV6_EXT_DIS.untruth || !(ip.rssExDstValid || ip.rssExSrcValid))) {

                if (ip.proto == PROTO_TCP && !ip.fragment && MRQC.TCPIPV6.truth) {
                    return RSSType.IPv6Tcp
                }

                if (MRQC.IPV6EX.truth) {
                    return RSSType.IPv6Ex
                }
            }

            if (MRQC.IPV6.truth) {
                return RSSType.IPv6
            }
        }

        return RSSType.None
    }

    private data class RSSInfo(val enabled: Boolean, val hash: UInt, val queue: UInt, val type: RSSType)

    private fun rssParsePacket(eth: Ethernet): RSSInfo {
        val rss = MRQC.BIT0.truth && MRQC.BIT1.untruth && RXCSUM.PCSD.truth && RFCTL.EXSTEN.truth
        if (!rss) {
            return RSSInfo(false, 0u, 0u, RSSType.None)
        }

        val type = rssHashType(eth)
        if (type == RSSType.None) {
            return RSSInfo(true, 0u, 0u, type)
        }

        val hash = (eth.nextLayer as IP).calcHash(type, RSSRK.map { it.data.byte })

        return RSSInfo(
            true,
            hash,
            RETA[hash and 0x7fu].queue.uint,
            type,
        )
    }
    */

    private fun isTcpAck(dissection: Dissection): Boolean {
        if (dissection.tcp == null || dissection.tcp.flags.ACK.untruth) {
            return false
        }

        if (RFCTL.ACK_DATA_DIS.truth) {
            return !dissection.tcp.hasData
        }

        return true
    }

    private class BufferState(var written: Array<UInt>, var curIdx: Int)

    private fun writeToRxBuffers(ba: Array<ULong>, state: BufferState, data: List<Byte>) {
        var dataIdx = 0u
        var dataLen = data.size.uint

        while (dataLen != 0u) {
            val curBufLen = rxbufSizes[state.curIdx]
            val curBufBytesLeft = curBufLen - state.written[state.curIdx]
            val bytesToWrite = min(dataLen, curBufBytesLeft)

            for (i in 0u until bytesToWrite) {
                AbsoluteField(dmam, "dma", ba[state.curIdx] + state.written[state.curIdx] + i, BYTE)
                    .data = data[dataIdx + i].ulong_z
            }

            state.written[state.curIdx] += bytesToWrite
            dataIdx += bytesToWrite
            dataLen -= bytesToWrite

            if (state.written[state.curIdx] == curBufLen) {
                if (state.curIdx == 0xff) {
                    state.curIdx = 0
                } else {
                    state.curIdx += 1
                }
            }
        }
    }

    private inner class RxMetadata(dissection: Dissection, eop: Boolean) {
        var flags = E1000_RXD_STAT_DD
            private set
        var ipId = 0u
            private set

        init {
            if (eop) {
                flags = flags or E1000_RXD_STAT_EOP
                if (RXCSUM.PCSD.untruth && dissection.ip4 != null) {
                    flags = flags or E1000_RXD_STAT_IPIDV
                    ipId = dissection.ip4.id
                }
                if (isTcpAck(dissection)) {
                    flags = flags or E1000_RXD_STAT_ACK
                }
                if (dissection.ip6 == null || RFCTL.IPV6_DIS.untruth) {
                    if (dissection.tcp != null || dissection.udp != null) {
                        flags = flags or ((if (dissection.ip4 != null) E1000_RXD_PKT_IP4_XDP else E1000_RXD_PKT_IP6_XDP) shl 16)
                    } else if (dissection.ip4 != null || dissection.ip6 != null) {
                        flags = flags or ((if (dissection.ip4 != null) E1000_RXD_PKT_IP4 else E1000_RXD_PKT_IP6) shl 16)
                    }
                }
                if (dissection.ip6 == null || RFCTL.IPV6_XSUM_DIS.untruth) {
                    if (RXCSUM.IPOFLD.truth) {
                        if (dissection.ip4 != null) {
                            flags = flags or E1000_RXD_STAT_IPCS
                            if (dissection.ip4.checksum != 0u.ushort) {
                                // incorrect checksum
                                flags = flags or E1000_RXDEXT_STATERR_IPE
                            }
                        }
                    }

                    if (RXCSUM.TUOFLD.truth && (dissection.tcp != null || dissection.udp != null)) {
                        if (!(dissection.udp != null && dissection.udp.checksum == 0uL) &&
                            !(dissection.ip4 != null && dissection.ip4.fragment)) {

                            val checksum = dissection.ip!!.l4Checksum
                            val valid = ((checksum == 0u.ushort) || (checksum == 0xFFFFu.ushort))
                            val validFlag = if (valid) 0uL else E1000_RXDEXT_STATERR_TCPE

                            flags = flags or E1000_RXD_STAT_TCPCS or validFlag
                            if (dissection.udp != null) {
                                flags = flags or E1000_RXD_STAT_UDPCS
                            }
                        }
                    }
                }
            }
        }
    }

    private fun packetSplit(dissection: Dissection): Int? {
        if (!RFCTL.EXSTEN.untruth || RCTL.DTYP_PS.untruth) {
            return null
        }

        if (dissection.ip == null) {
            return null
        }

        if (dissection.ip.fragment && RFCTL.IPFRSP_DIS.truth) {
            return null
        }

        val hdrLen = if (!dissection.ip.fragment && (dissection.tcp != null || dissection.udp != null)) {
            dissection.l5offset!!
        } else {
            dissection.l4offset
        }

        if (hdrLen.uint > rxbufSizes[0]) {
            return null
        }

        return hdrLen
    }

    private fun writePacket(buf: List<Byte>, dissection: Dissection, ring: RxRingInfo) {
        var descOfft = 0u
        val size = dissection.fullSize.uint
        val totalSize = dissection.fullSize.uint + if (RCTL.SECRC.truth) 0u else 4u
        var dataOfft = 0

        val psHdrLen = packetSplit(dissection)

        do {
            val state = BufferState(Array(4) { 0u }, 0)

            val descSize = (totalSize - descOfft).coerceAtMost(rxDescBufSize)

            if (ring.dh.data == ring.dt.data || ring.dt.data >= ring.dlen.data / 16u) {
                return
            }

            val base = ring.dba.data + 16u * ring.dh.data
            val ba = if (RFCTL.EXSTEN.untruth) {
                // legacy descriptor
                arrayOf(
                    AbsoluteField(dmam, "dma", base, QWORD).data,
                    0uL,
                    0uL,
                    0uL,
                )
            } else if (RCTL.DTYP_PS.truth) {
                // ps descriptor
                arrayOf(
                    AbsoluteField(dmam, "dma", base, QWORD).data,
                    AbsoluteField(dmam, "dma", base + 8u, QWORD).data,
                    AbsoluteField(dmam, "dma", base + 16u, QWORD).data,
                    AbsoluteField(dmam, "dma", base + 24u, QWORD).data,
                )
            } else {
                // ext descriptor
                arrayOf(
                    AbsoluteField(dmam, "dma", base, QWORD).data,
                    0uL,
                    0uL,
                    0uL,
                )
            }

            if (ba[0].truth && descOfft < size) {
                val copySize = (size - descOfft).coerceAtMost(rxDescBufSize).int
                if (psHdrLen != null) {
                    TODO("packet split")
                }

                writeToRxBuffers(ba, state, buf.subList(dataOfft, dataOfft + copySize))
                dataOfft += copySize

                if (descOfft + descSize >= totalSize && RCTL.SECRC.untruth) {
                    writeToRxBuffers(ba, state, listOf(0, 0, 0, 0))
                }
            }

            descOfft += descSize

            val meta = RxMetadata(dissection, descOfft >= totalSize)

            if (RFCTL.EXSTEN.untruth) {
                // legacy descriptor

                // length
                AbsoluteField(dmam, "dma", base + 8u, WORD).data = state.written[0].ulong_z

                // csum
                AbsoluteField(dmam, "dma", base + 10u, WORD).data = 0u

                // errors
                AbsoluteField(dmam, "dma", base + 13u, BYTE).data = ((meta.flags ushr 24) and 0xFFu)
                // status
                AbsoluteField(dmam, "dma", base + 12u, BYTE).data = meta.flags and 0xFFu
            } else if (RCTL.DTYP_PS.truth) {
                // ps descriptor

                for (i in 0 until 32) {
                    AbsoluteField(dmam, "dma", base + i, BYTE).data = 0u
                }

                AbsoluteField(dmam, "dma", base + 12u, WORD).data = state.written[0].ulong_z

                for (i in 0 until 3) {
                    AbsoluteField(dmam, "dma", base + 18u + i.uint * 2u, WORD).data = state.written[1 + i].ulong_z
                }

                AbsoluteField(dmam, "dma", base + 8u, DWORD).data = meta.flags
                AbsoluteField(dmam, "dma", base + 4u, WORD).data = meta.ipId.ulong_z

                val psHdrLen2 = psHdrLen?.ulong_z ?: 0uL
                AbsoluteField(dmam, "dma", base + 16u, WORD).data =
                    psHdrLen2 or if (psHdrLen2.truth) E1000_RXDPS_HDRSTAT_HDRSP else 0uL
            } else {
                // ext descriptor

                for (i in 0 until 16) {
                    AbsoluteField(dmam, "dma", base + i, BYTE).data = 0u
                }

                AbsoluteField(dmam, "dma", base + 12u, WORD).data = state.written[0].ulong_z

                AbsoluteField(dmam, "dma", base + 8u, DWORD).data = meta.flags
                AbsoluteField(dmam, "dma", base + 4u, WORD).data = meta.ipId.ulong_z
            }

            ring.dh.data += rxDescLen / 16u
            if (ring.dh.data * 16u >= ring.dlen.data) {
                ring.dh.data = 0u
            }
        } while (descOfft < totalSize)

        updateRxStatistics(dissection)
    }

    private fun updateRxStatistics(dissection: Dissection) {
        val length = dissection.fullSize

        when (length + if (RCTL.SECRC.truth) 0 else 4) {
            in 0..64 -> PRC64
            in 65..127 -> PRC127
            in 128..255 -> PRC255
            in 256..511 -> PRC511
            in 512..1023 -> PRC1023
            else -> PRC1522
        }.apply {
            if (data != 0xffffffffuL) {
                data += 1u
            }
        }

        if (TPR.data != 0xffffffffuL) {
            TPR.data += 1uL
        }

        GPRC.data = TPR.data
        if (TOR.data + length + 4u < TOR.data) {
            TOR.data = ULONG_MAX
        } else {
            TOR.data += length.uint + 4u
        }

        GORC.data = TOR.data

        when (dissection.l2.packetType) {
            CarelessEthernet.Companion.PacketType.Broadcast -> BPRC.data += 1u
            CarelessEthernet.Companion.PacketType.Multicast -> MPRC.data += 1u
            else -> { }
        }
    }

    internal fun receive(buf: ArrayList<Byte>, dissection: Dissection) {
        packetRxBacklogMtx.withLock {
            packetRxBacklog.add(buf to dissection)
        }
    }

    internal val receiveBacklogFull: Boolean
        get() = packetRxBacklogMtx.withLock { packetRxBacklog.size > 3 }

    private fun rxPacket(buf: List<Byte>, dissection: Dissection) {
        if (STATUS.LU.untruth || RCTL.EN.untruth || COMMAND_STATUS.BME.untruth) {
            return
        }

        if (!RxRingInfos.any { it.dlen.data != 0uL && it.freeDescrNum() / (rxDescLen / 16u) * rxDescBufSize >= 1u }) {
            return
        }

        if (MRQC.MRQ_EN0.truth && MRQC.MRQ_EN1.untruth && RXCSUM.PCSD.truth && RFCTL.EXSTEN.truth) {
            TODO("RSS")
        }

        val ring = RxRingInfos[0 /* rss.queue */]
        val totalLength = dissection.fullSize + if (RCTL.SECRC.truth) 0 else 4

        var causes = 0uL
        if (ring.freeDescrNum() / (rxDescLen / 16u) * rxDescBufSize >= totalLength.uint) {
            writePacket(buf, dissection, ring)

            if (totalLength.uint < RSRPD.data) {
                causes = causes or E1000_ICR_SRPD
            }

            if (RFCTL.ACK_DIS.untruth && isTcpAck(dissection)) {
                causes = causes or E1000_ICR_ACK
            }

            val minThresholdHit = ring.freeDescrNum() == (ring.dlen.data ushr rxbufMinShift.int)
            causes = causes or E1000_ICR_RXT0 or if (minThresholdHit) E1000_ICR_RXDMT0 else 0uL
        } else {
            causes = causes or E1000_ICR_RXO
        }

        delayRxCauses(causes)
    }

    private fun delayRxCauses(causes: ULong) {
        var delayableCauses = E1000_ICR_RXQ0 or
            E1000_ICR_RXQ1 or E1000_ICR_RXT0

        if (RFCTL.ACK_DIS.untruth) {
            delayableCauses = delayableCauses or E1000_ICR_ACK
        }

        var causesMod = causes
        delayedCauses = delayedCauses or (causesMod and delayableCauses)
        causesMod = causesMod and delayableCauses.inv()

        if (RDTR.data.untruth || causesMod != 0uL) {
            setInterruptCause(causesMod)
            return
        }

        if (RAID.data.untruth && (delayedCauses and E1000_ICR_ACK).truth) {
            setInterruptCause(causesMod)
            return
        }

        rdtrTmr.rearm()

        if (!radvTmr.enabled && RADV.data.truth) {
            radvTmr.rearm()
        }

        if (!raidTmr.enabled && (delayedCauses and E1000_ICR_ACK).truth) {
            raidTmr.rearm()
        }
    }

    private fun delayTxCauses(causes: ULong) {
        val delayableCauses = E1000_ICR_TXQ0 or
            E1000_ICR_TXQ1 or E1000_ICR_TXQE or
            E1000_ICR_TXDW

        var causesMod = causes
        delayedCauses = delayedCauses or (causesMod and delayableCauses)
        causesMod = causesMod and delayableCauses.inv()

        if (causesMod.truth) {
            setInterruptCause(causesMod)
            return
        }

        tidvTmr.rearm()

        if (!tadvTmr.enabled && TADV.data.truth) {
            tadvTmr.rearm()
        }
    }

    private fun processTxDesc(base: ULong, txr: TxRingInfo) {
        val lower = AbsoluteField(dmam, "dma", base + 8u, DWORD).data

        when (lower and (E1000_TXD_CMD_DEXT or E1000_TXD_DTYP_D)) {
            E1000_TXD_CMD_DEXT -> {
                // TODO: context descriptor

                /*
                val descriptor = object : IMemoryRef, IOffsetable {
                    override val memory = dmam
                    override val baseAddress = base

                    val ipcss by offsetField("ipcss", 0u, BYTE)
                    val ipcso by offsetField("ipcso", 1u, BYTE)
                    val ipcse by offsetField("ipcse", 2u, WORD)
                    val tucss by offsetField("tucss", 4u, BYTE)
                    val tucso by offsetField("tucso", 5u, BYTE)
                    val tucse by offsetField("tucse", 6u, WORD)
                    val op by offsetField("op", 8u, DWORD)
                    val hdrLen by offsetField("hdrLen", 13u, BYTE)
                    val mss by offsetField("mss", 14u, WORD)
                }

                txr.props.apply {
                    ipcss = descriptor.ipcss.ubyte
                    ipcso = descriptor.ipcso.ubyte
                    ipcse = descriptor.ipcse.ushort
                    tucss = descriptor.tucss.ubyte
                    tucso = descriptor.tucso.ubyte
                    tucse = descriptor.tucse.ushort
                    paylen = descriptor.op and 0xfffffuL
                    hdrLen = descriptor.hdrLen.ubyte
                    mss = descriptor.mss.ushort
                    ip = (descriptor.op and E1000_TXD_CMD_IP).coerceAtMost(1uL).byte
                    tcp = (descriptor.op and E1000_TXD_CMD_TCP).coerceAtMost(1uL).byte
                    tse = (descriptor.op and E1000_TXD_CMD_TSE).truth
                }
                */

                return
            }
            /*
            (E1000_TXD_CMD_DEXT or E1000_TXD_DTYP_D) -> {
                // TODO: data descriptor
                txr.props.apply {
                    sumNeeded = ((AbsoluteField(dmam, "dma", base + 12u, DWORD).data ushr 8) and 0xFFuL).ubyte
                    cptse = (lower and E1000_TXD_CMD_TSE).truth
                }
            }
            else -> txr.props.cptse = false
            */
        }

        val start = AbsoluteField(dmam, "dma", base, QWORD).data
        val length = lower and 0xFFFFuL

        txr.packetCache.addAll((0uL until length).map {
            AbsoluteField(dmam, "dma", start + it, BYTE).data.byte }
        )

        // end of packet
        if ((lower and E1000_TXD_CMD_EOP).truth) {
            Dissection.dissect(txr.packetCache)?.let { dis ->
                updateTxStatistics(txr, dis.l2)

                // FIXME: очень макаронно, по-хорошему переписать Dissection'ы на Sealed Class'ы
                // И как-то с изменяемыми пакетами поиграться
                dis.udp?.also {
                    txr.packetCache[dis.l2.headerSize() + dis.ip!!.headerSize() + 6] = 0.byte;
                    txr.packetCache[dis.l2.headerSize() + dis.ip.headerSize() + 7] = 0.byte;
                    dis.ip4?.makeL4Checksum(dis.ip4.buffer.asSequence())?.also { sum ->
                        log.finest { "[0x${core.pc.hex}] Set udp checksum 0x${sum.hex}" }
                        txr.packetCache[dis.l2.headerSize() + dis.ip.headerSize() + 6] = (sum ushr 8).byte;
                        txr.packetCache[dis.l2.headerSize() + dis.ip.headerSize() + 7] = sum.byte;
                    }
                }
                dis.tcp?.also {
                    txr.packetCache[dis.l2.headerSize() + dis.ip!!.headerSize() + 16] = 0;
                    txr.packetCache[dis.l2.headerSize() + dis.ip.headerSize() + 17] = 0;
                    dis.ip4?.makeL4Checksum(dis.ip4.buffer.asSequence())?.also { sum ->
                        log.finest { "[0x${core.pc.hex}] Set tcp checksum 0x${sum.hex}" }
                        txr.packetCache[dis.l2.headerSize() + dis.ip.headerSize() + 16] = (sum ushr 8).byte;
                        txr.packetCache[dis.l2.headerSize() + dis.ip.headerSize() + 17] = sum.byte;
                    }
                }

                packetSource.send(txr.packetCache)
            }
            txr.packetCache.clear()

            /*
            txr.props.apply {
                sumNeeded = 0u
                cptse = false
            }
            */
        }
    }

    private fun updateTxStatistics(txr: TxRingInfo, ethernet: CarelessEthernet) {
        when (txr.packetCache.size) {
            in 0..64 -> PTC64
            in 65..127 -> PTC127
            in 128..255 -> PTC255
            in 256..511 -> PTC511
            in 512..1023 -> PTC1023
            else -> PTC1522
        }.apply {
            if (data != 0xffffffffuL) {
                data += 1u
            }
        }

        if (TPT.data != 0xffffffffuL) {
            TPT.data += 1u
        }

        if (TOT.data + txr.packetCache.size < TOT.data) {
            TOT.data = ULONG_MAX
        } else {
            TOT.data += txr.packetCache.size
        }

        when (ethernet.packetType) {
            CarelessEthernet.Companion.PacketType.Broadcast -> BPTC
            CarelessEthernet.Companion.PacketType.Multicast -> MPTC
            else -> null
        }?.apply {
            if (data != 0xffffffffuL) {
                data += 1u
            }
        }

        GPTC.data = TPT.data
        GOTC.data = TOT.data
    }

    private fun tx(txr: TxRingInfo) {
        if (TCTL.EN.untruth) {
            return
        }

        var ide = false
        var cause = E1000_ICR_TXQE

        while (txr.dh.data != txr.dt.data && txr.dt.data < txr.dlen.data / 16u) {
            val base = txr.dba.data + 16u * txr.dh.data

            processTxDesc(base, txr)

            val lower = AbsoluteField(dmam, "dma", base + 8u, DWORD).data
            if ((lower and E1000_TXD_CMD_RS).truth || IVAR.TX_INT_EVERY_WB.truth) {
                ide = (lower and E1000_TXD_CMD_IDE).truth
                AbsoluteField(dmam, "dma", base + 12u, DWORD).data =
                    AbsoluteField(dmam, "dma", base + 12u, DWORD).data or E1000_TXD_STAT_DD
                cause = cause or E1000_ICR_TXDW
            }

            txr.dh.data += 1u
            if (txr.dh.data * 16uL >= txr.dlen.data) {
                txr.dh.data = 0uL
            }
        }

        if (!ide) {
            setInterruptCause(cause)
        } else {
            delayTxCauses(cause)
        }
    }

    /** Device Control */
    internal inner class ControlClass(name: String) : Register(mem, 0x00u, DWORD, name, level = REG_LOG_LEVEL) {
        /** Full duplex */
        var FD by bit(0)

        /** GIO Master Disable */
        var GIOD by bit(2)

        /** Speed */
        var SPEED by field(9..8)

        /** Set link up (Force Link) */
        private var SLU by bit(6)

        /** Force Speed */
        var FRCSPD by bit(11)

        /** Force Duplex */
        var FRCDPLX by bit(12)

        /** D3Cold WakeUp Capability Advertisement Enable */
        private var ADVD3WUC by bit(20)

        /** Device Reset */
        private var RST by bit(26)

        /** Receive flow control enable */
        var RFCE by bit(27)

        /** Transmit flow control enable */
        var TFCE by bit(28)

        /** PHY Reset */
        private var PHY_RST by bit(31)

        override fun reset() {
            super.reset()
            FD = 1
            ADVD3WUC = 1
            SPEED = 2u // 1000 Mb/s
            SLU = 1
        }

        private fun update() {
            val newRst = RST
            RST = 0

            if (newRst.truth) {
                RAs[0].MAC = mac.foldIndexed(0uL) { idx, acc, b ->
                    acc or (b.ulong_z shl (8 * idx))
                }
                RAs[0].AV = 1

                // TODO: reset mac address
            }

            if (PHY_RST.truth) {
                STATUS.PHYRA = 1
            }
        }

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            update()
        }
    }

    /** Device Control */
    internal val CTRL = ControlClass("CTRL")

    // val UNKNOWN_REG = Register(mem, 0x04u, WORD, "")

    /** Device Status Register */
    internal inner class StatusClass(name: String) : Register(mem, 0x08u, DWORD, name, level = REG_LOG_LEVEL) {
        /** Full Duplex */
        private var FD by bit(0)

        /** Link Up */
        var LU by bit(1)

        /** Link speed setting */
        private var SPEED by field(7..6)

        /** Auto-Speed Detection Value */
        private var ASDV by field(9..8)

        /** PHY Reset Asserted */
        var PHYRA by bit(10)

        /** GIO Master Enable Status */
        private var GIOE by bit(19)

        override fun reset() {
            super.reset()
            ASDV = 2u // 1000 Mb/s
            LU = 1
        }

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            GIOE = if (CTRL.GIOD.untruth) 1 else 0
            FD = if (CTRL.FRCDPLX.truth) CTRL.FD else 1
            SPEED = if (CTRL.FRCSPD.truth or CTRL_EXT.SPD_BYPS.truth) CTRL.SPEED else 2u // 1000 Mb/s
            return super.read(ea, ss, size)
        }

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) = Unit
    }

    /** Device Status Register */
    internal val STATUS = StatusClass("STATUS")

    /** EEPROM/Flash Control */
    @Suppress("unused")
    private val EECD = object : Register(mem, 0x10u, DWORD, "EECD", level = REG_LOG_LEVEL) {
        /** NVM Present */
        var PRES by bit(8)

        /** NVM Auto Read Done */
        var AUTO_RD by bit(9)

        /** NVM Size */
        var SIZE by field(14..11)

        override fun reset() {
            super.reset()
            PRES = 1
            AUTO_RD = 1
        }

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            val oldPres = PRES
            val oldAutoRd = AUTO_RD
            val oldSize = SIZE

            super.write(ea, ss, size, value)

            PRES = oldPres
            AUTO_RD = oldAutoRd
            SIZE = oldSize
        }
    }

    /** EEPROM Read */
    @Suppress("unused")
    private val EERD = object : Register(mem, 0x14u, DWORD, "EERD") {
        /** Start Read */
        var START by bit(0)

        /** Read Done */
        var DONE by bit(1)

        /** Read Address */
        val ADDR by field(15..2)

        /** Read Data */
        var DATA by field(31..16)

        override fun reset() {
            super.reset()
            DONE = 1
        }

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)

            val addr = ADDR.int

            if (addr < eeprom.size && START.truth) {
                DATA = eeprom[addr].ulong_z
                DONE = 1
            } else {
                DONE = 0
            }

            START = 0
        }
    }

    /** Extended Device Control */
    private val CTRL_EXT = object : Register(mem, 0x18u, DWORD, "CTRL_EXT", level = REG_LOG_LEVEL) {
        /** ASD (Auto Speed Detection) Check */
        var ASDCHK by bit(12)

        /** EEPROM Reset */
        var EE_RST by bit(13)

        /** Speed Select Bypass */
        val SPD_BYPS by bit(15)

        /** Interrupt acknowledge auto-mask enable */
        val IAME by bit(27)

        /** Enables the clearing of the interrupt timers following an IMS clear */
        val INT_TIMERS_CLEAR_ENA by bit(29)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            ASDCHK = 0
            EE_RST = 0
        }
    }

    /** MDI Control */
    @Suppress("unused")
    private val MDIC = object : Register(mem, 0x20u, DWORD, "MDIC", level = REG_LOG_LEVEL) {
        /** Data */
        val DATA by field(15..0)

        /** PHY register address */
        val REGADD by field(20..16)

        /** PHY Address */
        val PHYADD by field(25..21)

        /** MDI write */
        val OP_WRITE by bit(26)

        /** MDI read */
        val OP_READ by bit(27)

        /** Ready Bit */
        var READY by bit(28)

        /** Interrupt Enable */
        val INT_EN by bit(29)

        /** Error */
        var ERROR by bit(30)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            val old = data
            super.write(ea, ss, size, value)

            if (PHYADD != 1uL) {
                // phy #
                data = old
                ERROR = 1
            } else if (OP_READ.truth) {
                val page = phy.checkCap(REGADD, Phy.PHY_R)

                if (page == null) {
                    ERROR = 1
                } else {
                    data = (data xor DATA) or phy.read(page, REGADD).ulong_z
                }
            } else if (OP_WRITE.truth) {
                val page = phy.checkCap(REGADD, Phy.PHY_W)

                if (page == null) {
                    ERROR = 1
                } else {
                    phy.write(page, REGADD, DATA.ushort)
                }
            }

            READY = 1

            if (INT_EN.truth) {
                setInterruptCause(E1000_ICR_MDAC)
            }
        }
    }

    private open inner class Dword16BitWriteRegister(addr: ULong, name: String) : Register(mem, addr, DWORD, name, level = REG_LOG_LEVEL) {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            data = data and 0xFFFFuL
        }
    }

    /** Flow Control Type */
    @Suppress("unused")
    private val FCT = Dword16BitWriteRegister(0x30u, "FCT")

    /** VLAN Ether Type */
    @Suppress("unused")
    private val VET = object : Dword16BitWriteRegister(0x38u, "VET") {
        override fun reset() {
            super.reset()
            data = 0x8100uL
        }
    }

    /** Interrupt Cause Read */
    private inner class ICRClass(name: String) : Register(mem, 0xC0u, DWORD, name, level = REG_LOG_LEVEL) {
        /** Interrupt Asserted */
        var ASSERTED by bit(31)

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            val ret = super.read(ea, ss, size)

            data = 0u

            updateInterruptState()
            return ret
        }

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            val old = data
            super.write(ea, ss, size, value)
            val new = data

            data = old
            if (ASSERTED.truth && CTRL_EXT.IAME.truth) {
                IMS.data = IMS.data and IAM.data.inv()
            }

            var icr = old and new.inv()
            if ((new and E1000_ICR_OTHER).truth) {
                icr = icr and E1000_ICR_OTHER_CAUSES.inv()
            }

            data = icr
            updateInterruptState()
        }
    }

    /** Interrupt Cause Read */
    private val ICR = ICRClass("ICR")

    /** Interrupt Throttling Rate */
    private val ITR = object : Register(mem, 0xC4u, DWORD, "ITR", level = REG_LOG_LEVEL) {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            data = data and 0xFFFFu
            if (500uL > data) {
                data = 500u
            }
        }
    }

    /** Interrupt Cause Set */
    private val ICS = object : Register(mem, 0xC8u, DWORD, "ICS", level = REG_LOG_LEVEL) {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            setInterruptCause(data)
        }
    }

    /** Interrupt Mask Set */
    private val IMS = object : Register(mem, 0xD0u, DWORD, "IMS", level = REG_LOG_LEVEL) {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            val old = data
            super.write(ea, ss, size, value)

            data = old or data

            if (data == 0xFFFFFFFFuL && CTRL_EXT.INT_TIMERS_CLEAR_ENA.truth) {
                val causes = delayedCauses
                delayedCauses = 0u
                stopDelayTimers()

                ICR.data = ICR.data or causes

                if (itrTmr.enabled) {
                    itrTmr.enabled = false
                    if (itrIntrPending) {
                        msiCausesPending = 0u
                        setInterruptCause(0u)
                    }
                }
            }

            updateInterruptState()
        }
    }

    /** Interrupt Mask Clear */
    @Suppress("unused")
    private val IMC = object : Register(mem, 0xD8u, DWORD, "IMC", level = REG_LOG_LEVEL) {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            IMS.data = IMS.data and data.inv()
            data = 0u
            updateInterruptState()
        }
    }

    /** Interrupt Acknowledge Auto – Mask */
    private val IAM = Register(mem, 0xE0u, DWORD, "IAM", level = REG_LOG_LEVEL)

    /** Interrupt Vector Allocation Register */
    private val IVAR = object : Register(mem, 0xE4u, DWORD, "IVAR", level = REG_LOG_LEVEL) {
        val TX_INT_EVERY_WB by bit(31)
    }

    /** RX Control */
    private val RCTL = object : Register(mem, 0x100u, DWORD, "RCTL", level = REG_LOG_LEVEL) {
        /** Enable */
        val EN by bit(1)

        /** Descriptor Type */
        val DTYP by field(11..10)

        /** Packet Split descriptor */
        val DTYP_PS by bit(10)

        /** Receive Buffer Size */
        val BSIZE by field(17..16)

        /** Buffer Size Extension */
        val BSEX by bit(25)

        /** Strip Ethernet CRC */
        val SECRC by bit(26)

        /** Flexible buffer size */
        val FLXBUF by field(30..27)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)

            if (EN.truth) {
                parseRxbufsize()
                calcRxdesclen()
                rxbufMinShift = ((data / 0x100uL) and 3u).uint + 5u
            }
        }
    }

    /** Flow Control Transmit Timer Value */
    @Suppress("unused")
    private val FCTTV = Dword16BitWriteRegister(0x170u, "FCTTV")

    /** Transmit Control Register */
    private val TCTL = object : Register(mem, 0x400u, DWORD, "TCTL", level = REG_LOG_LEVEL) {
        /** Enable */
        val EN by bit(1)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)

            if (TARC0.EN.truth) {
                tx(TxRingInfos[0])
            }

            if (TARC1.EN.truth) {
                tx(TxRingInfos[1])
            }
        }
    }

    /** Adaptive Interframe Spacing Throttle */
    @Suppress("unused")
    private val AIT = Dword16BitWriteRegister(0x458u, "AIT")

    /** Extended Configuration Control */
    @Suppress("unused")
    private val EXTCNF_CTRL = Register(mem, 0xF00u, DWORD, "EXTCNF_CTRL", default = 1uL shl 3, level = REG_LOG_LEVEL)

    /** Packet Buffer Allocation */
    @Suppress("unused")
    private val PBA = object : Register(mem, 0x1000u, DWORD, "PBA", level = REG_LOG_LEVEL) {
        /** Receive packet buffer allocation in KB */
        var RXA by field(15..0)

        /** Transmit packet buffer allocation in KB */
        var TXA by field(31..16)

        override fun reset() {
            super.reset()
            RXA = 0x14u
            TXA = 0x14u
        }
    }

    /** Flow Control Receive Threshold Low */
    @Suppress("unused")
    private val FCRTL = object : Register(mem, 0x2160uL, DWORD, "FCRTL", level = REG_LOG_LEVEL) {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            data = data and 0x8000FFF8uL
        }
    }

    /** Flow Control Receive Threshold High */
    @Suppress("unused")
    private val FCRTH = object : Register(mem, 0x2168uL, DWORD, "FCRTH", level = REG_LOG_LEVEL) {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            data = data and 0xFFF8uL
        }
    }

    /** Packet Split Receive Control Register */
    private val PSRCTL = object : Register(mem, 0x2170u, DWORD, "PSRCTL", level = REG_LOG_LEVEL) {
        /** Receive Buffer Size for Buffer 0 */
        var BSIZE0 by field(6..0)

        /** Receive Buffer Size for Buffer 0 */
        var BSIZE1 by field(13..8)

        /** Receive Buffer Size for Buffer 0 */
        var BSIZE2 by field(21..16)

        /** Receive Buffer Size for Buffer 0 */
        val BSIZE3 by field(29..24)

        override fun reset() {
            super.reset()
            BSIZE0 = 2u
            BSIZE1 = 4u
            BSIZE2 = 4u
        }

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            val old = data
            super.write(ea, ss, size, value)

            if (RCTL.DTYP.truth) {
                if (BSIZE0.untruth) {
                    log.severe {
                        "$name: PSRCTL.BSIZE0 cannot be zero"
                    }

                    data = old
                    return
                }
                if (BSIZE1.untruth) {
                    log.severe {
                        "$name: PSRCTL.BSIZE1 cannot be zero"
                    }

                    data = old
                    return
                }
            }
        }
    }

    /** XX Descriptor Length */
    private inner class XDLEN(addr: ULong, name: String) : Register(mem, addr, DWORD, name, level = REG_LOG_LEVEL) {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            data = data and (((1uL shl 20) - 1uL) and ((1uL shl 7) - 1uL).inv())
        }
    }

    private inner class RxRingInfo(idx: UInt) {
        /** RX Descriptor Base Address */
        val dba = ByteAccessRegister(mem, 0x2800u + idx * 0x100uL, QWORD, "RDBA$idx", level = REG_LOG_LEVEL)

        /** RX Descriptor Length */
        val dlen = XDLEN(0x2808uL + idx * 0x100uL, "RDLEN$idx")

        /** RX Descriptor Head */
        val dh = Dword16BitWriteRegister(0x2810uL + idx * 0x100uL, "RDH$idx")

        /** RX Descriptor Tail */
        val dt = Dword16BitWriteRegister(0x2818uL + idx * 0x100uL, "RDT$idx")

        fun freeDescrNum() = if (dh.data <= dt.data) {
            dt.data - dh.data
        } else {
            dlen.data / 16u + dt.data - dh.data
        }
    }

    /** Rx Rings */
    private val RxRingInfos = Array(2) { RxRingInfo(it.uint) }

    /** RX Delay Timer 1 */
    private inner class RDTRClass(name: String) : Register(mem, 0x2820uL, DWORD, name, level = REG_LOG_LEVEL) {
        /** Rx Interrupt Delay Timer */
        val FPD by bit(31)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)

            val fpd = FPD.truth
            data = data and 0xFFFFu

            if (fpd && rdtrTmr.enabled) {
                setInterruptCause(0u)
            }
        }
    }

    /** RX Delay Timer 1 */
    private val RDTR = RDTRClass("RDTR")

    /** RX Interrupt Absolute Delay Timer */
    private val RADV = Dword16BitWriteRegister(0x282CuL, "RADV")

    /** RX Small Packet Detect */
    private val RSRPD = object : Register(mem, 0x02C00uL, DWORD, "RXDCTL0", level = REG_LOG_LEVEL) {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            data = data and 0xFFFuL
        }
    }

    /** Receive Ack Interrupt Delay */
    private val RAID = Dword16BitWriteRegister(0x02C08uL, "RAID")

    /** RX Descriptor Control queue 0 */
    private val RXDCTL0 = object : Register(mem, 0x2828uL, DWORD, "RXDCTL0", level = REG_LOG_LEVEL) {
        override fun reset() {
            super.reset()
            data = data or (1uL shl 16)
        }
    }

    /** RX Descriptor Control queue 1 */
    @Suppress("unused")
    private val RXDCTL1 = object : Register(mem, 0x2928uL, DWORD, "RXDCTL1", level = REG_LOG_LEVEL) {
        override fun read(ea: ULong, ss: Int, size: Int) = RXDCTL0.read(ea - 0x0100uL, ss, size)
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) =
            RXDCTL0.write(ea - 0x0100uL, ss, size, value)
    }

    /** TX Descriptor Tail */
    private inner class TDT(addr: ULong, name: String, val queue: UInt) : Dword16BitWriteRegister(addr, name) {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)

            if ((if (queue == 0u) TARC0.EN else TARC1.EN).truth) {
                tx(TxRingInfos[queue])
            }
        }
    }

    /** Transmit Arbitration Count */
    private inner class TARC(addr: ULong, name: String) : Register(mem, addr, DWORD, name, level = REG_LOG_LEVEL) {
        /** Transmit Arbitration Count */
        private var COUNT by field(6..0)

        /** Descriptor Enable */
        var EN by bit(10)

        override fun reset() {
            super.reset()
            EN = 1
            COUNT = 3u
        }
    }

    /*
    private data class TxProps(
        var ipcss: UByte = 0u,
        var ipcso: UByte = 0u,
        var ipcse: UShort = 0u,
        var tucss: UByte = 0u,
        var tucso: UByte = 0u,
        var tucse: UShort = 0u,
        var paylen: ULong = 0u,
        var hdrLen: UByte = 0u,
        var mss: UShort = 0u,
        var ip: Byte = 0,
        var tcp: Byte = 0,
        var tse: Boolean = false,
        var cptse: Boolean = false,
        var sumNeeded: UByte = 0u,
    ) : ISerializable
    */

    private inner class TxRingInfo(idx: UInt) {
        /** Packet reassembly cache */
        val packetCache: ArrayList<Byte> = ArrayList()

        // Remember to {,de}serialize!
        // val props = TxProps()

        /** Transmit Descriptor Base Address */
        val dba = ByteAccessRegister(mem, 0x3800uL + idx * 0x100uL, QWORD, "TDBA$idx", level = REG_LOG_LEVEL)

        /** TX Descriptor Length */
        val dlen = XDLEN(0x3808u + idx * 0x100uL, "TDLEN$idx")

        /** TX Descriptor Head */
        val dh = Dword16BitWriteRegister(0x3810u + idx * 0x100uL, "TDH$idx")

        /** TX Descriptor Tail */
        val dt = TDT(0x3818u + idx * 0x100uL, "TDT$idx", idx)
    }

    /** Tx Rings */
    private val TxRingInfos = Array(2) { TxRingInfo(it.uint) }

    /** TX Interrupt Delay Value */
    private inner class TIDVClass(name: String) : Register(mem, 0x3820uL, DWORD, name, level = REG_LOG_LEVEL) {
        /** Tx Interrupt Delay Timer */
        private val FPD by bit(31)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)

            val fpd = FPD.truth
            data = data and 0xFFFFu

            if (fpd && tidvTmr.enabled) {
                setInterruptCause(0u)
            }
        }
    }

    /** TX Interrupt Delay Value */
    private val TIDV = TIDVClass("TIDV")

    /** TX Interrupt Absolute Delay Val */
    private val TADV = Dword16BitWriteRegister(0x382CuL, "TADV")

    /** TX Descriptor Control 0 */
    @Suppress("unused")
    private val TXDCTL0 = Register(mem, 0x3828u, DWORD, "TXDCTL0", level = REG_LOG_LEVEL)

    /** Transmit Arbitration Count 0 */
    private val TARC0 = TARC(0x3840u, "TARC0")

    /** TX Descriptor Control 1 */
    @Suppress("unused")
    private val TXDCTL1 = Register(mem, 0x3928u, DWORD, "TXDCTL1", level = REG_LOG_LEVEL)

    /** Transmit Arbitration Count 1 */
    private val TARC1 = TARC(0x3940u, "TARC1")

    /** RX Checksum Control */
    private val RXCSUM = object : Register(mem, 0x5000uL, DWORD, "RXCSUM", level = REG_LOG_LEVEL) {
        /** IP Checksum Offload Enable */
        var IPOFLD by bit(8)

        /** TCP/UDP Checksum Offload Enable */
        var TUOFLD by bit(9)

        /** Packet Checksum Disable */
        val PCSD by bit(13)

        override fun reset() {
            super.reset()
            IPOFLD = 1
            TUOFLD = 1
        }
    }

    /** Receive Filter Control Register */
    private val RFCTL = object : Register(mem, 0x5008uL, DWORD, "RFCTL", level = REG_LOG_LEVEL) {
        /** Disable IPv6 packet filtering */
        val IPV6_DIS by bit(10)

        /** Disable XSUM on IPv6 packets */
        val IPV6_XSUM_DIS by bit(11)

        /**
         * ACK Accelerate Disable
         *
         * When this bit is set, the 82574 does not accelerate interrupt on TCP
         * ACK packets
         */
        val ACK_DIS by bit(12)

        /**
         * ACK data Disable.
         *
         * 1b = The 82574L recognizes ACK packets according to the ACK bit in the TCP header + No –CP data.
         *
         * This bit is relevant only if the ACKDIS bit is not set.
         */
        val ACK_DATA_DIS by bit(13)

        /**
         * IP Fragment Split Disable
         *
         * When this bit is set, the header of IP fragmented packets are not set.
         */
        val IPFRSP_DIS by bit(14)

        /**
         * Extended status Enable
         *
         * When the EXSTEN bit is set or when the packet split receive
         * descriptor is used, the 82574 writes the extended status to the Rx
         * descriptor.
         */
        val EXSTEN by bit(15)
    }

    @Suppress("unused")
    private val MTA = Array(128) {
        Register(mem, 0x5200u + 4u * it.ulong_z, DWORD, "MTA$it", level = REG_LOG_LEVEL)
    }

    private inner class RA(addr: ULong, name: String) : ByteAccessRegister(mem, addr, QWORD, name, level = REG_LOG_LEVEL) {
        /** MAC address */
        var MAC by field(47..0)

        /** Address Valid */
        var AV by bit(31)
    }

    /** Receive Address Array */
    private val RAs = Array(16) {
        RA(0x5400u + 8u * it.ulong_z, "RA$it")
    }

    /** VLAN Filter Table Array */
    @Suppress("unused")
    private val VFTA = Array(128) {
        Register(mem, 0x5600u + 4u * it.ulong_z, DWORD, "VFTA$it", level = REG_LOG_LEVEL)
    }

    /** Management Control Register */
    @Suppress("unused")
    private val MANC = object : Register(mem, 0x5820u, DWORD, "MANC", level = REG_LOG_LEVEL) {
        /** Disable IP Address Checking for ARP Packets */
        var DIS_IP_ADDR_for_ARP by bit(28)

        override fun reset() {
            super.reset()
            DIS_IP_ADDR_for_ARP = 1
        }
    }

    /** PCI-Ex Control */
    @Suppress("unused")
    private val GCR = object : Register(mem, 0x5B00u, DWORD, "GCR", level = REG_LOG_LEVEL) {
        var RX_L0S_ADJ by bit(9)
        var L1_ENTRY_LATENCY_MSB by bit(23)
        var L1_ENTRY_LATENCY_LSB by field(26..25)

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            RX_L0S_ADJ = 1
            L1_ENTRY_LATENCY_MSB = 1
            L1_ENTRY_LATENCY_LSB = 3u
            return super.read(ea, ss, size)
        }
    }

    /** SW Semaphore */
    @Suppress("unused")
    private val SWSM = object : Register(mem, 0x5B50u, DWORD, "SWSM", default = 1uL, level = REG_LOG_LEVEL) {
        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            data = data or 1uL
            return super.read(ea, ss, size)
        }
    }

    /** FW Semaphore */
    @Suppress("unused")
    private val FWSM = object : Register(mem, 0x5B54u, DWORD, "FWSM", default = 1uL, level = REG_LOG_LEVEL) {
        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            data = data or 1uL
            return super.read(ea, ss, size)
        }
    }

    /** Redirection Table */
    @Suppress("unused")
    private val RETA = Array(128) {
        Register(mem, 0x5C00u + it.ulong_z, BYTE, "RETA$it", level = REG_LOG_LEVEL)
    }

    /** RSS Random Key */
    @Suppress("unused")
    private val RSSRK = Array(128) {
        Register(mem, 0x5C80u + it.ulong_z, BYTE, "RSSRK$it", level = REG_LOG_LEVEL)
    }

    private open inner class RClrRegister(port: Port, address: ULong, datatype: Datatype, name: String) :
        ByteAccessRegister(port, address, datatype, name, level = REG_LOG_LEVEL) {
        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            val result = super.read(ea, ss, size)
            data = 0u
            return result
        }
    }

    /** Counter registers */
    @Suppress("unused")
    private val COUNTERS = mapOf(
        "CRCERRS" to (0x4000uL to DWORD), // CRC Error Count
        "ALGNERRC" to (0x4004uL to DWORD), // Alignment Error Count
        "SYMERRS" to (0x4008uL to DWORD), // Symbol Error Count
        "RXERRC" to (0x400CuL to DWORD), // RX Error Count
        "MPC" to (0x4010uL to DWORD), // Missed Packet Count
        "SCC" to (0x4014uL to DWORD), // Single Collision Count
        "ECOL" to (0x4018uL to DWORD), // Excessive Collision Count
        "MCC" to (0x401CuL to DWORD), // Multiple Collision Count
        "LATECOL" to (0x4020uL to DWORD), // Late Collision Count
        "COLC" to (0x4028uL to DWORD), // Collision Count
        "DC" to (0x4030uL to DWORD), // Defer Count
        "TNCRS" to (0x04034uL to DWORD), // TX-No CRS
        "SEQEC" to (0x4038uL to DWORD), // Sequence Error Count
        "CEXTERR" to (0x403CuL to DWORD), // Carrier Extension Error Count
        "RLEC" to (0x4040uL to DWORD), // Receive Length Error Count
        "XONRXC" to (0x4048uL to DWORD), // XON RX Count
        "XONTXC" to (0x404CuL to DWORD), // XON TX Count
        "XOFFRXC" to (0x4050uL to DWORD), // XOFF RX Count
        "XOFFTXC" to (0x4054uL to DWORD), // XOFF TX Count
        "FCRUC" to (0x4058uL to DWORD), // Flow Control RX Unsupported Count
        "RNBC" to (0x040A0uL to DWORD), // RX No Buffers Count
        "RUC" to (0x040A4uL to DWORD), // RX Undersize Count
        "RFC" to (0x040A8uL to DWORD), // RX Fragment Count
        "ROC" to (0x040ACuL to DWORD), // RX Oversize Count
        "RJC" to (0x040B0uL to DWORD), // RX Jabber Count
        "MGTPRC" to (0x040B4uL to DWORD), // Management Packets RX Count
        "MGTPDC" to (0x040B8uL to DWORD), // Management Packets Dropped Count
        "MGTPTC" to (0x040BCuL to DWORD), // Management Packets TX Count
        "TSCTC" to (0x040F8uL to DWORD), // TCP Segmentation Context TX
        "TSCTFC" to (0x040FCuL to DWORD), // TCP Segmentation Context TX Fail
        "IAC" to (0x04100uL to DWORD), // Interrupt Assertion Count
        "ICRXPTC" to (0x04104uL to DWORD), // Interrupt Cause Rx Packet Timer Expire Count
        "ICRXATC" to (0x04108uL to DWORD), // Interrupt Cause Rx Absolute Timer Expire Count
        "ICTXPTC" to (0x0410CuL to DWORD), // Interrupt Cause Tx Packet Timer Expire Count
        "ICTXATC" to (0x04110uL to DWORD), // Interrupt Cause Tx Absolute Timer Expire Count
        "ICTXQEC" to (0x04118uL to DWORD), // Interrupt Cause Tx Queue Empty Count
        "ICTXQMTC" to (0x0411CuL to DWORD), // Interrupt Cause Tx Queue Minimum Threshold Count
        "ICRXDMTC" to (0x04120uL to DWORD), // Interrupt Cause Rx Descriptor Minimum Threshold Count
        "ICRXOC" to (0x04124uL to DWORD), // Interrupt Cause Receiver Overrun Count
    ).map { (name, addrType) ->
        RClrRegister(mem, addrType.first, addrType.second, name)
    }

    /** Good Octets RX Count */
    private val GORC = RClrRegister(mem, 0x4088uL, QWORD, "GORC")

    /** Good Packets RX Count */
    private val GPRC = RClrRegister(mem, 0x4074uL, DWORD, "GPRC")

    /** Total Octets RX */
    private val TOR = RClrRegister(mem, 0x040C0uL, QWORD, "TOR")

    /** Broadcast Packets RX Count */
    private val BPRC = RClrRegister(mem, 0x4078uL, DWORD, "BPRC")

    /** Multicast Packets RX Count */
    private val MPRC = RClrRegister(mem, 0x407CuL, DWORD, "MPRC")

    /** Total Packets RX */
    private val TPR = RClrRegister(mem, 0x040D0uL, DWORD, "TPR")

    /** Packets RX (64 bytes) */
    private val PRC64 = RClrRegister(mem, 0x0405CuL, DWORD, "PRC64")

    /** Packets RX (65-127 bytes) */
    private val PRC127 = RClrRegister(mem, 0x04060uL, DWORD, "PRC127")

    /** Packets RX (128-255 bytes) */
    private val PRC255 = RClrRegister(mem, 0x04064uL, DWORD, "PRC255")

    /** Packets RX (255-511 bytes) */
    private val PRC511 = RClrRegister(mem, 0x04068uL, DWORD, "PRC511")

    /** Packets RX (512-1023 bytes) */
    private val PRC1023 = RClrRegister(mem, 0x0406CuL, DWORD, "PRC1023")

    /** Packets RX (1024-1522 bytes) */
    private val PRC1522 = RClrRegister(mem, 0x04070uL, DWORD, "PRC1522")

    /** Total Packets TX */
    private val TPT = RClrRegister(mem, 0x040D4uL, DWORD, "TPT")

    /** Packets TX (64 bytes) */
    private val PTC64 = RClrRegister(mem, 0x040D8uL, DWORD, "PTC64")

    /** Packets TX (65-127 bytes) */
    private val PTC127 = RClrRegister(mem, 0x040DCuL, DWORD, "PTC127")

    /** Packets TX (128-255 bytes) */
    private val PTC255 = RClrRegister(mem, 0x040E0uL, DWORD, "PTC255")

    /** Packets TX (255-511 bytes) */
    private val PTC511 = RClrRegister(mem, 0x040E4uL, DWORD, "PTC511")

    /** Packets TX (512-1023 bytes) */
    private val PTC1023 = RClrRegister(mem, 0x040E8uL, DWORD, "PTC1023")

    /** Packets TX (1024-1522 bytes) */
    private val PTC1522 = RClrRegister(mem, 0x040ECuL, DWORD, "PTC1522")

    /** Total Octets TX */
    private val TOT = RClrRegister(mem, 0x040C8uL, QWORD, "TOT")

    /** Good Packets TX Count */
    private val GPTC = RClrRegister(mem, 0x4080uL, DWORD, "GPTC")

    /** Good Octets TX Count */
    private val GOTC = RClrRegister(mem, 0x4090uL, QWORD, "GOTC")

    /** Multicast Packets TX Count */
    private val MPTC = RClrRegister(mem, 0x040F0uL, DWORD, "MPTC")

    /** Broadcast Packets TX Count */
    private val BPTC = RClrRegister(mem, 0x040F4uL, DWORD, "BPTC")

    /** Dummy registers */
    @Suppress("unused")
    private val DUMMY = mapOf(
        "FCA" to (0x28uL to QWORD), // Flow Control Address
        "LEDCTL" to (0xE00uL to DWORD), // LED Control
        "WUC" to (0x5800uL to DWORD), // Wakeup Control
        "GCR2" to (0x5B64uL to DWORD), // 3GIO Control Register 2
        "SYSTIM" to (0xB600uL to QWORD), // System time register
        "TIMINCA" to (0xB608uL to DWORD), // Increment attributes register - RW
        "TSYNCTXCTL" to (0xB614uL to DWORD), // Tx Time Sync Control register
        "TXSTMP" to (0xB618uL to QWORD), // Tx timestamp value
        "TSYNCRXCTL" to (0xB620uL to DWORD), // Rx Time Sync Control register
        "RXSTMP" to (0xB624uL to QWORD), // Rx timestamp
        "RXMTRL" to (0xB634uL to DWORD), // Time sync Rx EtherType and Msg Type - RW
    ).map { (name, addrType) ->
        if (addrType.second == DWORD) {
            Register(mem, addrType.first, addrType.second, name, level = REG_LOG_LEVEL)
        } else {
            ByteAccessRegister(mem, addrType.first, addrType.second, name, level = REG_LOG_LEVEL)
        }
    }

    /** Multiple Receive Control */
    private val MRQC = object : Register(mem, 0x5818uL, DWORD, "MRQC", level = REG_LOG_LEVEL) {
        val MRQ_EN0 by bit(0)
        val MRQ_EN1 by bit(1)

        // /** Enable TcpIPv4 hash function */
        // val TCPIPV4 by bit(16)

        // /** Enable IPv4 hash function */
        // val IPV4 by bit(17)

        // /** Enable TcpIPv6 hash function */
        // val TCPIPV6 by bit(18)

        // /** Enable IPv6Ex hash function */
        // val IPV6EX by bit(19)

        // /** Enable IPv6 hash function */
        // val IPV6 by bit(20)
    }

    /** Time Sync Rx UDP Port */
    @Suppress("unused")
    private val RXUDP = Register(mem, 0xB638uL, DWORD, "RXUDP", level = REG_LOG_LEVEL, default = 0x319uL)

    private inner class TimerCommon(name: String, val reg: Register) : SystemClock.PeriodicalTimer(name) {
        fun rearm() {
            triggered = 0u
            enabled = true
        }

        override fun trigger() {
            super.trigger()

            if (triggered >= reg.data) {
                log.warning {
                    "$name timer triggered"
                }

                enabled = false
                setInterruptCause(0uL)
            }
        }
    }

    private val radvTmr = TimerCommon("radvTmr", RADV)
    private val rdtrTmr = TimerCommon("rdtrTmr", RDTR)
    private val raidTmr = TimerCommon("raidTmr", RAID)
    private val tadvTmr = TimerCommon("tadvTmr", TADV)
    private val tidvTmr = TimerCommon("tidvTmr", TIDV)
    private val itrTmr = object : SystemClock.PeriodicalTimer("itrTmr") {
        fun rearm() {
            triggered = 0u
            enabled = true
        }

        override fun trigger() {
            super.trigger()

            if (triggered < ITR.data) {
                return
            }

            enabled = false

            if (!itrIntrPending) {
                return
            }

            msiCausesPending = 0u
            setInterruptCause(0u)
        }
    }
}
