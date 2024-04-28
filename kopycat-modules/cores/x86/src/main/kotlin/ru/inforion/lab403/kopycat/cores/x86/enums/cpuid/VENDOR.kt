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
package ru.inforion.lab403.kopycat.cores.x86.enums.cpuid

import ru.inforion.lab403.common.extensions.bytes
import ru.inforion.lab403.common.extensions.getInt32
import ru.inforion.lab403.common.extensions.uint

enum class VENDOR(val idString: String) {
    AMDK5("AMDisbetter!"),
    AMD("AuthenticAMD"),
    CENTAUR("CentaurHauls"),
    CYRIX("CyrixInstead"),
    INTEL("GenuineIntel"),
    TRANSMETA("TransmetaCPU"),
    TRANSMETAG("GenuineTMx86"),
    NATIONAL_SEMIDCONDUCTOR("Geode by NSC"),
    NEXGEN("NexGenDriven"),
    RISE("RiseRiseRise"),
    SIS("SiS SiS SiS "),
    UMC("UMC UMC UMC "),
    VIA("VIA VIA VIA "),
    VORTEX("Vortex86 SoC"),
    ZHAOXIN("  Shanghai  "),
    HYGON("HygonGenuine"),
    ELBRUS("E2K MACHINE"),
    AO486("GenuineAO486"),
    V586("GenuineIntel"),
    BHYVE("bhyve bhyve "),
    KVM(" KVMKVMKVM  "),
    QEMU("TCGTCGTCGTCG"),
    HYPERV("Microsoft Hv"),
    PARALLELS(" lrpepyh  vr"),
    VMWARE("VMwareVMware"),
    XENHVM("XenVMMXenVMM"),
    ACRN("ACRNACRNACRN"),
    QNX(" QNXQVMBSQG "),
    ROSETTA2("GenuineIntel");

    fun getUInt(ind: Int) = idString.substring((4*ind) until (4*(ind + 1))).bytes.getInt32(0).uint
}