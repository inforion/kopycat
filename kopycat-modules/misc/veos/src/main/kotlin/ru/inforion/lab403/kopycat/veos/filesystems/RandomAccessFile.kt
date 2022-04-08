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
@file:Suppress("unused")

package ru.inforion.lab403.kopycat.veos.filesystems

import ru.inforion.lab403.kopycat.veos.filesystems.interfaces.IRandomAccessFile

/**
 * {EN}
 * Class to be used in script tracers without implementation all methods (like IRandomAccessFileImpl)
 *
 * class Tracer:
 *    from ru.inforion.lab403.kopycat.veos.filesystems import RandomAccessFile
 *
 *    class PseudoOutputFile(RandomAccessFile):
 *        def write(self, bytes):
 *            pass
 *
 *    def reset(self, core, simulation, injector):
 *        veos = core.getComponentsByInstanceName("veos")[0]
 *        veos.filesystem.virtualFile(Tracer.PseudoOutputFile(), "result.txt")
 *
 *    def executed(self, core, status, simulation, injector):
 *        return simulation.executed < self.steps if self.steps is not None else True
 * {EN}
 */
open class RandomAccessFile: IRandomAccessFile