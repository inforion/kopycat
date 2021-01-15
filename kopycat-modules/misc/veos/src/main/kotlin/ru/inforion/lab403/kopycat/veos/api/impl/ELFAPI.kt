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
package ru.inforion.lab403.kopycat.veos.api.impl

import ru.inforion.lab403.kopycat.cores.base.enums.ArgType
import ru.inforion.lab403.kopycat.veos.VEOS
import ru.inforion.lab403.kopycat.veos.api.abstracts.API
import ru.inforion.lab403.kopycat.veos.api.abstracts.APIFunction
import ru.inforion.lab403.kopycat.veos.api.interfaces.APIResult


/**
 *
 * ELF routines and routines of specific implementations of C standard library
 */
class ELFAPI(os: VEOS<*>) : API(os) {
    val _preinit_array = nullsub("_preinit_array")
    val _init = nullsub("_init")
    val _init_array = nullsub("_init_array")
    val _fini_array = nullsub("_fini_array")
    val __aeabi_atexit = nullsub("__aeabi_atexit")

    val __uClibc_main = object : APIFunction("__uClibc_main") {
        override val args = arrayOf(
                ArgType.Pointer, ArgType.Int, ArgType.Pointer, ArgType.Pointer,
                ArgType.Pointer, ArgType.Pointer, ArgType.Pointer
        )

        override fun exec(name: String, vararg argv: Long): APIResult {
            val main = argv[0]
            val argc = argv[1]
            val _argv = argv[2]

            /* The environment begins right after argv.  */
            var __environ = os.abi.readInt(_argv + (argc + 1) * 4)

            /* If the first thing after argv is the arguments
             * then the environment is empty. */
            if (__environ == os.abi.readInt(_argv)) {
                /* Make __environ point to the NULL at argv[argc] */
                __environ = os.abi.readInt(_argv + argc * 4)
            }

            // TODO: Init/fini

            os.abi.returnAddressValue = os.sys.processExitAddress

            os.abi.setArgs(arrayOf(argc, _argv, __environ), true)

            return redirect(main)
        }
    }

    /** TODO: merge with [__uClibc_main] */
    val __libc_start_main = object : APIFunction("__libc_start_main") {
        override val args = arrayOf(
                ArgType.Pointer,    // int (*main)(int, char**, char**)
                ArgType.Int,       // int argc
                ArgType.Pointer,    // char** ubp_av
                ArgType.Pointer,    // void (*init)(void)
                ArgType.Pointer,    // void (*fini)(void)
                ArgType.Pointer,    // void (*rtld_fini)(void)
                ArgType.Pointer     // void* stack_end
        )

        override fun exec(name: String, vararg argv: Long): APIResult {
            val main = argv[0]
            val argc = argv[1]
            val _argv = argv[2]

            /* The environment begins right after argv.  */
            var __environ = os.abi.readInt(_argv + (argc + 1) * 4)

            /* If the first thing after argv is the arguments
             * then the environment is empty. */
            if (__environ == os.abi.readInt(_argv)) {
                /* Make __environ point to the NULL at argv[argc] */
                __environ = os.abi.readInt(_argv + argc * 4)
            }

            // TODO: Init/fini

            os.abi.returnAddressValue = os.sys.processExitAddress

            os.abi.setArgs(arrayOf(argc, _argv, __environ), true)

            return redirect(main)
        }
    }
}